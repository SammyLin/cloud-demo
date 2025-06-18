# Service Connector between Function App and Cosmos DB using Managed Identity
resource "azurerm_app_service_connection" "cosmos_connection" {
  name               = var.connection_name
  app_service_id     = var.function_app_id
  target_resource_id = var.cosmos_db_id
  client_type        = "java"

  authentication {
    type = "systemAssignedIdentity"
  }

  # Optional: Use Key Vault for secret storage
  dynamic "secret_store" {
    for_each = var.key_vault_id != null ? [1] : []
    content {
      key_vault_id = var.key_vault_id
    }
  }

}