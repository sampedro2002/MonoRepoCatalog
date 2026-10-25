resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/catalog-backend"
  retention_in_days = 7
}
