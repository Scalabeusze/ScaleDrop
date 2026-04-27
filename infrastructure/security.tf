# Load Balancer Firewall (Front Door)
resource "aws_security_group" "alb_sg" {
  name        = "sd-alb-sg"
  description = "Allow inbound HTTP/HTTPS traffic to ALB from the internet"
  vpc_id      = module.vpc.vpc_id

  # Allow ingress HTTP traffic from anywhere
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] 
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Firewall for containers (Backend)
resource "aws_security_group" "app_sg" {
  name        = "sd-app-sg"
  description = "Security Group for microservices (BFF, account, upload, download)"
  vpc_id      = module.vpc.vpc_id

  # Allow ingress traffic only from ALB
  ingress {
    description     = "Allow traffic from ALB"
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  # Outgoing traffic: We allow containers to go out to the internet (e.g., to connect to Google Auth or fetch packages)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# PostgreSQL Firewall (Database Layer)
resource "aws_security_group" "rds_sg" {
  name        = "sd-rds-sg"
  description = "Security Group for PostgreSQL database"
  vpc_id      = module.vpc.vpc_id

  # Ingress: We allow ONLY traffic on port 5432 and ONLY from resources that have the "app_sg" firewall (i.e., containers)
  ingress {
    description     = "Dostep tylko z wewnetrznych mikroserwisow"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }
}