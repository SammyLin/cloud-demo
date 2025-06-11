variable "subscription_id" {
  description = "Azure subscription ID"
  type        = string
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
  default     = "taiwan-functions-rg"
}

variable "location" {
  description = "Azure region for resources"
  type        = string
  default     = "East Asia"
}

variable "function_app_name" {
  description = "Name of the Function App"
  type        = string
  default     = "taiwan-city-functions"
}

variable "storage_account_name" {
  description = "Name of the storage account"
  type        = string
  default     = "taiwanfunctionsstorage"
}

# Custom environment variables
variable "app_environment" {
  description = "Application environment"
  type        = string
  default     = "development"
}

variable "api_version" {
  description = "API version"
  type        = string
  default     = "v1"
}

variable "debug_mode" {
  description = "Enable debug mode"
  type        = string
  default     = "true"
}

variable "max_cities_count" {
  description = "Maximum number of cities to return"
  type        = string
  default     = "50"
}

# Key Vault variables (optional)
variable "enable_key_vault" {
  description = "Enable Key Vault integration"
  type        = bool
  default     = false
}

variable "key_vault_name" {
  description = "Name of the Key Vault"
  type        = string
  default     = "taiwan-func-kv-01"
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