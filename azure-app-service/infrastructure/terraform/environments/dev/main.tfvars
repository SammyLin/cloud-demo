resource_group_name = "cloud-demo-dev-rg"
location           = "eastasia"
app_service_plan_name = "cloud-demo-dev-plan"
app_service_sku    = "B1"
web_app_name       = "cloud-demo-dev-web"
java_version       = "JAVA|21-java21"
app_service_label_value = "dev-label"

# 建議將 subscription_id 以環境變數 TF_VAR_subscription_id 或 dev.auto.tfvars 設定，避免敏感資訊進 git。
