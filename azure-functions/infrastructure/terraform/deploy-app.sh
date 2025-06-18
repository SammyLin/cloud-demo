#!/bin/bash
set -e

# 0. Set absolute directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$SCRIPT_DIR/../../applications/cloud-demo-functions"
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
cd "$APP_DIR/target/azure-functions/cloud-demo-functions"
rm -f "$ZIP_FILE"
zip -r "$ZIP_FILE" .
echo "[INFO] Created deployment package: $ZIP_FILE"

# 2. Get Terraform outputs
if ! command -v terraform &> /dev/null; then
  echo "[ERROR] Terraform CLI not found. Please install Terraform first." >&2
  exit 1
fi

cd "$TERRAFORM_DIR"
RESOURCE_GROUP=$(terraform output -raw resource_group_name)
FUNCTION_APP_NAME=$(terraform output -raw function_app_name)
FUNCTION_APP_URL=$(terraform output -raw function_app_url)

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
echo "[INFO] Available API endpoints:"
echo ""
echo "Core APIs:"
echo "  - GET $FUNCTION_APP_URL/api/echo?name=World"
echo "  - GET $FUNCTION_APP_URL/api/config-env"
echo "  - GET $FUNCTION_APP_URL/api/config-kv"
echo "  - GET $FUNCTION_APP_URL/api/db-test"
echo ""
echo ""
echo "Logging and Monitoring:"
echo "  - Application Insights: ✅ Enabled (90 days retention)"
echo "  - Function App Logs: ✅ Enabled (Information level)"
echo "  - Service Connector: ✅ Cosmos DB with Managed Identity"
echo ""
echo "Log Access Commands:"
echo "  # Stream real-time logs"
echo "  az functionapp log tail --name $FUNCTION_APP_NAME --resource-group $RESOURCE_GROUP"
echo ""
echo "  # Download logs"
echo "  az webapp log download --name $FUNCTION_APP_NAME --resource-group $RESOURCE_GROUP --log-file app-logs.zip"
echo ""
echo "  # View Application Insights"
echo "  az monitor app-insights query --app appins-clouddemo-mcips-dev-twn-01 --analytics-query \"requests | limit 10\""