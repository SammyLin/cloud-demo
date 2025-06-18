output "instrumentation_key" {
  description = "Application Insights instrumentation key"
  value       = azurerm_application_insights.main.instrumentation_key
  sensitive   = true
}

output "connection_string" {
  description = "Application Insights connection string"
  value       = azurerm_application_insights.main.connection_string
  sensitive   = true
}

output "app_id" {
  description = "Application Insights application ID"
  value       = azurerm_application_insights.main.app_id
}

output "name" {
  description = "Name of the Application Insights instance"
  value       = azurerm_application_insights.main.name
}