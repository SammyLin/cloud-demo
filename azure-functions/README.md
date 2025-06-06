# Azure Functions – Infrastructure as Code

This directory provides Infrastructure as Code (IaC) examples for Azure Functions, supporting both Terraform and Pulumi workflows, with automated deployment of Java-based serverless functions.

---

## Directory Structure
```
azure-functions/
├── applications/                # Java Azure Functions application source code
│   └── taiwan-city-functions/   # Demo function app with HTTP triggers
├── infrastructure/
│   ├── pulumi/                  # Pulumi IaC and deployment scripts
│   │   └── deploy-app.sh
│   └── terraform/               # Terraform IaC and deployment scripts
│       └── deploy-app.sh
└── README.md                    # This documentation file
```

## Application Overview

The demo includes two HTTP-triggered functions:
- `HttpExample` - Simple "Hello World" function with name parameter
- `TaiwanCities` - Returns a list of Taiwan cities/counties as JSON

---

## Prerequisites

- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) installed and logged in
- [Maven](https://maven.apache.org/install.html) for building Java projects
- [Pulumi CLI](https://www.pulumi.com/docs/get-started/install/) (for Pulumi workflow)
- [Terraform CLI](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli) (for Terraform workflow)

## Terraform Workflow

1. **Enter the Terraform directory:**
   ```sh
   cd azure-functions/infrastructure/terraform
   ```

2. **Configure Azure subscription:**
   ```sh
   # Option 1: Use environment variable
   export TF_VAR_subscription_id=your-subscription-id
   
   # Option 2: Create dev.auto.tfvars file (not tracked in git)
   cp environments/dev/dev.auto.tfvars.example environments/dev/dev.auto.tfvars
   # Edit the file and add your subscription_id
   ```

3. **Initialize and deploy infrastructure:**
   ```sh
   terraform init
   terraform apply -var-file=environments/dev/main.tfvars
   ```

4. **Deploy the application:**
   ```sh
   ./deploy-app.sh
   ```

5. **Test the functions:**
   ```sh
   # Get the Function App URL from terraform output
   FUNCTION_URL=$(terraform output -raw function_app_url)
   
   # Test the endpoints
   curl "$FUNCTION_URL/api/HttpExample?name=World"
   curl "$FUNCTION_URL/api/cities"
   ```

## Pulumi Workflow

1. **Enter the Pulumi directory:**
   ```sh
   cd azure-functions/infrastructure/pulumi
   ```

2. **Configure Pulumi stack:**
   ```sh
   # Copy and edit the configuration file
   cp Pulumi.dev.yaml.example Pulumi.dev.yaml
   # Edit Pulumi.dev.yaml with your desired settings
   ```

3. **Initialize and deploy infrastructure:**
   ```sh
   pulumi login
   pulumi stack init dev  # or select existing stack
   pulumi up
   ```

4. **Deploy the application:**
   ```sh
   ./deploy-app.sh
   ```

5. **Test the functions:**
   ```sh
   # Get the Function App URL from Pulumi output
   FUNCTION_URL=$(pulumi stack output functionAppUrl)
   
   # Test the endpoints
   curl "$FUNCTION_URL/api/HttpExample?name=World"
   curl "$FUNCTION_URL/api/cities"
   ```

---

## About deploy-app.sh

Each IaC subdirectory contains a `deploy-app.sh` script that:

1. **Builds the Java project** using Maven (`mvn clean package`)
2. **Creates deployment package** from the Azure Functions build output
3. **Extracts resource information** from IaC outputs (Pulumi stack outputs or Terraform outputs)
4. **Deploys to Azure** using `az functionapp deployment source config-zip`

## Configuration Details

### Pulumi Configuration (`Pulumi.dev.yaml`)
```yaml
config:
  azure-functions-demo:resourceGroupName: taiwan-functions-rg
  azure-functions-demo:functionAppName: taiwan-city-functions
  azure-functions-demo:storageAccountName: taiwanfunctionsstorage
  azure-functions-demo:location: eastasia
```

### Terraform Configuration (`main.tfvars`)
```hcl
resource_group_name    = "taiwan-functions-rg"
location               = "East Asia"
function_app_name      = "taiwan-city-functions"
storage_account_name   = "taiwanfunctionsstorage"
```

---

## Local Development

To run the functions locally:

```sh
cd azure-functions/applications/taiwan-city-functions

# Install Azure Functions Core Tools if not already installed
# npm install -g azure-functions-core-tools@4 --unsafe-perm true

# Start local development server
mvn clean package
func start
```

Functions will be available at:
- `http://localhost:7071/api/HttpExample?name=World`
- `http://localhost:7071/api/cities`

---

## Clean Up Resources

**Terraform:**
```sh
terraform destroy -var-file=environments/dev/main.tfvars
```

**Pulumi:**
```sh
pulumi destroy
```