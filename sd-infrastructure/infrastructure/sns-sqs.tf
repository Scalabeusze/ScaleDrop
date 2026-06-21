# Create SNS Topic
resource "aws_sns_topic" "file_updates" {
  name = "sd-file-updates-topic"
}

# Create SQS Queue
resource "aws_sqs_queue" "file_updates_queue" {
  name = "sd-file-updates-queue"
}

# Connect SNS Topic to SQS Queue
resource "aws_sns_topic_subscription" "file_updates_sqs_target" {
  topic_arn = aws_sns_topic.file_updates.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.file_updates_queue.arn

  depends_on = [
    aws_sqs_queue_policy.sns_to_sqs
  ]
}

# Security policy
# By default SNS does not have permissions to send messages to SQS
# It has to be explicitly allowed in the SQS resource policy
resource "aws_sqs_queue_policy" "sns_to_sqs" {
  queue_url = aws_sqs_queue.file_updates_queue.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.file_updates_queue.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.file_updates.arn
          }
        }
      }
    ]
  })
}
