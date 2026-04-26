terraform {
  backend "s3" {
    bucket = "sd-fileserver-dev" # Reusing bucket for state backup
    key    = "terraform.tfstate"
    region = "eu-north-1"
  }
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "eu-north-1" # SSetting region is required
}
