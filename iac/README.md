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

- Init terraform

```bash
terraform init
```

- Select the workspace

```bash
terraform workspace select staging || terraform workspace new staging
```

- Run the plan

```bash
terraform plan
```
