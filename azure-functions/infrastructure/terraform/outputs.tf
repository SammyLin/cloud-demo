output "resource_group_name" {
  description = "Name of the resource group"
  value       = module.resource_group.name
}

output "function_app_name" {
  description = "Name of the Function App"
  value       = module.function_app.name
}

output "function_app_url" {
  description = "Function App URL"
  value       = module.function_app.default_hostname
}

output "storage_account_name" {
  description = "Name of the storage account"
  value       = module.storage_account.name
}

output "key_vault_name" {
  description = "Name of the Key Vault"
  value       = var.enable_key_vault ? module.key_vault[0].key_vault_name : null
}

output "key_vault_uri" {
  description = "URI of the Key Vault"
  value       = var.enable_key_vault ? module.key_vault[0].key_vault_uri : null
}

output "managed_identity_id" {
  description = "ID of the managed identity"
  value       = var.enable_key_vault ? module.key_vault[0].user_assigned_identity_id : null
}