# Cloud Demo Functions

Azure Functions application demonstrating serverless Java development with Azure cloud service integrations.

## Features

### HTTP-Triggered Functions
- **HttpExample** (`/api/HttpExample`) - Simple hello world function
- **TaiwanCities** (`/api/cities`) - Taiwan cities data with environment configuration
- **Config** (`/api/config`) - Display current environment variables  
- **KeyVaultSecrets** (`/api/secrets`) - Azure Key Vault integration demo
- **DataReader** (`/api/data`) - Read processed CSV data from Cosmos DB

### Blob-Triggered Functions
- **CsvBlobProcessor** - Automatically processes CSV files uploaded to blob storage

## Project Structure

```
src/main/java/org/cloud/demo/
├── Function.java                   # Main Functions class
├── config/ApplicationConfig.java   # Environment configuration
├── model/                          # Data models (CsvData, ProcessingResult)
├── service/                        # Business services
└── util/ResponseUtil.java          # HTTP response utilities
```

## Local Development

```bash
# Build and run locally
mvn clean package
func start
```

**Local endpoints:**
- `http://localhost:7071/api/HttpExample?name=World`
- `http://localhost:7071/api/cities`
- `http://localhost:7071/api/config`

## Configuration

Configure environment variables in `local.settings.json`:

```json
{
  "Values": {
    "APP_ENVIRONMENT": "local",
    "API_VERSION": "v1", 
    "DEBUG_MODE": "true",
    "MAX_CITIES_COUNT": "10",
    "COSMOS_DB_ENABLED": "false",
    "KEY_VAULT_ENABLED": "false"
  }
}
```

## Azure Deployment

This application is deployed automatically via the `deploy-app.sh` scripts in the infrastructure directories. See the parent [README](../README.md) for deployment instructions. 