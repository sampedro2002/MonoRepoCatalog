variable "aws_region" {
  description = "AWS region. The Learner Lab only allows us-east-1 and us-west-2."
  type        = string
  default     = "us-east-1"
}

variable "dockerhub_username" {
  description = "Docker Hub user that owns the public catalog-backend and catalog-frontend images"
  type        = string
}

variable "image_tag" {
  description = "Image tag to run: prod (promoted from main) or qa (built after the E2E tests on test)"
  type        = string
  default     = "prod"
}

variable "instance_type" {
  description = "EC2 size. t3.small (2 GB) fits Postgres, the Spring Boot API and nginx"
  type        = string
  default     = "t3.small"
}

variable "key_name" {
  description = "Existing EC2 key pair. The Learner Lab ships one called vockey (labsuser.pem)"
  type        = string
  default     = "vockey"
}

variable "ssh_allowed_cidr" {
  description = "Who may SSH into the host. GitHub-hosted runners have no fixed IP, so the CD needs 0.0.0.0/0"
  type        = string
  default     = "0.0.0.0/0"
}

variable "load_demo_data" {
  description = "Also apply db/seed (15 demo products). Production normally runs the schema only"
  type        = bool
  default     = false
}
