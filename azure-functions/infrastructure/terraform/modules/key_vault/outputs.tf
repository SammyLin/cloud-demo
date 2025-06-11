output "key_vault_id" {
  description = "The ID of the Key Vault"
  value       = azurerm_key_vault.main.id
}

output "key_vault_name" {
  description = "The name of the Key Vault"
  value       = azurerm_key_vault.main.name
}

output "key_vault_uri" {
  description = "The URI of the Key Vault"
  value       = azurerm_key_vault.main.vault_uri
}

output "user_assigned_identity_id" {
  description = "The ID of the user assigned managed identity"
  value       = azurerm_user_assigned_identity.main.id
}

output "user_assigned_identity_principal_id" {
  description = "The principal ID of the user assigned managed identity"
  value       = azurerm_user_assigned_identity.main.principal_id
}

output "user_assigned_identity_client_id" {
  description = "The client ID of the user assigned managed identity"
  value       = azurerm_user_assigned_identity.main.client_id
}

output "key_vault_tenant_id" {
  description = "The tenant ID of the Key Vault"
  value       = azurerm_key_vault.main.tenant_id
}