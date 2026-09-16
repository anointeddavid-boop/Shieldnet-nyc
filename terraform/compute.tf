# Get latest Amazon Linux 2 AMI
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

# EC2 Instance
resource "aws_instance" "shieldnet_server" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.shieldnet_public_subnet.id
  vpc_security_group_ids = [aws_security_group.shieldnet_sg.id]

  user_data = <<-USERDATA
    #!/bin/bash
    yum update -y
    yum install -y docker
    service docker start
    usermod -a -G docker ec2-user
    docker run -d \
      --name shieldnet-api \
      -p 8080:8080 \
      --restart unless-stopped \
      shieldnet/benefits-api:latest
  USERDATA

  tags = {
    Name        = "${var.project_name}-server"
    Environment = var.environment
    Project     = "ShieldNet NYC"
  }
}

# S3 Bucket for documents and Terraform state
resource "aws_s3_bucket" "shieldnet_bucket" {
  bucket = "${var.project_name}-documents-${var.environment}-030157669158"

  tags = {
    Name        = "${var.project_name}-documents"
    Environment = var.environment
    Project     = "ShieldNet NYC"
  }
}

# Block public access on S3
resource "aws_s3_bucket_public_access_block" "shieldnet_bucket_pab" {
  bucket = aws_s3_bucket.shieldnet_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
