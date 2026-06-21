# Target group
resource "aws_lb_target_group" "sd_iam_tg" {
  name        = "sd-iam-tg"
  port        = 7300
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/sd-iam/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 20
    matcher             = "200"
  }
}

# Redirection rule
resource "aws_lb_listener_rule" "sd_iam_rule" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.sd_iam_tg.arn
  }

  condition {
    path_pattern {
      values = ["/sd-iam/*"]
    }
  }
}

output "sd_iam_alb_dns_name" {
  value = aws_lb.main.dns_name
}
