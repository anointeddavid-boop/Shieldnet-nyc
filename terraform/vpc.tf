# VPC
resource "aws_vpc" "shieldnet_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.project_name}-vpc"
    Environment = var.environment
    Project     = "ShieldNet NYC"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "shieldnet_igw" {
  vpc_id = aws_vpc.shieldnet_vpc.id

  tags = {
    Name    = "${var.project_name}-igw"
    Project = "ShieldNet NYC"
  }
}

# Public Subnet
resource "aws_subnet" "shieldnet_public_subnet" {
  vpc_id                  = aws_vpc.shieldnet_vpc.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name    = "${var.project_name}-public-subnet"
    Project = "ShieldNet NYC"
  }
}

# Route Table
resource "aws_route_table" "shieldnet_rt" {
  vpc_id = aws_vpc.shieldnet_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.shieldnet_igw.id
  }

  tags = {
    Name    = "${var.project_name}-rt"
    Project = "ShieldNet NYC"
  }
}

# Route Table Association
resource "aws_route_table_association" "shieldnet_rta" {
  subnet_id      = aws_subnet.shieldnet_public_subnet.id
  route_table_id = aws_route_table.shieldnet_rt.id
}

# Security Group
resource "aws_security_group" "shieldnet_sg" {
  name        = "${var.project_name}-sg"
  description = "ShieldNet NYC security group"
  vpc_id      = aws_vpc.shieldnet_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH"
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "ShieldNet API"
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name    = "${var.project_name}-sg"
    Project = "ShieldNet NYC"
  }
}
