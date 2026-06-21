variable "enable_ecs_schedule" {
  description = "Whether to turn on autoscale"
  type        = bool
  default     = true
}

locals {
  ecs_services_to_schedule = [
    aws_ecs_service.sd_iam_service.name,
    aws_ecs_service.sd_download_service.name,
    aws_ecs_service.sd_upload_service.name,
    aws_ecs_service.sd_bff_service.name
  ]
}

# Register all services in Auto Scaling
resource "aws_appautoscaling_target" "ecs_targets" {
  for_each     = var.enable_ecs_schedule ? toset(local.ecs_services_to_schedule) : []
  max_capacity = 2
  min_capacity = 0

  resource_id        = "service/${aws_ecs_cluster.main.name}/${each.value}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

# Action to turn off at 21:00 UTC
resource "aws_appautoscaling_scheduled_action" "scale_down_night" {
  for_each = aws_appautoscaling_target.ecs_targets

  name               = "scale-down-${each.key}-23pm"
  service_namespace  = each.value.service_namespace
  resource_id        = each.value.resource_id
  scalable_dimension = each.value.scalable_dimension

  schedule = "cron(0 21 * * ? *)"

  scalable_target_action {
    min_capacity = 0
    max_capacity = 0
  }
}

# Action to turn on at 07:00 UTC
resource "aws_appautoscaling_scheduled_action" "scale_up_morning" {
  for_each = aws_appautoscaling_target.ecs_targets

  name               = "scale-up-${each.key}-9am"
  service_namespace  = each.value.service_namespace
  resource_id        = each.value.resource_id
  scalable_dimension = each.value.scalable_dimension

  schedule = "cron(0 7 * * ? *)"

  scalable_target_action {
    min_capacity = 1
    max_capacity = 2
  }
}
