# Serverless File Processing Workflow – Azure Functions Demo

This project demonstrates a complete serverless file processing workflow using Azure Functions with automatic CSV processing, SFTP upload support, and cloud service integrations. The system automatically processes files uploaded via blob storage or SFTP, validates data, stores results in Cosmos DB, and provides REST APIs for data access.

---

- **Core functionality:** Event-driven CSV file processing with automatic triggers
- **Upload methods:** Blob storage uploads and SFTP file transfers
- **Architecture:** Serverless functions with comprehensive cloud service integration
- **Demo apps:** Java Azure Functions with HTTP triggers and Blob triggers for automated file processing

## Project Structure

```
cloud-demo/
├── azure-app-service/      # Azure App Service IaC & Spring Boot application
│   ├── applications/       # Spring Boot app source code
│   │   └── taiwan-city-demo/
│   └── infrastructure/     # Pulumi & Terraform configs/scripts
│       ├── pulumi/
│       │   └── deploy-app.sh
│       └── terraform/
│           └── deploy-app.sh
├── azure-functions/        # Azure Functions IaC & serverless application
│   ├── applications/       # Java Azure Functions source code
│   │   └── taiwan-city-functions/
│   └── infrastructure/     # Pulumi & Terraform configs/scripts
│       ├── pulumi/
│       │   └── deploy-app.sh
│       └── terraform/
│           └── deploy-app.sh
└── README.md               # This documentation
```

## Available Implementations

### 1. Azure App Service (Spring Boot)
- **Location:** `azure-app-service/`
- **Technology:** Java Spring Boot web application
- **Endpoints:** 
  - `/` - Hello World endpoint
  - Custom endpoints for Taiwan city data
- **Infrastructure:** Pulumi and Terraform support
- **Features:** Web application hosting, automated deployment

### 2. Azure Functions (Serverless)
- **Location:** `azure-functions/`
- **Technology:** Java Azure Functions (HTTP triggers + Blob triggers)
- **Endpoints:**
  - `/api/HttpExample` - Simple "Hello World" function
  - `/api/cities` - Taiwan cities data with environment-based configuration
  - `/api/config` - Display current environment variables
  - `/api/secrets` - Key Vault integration demonstration (optional)
  - `/api/data` - Read processed CSV data from Cosmos DB
- **Features:** 
  - Serverless computing with environment variables support
  - **CSV Processing Workflow**: Automatic processing of uploaded CSV files
  - **Azure Cosmos DB integration**: Storage for processed data
  - **Azure Key Vault integration**: Secure secret management (optional)
  - **File management**: Success/failure handling with error logging
- **Infrastructure:** Pulumi and Terraform support

## Environment Variables Support

The Azure Functions implementation includes comprehensive environment variables support:

### Core Application Settings
- `APP_ENVIRONMENT` - Application environment (development/staging/production)
- `API_VERSION` - API version for client compatibility  
- `DEBUG_MODE` - Enable detailed logging when set to "true"
- `MAX_CITIES_COUNT` - Maximum number of cities to return

### CSV Processing & Cosmos DB Settings
- `COSMOS_CONNECTION_STRING` - MongoDB connection string for Cosmos DB
- `COSMOS_DATABASE` - Cosmos database name (default: csvdata)
- `COSMOS_COLLECTION` - Cosmos collection name (default: records)
- `DATA_LIMIT` - Maximum records returned by API (default: 100)
- `COSMOS_DB_ENABLED` - Enable Cosmos DB integration (true/false)

### Key Vault Settings (Optional)
- `KEY_VAULT_ENABLED` - Enable Key Vault integration (true/false)
- `KEY_VAULT_NAME` - Azure Key Vault name
- `API_KEY` - Key Vault reference for API key
- `DATABASE_CONNECTION` - Key Vault reference for database connection

## Prerequisites

- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) installed and logged in (`az login`)
- [Maven](https://maven.apache.org/install.html) for building Java applications
- [Pulumi CLI](https://www.pulumi.com/docs/get-started/install/) (for Pulumi workflows)
- [Terraform CLI](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli) (for Terraform workflows)

## Quick Start

### Azure App Service (Spring Boot)
```bash
cd azure-app-service/infrastructure/pulumi
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit Pulumi.dev.yaml with your settings
pulumi up
./deploy-app.sh
```

### Azure Functions (Serverless)

#### Basic Setup (Cities API only)
```bash
cd azure-functions/infrastructure/pulumi  
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit Pulumi.dev.yaml with your settings
pulumi up
./deploy-app.sh
```

#### Full Setup (with CSV Processing & Cosmos DB)
```bash
cd azure-functions/infrastructure/pulumi  
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit Pulumi.dev.yaml and enable CSV processing:
# enableCosmosDb: true
# cosmosDbAccountName: your-cosmos-account
pulumi up
./deploy-app.sh
```

#### Terraform Alternative
```bash
cd azure-functions/infrastructure/terraform
# Edit environments/dev/main.tfvars and enable:
# enable_cosmos_db = true
terraform init
terraform apply -var-file=environments/dev/main.tfvars
./deploy-app.sh
```

## Configuration Management

Both implementations use configuration files for environment-specific settings:

- **Pulumi:** `Pulumi.dev.yaml` files for stack configuration
- **Terraform:** `.tfvars` files for variable configuration
- **Local Development:** `local.settings.json` (Functions) and `application.properties` (Spring Boot)

## Deployment Scripts

Each implementation includes automated deployment scripts (`deploy-app.sh`) that:

1. Build the Java application using Maven
2. Create deployment packages
3. Extract resource information from IaC outputs
4. Deploy to Azure using Azure CLI

## Documentation

For detailed implementation-specific documentation, see:
- [Azure App Service README](azure-app-service/README.md)
- [Azure Functions README](azure-functions/README.md)
- [SFTP Upload Guide](SFTP_UPLOAD_GUIDE.md) - Complete SFTP setup and testing guide
- [CLAUDE.md](CLAUDE.md) - Project configuration and commands reference

## Testing Endpoints

After deployment, test the applications:

**Azure App Service:**
```bash
curl "https://your-webapp.azurewebsites.net/"
```

**Azure Functions (Core APIs):**
```bash
curl "https://your-functions.azurewebsites.net/api/HttpExample?name=World"
curl "https://your-functions.azurewebsites.net/api/cities"
curl "https://your-functions.azurewebsites.net/api/config"
curl "https://your-functions.azurewebsites.net/api/secrets"  # If Key Vault enabled
```

**Azure Functions (CSV Processing - if Cosmos DB enabled):**
```bash
# Read processed CSV data
curl "https://your-functions.azurewebsites.net/api/data"
```

## CSV Processing Workflow

When Cosmos DB is enabled, the system provides a complete CSV processing pipeline:

### 1. Upload Test Files
Use the provided test files for testing:
- `test_data_valid.csv` - Valid CSV with 10 records
- `test_data_invalid.csv` - Mixed valid/invalid data  
- `test_data_malformed.csv` - Invalid CSV format

### 2. Get SFTP Storage Account Name
After Terraform deployment, get the SFTP storage account name:
```bash
# Get SFTP storage account name from Terraform output
SFTP_STORAGE_ACCOUNT=$(terraform output -raw sftp_storage_account_name)
echo "SFTP Storage Account: $SFTP_STORAGE_ACCOUNT"
```

### 3. Upload via Azure CLI (Simplest Method)
```bash
# Upload to trigger processing (use SFTP storage account)
az storage blob upload \
  --account-name $SFTP_STORAGE_ACCOUNT \
  --container-name csv-uploads \
  --name test_data_valid.csv \
  --file test_data_valid.csv
```

### 4. Upload via SFTP (Recommended for Production)
The system includes a dedicated SFTP-enabled storage account with pre-configured SSH key authentication:

```bash
# Copy the generated SSH private key to a secure location
cp /tmp/azure_sftp_key ~/.ssh/azure_sftp_key
chmod 600 ~/.ssh/azure_sftp_key

# Get SFTP endpoint from Terraform outputs
SFTP_ENDPOINT=$(terraform output -raw sftp_endpoint)
SFTP_USER=$(terraform output -raw sftp_user_name)

# Connect via SFTP and upload files
sftp -i ~/.ssh/azure_sftp_key $SFTP_USER@$SFTP_ENDPOINT

# In SFTP session:
put test_data_valid.csv
put test_data_invalid.csv
put test_data_malformed.csv
exit
```

**Alternative: Direct SCP Upload**
```bash
# Upload files directly via SCP
scp -i ~/.ssh/azure_sftp_key test_data_valid.csv $SFTP_USER@$SFTP_ENDPOINT:/
scp -i ~/.ssh/azure_sftp_key test_data_invalid.csv $SFTP_USER@$SFTP_ENDPOINT:/
scp -i ~/.ssh/azure_sftp_key test_data_malformed.csv $SFTP_USER@$SFTP_ENDPOINT:/
```

### 5. Monitor Processing Results
```bash
# Check success container
az storage blob list \
  --account-name $SFTP_STORAGE_ACCOUNT \
  --container-name csv-success \
  --output table

# Check failure container  
az storage blob list \
  --account-name $SFTP_STORAGE_ACCOUNT \
  --container-name csv-failure \
  --output table

# Download error logs if any
az storage blob download \
  --account-name $SFTP_STORAGE_ACCOUNT \
  --container-name csv-failure \
  --name test_data_malformed_failure.log \
  --file error.log
```

### 6. SFTP Connection Details
- **SFTP Endpoint**: Automatically configured during Terraform deployment
- **Username**: `sftpuser`
- **Authentication**: SSH key-based (private key at `/tmp/azure_sftp_key`)
- **Home Directory**: `csv-uploads` (files uploaded here trigger processing)
- **Permissions**: Full read/write access to csv-uploads container

**⚠️ Important SSH Key Information**:
- Private key location: `/tmp/azure_sftp_key` (generated during Terraform deployment)
- **Save this key securely** - it's required for SFTP access
- Copy to `~/.ssh/azure_sftp_key` and set permissions: `chmod 600`
- If you lose the key, you'll need to regenerate it via Terraform

**Key Management**:
```bash
# Backup the SSH key (run after terraform apply)
cp /tmp/azure_sftp_key ~/.ssh/azure_sftp_key
cp /tmp/azure_sftp_key.pub ~/.ssh/azure_sftp_key.pub
chmod 600 ~/.ssh/azure_sftp_key
chmod 644 ~/.ssh/azure_sftp_key.pub

# Test SFTP connection
sftp -i ~/.ssh/azure_sftp_key sftpuser@$(terraform output -raw sftp_endpoint)
```

For detailed SFTP setup and troubleshooting, see [SFTP_UPLOAD_GUIDE.md](SFTP_UPLOAD_GUIDE.md)

## Clean Up Resources

```bash
# Pulumi
pulumi destroy

# Terraform
terraform destroy -var-file=environments/dev/main.tfvars
```

## Architecture Features

### Azure Functions CSV Processing Pipeline
- **Dual Storage Design**: 
  - Primary storage for Azure Functions runtime (`twfuncsstorage01`)
  - Dedicated SFTP storage for CSV processing (`twfuncsstorage01sftp`)
- **Blob Trigger**: Automatically processes files uploaded to `csv-uploads` container
- **Data Validation**: Validates CSV format and data integrity  
- **Error Handling**: Comprehensive error logging and file management
- **File Management**: 
  - Success → `csv-success` container
  - Failure → `csv-failure` container + `<filename>_failure.log`
- **Data Storage**: Processed records stored in Azure Cosmos DB for MongoDB
- **REST API**: Query processed data via `/api/data` endpoint

### SFTP Integration Features
- **Dedicated SFTP Storage**: Separate Data Lake Storage Gen2 account with SFTP enabled
- **SSH Key Authentication**: Secure key-based authentication (no passwords)
- **Pre-configured User**: `sftpuser` with full permissions to csv-uploads container
- **Automatic SSH Key Generation**: Terraform creates and configures SSH keys during deployment
- **Home Directory**: Direct upload to `csv-uploads` triggers immediate processing

### Optional Integrations
- **Azure Key Vault**: Secure secret management with managed identity
- **Environment Configuration**: Flexible configuration via environment variables
- **Multi-Container Support**: Separate containers for success, failure, and processing states

---

**Next Steps:** Future plans include extending this foundation to Google Cloud Platform (GCP) and Amazon Web Services (AWS) with similar unified deployment patterns.