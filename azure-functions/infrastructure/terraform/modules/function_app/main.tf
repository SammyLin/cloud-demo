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

# Add a short delay to ensure storage account is fully ready
resource "time_sleep" "wait_for_storage" {
  depends_on = [azurerm_service_plan.main]
  create_duration = "30s"
}

resource "azurerm_linux_function_app" "main" {
  name                = var.name
  resource_group_name = var.resource_group_name
  location            = var.location

  storage_account_name          = var.storage_account_name
  storage_uses_managed_identity = false
  service_plan_id               = azurerm_service_plan.main.id
  
  depends_on = [time_sleep.wait_for_storage]

  # Configure identity based on Key Vault enablement
  dynamic "identity" {
    for_each = var.enable_key_vault && var.user_assigned_identity_id != null ? [1] : [1]
    content {
      type         = var.enable_key_vault && var.user_assigned_identity_id != null ? "SystemAssigned, UserAssigned" : "SystemAssigned"
      identity_ids = var.enable_key_vault && var.user_assigned_identity_id != null ? [var.user_assigned_identity_id] : null
    }
  }

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
  }, var.enable_key_vault && var.key_vault_name != null ? {
    # Key Vault references (only when enabled)
    "API_KEY"                     = "@Microsoft.KeyVault(VaultName=${var.key_vault_name};SecretName=api-key)"
    "DATABASE_CONNECTION"         = "@Microsoft.KeyVault(VaultName=${var.key_vault_name};SecretName=database-connection)"
  } : {}, var.custom_app_settings)

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}