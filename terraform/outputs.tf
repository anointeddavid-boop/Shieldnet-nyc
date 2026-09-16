output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.shieldnet_vpc.id
}

output "public_subnet_id" {
  description = "Public subnet ID"
  value       = aws_subnet.shieldnet_public_subnet.id
}

output "security_group_id" {
  description = "Security group ID"
  value       = aws_security_group.shieldnet_sg.id
}

output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.shieldnet_server.id
}

output "ec2_public_ip" {
  description = "EC2 public IP address"
  value       = aws_instance.shieldnet_server.public_ip
}

output "ec2_public_dns" {
  description = "EC2 public DNS"
  value       = aws_instance.shieldnet_server.public_dns
}

output "s3_bucket_name" {
  description = "S3 bucket name"
  value       = aws_s3_bucket.shieldnet_bucket.id
}

output "api_endpoint" {
  description = "ShieldNet API endpoint"
  value       = "http://${aws_instance.shieldnet_server.public_ip}:8080/api/v1/health"
}
