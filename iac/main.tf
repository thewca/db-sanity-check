terraform {
  backend "s3" {
    bucket = "{{SOME BUCKET HERE}}"
    key    = "sanity-check"
    region = "us-west-2"
  }
}

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}
