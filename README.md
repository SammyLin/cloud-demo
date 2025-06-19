# Cloud Demo - Multi-Cloud Infrastructure as Code

This project demonstrates cross-cloud Infrastructure as Code (IaC) implementations with both Azure App Service and Azure Functions deployments. Each implementation supports both Terraform and Pulumi workflows with automated Java application deployment.

## Project Structure

```
cloud-demo/
├── azure-app-service/      # Azure App Service (Spring Boot)
│   ├── applications/
│   │   └── taiwan-city-demo/     # Spring Boot application
│   └── infrastructure/
│       ├── pulumi/               # Pulumi IaC + deploy script
│       └── terraform/            # Terraform IaC + deploy script
├── azure-functions/        # Azure Functions (Serverless)
│   ├── applications/
│   │   └── cloud-demo-functions/ # Java Azure Functions
│   └── infrastructure/
│       ├── pulumi/               # Pulumi IaC + deploy script  
│       └── terraform/            # Terraform IaC + deploy script
├── CLAUDE.md               # Detailed configuration guide
└── README.md               # This overview
```

## Available Implementations

### 1. Azure App Service (Spring Boot)
- **Path:** `azure-app-service/`
- **Technology:** Java Spring Boot web application
- **Infrastructure:** Pulumi (Go) and Terraform
- **Deployment:** Automated via `deploy-app.sh` scripts

### 2. Azure Functions (Serverless)
- **Path:** `azure-functions/`
- **Technology:** Java Azure Functions with HTTP and Blob triggers
- **Features:**
  - HTTP-triggered functions for API endpoints
  - Blob-triggered functions for CSV file processing
  - Azure Cosmos DB integration for data storage
  - Azure Key Vault integration (optional)
  - SFTP upload support for file processing
- **Infrastructure:** Pulumi (TypeScript) and Terraform
- **Deployment:** Automated via `deploy-app.sh` scripts

## Quick Start

### Prerequisites
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) logged in (`az login`)
- [Maven](https://maven.apache.org/install.html) for Java builds
- [Pulumi CLI](https://www.pulumi.com/docs/get-started/install/) or [Terraform CLI](https://developer.hashicorp.com/terraform/install)

### Deployment Options

Choose your preferred implementation and IaC tool:

#### Azure App Service with Pulumi
```bash
cd azure-app-service/infrastructure/pulumi
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit configuration file
pulumi up
./deploy-app.sh
```

#### Azure Functions with Terraform
```bash
cd azure-functions/infrastructure/terraform
# Configure terraform.tfvars
terraform init
terraform apply
./deploy-app.sh
```

## Documentation

For detailed configuration and deployment instructions:
- **[CLAUDE.md](CLAUDE.md)** - Complete configuration guide and commands reference
- **[Azure App Service README](azure-app-service/README.md)** - Spring Boot implementation details
- **[Azure Functions README](azure-functions/README.md)** - Serverless implementation details

## Key Features

### Azure App Service
- Spring Boot web application
- Unified deployment with Maven build integration
- Support for both Pulumi (Go) and Terraform IaC

### Azure Functions  
- Java-based serverless functions
- HTTP and Blob trigger support
- CSV file processing workflow
- Azure Cosmos DB integration for data storage
- Optional Azure Key Vault for secrets management
- SFTP upload capabilities

## Clean Up

```bash
# Pulumi
pulumi destroy

# Terraform  
terraform destroy
```

---

**Architecture:** This project demonstrates unified multi-cloud IaC patterns that can be extended to Google Cloud Platform (GCP) and Amazon Web Services (AWS).