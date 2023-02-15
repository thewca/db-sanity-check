resource "aws_security_group" "sanity_check_sg" {
  name   = "sanity-check-security-group${local.env_suffix}"
  vpc_id = aws_default_vpc.default.id

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = {
    Reason = "Sanity Check"
  }
}
