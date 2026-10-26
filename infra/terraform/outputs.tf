output "public_ip" {
  description = "Fixed IP of the host. Store it as the EC2_HOST variable in GitHub"
  value       = aws_eip.app.public_ip
}

output "app_url" {
  description = "Open this from any phone or computer"
  value       = "http://${aws_eip.app.public_ip}"
}

output "ssh_command" {
  value = "ssh -i labsuser.pem ec2-user@${aws_eip.app.public_ip}"
}

output "db_password" {
  description = "Postgres password written to /opt/catalog/.env on the host"
  value       = random_password.db.result
  sensitive   = true
}
