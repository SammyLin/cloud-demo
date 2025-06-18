#!/bin/bash
set -e

# 0. Set absolute directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$SCRIPT_DIR/../../applications/cloud-demo-functions"
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
cd target/azure-functions/cloud-demo-functions
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

echo "1️⃣  Echo Function:"
echo "   curl \"$FUNCTION_APP_URL/api/echo?name=World\""
echo ""

echo "2️⃣  Environment Variables:"
echo "   curl \"$FUNCTION_APP_URL/api/config-env\""
echo ""

echo "3️⃣  Key Vault Configuration:"
echo "   curl \"$FUNCTION_APP_URL/api/config-kv\""
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

echo "4️⃣  Database Test:"
echo "   curl \"$FUNCTION_APP_URL/api/db-test\""
if [ "$COSMOS_ENABLED" = "true" ]; then
    echo "   (Cosmos DB enabled ✅)"
else
    echo "   (Cosmos DB disabled - will return error message)"
fi
echo ""

echo ""

echo "=========================================="
echo ""
echo "Logging and Monitoring:"
echo "  - Application Insights: ✅ Enabled (90 days retention)"
echo "  - Function App Logs: ✅ Enabled (Information level)"
if [ "$COSMOS_ENABLED" = "true" ]; then
    echo "  - Service Connector: ✅ Cosmos DB with Managed Identity"
else
    echo "  - Service Connector: ❌ Disabled (Cosmos DB not enabled)"
fi
echo ""
echo "Log Access Commands:"
echo "  # Stream real-time logs"
echo "  az functionapp log tail --name $FUNCTION_APP_NAME --resource-group $RESOURCE_GROUP"
echo ""
echo "  # Download logs"
echo "  az webapp log download --name $FUNCTION_APP_NAME --resource-group $RESOURCE_GROUP --log-file app-logs.zip"
echo ""
echo "  # View Application Insights"
APPINSIGHTS_NAME=$(pulumi stack output applicationInsightsName 2>/dev/null || echo "appins-not-found")
echo "  az monitor app-insights query --app $APPINSIGHTS_NAME --analytics-query \"requests | limit 10\""
echo ""
echo "=========================================="