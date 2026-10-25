terraform {
  backend "s3" {
    bucket         = "sampedro2002-catalog-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  default = "us-east-1"
}

variable "dockerhub_username" {
  description = "Tu usuario de Docker Hub"
  type        = string
}
