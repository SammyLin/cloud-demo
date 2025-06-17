data "azurerm_client_config" "current" {}

resource "azurerm_key_vault" "main" {
  name                = var.name
  location            = var.location
  resource_group_name = var.resource_group_name
  enabled_for_disk_encryption = true
  tenant_id           = data.azurerm_client_config.current.tenant_id
  soft_delete_retention_days = 7
  purge_protection_enabled = false

  sku_name = "standard"

  # Allow current user to manage secrets for deployment
  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = data.azurerm_client_config.current.object_id

    secret_permissions = [
      "Get",
      "List",
      "Set",
      "Delete",
      "Purge",
      "Recover"
    ]
  }

  tags = {
    Environment = "dev"
    Project     = "Taiwan Demo Functions"
  }
}

# Access policy for System Assigned Identity (will be added after Function App is created)
resource "azurerm_key_vault_access_policy" "system_identity" {
  count = var.system_assigned_identity_principal_id != null ? 1 : 0
  
  key_vault_id = azurerm_key_vault.main.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = var.system_assigned_identity_principal_id

  secret_permissions = [
    "Get",
    "List",
  ]

  depends_on = [azurerm_key_vault.main]
}

# Sample secrets
resource "azurerm_key_vault_secret" "api_key" {
  name         = "api-key"
  value        = var.api_key_value
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_key_vault.main]
}

resource "azurerm_key_vault_secret" "database_connection" {
  name         = "database-connection"
  value        = var.database_connection_value
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [azurerm_key_vault.main]
}