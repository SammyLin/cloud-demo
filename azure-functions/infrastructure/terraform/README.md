# Azure Functions Terraform Infrastructure

This directory contains the Terraform configuration for the Azure Functions project, following the Public Cloud Platform Resource Naming Standards.

## File Structure

```
terraform/
├── main.tf                    # Main infrastructure configuration
├── variables.tf               # Variable definitions with naming standard
├── outputs.tf                 # Output values
├── terraform.tfvars           # Root configuration (default values)
├── terraform.tfvars.example   # Example configuration template
├── deploy-app.sh              # Deployment script
├── modules/                   # Reusable Terraform modules
│   ├── resource_group/
│   ├── storage_account/
│   ├── function_app/
│   ├── key_vault/
│   └── cosmos_db/
└── environments/              # Environment-specific configurations
    └── dev/
        └── main.tfvars        # Development environment configuration
```

## Configuration Files

### 1. Root Configuration (`terraform.tfvars`)
- Contains default values for all environments
- Uses new naming standard with computed resource names
- Serves as a template for environment-specific configurations

### 2. Environment-Specific Configuration (`environments/dev/main.tfvars`)
- Overrides default values for the development environment
- Contains environment-specific settings
- Can be used with `terraform plan -var-file="environments/dev/main.tfvars"`

### 3. Example Configuration (`terraform.tfvars.example`)
- Template for creating new configurations
- Contains all available variables with examples
- Safe to commit to version control

## Naming Standard

All resources follow the Public Cloud Platform Resource Naming Standards:

```
[Resource]-[Application]-[CIPS]-[Environment]-[Region]-[Description]-[Suffix]
```

### Examples for Development Environment:
- **Resource Group**: `rg-twdemo-mcips-dev-twn-functions`
- **Function App**: `func-twdemo-mcips-dev-twn-citydata`
- **Storage Account**: `satwmcipsdevtwnfunc01`
- **Key Vault**: `kv-twdemo-mcips-dev-twn-secrets`
- **Cosmos DB**: `cosmos-twdemo-mcips-dev-twn-data`
- **SFTP Storage**: `sftp-twdemo-mcips-dev-twn-csv`

## Usage

### Development Environment
```bash
# Deploy with development configuration
terraform plan -var-file="environments/dev/main.tfvars"
terraform apply -var-file="environments/dev/main.tfvars"

# Or use the deployment script
./deploy-app.sh dev
```

### Production Environment
```bash
# Create production configuration
cp environments/dev/main.tfvars environments/prd/main.tfvars
# Edit environments/prd/main.tfvars with production values

# Deploy to production
terraform plan -var-file="environments/prd/main.tfvars"
terraform apply -var-file="environments/prd/main.tfvars"
```

## Variables

### Required Variables
- `subscription_id` - Azure subscription ID

### Environment Configuration
- `environment` - Environment (dev, tst, prd)
- `region` - Azure region abbreviation (twn, hk, sg)

### Application Configuration
- `application` - Application name (default: twdemo)
- `cips` - CIPS identifier (default: mcips)
- `description` - Resource description (default: functions)

### Application Settings
- `app_environment` - Application environment
- `api_version` - API version
- `debug_mode` - Enable debug mode
- `max_cities_count` - Maximum number of cities
- `data_limit` - Maximum number of records

### Optional Services
- `enable_key_vault` - Enable Key Vault integration
- `enable_cosmos_db` - Enable Cosmos DB integration

## Computed Resource Names

Resource names are automatically computed based on the naming standard:

```hcl
locals {
  computed_resource_group_name = "rg-${var.application}-${var.cips}-${var.environment}-${var.region}-${var.description}"
  computed_function_app_name = "func-${var.application}-${var.cips}-${var.environment}-${var.region}-citydata"
  computed_storage_account_name = "sa${substr(var.application, 0, 3)}${var.cips}${var.environment}${var.region}func01"
  # ... more computed names
}
```

## Backward Compatibility

Legacy variables are still supported for backward compatibility:

```hcl
# These will override computed names if provided
resource_group_name = "custom-rg-name"
function_app_name   = "custom-function-name"
storage_account_name = "customstorage"
```

## Validation

```bash
# Validate configuration
terraform validate

# Check computed resource names
terraform output computed_resource_names

# Plan without applying
terraform plan -var-file="environments/dev/main.tfvars"
```

## Logging and Monitoring

### Application Insights
- **Status**: ✅ Enabled
- **Connection**: Automatically configured in Function App
- **Retention**: 90 days
- **Sampling**: 100%
- **Location**: East Asia

### Function App Logging
- **Application Logs**: Information level
- **HTTP Logs**: Enabled (3 days retention)
- **Detailed Error Messages**: Enabled
- **Failed Request Tracing**: Enabled

### Service Connector (Cosmos DB)
- **Authentication**: System-assigned Managed Identity
- **Status**: ✅ Configured and working
- **Environment Variables**: 
  - `AZURE_COSMOS_RESOURCEENDPOINT`
  - `AZURE_COSMOS_LISTCONNECTIONSTRINGURL`
  - `AZURE_COSMOS_SCOPE`
- **Benefits**: No connection string secrets needed

### Log Access Commands
```bash
# View Application Insights logs
az monitor app-insights query \
  --app appins-twdemo-mcips-dev-twn-01 \
  --analytics-query "requests | limit 10"

# Stream Function App logs (real-time)
az functionapp log tail \
  --name func-twdemo-mcips-dev-twn-citydata \
  --resource-group rg-twdemo-mcips-dev-twn-functions

# Download Function App logs
az webapp log download \
  --name func-twdemo-mcips-dev-twn-citydata \
  --resource-group rg-twdemo-mcips-dev-twn-functions \
  --log-file app-logs.zip
```

## Related Documentation

- [Cursor Rules](../.cursor/rules/) - Development standards and best practices
- [Azure Naming Standards](../.cursor/rules/azure-naming-standard.mdc) - Resource naming conventions
- [Terraform Best Practices](../.cursor/rules/terraform-best-practices.mdc) - Terraform development standards 