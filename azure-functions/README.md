# Azure Functions - Serverless Java Functions

This directory demonstrates Azure Functions deployment using Infrastructure as Code (IaC) with both Terraform and Pulumi, featuring automated Java serverless application deployment with comprehensive CSV processing capabilities.

## Directory Structure
```
azure-functions/
├── applications/
│   └── cloud-demo-functions/    # Java Azure Functions application
│       ├── src/main/java/org/cloud/demo/
│       │   ├── Function.java           # Main Functions class
│       │   ├── config/                 # Configuration classes
│       │   ├── model/                  # Data models
│       │   ├── service/                # Business services
│       │   └── util/                   # Utility classes
│       ├── host.json                   # Functions runtime config
│       ├── local.settings.json         # Local development settings
│       └── pom.xml                     # Maven configuration
├── infrastructure/
│   ├── pulumi/                  # Pulumi (TypeScript) IaC implementation
│   │   ├── index.ts
│   │   ├── Pulumi.dev.yaml.example
│   │   └── deploy-app.sh        # Automated deployment script
│   └── terraform/               # Terraform IaC implementation
│       ├── main.tf
│       ├── terraform.tfvars.example
│       └── deploy-app.sh        # Automated deployment script
└── README.md
```

## Application Overview

The `cloud-demo-functions` application provides a comprehensive serverless solution with:

### HTTP-Triggered Functions
- `HttpExample` - Simple "Hello World" function with name parameter
- `TaiwanCities` - Returns Taiwan cities data with environment-based configuration
- `Config` - Displays current environment variables and configuration settings
- `KeyVaultSecrets` - Demonstrates Azure Key Vault integration (optional)
- `DataReader` - Reads processed CSV data from Cosmos DB
- `QueryData` - Advanced data querying capabilities

### Blob-Triggered Functions
- `CsvBlobProcessor` - Automatically processes CSV files uploaded to blob storage

### Key Features
- **CSV File Processing**: Automatic validation and processing of uploaded CSV files
- **Azure Cosmos DB Integration**: Stores processed data for querying
- **Azure Key Vault Integration**: Secure secrets management (optional)
- **SFTP Upload Support**: Dedicated SFTP-enabled storage for file uploads
- **Environment Configuration**: Flexible configuration via environment variables
- **Error Handling**: Comprehensive error logging and file management

## Prerequisites
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) logged in (`az login`)
- [Maven](https://maven.apache.org/install.html) for building Java projects
- [Pulumi CLI](https://www.pulumi.com/docs/get-started/install/) (for Pulumi workflow)
- [Terraform CLI](https://developer.hashicorp.com/terraform/install) (for Terraform workflow)

## Deployment Options

### Option 1: Terraform
```bash
cd azure-functions/infrastructure/terraform

# Configure your subscription ID
export TF_VAR_subscription_id=your-subscription-id

# Deploy infrastructure
terraform init
terraform apply

# Deploy Functions application
./deploy-app.sh

# Test the endpoints
FUNCTION_URL=$(terraform output -raw function_app_url)
curl "$FUNCTION_URL/api/HttpExample?name=World"
curl "$FUNCTION_URL/api/cities"
curl "$FUNCTION_URL/api/config"
```

### Option 2: Pulumi
```bash
cd azure-functions/infrastructure/pulumi

# Configure stack settings
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit Pulumi.dev.yaml with your settings

# Deploy infrastructure
pulumi login
pulumi stack init dev  # or select existing
pulumi up

# Deploy Functions application
./deploy-app.sh

# Test the endpoints
FUNCTION_URL=$(pulumi stack output functionAppUrl)
curl "$FUNCTION_URL/api/HttpExample?name=World"
curl "$FUNCTION_URL/api/cities"
curl "$FUNCTION_URL/api/config"
```

## CSV Processing Workflow

When Cosmos DB is enabled, you can test the complete CSV processing pipeline:

### 1. Upload Test Files
```bash
# Get storage account from outputs
STORAGE_ACCOUNT=$(terraform output -raw storage_account_name)

# Upload CSV file to trigger processing
az storage blob upload \
  --account-name $STORAGE_ACCOUNT \
  --container-name csv-uploads \
  --name test_data.csv \
  --file test_data_valid.csv
```

### 2. Monitor Processing
```bash
# Check processed data
curl "$FUNCTION_URL/api/data"

# Check processing results
az storage blob list --account-name $STORAGE_ACCOUNT --container-name csv-success
az storage blob list --account-name $STORAGE_ACCOUNT --container-name csv-failure
```

## Local Development

```bash
cd azure-functions/applications/cloud-demo-functions

# Build and run locally
mvn clean package
func start
```

Local endpoints:
- `http://localhost:7071/api/HttpExample?name=World`
- `http://localhost:7071/api/cities`
- `http://localhost:7071/api/config`

## Configuration

### Environment Variables
The application supports these environment variables:
- `APP_ENVIRONMENT` - Application environment (development/staging/production)
- `API_VERSION` - API version for client compatibility
- `DEBUG_MODE` - Enable detailed logging ("true"/"false")
- `MAX_CITIES_COUNT` - Maximum cities to return
- `COSMOS_DB_ENABLED` - Enable Cosmos DB integration
- `KEY_VAULT_ENABLED` - Enable Key Vault integration

### Configuration Files
- **Pulumi**: Configure in `Pulumi.dev.yaml`
- **Terraform**: Configure in `terraform.tfvars`
- **Local**: Configure in `local.settings.json`

## Deployment Scripts
Each `deploy-app.sh` script automatically:
1. Builds the Java project using Maven
2. Creates ZIP deployment package
3. Extracts resource names from IaC outputs
4. Deploys to Azure Function App using Azure CLI

## Clean Up
```bash
# Pulumi
pulumi destroy

# Terraform
terraform destroy
```

For detailed configuration and SFTP setup, see [CLAUDE.md](../CLAUDE.md).