resource "aws_cloudwatch_event_rule" "sanity_check_cron" {
  name        = "sanity-check-cron-${terraform.workspace}"
  description = "Sanity check cron"

  schedule_expression = "cron(0 8 1 * ? *)" # Run at 8:00 AM UTC on the 1st day of every month
}

resource "aws_cloudwatch_event_target" "sanity_check_cron" {
  rule      = aws_cloudwatch_event_rule.sanity_check_cron.name
  target_id = "SubmitSanityCheckJob"
  arn       = aws_batch_job_queue.sanity_check_cron_job_queue.arn
  role_arn  = aws_iam_role.event_bus_invoke_remote_event_bus.arn
  batch_target {
    job_definition = aws_batch_job_definition.sanity_check_cron_job_definition.arn
    job_name       = "sanity-check-cron-${terraform.workspace}"
  }
}

data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}


resource "aws_iam_role" "event_bus_invoke_remote_event_bus" {
  name               = "event-bus-invoke-remote-event-bus-${terraform.workspace}"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

data "aws_iam_policy_document" "event_bus_invoke_remote_event_bus" {
  statement {
    effect    = "Allow"
    actions   = ["events:PutEvents"]
    resources = ["*"]
  }
  statement {
    effect  = "Allow"
    actions = ["batch:SubmitJob"]
    resources = [
      "*"
    ]
  }
}

resource "aws_iam_policy" "event_bus_invoke_remote_event_bus" {
  name   = "event-bus-invoke-remote-event-bus-${terraform.workspace}"
  policy = data.aws_iam_policy_document.event_bus_invoke_remote_event_bus.json
}

resource "aws_iam_role_policy_attachment" "event_bus_invoke_remote_event_bus" {
  role       = aws_iam_role.event_bus_invoke_remote_event_bus.name
  policy_arn = aws_iam_policy.event_bus_invoke_remote_event_bus.arn
}
