# Firewall for containers
resource "aws_security_group" "app_sg" {
  name        = "sd-app-sg"
  description = "Security Group for microservices (BFF, account, upload, download)"
  vpc_id      = module.vpc.vpc_id

  # Outgoing traffic: We allow containers to go out to the internet (e.g., to connect to Google Auth or fetch packages)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Ingress will be created by ALB security group, which will allow traffic on ports 80 and 443 from the internet
}

# PostgreSQL Firewall
resource "aws_security_group" "rds_sg" {
  name        = "sd-rds-sg"
  description = "Security Group for PostgreSQL database"
  vpc_id      = module.vpc.vpc_id

  # Ingress: We allow ONLY traffic on port 5432 and ONLY from resources that have the "app_sg" firewall (i.e., conainers containers)
  ingress {
    description     = "Dostep tylko z wewnetrznych mikroserwisow"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }
}
