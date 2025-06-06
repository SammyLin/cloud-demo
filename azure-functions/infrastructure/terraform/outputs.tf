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