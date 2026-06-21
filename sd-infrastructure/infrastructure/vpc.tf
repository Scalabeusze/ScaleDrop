module "vpc" {
  # Using official module to reduce the amount of code we have to write (e.g. routing tables, gateways)
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "sd-vpc-dev"

  # Main IP address pool (65,536 addresses)
  cidr = "10.0.0.0/16"

  # Network split to two zones, required by RDS (must have two subnets to choose from)
  azs = ["eu-north-1a", "eu-north-1b"]

  # Internet Gateway (it's free)
  # Public subnets (ALB and ECS Fargate) - they will go to the internet for free, without NAT
  public_subnets = ["10.0.1.0/24", "10.0.2.0/24"]

  # Isolated database subents
  # Here will lie only PostgreSQL, with no route to the internet (can't fetch anything from outside, and no one from outside can access them)
  database_subnets = ["10.0.20.0/24", "10.0.21.0/24"]

  # Automatically creates a subnet group, wchis will be attached to the database resource
  create_database_subnet_group = true
  create_database_subnet_route_table = true

  # Make sure that the paid address translation mechanism is DISABLED (cost cutting)
  enable_nat_gateway = false
  single_nat_gateway = false

  # DNS config
  # Required by RDS and for internal communication between services (e.g. sd-bff can find sd-account by name, not IP)
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Project     = "sd-services"
    Environment = "dev"
    ManagedBy   = "Terraform"             # Don't manage this resource manually, use Terraform instead
    Owner       = "Scalabeusze"
  }
}
