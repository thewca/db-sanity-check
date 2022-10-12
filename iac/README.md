# IAC for db-sanity-check

You should have [terraform](https://www.terraform.io/downloads) installed and a [configured AWS account](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html).

## Get started

This assumes you already have a clone of this repo.

- Navigate to the iac folder

`cd iac`

- Replace the name of the bucket in the file `main.tf`

`bucket = "{{YOUR_BUCKET_NAME}}"`

- Init terraform

`terraform init`

- Apply

`terraform apply`
