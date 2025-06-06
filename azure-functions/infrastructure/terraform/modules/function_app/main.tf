resource "azurerm_service_plan" "main" {
  name                = "${var.name}-plan"
  resource_group_name = var.resource_group_name
  location            = var.location
  os_type             = "Linux"
  sku_name            = "Y1"

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}

resource "azurerm_linux_function_app" "main" {
  name                = var.name
  resource_group_name = var.resource_group_name
  location            = var.location

  storage_account_name          = var.storage_account_name
  storage_uses_managed_identity = false
  service_plan_id            = azurerm_service_plan.main.id

  site_config {
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
  }, var.custom_app_settings)

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}