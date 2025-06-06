#!/bin/bash
set -e

# 0. Set absolute directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$SCRIPT_DIR/../../applications/taiwan-city-functions"
ZIP_FILE="$APP_DIR/target/functions.zip"
TERRAFORM_DIR="$SCRIPT_DIR"

# 1. Build Azure Functions project with Maven
if ! command -v mvn &> /dev/null; then
  echo "[ERROR] Maven (mvn) not found. Please install Maven first." >&2
  exit 1
fi

echo "[INFO] Building Azure Functions project..."
mvn clean package -DskipTests -f "$APP_DIR/pom.xml"

# Check if the package was created successfully
if [ ! -d "$APP_DIR/target/azure-functions" ]; then
  echo "[ERROR] Azure Functions package not found after build." >&2
  exit 1
fi

# Create ZIP file from the Azure Functions package
cd "$APP_DIR/target/azure-functions/taiwan-city-functions"
rm -f "$ZIP_FILE"
zip -r "$ZIP_FILE" .
echo "[INFO] Created deployment package: $ZIP_FILE"

# 2. Get Terraform outputs
if ! command -v terraform &> /dev/null; then
  echo "[ERROR] Terraform CLI not found. Please install Terraform first." >&2
  exit 1
fi

RESOURCE_GROUP=$(terraform output -raw resource_group_name -chdir="$TERRAFORM_DIR")
FUNCTION_APP_NAME=$(terraform output -raw function_app_name -chdir="$TERRAFORM_DIR")
FUNCTION_APP_URL=$(terraform output -raw function_app_url -chdir="$TERRAFORM_DIR")

echo "[INFO] Resource Group: $RESOURCE_GROUP"
echo "[INFO] Function App Name: $FUNCTION_APP_NAME"

# 3. Deploy to Azure Function App
if ! command -v az &> /dev/null; then
  echo "[ERROR] Azure CLI (az) not found. Please install Azure CLI first." >&2
  exit 1
fi

echo "[INFO] Deploying to Azure Function App..."
az functionapp deployment source config-zip \
  --resource-group "$RESOURCE_GROUP" \
  --name "$FUNCTION_APP_NAME" \
  --src "$ZIP_FILE"

echo ""
echo "[INFO] Deployment complete!"
echo "[INFO] Function App URL: $FUNCTION_APP_URL"
echo "[INFO] Test endpoints:"
echo "  - GET $FUNCTION_APP_URL/api/HttpExample?name=World"
echo "  - GET $FUNCTION_APP_URL/api/cities"