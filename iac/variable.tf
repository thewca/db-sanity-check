variable "aws_region" {
  description = "Default region"
  default     = "us-west-2"
}

variable "environment_to_suffix_map" {
  type = map(any)
  default = {
    dev     = "-dev"
    staging = "-stg"
    prod    = "-prod"
  }
}

locals {
  env_suffix = lookup(var.environment_to_suffix_map, terraform.workspace, "-dev")
}

variable "mail_to" {
  description = "Email receiver"
  default     = "wrt@worldcubeassociation.org,wst@worldcubeassociation.org"
}

