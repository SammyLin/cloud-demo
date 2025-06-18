output "connection_id" {
  description = "ID of the service connection"
  value       = azurerm_app_service_connection.cosmos_connection.id
}

output "connection_name" {
  description = "Name of the service connection"
  value       = azurerm_app_service_connection.cosmos_connection.name
}