resource "aws_iam_role" "sanity_check_iam_role" {
  name = "sanity-check-service-role"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
          "Action": "sts:AssumeRole",
          "Effect": "Allow",
          "Principal": {
          "Service": "batch.amazonaws.com"
          }
      }
    ]
}
EOF
}

resource "aws_batch_compute_environment" "sanity_check_compute_environment" {
  type                     = "MANAGED"
  compute_environment_name = "sanity-check-compute-environment"
  state                    = "ENABLED"
  service_role             = aws_iam_role.sanity_check_iam_role.arn

  compute_resources {
    max_vcpus          = 1
    type               = "FARGATE"
    subnets            = [aws_default_subnet.default_subnet.id]
    security_group_ids = [aws_security_group.default_security_group.id]
  }

  tags = {
    Reason = "Sanity Check"
  }
}
