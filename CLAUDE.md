# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a cross-cloud Infrastructure as Code (IaC) demo project that provisions and deploys a Java Spring Boot application (Taiwan City Data Service) to Azure App Service. The architecture supports both Terraform and Pulumi workflows with automated deployment scripts.

## Key Architecture

- **Multi-IaC Support**: Both Terraform and Pulumi implementations for the same infrastructure
- **Unified Deployment**: Each IaC stack has its own `deploy-app.sh` script that builds the Java application and deploys it
- **Configuration Management**: Pulumi uses YAML config files (`Pulumi.dev.yaml`) while Terraform uses `.tfvars` files
- **Resource Abstraction**: Terraform uses modular structure with separate modules for resource groups, app service plans, and web apps

## Common Commands

### Java Application (Maven)
```bash
# Build the Spring Boot application
cd azure-app-service/applications/taiwan-city-demo
mvn clean package

# Run locally
mvn spring-boot:run
```

### Pulumi Workflow
```bash
cd azure-app-service/infrastructure/pulumi

# Initialize (first time)
pulumi login
pulumi stack init dev

# Deploy infrastructure and application
pulumi up
./deploy-app.sh

# Destroy resources
pulumi destroy
```

### Terraform Workflow  
```bash
cd azure-app-service/infrastructure/terraform

# Initialize and deploy infrastructure
terraform init
terraform apply -var-file=environments/dev/main.tfvars

# Deploy application
./deploy-app.sh

# Destroy resources
terraform destroy -var-file=environments/dev/main.tfvars
```

## Configuration Requirements

### Pulumi Configuration
- Copy `Pulumi.dev.yaml.example` to `Pulumi.dev.yaml` and customize:
  - `resourceGroupName`: Azure resource group name
  - `webAppName`: Azure Web App name
  - `appServicePlanName`: App Service Plan name
  - `javaVersion`: Java runtime version (e.g., "Java|21")
  - `appServiceSku`: SKU tier (e.g., "B1")

### Terraform Configuration
- Create `environments/dev/dev.auto.tfvars` (not tracked in git) with subscription_id
- Or set environment variable: `export TF_VAR_subscription_id=your-id`

## Prerequisites
- Azure CLI (`az`) logged in with `az login`
- Maven (`mvn`) for building Java applications
- Pulumi CLI for Pulumi workflows
- Terraform CLI for Terraform workflows

## Important Files

- `azure-app-service/applications/taiwan-city-demo/`: Spring Boot application source
- `azure-app-service/infrastructure/pulumi/main.go`: Pulumi Go infrastructure code
- `azure-app-service/infrastructure/terraform/main.tf`: Terraform infrastructure code
- `*/deploy-app.sh`: Automated deployment scripts that build JAR and deploy to Azure

## Deployment Scripts Logic

Both `deploy-app.sh` scripts:
1. Build Spring Boot JAR using Maven
2. Package JAR into ZIP file
3. Extract resource names from IaC outputs (Pulumi stack outputs or Terraform outputs)
4. Deploy ZIP to Azure Web App using `az webapp deploy`

## Git Commit Guidelines

- All commit messages must be in English
- Follow conventional commit format when possible