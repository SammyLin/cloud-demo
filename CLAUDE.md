# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a cross-cloud Infrastructure as Code (IaC) demo project that provisions and deploys Java applications (Taiwan City Data Service) to Azure. The project includes both Azure App Service (Spring Boot) and Azure Functions (serverless) implementations. The architecture supports both Terraform and Pulumi workflows with automated deployment scripts.

## Key Architecture

- **Multi-Platform Support**: Azure App Service (Spring Boot) and Azure Functions (serverless) implementations
- **Multi-IaC Support**: Both Terraform and Pulumi implementations for the same infrastructure
- **Unified Deployment**: Each IaC stack has its own `deploy-app.sh` script that builds the Java application and deploys it
- **Configuration Management**: Pulumi uses YAML config files (`Pulumi.dev.yaml`) while Terraform uses `.tfvars` files
- **Environment Variables Support**: Azure Functions implementation includes comprehensive environment variables configuration
- **Resource Abstraction**: Terraform uses modular structure with separate modules for resource groups, app service plans, web apps, function apps, and key vault
- **Security**: Azure Functions implementation includes Azure Key Vault integration with managed identity for secure secret management

## Common Commands

### Java Applications (Maven)

#### Spring Boot Application
```bash
# Build the Spring Boot application
cd azure-app-service/applications/taiwan-city-demo
mvn clean package

# Run locally
mvn spring-boot:run
```

#### Azure Functions Application
```bash
# Build the Azure Functions application
cd azure-functions/applications/taiwan-city-functions
mvn clean package

# Run locally (requires Azure Functions Core Tools)
func start
```

### Pulumi Workflows

#### Azure App Service (Spring Boot)
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

#### Azure Functions (Serverless)
```bash
cd azure-functions/infrastructure/pulumi

# Initialize (first time)
pulumi login
pulumi stack init dev

# Deploy infrastructure and application
pulumi up
./deploy-app.sh

# Destroy resources
pulumi destroy
```

### Terraform Workflows

#### Azure App Service (Spring Boot)
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

#### Azure Functions (Serverless)
```bash
cd azure-functions/infrastructure/terraform

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

#### Azure App Service
- Copy `Pulumi.dev.yaml.example` to `Pulumi.dev.yaml` and customize:
  - `resourceGroupName`: Azure resource group name
  - `webAppName`: Azure Web App name
  - `appServicePlanName`: App Service Plan name
  - `javaVersion`: Java runtime version (e.g., "Java|21")
  - `appServiceSku`: SKU tier (e.g., "B1")

#### Azure Functions
- Copy `Pulumi.dev.yaml.example` to `Pulumi.dev.yaml` and customize:
  - `resourceGroupName`: Azure resource group name
  - `functionAppName`: Azure Function App name
  - `storageAccountName`: Storage account name
  - `location`: Azure region
  - `enableKeyVault`: Enable Key Vault integration (true/false, default: false)
  - `keyVaultName`: Azure Key Vault name (required if enableKeyVault is true)
  - Environment variables:
    - `appEnvironment`: Application environment (development/staging/production)
    - `apiVersion`: API version
    - `debugMode`: Enable debug logging ("true"/"false")
    - `maxCitiesCount`: Maximum cities to return

### Terraform Configuration
- Create `environments/dev/dev.auto.tfvars` (not tracked in git) with subscription_id
- Or set environment variable: `export TF_VAR_subscription_id=your-id`
- For Azure Functions, additional variables available:
  - `app_environment`, `api_version`, `debug_mode`, `max_cities_count`
  - `enable_key_vault`: Enable Key Vault integration (true/false, default: false)
  - `key_vault_name`: Azure Key Vault name (required if enable_key_vault is true)
  - `api_key_value`: Secret value for API key (sensitive, only used when enable_key_vault is true)
  - `database_connection_value`: Secret value for database connection (sensitive, only used when enable_key_vault is true)

## Prerequisites
- Azure CLI (`az`) logged in with `az login`
- Maven (`mvn`) for building Java applications
- Pulumi CLI for Pulumi workflows
- Terraform CLI for Terraform workflows
- Azure Functions Core Tools (for local Azure Functions development)

## Important Files

### Azure App Service
- `azure-app-service/applications/taiwan-city-demo/`: Spring Boot application source
- `azure-app-service/infrastructure/pulumi/main.go`: Pulumi Go infrastructure code
- `azure-app-service/infrastructure/terraform/main.tf`: Terraform infrastructure code
- `azure-app-service/infrastructure/*/deploy-app.sh`: Automated deployment scripts

### Azure Functions
- `azure-functions/applications/taiwan-city-functions/`: Java Azure Functions source
- `azure-functions/applications/taiwan-city-functions/src/main/java/org/cloud/demo/Function.java`: Functions implementation
- `azure-functions/applications/taiwan-city-functions/local.settings.json`: Local environment variables
- `azure-functions/infrastructure/pulumi/main.go`: Pulumi Go infrastructure code
- `azure-functions/infrastructure/terraform/main.tf`: Terraform infrastructure code
- `azure-functions/infrastructure/*/deploy-app.sh`: Automated deployment scripts

## Deployment Scripts Logic

### Azure App Service `deploy-app.sh` scripts:
1. Build Spring Boot JAR using Maven
2. Package JAR into ZIP file
3. Extract resource names from IaC outputs (Pulumi stack outputs or Terraform outputs)
4. Deploy ZIP to Azure Web App using `az webapp deploy`

### Azure Functions `deploy-app.sh` scripts:
1. Build Azure Functions package using Maven
2. Create ZIP deployment package from Azure Functions build output
3. Extract resource names from IaC outputs (Pulumi stack outputs or Terraform outputs)
4. Deploy ZIP to Azure Function App using `az functionapp deployment source config-zip`

## Environment Variables

The Azure Functions implementation supports environment variables for configuration:
- `APP_ENVIRONMENT`: Current environment (development/staging/production)
- `API_VERSION`: API version for client compatibility
- `DEBUG_MODE`: Enable detailed logging when set to "true"
- `MAX_CITIES_COUNT`: Maximum number of cities returned by the `/api/cities` endpoint

## Azure Key Vault Integration (Optional)

The Azure Functions implementation includes optional Azure Key Vault integration for secure secret management. This feature can be enabled or disabled based on your requirements.

### Enabling Key Vault Integration

#### Pulumi
Set `enableKeyVault: true` and provide `keyVaultName` in your `Pulumi.dev.yaml`:
```yaml
azure-functions-demo:enableKeyVault: true
azure-functions-demo:keyVaultName: taiwan-func-kv-01
```

#### Terraform
Set `enable_key_vault = true` and provide `key_vault_name` in your `dev.auto.tfvars`:
```hcl
enable_key_vault = true
key_vault_name = "taiwan-func-kv-01"
```

### Key Features
- **Optional Integration**: Key Vault can be enabled/disabled without changing application code
- **Managed Identity**: Uses user-assigned managed identity for secure authentication to Key Vault
- **Automatic Secret Resolution**: Environment variables can reference Key Vault secrets using `@Microsoft.KeyVault()` syntax
- **Direct SDK Access**: Java SDK for Azure Key Vault allows programmatic access to secrets
- **Secret Masking**: Sensitive values are masked in logs and API responses for security
- **Graceful Degradation**: Application works with or without Key Vault enabled

### How It Works

#### Method 1: Key Vault References (Recommended)
When Key Vault is enabled, environment variables in the Function App can reference Key Vault secrets:
```
API_KEY=@Microsoft.KeyVault(VaultName=your-vault;SecretName=api-key)
```
Azure automatically resolves these references using the Function App's managed identity.

#### Method 2: Direct SDK Access
Java code can directly access Key Vault using the Azure SDK:
```java
SecretClient secretClient = new SecretClientBuilder()
    .vaultUrl("https://your-vault.vault.azure.net/")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Configuration Behavior
- **When Key Vault is enabled**: Both Pulumi and Terraform create:
  - User-assigned managed identity (for direct SDK access)
  - System-assigned managed identity (for Key Vault references in app settings)
  - Key Vault with appropriate access policies for both identities
  - Sample secrets (api-key, database-connection)
  - Function App with Key Vault references and both managed identities
- **When Key Vault is disabled**: Function App is created with only system-assigned managed identity (for future use)

### Security Best Practices
- Secrets are never logged in plain text
- API responses mask sensitive values
- Access is controlled through Azure RBAC and Key Vault access policies
- **Dual Managed Identity**: System-assigned for Key Vault references, User-assigned for SDK access
- Managed identities eliminate need for stored credentials
- Application gracefully handles missing Key Vault configuration
- Tenant ID is automatically detected from current Azure context

### Important Notes
- **System-assigned managed identity** is required for `@Microsoft.KeyVault()` references in app settings
- **User-assigned managed identity** is used for direct Azure SDK access in Java code
- Both identities are automatically configured with appropriate Key Vault permissions
- Tenant ID issues are resolved by dynamic detection from Azure context

### Available Endpoints

#### Azure Functions
- `/api/HttpExample?name=World` - Simple hello world function
- `/api/cities` - Taiwan cities list with environment-based configuration
- `/api/config` - Display current environment variables
- `/api/secrets` - Demonstrate Key Vault integration (shows masked secret values or disabled message)

#### Azure App Service
- `/` - Spring Boot application endpoints

## Git Commit Guidelines

- All commit messages must be in English
- Follow conventional commit format when possible

## Testing Commands

After deployment, test the applications:

### Azure App Service
```bash
WEBAPP_URL=$(pulumi stack output webAppUrl)  # or terraform output
curl "$WEBAPP_URL/"
```

### Azure Functions
```bash
FUNCTION_URL=$(pulumi stack output functionAppUrl)  # or terraform output
curl "$FUNCTION_URL/api/HttpExample?name=World"
curl "$FUNCTION_URL/api/cities"
curl "$FUNCTION_URL/api/config"
curl "$FUNCTION_URL/api/secrets"
```