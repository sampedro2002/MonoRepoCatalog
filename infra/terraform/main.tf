# Single EC2 host running the whole stack with Docker Compose (docker-compose.prod.yml).
# The browser reaches nginx on port 80 through a fixed Elastic IP; nginx proxies /api
# to the backend, and Postgres is only reachable inside the Docker network.

# --- Network: one public subnet ---
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  tags                 = { Name = "catalog-vpc" }
}

data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags                    = { Name = "catalog-public" }
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "catalog-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }
  tags = { Name = "catalog-public" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# --- Firewall: HTTP for everyone, SSH for the deploy ---
resource "aws_security_group" "app" {
  name        = "catalog-app"
  description = "Catalog web host"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Web (nginx)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH (key only)"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_allowed_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# --- Host ---
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

# Generated once and kept in the Terraform state, so nobody has to invent or commit a password
resource "random_password" "db" {
  length  = 24
  special = false
}

resource "aws_instance" "app" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.app.id]

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  # Runs on the first boot only. Later releases are rolled out by the CD over SSH.
  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    compose_file       = file("${path.module}/../../docker-compose.prod.yml")
    dockerhub_username = var.dockerhub_username
    image_tag          = var.image_tag
    db_password        = random_password.db.result
    flyway_locations   = var.load_demo_data ? "classpath:db/migration,classpath:db/seed" : "classpath:db/migration"
  })

  tags = { Name = "catalog-app" }
}

# Fixed public IP: it survives the instance being stopped when the lab session ends
resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"
  tags     = { Name = "catalog-ip" }
}
