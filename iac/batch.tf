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

resource "aws_batch_job_definition" "statistics_cron_job_definition" {
  name = "sanity-check-job-definition${local.env_suffix}"
  type = "container"
  platform_capabilities = [
    "FARGATE",
  ]

  container_properties = <<CONTAINER_PROPERTIES
{
  "command": [],
  "image": "thewca/statistics-cron",
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
      "value": "1"
    },
    {
      "type": "MEMORY",
      "value": "2048"
    }
  ],
  "executionRoleArn": "${aws_iam_role.sanity_check_execution_role.arn}"
}
CONTAINER_PROPERTIES
}
