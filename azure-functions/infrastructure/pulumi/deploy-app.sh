#!/bin/bash
set -e

# 0. Set absolute directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$SCRIPT_DIR/../../applications/taiwan-city-functions"
ZIP_FILE="$APP_DIR/target/functions.zip"

# 1. Build Azure Functions project with Maven
cd "$APP_DIR"
if ! command -v mvn &> /dev/null; then
  echo "[ERROR] Maven (mvn) not found. Please install Maven first." >&2
  exit 1
fi

echo "[INFO] Building Azure Functions project..."
mvn clean package -DskipTests

# Check if the package was created successfully
if [ ! -d "target/azure-functions" ]; then
  echo "[ERROR] Azure Functions package not found after build." >&2
  exit 1
fi

# Create ZIP file from the Azure Functions package
cd target/azure-functions/taiwan-city-functions
rm -f "$ZIP_FILE"
zip -r "$ZIP_FILE" .
echo "[INFO] Created deployment package: $ZIP_FILE"

cd "$SCRIPT_DIR"

# 2. Get Pulumi outputs
if ! command -v pulumi &> /dev/null; then
  echo "[ERROR] Pulumi CLI not found. Please install Pulumi first." >&2
  exit 1
fi

RESOURCE_GROUP=$(pulumi stack output resourceGroupName)
FUNCTION_APP_NAME=$(pulumi stack output functionAppName)
FUNCTION_APP_URL=$(pulumi stack output functionAppUrl)

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

# Check if Key Vault is enabled
KEY_VAULT_ENABLED=$(pulumi stack output keyVaultName 2>/dev/null && echo "true" || echo "false")

echo ""
echo "=========================================="
echo "🎯 TEST ENDPOINTS"
echo "=========================================="
echo ""

echo "1️⃣  Basic HTTP Function:"
echo "   curl \"$FUNCTION_APP_URL/api/HttpExample?name=TypeScript\""
echo ""

echo "2️⃣  Taiwan Cities API:"
echo "   curl \"$FUNCTION_APP_URL/api/cities\""
echo ""

echo "3️⃣  Environment Configuration:"
echo "   curl \"$FUNCTION_APP_URL/api/config\""
echo ""

echo "4️⃣  Key Vault Secrets:"
echo "   curl \"$FUNCTION_APP_URL/api/secrets\""
if [ "$KEY_VAULT_ENABLED" = "true" ]; then
    echo "   (Key Vault enabled ✅)"
    
    KEYVAULT_COMMAND=$(pulumi stack output keyVaultAccessPolicyCommand 2>/dev/null || echo "")
    if [ -n "$KEYVAULT_COMMAND" ]; then
        echo ""
        echo "🔑 Key Vault Access Policy Setup:"
        echo "   $KEYVAULT_COMMAND"
    fi
else
    echo "   (Key Vault disabled - will return disabled message)"
fi
echo ""

# Check if Cosmos DB is enabled
COSMOS_ENABLED=$(pulumi stack output cosmosAccountName 2>/dev/null && echo "true" || echo "false")

echo "5️⃣  CSV Data APIs:"
echo "   curl \"$FUNCTION_APP_URL/api/data\""
echo "   curl \"$FUNCTION_APP_URL/api/query\""
echo "   curl \"$FUNCTION_APP_URL/api/query?id=001\""
echo "   curl \"$FUNCTION_APP_URL/api/query?fileName=test_data_valid\""
echo "   curl \"$FUNCTION_APP_URL/api/query?limit=10\""
if [ "$COSMOS_ENABLED" = "true" ]; then
    echo "   (Cosmos DB enabled ✅)"
else
    echo "   (Cosmos DB disabled - will return error message)"
fi
echo ""

echo "6️⃣  CSV Processing Workflow:"
if [ "$COSMOS_ENABLED" = "true" ]; then
    SFTP_STORAGE=$(pulumi stack output csvUploadContainerName 2>/dev/null || echo "csv-uploads")
    echo "   1. Upload CSV files to: $SFTP_STORAGE container"
    echo "   2. Files processed by CsvBlobProcessor function"
    echo "   3. Success → csv-success container"
    echo "   4. Failure → csv-failure container (with error logs)"
    echo "   5. Query data via /api/data and /api/query endpoints"
else
    echo "   (CSV processing disabled - Cosmos DB not enabled)"
fi
echo ""

echo "=========================================="