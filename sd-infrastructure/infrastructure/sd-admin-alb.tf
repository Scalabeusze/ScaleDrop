# Admin panel target group (Streamlit runs on 8501)
resource "aws_lb_target_group" "sd_admin_tg" {
  name        = "sd-admin-tg"
  port        = 8501
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/admin/healthz" # Streamlit healthcheck
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }
}

# Admin panel listener rule
resource "aws_lb_listener_rule" "sd_admin_rule" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 50 # Unique priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.sd_admin_tg.arn
  }

  condition {
    path_pattern {
      values = ["/admin/*", "/admin"]
    }
  }
}
