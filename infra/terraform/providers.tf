terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Local state (terraform.tfstate, gitignored). An AWS Academy Learner Lab account
  # can be reset at any time, so a remote S3 backend inside it would be lost as well.
}

provider "aws" {
  region = var.aws_region
}
