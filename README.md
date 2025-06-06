# Cross-Cloud Taiwan City Data Service – Infrastructure Demo

This project provides a cross-cloud (multi-cloud) Infrastructure as Code (IaC) foundation for deploying Java applications that demonstrate Taiwan city/county data. The goal is "write once, deploy anywhere"—enabling automated provisioning and deployment on different cloud providers via unified interfaces and best practices.

---

- **Initial focus:** Azure implementation and connectivity verification
- **Architecture:** Designed for future extensibility to GCP and AWS
- **Core features:** Unified resource abstraction, secret/config management, automated deployment
- **Demo apps:** Java Spring Boot (Azure App Service) and Java Azure Functions with environment variables support

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
- **Technology:** Java Azure Functions (HTTP triggers)
- **Endpoints:**
  - `/api/HttpExample` - Simple "Hello World" function
  - `/api/cities` - Taiwan cities data with environment-based configuration
  - `/api/config` - Display current environment variables
- **Infrastructure:** Pulumi and Terraform support
- **Features:** Serverless computing, environment variables support, configurable responses

## Environment Variables Support

The Azure Functions implementation includes comprehensive environment variables support:

- `APP_ENVIRONMENT` - Application environment (development/staging/production)
- `API_VERSION` - API version for client compatibility  
- `DEBUG_MODE` - Enable detailed logging when set to "true"
- `MAX_CITIES_COUNT` - Maximum number of cities to return

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
```bash
cd azure-functions/infrastructure/pulumi  
cp Pulumi.dev.yaml.example Pulumi.dev.yaml
# Edit Pulumi.dev.yaml with your settings
pulumi up
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

## Testing Endpoints

After deployment, test the applications:

**Azure App Service:**
```bash
curl "https://your-webapp.azurewebsites.net/"
```

**Azure Functions:**
```bash
curl "https://your-functions.azurewebsites.net/api/HttpExample?name=World"
curl "https://your-functions.azurewebsites.net/api/cities"
curl "https://your-functions.azurewebsites.net/api/config"
```

## Clean Up Resources

```bash
# Pulumi
pulumi destroy

# Terraform
terraform destroy -var-file=environments/dev/main.tfvars
```

---

**Next Steps:** Future plans include extending this foundation to Google Cloud Platform (GCP) and Amazon Web Services (AWS) with similar unified deployment patterns.