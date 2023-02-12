# Sanity Check IAC

IAC for sanity check. It is compatible with AWS.

## Requirements

- An AWS account (already configured with `aws configure`)
- [Terraform](https://developer.hashicorp.com/terraform/downloads)

## Get started

- Navigate to this folder

```bash
cd iac
```

- Replace the bucket name in the file `iac/main.tf`. You can find the state bucket [here](https://s3.console.aws.amazon.com/s3/buckets?region=us-west-2).

- Init terraform

```bash
terraform init
```

- Select the workspace (you can also use workspace prod)

```bash
terraform workspace select staging || terraform workspace new staging
```

- Run the plan

```bash
terraform plan
```
