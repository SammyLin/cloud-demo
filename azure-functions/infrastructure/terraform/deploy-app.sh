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
echo "  - GET $FUNCTION_APP_URL/api/HttpExample?name=World"
echo "  - GET $FUNCTION_APP_URL/api/cities"
echo "  - GET $FUNCTION_APP_URL/api/config"
echo "  - GET $FUNCTION_APP_URL/api/secrets"
echo ""
echo "CSV Data APIs (if Cosmos DB enabled):"
echo "  - GET $FUNCTION_APP_URL/api/data"
echo "  - GET $FUNCTION_APP_URL/api/query"
echo "  - GET $FUNCTION_APP_URL/api/query?id=001"
echo "  - GET $FUNCTION_APP_URL/api/query?fileName=test_data_valid"
echo "  - GET $FUNCTION_APP_URL/api/query?limit=10"
echo ""
echo "CSV Processing Workflow:"
echo "  1. Upload CSV files to: $(terraform output -raw sftp_storage_account_name)/csv-uploads"
echo "  2. Files are automatically processed by CsvBlobProcessor function"
echo "  3. Success files moved to: csv-success container"
echo "  4. Failed files moved to: csv-failure container (with error logs)"
echo "  5. Processed data available via /api/data and /api/query endpoints"