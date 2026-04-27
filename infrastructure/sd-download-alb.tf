# sd-download target group
resource "aws_lb_target_group" "sd_download_tg" {
  name        = "sd-download-tg"
  port        = 8080 # Port matching application.yml
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/sd-download/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 20
    matcher             = "200"
  }
}

# sd-download redirect rule
resource "aws_lb_listener_rule" "sd_download_rule" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 120 # Different priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.sd_download_tg.arn
  }

  condition {
    path_pattern {
      values = ["/sd-download/*"]
    }
  }
}