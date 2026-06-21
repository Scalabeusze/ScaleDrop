# Generate random password
resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Store password in Parameter Store (SSM) as a SecureString
resource "aws_ssm_parameter" "db_password_param" {
  name        = "/dev/database/password"
  description = "Admin PostgreSQL database password (MANAGED BY TERRAFORM - DO NOT CHANGE MANUALLY)"
  type        = "SecureString"
  value       = random_password.db_password.result

  tags = {
    Environment = "dev"
    Project     = "sd-services"
  }
}

resource "aws_db_instance" "postgres" {
  identifier           = "sd-database-dev"

  # Engine configuration
  engine               = "postgres"
  engine_version       = "15"          # Stable version
  instance_class       = "db.t3.micro" # Free Tier
  allocated_storage    = 20            # Free Tier (max 20GB)

  # Access configuration
  db_name              = "sd_database"
  username             = "sd_admin"
  password             = random_password.db_password.result # Take secret password

  # Connect to network and firewall
  db_subnet_group_name   = module.vpc.database_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  # Security and cost settings
  publicly_accessible  = false # access only from VPC, not from the internet
  multi_az             = false # x2 more expensive if true
  skip_final_snapshot  = true  # don't do paid backup on destroy

  tags = {
    Project     = "sd-services"
    Environment = "dev"
  }
}
