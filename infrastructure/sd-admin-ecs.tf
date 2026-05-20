# ECR Repo
resource "aws_ecr_repository" "sd_admin_repo" {
  name                 = "sd-admin"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# Random password
resource "random_password" "admin_password" {
  length  = 16
  special = false
}

resource "aws_ssm_parameter" "admin_password_param" {
  name  = "/dev/admin/security/password"
  type  = "SecureString"
  value = random_password.admin_password.result
}

# Admin panel task role
resource "aws_iam_role" "admin_task_role" {
  name = "sd-admin-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ecs-tasks.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "admin_task_policy" {
  name = "sd-admin-task-policy"
  role = aws_iam_role.admin_task_role.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecs:UpdateService",
          "ecs:DescribeServices",
          "ecs:ListServices",
          "ecs:ListClusters"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "logs:DescribeLogGroups",
          "logs:FilterLogEvents"
        ]
        Resource = "*"
      }
    ]
  })
}

# Logs config
resource "aws_cloudwatch_log_group" "sd_admin_logs" {
  name              = "/ecs/sd-admin"
  retention_in_days = 3
}

# Task definition
resource "aws_ecs_task_definition" "sd_admin" {
  family                   = "sd-admin-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256 # Lowest resources
  memory                   = 512

  # Using existing roles
  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  task_role_arn      = aws_iam_role.admin_task_role.arn

  container_definitions = jsonencode([{
    name      = "sd-admin"
    image     = "${aws_ecr_repository.sd_admin_repo.repository_url}:latest"
    essential = true

    portMappings = [{
      containerPort = 8501
      hostPort      = 8501
    }]

    secrets = [
      {
        name      = "ADMIN_PASSWORD"
        valueFrom = aws_ssm_parameter.admin_password_param.arn
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.sd_admin_logs.name
        "awslogs-region"        = "eu-north-1"
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

# Run on Fargate Spot to cut costs
resource "aws_ecs_service" "sd_admin_service" {
  name            = "sd-admin-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.sd_admin.arn

  desired_count = 1

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 100
  }

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.app_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.sd_admin_tg.arn
    container_name   = "sd-admin"
    container_port   = 8501
  }

  lifecycle {
    ignore_changes = [task_definition]
  }
}
