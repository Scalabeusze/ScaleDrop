# ECR Repository (for container images)
resource "aws_ecr_repository" "sd_iam_repo" {
  name                 = "sd-iam"
  image_tag_mutability = "MUTABLE" # Allows overriding tags, e.g. 'latest'
  force_delete         = true      # Makes it easier to clean up resources

  image_scanning_configuration {
    scan_on_push = true # Scans images for vulnerabilities when pushed to the repository (free security feature)
  }
}

# Generate swagger password
resource "random_password" "doc_password" {
  length  = 12
  special = false
}

resource "aws_ssm_parameter" "doc_password_param" {
  name  = "/dev/iam/security/doc-password"
  type  = "SecureString"
  value = random_password.doc_password.result
}

# Generate internal API password
resource "random_password" "internal_password" {
  length  = 16
  special = true
}

resource "aws_ssm_parameter" "internal_password_param" {
  name  = "/dev/iam/security/internal-password"
  type  = "SecureString"
  value = random_password.internal_password.result
}

# Logs (CloudWatch)
resource "aws_cloudwatch_log_group" "sd_iam_logs" {
  name              = "/ecs/sd-iam"
  retention_in_days = 3 # Cost saving
}

# Task definition
resource "aws_ecs_task_definition" "sd_iam" {
  family                   = "sd-iam-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]

  # Lowest resources for cost cutting (0.25 vCPU, 0.5 GB RAM)
  cpu                      = 256
  memory                   = 512

  execution_role_arn       = aws_iam_role.ecs_execution_role.arn

  container_definitions = jsonencode([{
    name      = "sd-iam"
    image     = "${aws_ecr_repository.sd_iam_repo.repository_url}:latest" # use latest image from ECR
    essential = true

    # server.port in application.yml
    portMappings = [{
      containerPort = 8080
      hostPort      = 8080
    }]

    # ENV variables
    environment = [
      {
        name  = "DB_URL"
        # Endpoint from database.tf
        value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/sd_database?currentSchema=sd_iam"
      },
      {
        name  = "DB_USERNAME"
        value = aws_db_instance.postgres.username
      }
    ]

    # Secrets downloaded at runtime, not stored in plain text in task definition (for security)
    secrets = [
      {
        name      = "DB_PASSWORD"
        valueFrom = aws_ssm_parameter.db_password_param.arn
      },
      {
        name      = "SECURITY_ACCESS_DOCUMENTATION_PASSWORD"
        valueFrom = aws_ssm_parameter.doc_password_param.arn
      },
      {
        name      = "SECURITY_ACCESS_INTERNAL_PASSWORD"
        valueFrom = aws_ssm_parameter.internal_password_param.arn
      }
    ]

    # Log configuration
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.sd_iam_logs.name
        "awslogs-region"        = "eu-north-1"
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

# Running the container on Fargate, which will automatically pull the image, set up the network, and run it according to the task definition
resource "aws_ecs_service" "sd_iam_service" {
  name            = "sd-iam-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.sd_iam.arn
  desired_count   = 1 # One instance of this app
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.app_sg.id]

    # Public IP because NAT Gateway is disabled for cost saving, so containers need public IP to pull Docker image from the internet.
    assign_public_ip = true
  }

  # ---------------------------------------------------------
  # Terraform apply won't override changes made by GH actions to task_definition
  lifecycle {
    ignore_changes = [task_definition]
  }
}
