output "name" {
  description = "Name of the Function App"
  value       = azurerm_linux_function_app.main.name
}

output "default_hostname" {
  description = "Default hostname of the Function App"
  value       = "https://${azurerm_linux_function_app.main.default_hostname}"
}

output "id" {
  description = "ID of the Function App"
  value       = azurerm_linux_function_app.main.id
}