# Create ECS cluster for all services
resource "aws_ecs_cluster" "main" {
  name = "sd-cluster-dev"
}

# ECS Task Execution Role
# Allows ECS to pull images and read SSM parameters for secrets
resource "aws_iam_role" "ecs_execution_role" {
  name = "sd-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
}

# Connect default ECS policy (pulling images, sending logs)
resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Allow to read secrets from SSM Parameter Store (e.g. database password) - needed for ECS tasks to fetch secrets at runtime
resource "aws_iam_role_policy" "ecs_ssm_policy" {
  name = "sd-ecs-ssm-policy"
  role = aws_iam_role.ecs_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ssm:GetParameters",
        "ssm:GetParameter"
      ]
      Resource = "arn:aws:ssm:${var.region}:${data.aws_caller_identity.current.account_id}:parameter/dev/*"
    }]
  })
}

data "aws_caller_identity" "current" {}
variable "region" {
  default = "eu-north-1"
}
