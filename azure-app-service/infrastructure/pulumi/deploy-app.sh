#!/bin/bash
set -e

# 0. Set absolute directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$SCRIPT_DIR/../../applications/taiwan-city-demo"
ZIP_FILE="$APP_DIR/target/app.zip"

# 1. Build Spring Boot JAR
cd "$APP_DIR"
if ! command -v mvn &> /dev/null; then
  echo "[ERROR] Maven (mvn) not found. Please install Maven first." >&2
  exit 1
fi
mvn clean package -DskipTests
JAR_FILE=$(ls target/*.jar | head -n 1)
if [ ! -f "$JAR_FILE" ]; then
  echo "[ERROR] JAR file not found after build." >&2
  exit 1
fi
rm -f "$ZIP_FILE"
zip -j "$ZIP_FILE" "$JAR_FILE"
cd "$SCRIPT_DIR"

# 2. Get Pulumi outputs
if ! command -v pulumi &> /dev/null; then
  echo "[ERROR] Pulumi CLI not found. Please install Pulumi first." >&2
  exit 1
fi
RESOURCE_GROUP=$(pulumi stack output resourceGroupName)
WEBAPP_NAME=$(pulumi stack output webAppUrl | sed -E 's|https://([^.]+)\.azurewebsites.net.*|\1|')

# 3. Deploy to Azure Web App
if ! command -v az &> /dev/null; then
  echo "[ERROR] Azure CLI (az) not found. Please install Azure CLI first." >&2
  exit 1
fi
az webapp deploy \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEBAPP_NAME" \
  --src-path "$ZIP_FILE" \
  --type zip

APP_URL="https://${WEBAPP_NAME}.azurewebsites.net/"
echo "\n[INFO] Deployment complete! Visit: $APP_URL"
