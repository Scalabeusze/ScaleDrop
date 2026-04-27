# sd-upload target group
resource "aws_lb_target_group" "sd_upload_tg" {
  name        = "sd-upload-tg"
  port        = 8080 # Port matches application.yml
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/sd-upload/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 20
    matcher             = "200"
  }
}

# sd-upload redirection rule
resource "aws_lb_listener_rule" "sd_upload_rule" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 110 # Priority must be different than in sd-iam_rule (which had 100)

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.sd_upload_tg.arn
  }

  condition {
    path_pattern {
      values = ["/sd-upload/*"]
    }
  }
}