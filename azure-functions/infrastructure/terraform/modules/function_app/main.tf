resource "azurerm_service_plan" "main" {
  name                = "plan-${var.name}"
  resource_group_name = var.resource_group_name
  location            = var.location
  os_type             = "Linux"
  sku_name            = "P1v2"

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}

# Add a short delay to ensure storage account is fully ready
resource "time_sleep" "wait_for_storage" {
  depends_on      = [azurerm_service_plan.main]
  create_duration = "30s"
}

resource "azurerm_linux_function_app" "main" {
  name                = var.name
  resource_group_name = var.resource_group_name
  location            = var.location
  service_plan_id     = azurerm_service_plan.main.id

  # 使用存儲帳戶的主訪問密鑰
  storage_account_name       = var.storage_account_name
  storage_account_access_key = var.storage_account_access_key

  depends_on = [time_sleep.wait_for_storage]

  # Configure identity - 總是配置 SystemAssigned
  identity {
    type = "SystemAssigned"
  }

  site_config {
    # 啟用 always_on 以支持 Blob 觸發器
    always_on = true

    application_stack {
      java_version = "17"
    }
  }

  app_settings = merge({
    # Azure Functions required settings
    "AzureWebJobsStorage"         = var.storage_connection_string
    "FUNCTIONS_EXTENSION_VERSION" = "~4"
    "FUNCTIONS_WORKER_RUNTIME"    = "java"
    "WEBSITE_RUN_FROM_PACKAGE"    = "1"
    "AzureWebJobsDisableHomepage" = "true"
    # Application Insights settings
    "APPLICATIONINSIGHTS_CONNECTION_STRING" = var.application_insights_connection_string
  }, var.custom_app_settings)

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}

# Note: Logging configuration will be set via Azure CLI post-deployment
# as Terraform AzureRM provider doesn't have direct logging configuration resources