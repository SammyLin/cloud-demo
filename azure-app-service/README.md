# Azure App Service - Spring Boot Deployment

This directory demonstrates Azure App Service deployment using Infrastructure as Code (IaC) with both Terraform and Pulumi, featuring automated Spring Boot application deployment.

## Directory Structure
```
azure-app-service/
├── applications/
│   └── taiwan-city-demo/        # Spring Boot application source
├── infrastructure/
│   ├── pulumi/                  # Pulumi (Go) IaC implementation
│   │   ├── main.go
│   │   ├── Pulumi.dev.yaml.example
│   │   └── deploy-app.sh        # Automated deployment script
│   └── terraform/               # Terraform IaC implementation
│       ├── main.tf
│       ├── environments/dev/main.tfvars
│       └── deploy-app.sh        # Automated deployment script
└── README.md
```

## Application Overview
The `taiwan-city-demo` Spring Boot application provides:
- RESTful API endpoints
- Taiwan city data services
- Health check endpoints
- Production-ready configuration

## Prerequisites
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) logged in (`az login`)
- [Maven](https://maven.apache.org/install.html) for building Java applications
- [Pulumi CLI](https://www.pulumi.com/docs/get-started/install/) (for Pulumi workflow)
- [Terraform CLI](https://developer.hashicorp.com/terraform/install) (for Terraform workflow)

## Deployment Options

### Option 1: Terraform
```bash
cd azure-app-service/infrastructure/terraform

# Configure your subscription ID
export TF_VAR_subscription_id=your-subscription-id

# Deploy infrastructure
terraform init
terraform apply -var-file=environments/dev/main.tfvars

# Deploy Spring Boot application
./deploy-app.sh
```

### Option 2: Pulumi
```bash
cd azure-app-service/infrastructure/pulumi

# Configure stack settings
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit Pulumi.dev.yaml with your settings

# Deploy infrastructure
pulumi login
pulumi stack init dev  # or select existing
pulumi up

# Deploy Spring Boot application  
./deploy-app.sh
```

## Deployment Scripts
Each `deploy-app.sh` script automatically:
1. Builds the Spring Boot JAR using Maven (`mvn clean package`)
2. Creates a deployment ZIP package
3. Extracts resource names from IaC outputs
4. Deploys to Azure Web App using `az webapp deploy`

## Testing
After deployment, test your application:
```bash
# Get Web App URL from outputs
WEBAPP_URL=$(pulumi stack output webAppUrl)  # or terraform output
curl "$WEBAPP_URL/"
```

## Configuration
- **Pulumi**: Configure settings in `Pulumi.dev.yaml`
- **Terraform**: Configure variables in `environments/dev/main.tfvars`
- **Application**: Spring Boot settings in `application.properties`

## Clean Up
```bash
# Pulumi
pulumi destroy

# Terraform
terraform destroy -var-file=environments/dev/main.tfvars
```

For detailed configuration options, see [CLAUDE.md](../CLAUDE.md).
