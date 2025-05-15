#!/bin/bash
set -e

# 0. Set absolute directories
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_DIR="$SCRIPT_DIR/../../applications/taiwan-city-demo"
ZIP_FILE="$APP_DIR/target/app.zip"
TERRAFORM_DIR="$SCRIPT_DIR"

# 1. Build Spring Boot JAR
if ! command -v mvn &> /dev/null; then
  echo "[ERROR] Maven (mvn) not found. Please install Maven first." >&2
  exit 1
fi
mvn clean package -DskipTests -f "$APP_DIR/pom.xml"
JAR_FILE=$(ls "$APP_DIR/target/*.jar" | head -n 1)
if [ ! -f "$JAR_FILE" ]; then
  echo "[ERROR] JAR file not found after build." >&2
  exit 1
fi
rm -f "$ZIP_FILE"
zip -j "$ZIP_FILE" "$JAR_FILE"
unzip -l "$ZIP_FILE"

# 2. Get Terraform outputs
if ! command -v terraform &> /dev/null; then
  echo "[ERROR] Terraform CLI not found. Please install Terraform first." >&2
  exit 1
fi
RESOURCE_GROUP=$(terraform output -raw resource_group_name -chdir="$TERRAFORM_DIR")
WEBAPP_NAME=$(terraform output -raw web_app_name -chdir="$TERRAFORM_DIR" 2>/dev/null || \
  terraform output -raw web_app_url -chdir="$TERRAFORM_DIR" | sed -E 's|(https://)?([^.]+)\.azurewebsites.net.*|\2|')

# 3. Deploy to Azure Web App
if ! command -v az &> /dev/null; then
  echo "[ERROR] Azure CLI (az) not found. Please install Azure CLI first." >&2
  exit 1
fi
ls -lh "$ZIP_FILE"
az webapp deploy \
  --resource-group "$RESOURCE_GROUP" \
  --name "$WEBAPP_NAME" \
  --src-path "$ZIP_FILE" \
  --type zip

APP_URL="https://${WEBAPP_NAME}.azurewebsites.net/"
echo "\n[INFO] Deployment complete! Visit: $APP_URL"
