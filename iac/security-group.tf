resource "aws_security_group" "default_security_group" {
  name        = "default-security-group"
  description = "Default security group"
  vpc_id      = aws_default_vpc.default.id

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = {
    Type = "Security Group"
  }
}
