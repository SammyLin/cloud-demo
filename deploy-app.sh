#!/bin/bash
set -e

# 1. Build Spring Boot JAR
cd applications/taiwan-city-demo
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

# 2. Package as zip for Azure deployment
cd target
ZIP_FILE="app.zip"
zip -j $ZIP_FILE *.jar
cd ../../..

# 3. Get Pulumi outputs
cd infrastructure
if ! command -v pulumi &> /dev/null; then
  echo "[ERROR] Pulumi CLI not found. Please install Pulumi first." >&2
  exit 1
fi
RESOURCE_GROUP=$(pulumi stack output resourceGroupName)
WEBAPP_NAME=$(pulumi stack output webAppUrl | sed -E 's|https://([^.]+)\.azurewebsites.net.*|\1|')
cd ..

# 4. Deploy to Azure Web App
if ! command -v az &> /dev/null; then
  echo "[ERROR] Azure CLI (az) not found. Please install Azure CLI first." >&2
  exit 1
fi
az webapp deploy \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEBAPP_NAME" \
  --src-path applications/taiwan-city-demo/target/app.zip \
  --type zip

APP_URL="https://${WEBAPP_NAME}.azurewebsites.net/"
echo "\n[INFO] Deployment complete! Visit: $APP_URL"
