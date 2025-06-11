variable "name" {
  description = "The name of the Key Vault"
  type        = string
}

variable "location" {
  description = "The Azure location where the Key Vault should be created"
  type        = string
}

variable "resource_group_name" {
  description = "The name of the resource group in which to create the Key Vault"
  type        = string
}

variable "api_key_value" {
  description = "The value for the API key secret"
  type        = string
  default     = "your-secret-api-key-value"
  sensitive   = true
}

variable "database_connection_value" {
  description = "The value for the database connection secret"
  type        = string
  default     = "server=example.com;database=mydb;user=myuser;password=secretpassword"
  sensitive   = true
}

variable "system_assigned_identity_principal_id" {
  description = "The principal ID of the system assigned managed identity"
  type        = string
  default     = null
}