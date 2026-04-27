# ECR Repository
resource "aws_ecr_repository" "sd_upload_repo" {
  name                 = "sd-upload"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# Internal and Swagger passwords (separate for isolation)
resource "random_password" "upload_doc_password" { 
    length = 12
    special = false 
}

resource "aws_ssm_parameter" "upload_doc_password_param" {
  name  = "/dev/upload/security/doc-password"
  type  = "SecureString"
  value = random_password.upload_doc_password.result
}

resource "random_password" "upload_internal_password" { 
    length = 16
    special = true 
}

resource "aws_ssm_parameter" "upload_internal_password_param" {
  name  = "/dev/upload/security/internal-password"
  type  = "SecureString"
  value = random_password.upload_internal_password.result
}

# CloudWatch Logs
resource "aws_cloudwatch_log_group" "sd_upload_logs" {
  name              = "/ecs/sd-upload"
  retention_in_days = 3
}

# App permissions (S3 and SNS)
resource "aws_iam_role" "sd_upload_task_role" {
  name = "sd-upload-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy" "sd_upload_task_policy" {
  name = "sd-upload-task-policy"
  role = aws_iam_role.sd_upload_task_role.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["s3:PutObject", "s3:GetObject", "s3:DeleteObject", "s3:ListBucket"]
        Resource = [aws_s3_bucket.fileserver.arn, "${aws_s3_bucket.fileserver.arn}/*"]
      },
      {
        Effect   = "Allow"
        Action   = "sns:Publish"
        Resource = aws_sns_topic.file_updates.arn
      }
    ]
  })
}

# Task definition
resource "aws_ecs_task_definition" "sd_upload" {
  family                   = "sd-upload-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512

  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  # Attack S3/SNS role to the app
  task_role_arn      = aws_iam_role.sd_upload_task_role.arn 

  container_definitions = jsonencode([{
    name      = "sd-upload"
    image     = "${aws_ecr_repository.sd_upload_repo.repository_url}:latest"
    essential = true

    portMappings = [{ containerPort = 8080, hostPort = 8080 }]

    environment = [
      {
        name  = "DB_URL"
        value = "${aws_db_instance.postgres.endpoint}/sd_database?currentSchema=sd_upload"
      },
      { name  = "DB_USERNAME", value = aws_db_instance.postgres.username },
      { 
        name  = "AWS_S3_BUCKET_NAME", 
        value = aws_s3_bucket.fileserver.bucket
      },
      { 
        name  = "AWS_FILE_UPDATES_SNS_TOPIC_ARN", 
        value = aws_sns_topic.file_updates.arn
      }
    ]

    secrets = [
      { name = "DB_PASSWORD", valueFrom = aws_ssm_parameter.db_password_param.arn },
      { name = "SECURITY_ACCESS_DOCUMENTATION_PASSWORD", valueFrom = aws_ssm_parameter.upload_doc_password_param.arn },
      { name = "SECURITY_ACCESS_INTERNAL_PASSWORD", valueFrom = aws_ssm_parameter.upload_internal_password_param.arn }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.sd_upload_logs.name
        "awslogs-region"        = "eu-north-1"
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "sd_upload_service" {
  name            = "sd-upload-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.sd_upload.arn
  
  desired_count   = 0 
  
  launch_type     = "FARGATE"
  health_check_grace_period_seconds = 120

  network_configuration {
    subnets          = module.vpc.public_subnets
    security_groups  = [aws_security_group.app_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.sd_upload_tg.arn
    container_name   = "sd-upload"
    container_port   = 8080
  }

  lifecycle {
    ignore_changes = [task_definition]
  }
}