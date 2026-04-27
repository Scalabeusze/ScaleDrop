# BFF target group
resource "aws_lb_target_group" "sd_bff_tg" {
  name        = "sd-bff-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/sd-bff/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 20
    matcher             = "200"
  }
}

# BFF listener rule
resource "aws_lb_listener_rule" "sd_bff_rule" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 130 # Unique priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.sd_bff_tg.arn
  }

  condition {
    path_pattern {
      values = ["/sd-bff/*"]
    }
  }
}