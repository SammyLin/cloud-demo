output "resource_group_name" {
  description = "Name of the resource group"
  value       = module.resource_group.name
}

output "resource_group_location" {
  description = "Location of the resource group"
  value       = module.resource_group.location
}

output "function_app_name" {
  description = "Name of the Function App"
  value       = module.function_app.name
}

output "function_app_url" {
  description = "URL of the Function App"
  value       = "https://${module.function_app.default_hostname}"
}

output "function_app_default_hostname" {
  description = "Default hostname of the Function App"
  value       = module.function_app.default_hostname
}

output "function_app_system_assigned_identity_principal_id" {
  description = "System assigned identity principal ID of the Function App"
  value       = module.function_app.system_assigned_identity_principal_id
}

output "storage_account_name" {
  description = "Name of the storage account"
  value       = module.storage_account.name
}

output "storage_account_primary_access_key" {
  description = "Primary access key for the storage account"
  value       = module.storage_account.primary_access_key
  sensitive   = true
}

output "storage_account_connection_string" {
  description = "Connection string for the storage account"
  value       = module.storage_account.connection_string
  sensitive   = true
}

output "key_vault_name" {
  description = "The name of the Key Vault"
  value       = var.enable_key_vault ? local.key_vault_name : null
}

output "key_vault_id" {
  description = "The ID of the Key Vault"
  value       = var.enable_key_vault ? module.key_vault[0].key_vault_id : null
}

output "key_vault_uri" {
  description = "The URI of the Key Vault"
  value       = var.enable_key_vault ? module.key_vault[0].key_vault_uri : null
}

output "cosmos_db_account_name" {
  description = "Name of the Cosmos DB account"
  value       = var.enable_cosmos_db ? module.cosmos_db[0].account_name : null
}

output "cosmos_db_endpoint" {
  description = "Endpoint of the Cosmos DB account"
  value       = var.enable_cosmos_db ? module.cosmos_db[0].endpoint : null
}

output "cosmos_db_database_name" {
  description = "Name of the Cosmos database"
  value       = var.enable_cosmos_db ? module.cosmos_db[0].database_name : null
}

output "cosmos_db_collection_name" {
  description = "Name of the Cosmos collection"
  value       = var.enable_cosmos_db ? module.cosmos_db[0].collection_name : null
}

output "service_connector_name" {
  description = "Name of the Service Connector"
  value       = var.enable_cosmos_db ? module.service_connector[0].connection_name : null
}

output "service_connector_id" {
  description = "ID of the Service Connector"
  value       = var.enable_cosmos_db ? module.service_connector[0].connection_id : null
}

output "csv_upload_container_name" {
  description = "Name of the CSV upload container"
  value       = azurerm_storage_container.csv_uploads.name
}

output "csv_success_container_name" {
  description = "Name of the CSV success container"
  value       = azurerm_storage_container.csv_success.name
}

output "csv_failure_container_name" {
  description = "Name of the CSV failure container"
  value       = azurerm_storage_container.csv_failure.name
}

output "sftp_storage_account_name" {
  description = "Name of the SFTP storage account"
  value       = azurerm_storage_account.sftp_storage.name
}

output "sftp_storage_account_primary_access_key" {
  description = "Primary access key for the SFTP storage account"
  value       = azurerm_storage_account.sftp_storage.primary_access_key
  sensitive   = true
}

output "sftp_storage_account_connection_string" {
  description = "Connection string for the SFTP storage account"
  value       = "DefaultEndpointsProtocol=https;AccountName=${azurerm_storage_account.sftp_storage.name};AccountKey=${azurerm_storage_account.sftp_storage.primary_access_key};EndpointSuffix=core.windows.net"
  sensitive   = true
}

output "sftp_endpoint" {
  description = "SFTP endpoint"
  value       = "${azurerm_storage_account.sftp_storage.name}.dfs.core.windows.net"
}

output "sftp_user_name" {
  description = "SFTP user name"
  value       = azurerm_storage_account_local_user.sftp_user.name
}

output "computed_resource_names" {
  description = "Computed resource names based on naming standard"
  value = {
    resource_group_name    = local.resource_group_name
    function_app_name      = local.function_app_name
    storage_account_name   = local.storage_account_name
    key_vault_name         = local.key_vault_name
    cosmos_db_account_name = local.cosmos_db_account_name
    sftp_storage_name      = local.sftp_storage_name
    location               = local.location
    environment            = var.environment
    region                 = var.region
    application            = var.application
    cips                   = var.cips
  }
}

output "standard_tags" {
  description = "Standard tags applied to resources"
  value       = local.standard_tags
}

output "container_names" {
  description = "Names of the blob containers"
  value = {
    csv_uploads = azurerm_storage_container.csv_uploads.name
    csv_success = azurerm_storage_container.csv_success.name
    csv_failure = azurerm_storage_container.csv_failure.name
  }
}