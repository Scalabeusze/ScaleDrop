resource "aws_ecr_repository" "sd_bff_repo" {
  name                 = "sd-bff"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

resource "random_password" "bff_doc_password" {
    length = 12
    special = false
}

resource "aws_ssm_parameter" "bff_doc_password_param" {
  name  = "/dev/bff/security/doc-password"
  type  = "SecureString"
  value = random_password.bff_doc_password.result
}

resource "random_password" "bff_internal_password" {
    length = 16
    special = true
}

resource "aws_ssm_parameter" "bff_internal_password_param" {
  name  = "/dev/bff/security/internal-password"
  type  = "SecureString"
  value = random_password.bff_internal_password.result
}

data "aws_ssm_parameter" "bff_google_client_id" {
  name = "/dev/bff/oauth/google-client-id"
}

resource "aws_cloudwatch_log_group" "sd_bff_logs" {
  name              = "/ecs/sd-bff"
  retention_in_days = 3
}

resource "aws_ecs_task_definition" "sd_bff" {
  family                   = "sd-bff-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn

  container_definitions = jsonencode([
    # BFF container
    {
      name      = "sd-bff"
      image     = "${aws_ecr_repository.sd_bff_repo.repository_url}:latest"
      essential = true
      portMappings = [{ containerPort = 8080, hostPort = 8080 }]

      # wait for Redis
      dependsOn = [{
        containerName = "redis-sidecar"
        condition     = "START"
      }]

      environment = [
        { name = "REDIS_HOST", value = "127.0.0.1" }, # Sidecar is on localhost
        { name = "REDIS_PASSWORD", value = "" },
        
        { name = "IAM_SERVICE_URL", value = "http://${aws_lb.main.dns_name}/sd-iam" },
        { name = "UPLOAD_SERVICE_URL", value = "http://${aws_lb.main.dns_name}/sd-upload" },
        { name = "DOWNLOAD_SERVICE_URL", value = "http://${aws_lb.main.dns_name}/sd-download" },
        
        { name = "IAM_SERVICE_USERNAME", value = "scaledrop" },
        { name = "UPLOAD_SERVICE_USERNAME", value = "scaledrop" },
        { name = "DOWNLOAD_SERVICE_USERNAME", value = "scaledrop" }
      ]

      secrets = [
        { name = "SECURITY_ACCESS_DOCUMENTATION_PASSWORD", valueFrom = aws_ssm_parameter.bff_doc_password_param.arn },
        { name = "SECURITY_ACCESS_INTERNAL_PASSWORD", valueFrom = aws_ssm_parameter.bff_internal_password_param.arn },
        { name = "IAM_SERVICE_PASSWORD", valueFrom = aws_ssm_parameter.internal_password_param.arn },
        { name = "UPLOAD_SERVICE_PASSWORD", valueFrom = aws_ssm_parameter.upload_internal_password_param.arn },
        { name = "DOWNLOAD_SERVICE_PASSWORD", valueFrom = aws_ssm_parameter.download_internal_password_param.arn },
        { name = "GOOGLE_CLIENT_ID", valueFrom = data.aws_ssm_parameter.bff_google_client_id.arn },
        { name = "SECURITY_JWT_SECRET", valueFrom = aws_ssm_parameter.jwt_secret_param.arn }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.sd_bff_logs.name
          "awslogs-region"        = "eu-north-1"
          "awslogs-stream-prefix" = "ecs"
        }
      }
    },
    # Redis Sidecar
    {
      name      = "redis-sidecar"
      image     = "redis:7-alpine"
      essential = true
      portMappings = [{ containerPort = 6379, hostPort = 6379 }]
      
      # Push Redis logs to CloudWatch
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.sd_bff_logs.name
          "awslogs-region"        = "eu-north-1"
          "awslogs-stream-prefix" = "redis"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "sd_bff_service" {
  name            = "sd-bff-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.sd_bff.arn
  
  desired_count   = 1

  launch_type     = "FARGATE"
  health_check_grace_period_seconds = 240

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.app_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.sd_bff_tg.arn
    container_name   = "sd-bff"
    container_port   = 8080
  }

  lifecycle { ignore_changes = [task_definition] }
}
