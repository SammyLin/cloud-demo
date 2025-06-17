output "name" {
  description = "Name of the Function App"
  value       = azurerm_linux_function_app.main.name
}

output "default_hostname" {
  description = "Default hostname of the Function App"
  value       = azurerm_linux_function_app.main.default_hostname
}

output "id" {
  description = "ID of the Function App"
  value       = azurerm_linux_function_app.main.id
}

output "system_assigned_identity_principal_id" {
  description = "Principal ID of the system assigned managed identity"
  value       = try(azurerm_linux_function_app.main.identity[0].principal_id, null)
}