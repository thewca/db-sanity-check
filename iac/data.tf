resource "aws_default_subnet" "default_az1" {
  availability_zone = "us-west-2a"

  tags = {
    Name = "Default subnet for us-west-2a"
  }
}

data "aws_ssm_parameter" "database_host" {
  name = "/db/credentials/replica/${terraform.workspace}/host"
}

data "aws_ssm_parameter" "database_username" {
  name = "/db/credentials/replica/${terraform.workspace}/user"
}

data "aws_ssm_parameter" "database_password" {
  name = "/db/credentials/replica/${terraform.workspace}/password"
}

data "aws_ssm_parameter" "mail_host" {
  name = "/notifications/${terraform.workspace}/host"
}

data "aws_ssm_parameter" "mail_username" {
  name = "/notifications/${terraform.workspace}/user"
}

data "aws_ssm_parameter" "mail_password" {
  name = "/notifications/${terraform.workspace}/password"
}

data "aws_ssm_parameter" "mail_from" {
  name = "/notifications/${terraform.workspace}/from"
}
