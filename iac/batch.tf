data "aws_iam_policy_document" "sanity_check_execution_role" {
  version = "2012-10-17"
  statement {
    sid     = ""
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "sanity_check_execution_role" {
  name               = "sanity-check-execution-role${local.env_suffix}"
  assume_role_policy = data.aws_iam_policy_document.sanity_check_execution_role.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role" {
  role       = aws_iam_role.sanity_check_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_batch_job_definition" "sanity_check_cron_job_definition" {
  name = "sanity-check-job-definition${local.env_suffix}"
  type = "container"
  platform_capabilities = [
    "FARGATE",
  ]

  container_properties = <<CONTAINER_PROPERTIES
{
  "command": [],
  "image": "thewca/db-sanity-check",
  "environment": [
    {
      "name": "service.mail.send",
      "value": "true"
    },
    {
      "name": "service.mail.to",
      "value": "${var.mail_to}"
    },
    {
      "name": "spring.profiles.active",
      "value": "prod"
    },
    {
      "name": "spring.datasource.url",
      "value": "${data.aws_ssm_parameter.database_host.value}"
    },
    {
      "name": "spring.datasource.username",
      "value": "${data.aws_ssm_parameter.database_username.value}"
    },
    {
      "name": "spring.datasource.password",
      "value": "${data.aws_ssm_parameter.database_password.value}"
    },
    {
      "name": "Spring.mail.host",
      "value": "${data.aws_ssm_parameter.mail_host.value}"
    },
    {
      "name": "Spring.mail.authentication",
      "value": "login"
    },
    {
      "name": "spring.mail.username",
      "value": "${data.aws_ssm_parameter.mail_username.value}"
    },
    {
      "name": "Spring.mail.password",
      "value": "${data.aws_ssm_parameter.mail_password.value}"
    },
    {
      "name": "service.mail.from",
      "value": "${data.aws_ssm_parameter.mail_from.value}"
    }    
  ],
  "fargatePlatformConfiguration": {
    "platformVersion": "LATEST"
  },
  "networkConfiguration": {
    "assignPublicIp": "ENABLED"
  },
  "resourceRequirements": [
    {
      "type": "VCPU",
      "value": "2"
    },
    {
      "type": "MEMORY",
      "value": "4096"
    }
  ],
  "executionRoleArn": "${aws_iam_role.sanity_check_execution_role.arn}"
}
CONTAINER_PROPERTIES
}

resource "aws_iam_role" "sanity_check_cron_service_role" {
  name = "sanity-check-cron-service-role${local.env_suffix}"

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

resource "aws_iam_role_policy_attachment" "sanity_check_cron_service_role" {
  role       = aws_iam_role.sanity_check_cron_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBatchServiceRole"
}

resource "aws_batch_compute_environment" "sanity_check_cron_compute_environment" {
  type                     = "MANAGED"
  compute_environment_name = "sanity-check-cron-compute-environment${local.env_suffix}"
  state                    = "ENABLED"
  service_role             = aws_iam_role.sanity_check_cron_service_role.arn

  compute_resources {
    max_vcpus = 2

    security_group_ids = [
      aws_security_group.sanity_check_sg.id
    ]

    subnets = [aws_default_subnet.default_az1.id]

    type = "FARGATE"
  }

  depends_on = [aws_iam_role_policy_attachment.sanity_check_cron_service_role]
}

resource "aws_batch_job_queue" "sanity_check_cron_job_queue" {
  name     = "sanity-check-cron-job-queue${local.env_suffix}"
  state    = "ENABLED"
  priority = 1
  compute_environments = [
    aws_batch_compute_environment.sanity_check_cron_compute_environment.arn
  ]
}
