# ECR Repository
resource "aws_ecr_repository" "sd_download_repo" {
  name                 = "sd-download"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# Passwords
resource "random_password" "download_doc_password" { 
    length = 12
    special = false
}

resource "aws_ssm_parameter" "download_doc_password_param" {
  name  = "/dev/download/security/doc-password"
  type  = "SecureString"
  value = random_password.download_doc_password.result
}

resource "random_password" "download_internal_password" { 
    length = 16
    special = true
}

resource "aws_ssm_parameter" "download_internal_password_param" {
  name  = "/dev/download/security/internal-password"
  type  = "SecureString"
  value = random_password.download_internal_password.result
}

# CloudWatch Logs
resource "aws_cloudwatch_log_group" "sd_download_logs" {
  name              = "/ecs/sd-download"
  retention_in_days = 3
}

# App Permissions (S3 and SQS)
resource "aws_iam_role" "sd_download_task_role" {
  name = "sd-download-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy" "sd_download_task_policy" {
  name = "sd-download-task-policy"
  role = aws_iam_role.sd_download_task_role.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["s3:GetObject", "s3:ListBucket"]
        Resource = [aws_s3_bucket.fileserver.arn, "${aws_s3_bucket.fileserver.arn}/*"]
      },
      {
        Effect   = "Allow"
        Action   = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:GetQueueUrl"
        ]
        Resource = aws_sqs_queue.file_updates_queue.arn
      }
    ]
  })
}

# Task definition
resource "aws_ecs_task_definition" "sd_download" {
  family                   = "sd-download-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512

  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  task_role_arn      = aws_iam_role.sd_download_task_role.arn 

  container_definitions = jsonencode([{
    name      = "sd-download"
    image     = "${aws_ecr_repository.sd_download_repo.repository_url}:latest"
    essential = true

    portMappings = [{ containerPort = 8080, hostPort = 8080 }]

    environment = [
      {
        name  = "DB_URL"
        value = "${aws_db_instance.postgres.endpoint}/sd_database?currentSchema=sd_download"
      },
      { name  = "DB_USERNAME", value = aws_db_instance.postgres.username },
      { 
        name  = "AWS_S3_BUCKET_NAME", 
        value = aws_s3_bucket.fileserver.bucket
      },
      { 
        name  = "AWS_FILE_UPDATES_SQS_QUEUE_URL", 
        value = aws_sqs_queue.file_updates_queue.url
      }
    ]

    secrets = [
      { name = "DB_PASSWORD", valueFrom = aws_ssm_parameter.db_password_param.arn },
      { name = "SECURITY_ACCESS_DOCUMENTATION_PASSWORD", valueFrom = aws_ssm_parameter.download_doc_password_param.arn },
      { name = "SECURITY_ACCESS_INTERNAL_PASSWORD", valueFrom = aws_ssm_parameter.download_internal_password_param.arn }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.sd_download_logs.name
        "awslogs-region"        = "eu-north-1"
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "sd_download_service" {
  name            = "sd-download-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.sd_download.arn
  
  desired_count   = 0 
  
  launch_type     = "FARGATE"
  health_check_grace_period_seconds = 120

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.app_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.sd_download_tg.arn
    container_name   = "sd-download"
    container_port   = 8080
  }

  lifecycle {
    ignore_changes = [task_definition]
  }
}