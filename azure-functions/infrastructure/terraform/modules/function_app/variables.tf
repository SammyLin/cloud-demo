variable "name" {
  description = "Name of the Function App"
  type        = string
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
}

variable "location" {
  description = "Location for the Function App"
  type        = string
}

variable "storage_account_name" {
  description = "Name of the storage account"
  type        = string
}

variable "storage_connection_string" {
  description = "Storage account connection string"
  type        = string
  sensitive   = true
}

variable "storage_account_access_key" {
  description = "Storage account access key"
  type        = string
  sensitive   = true
}

variable "custom_app_settings" {
  description = "Custom application settings for the Function App"
  type        = map(string)
  default     = {}
}

variable "enable_key_vault" {
  description = "Enable Key Vault integration"
  type        = bool
  default     = false
}

variable "user_assigned_identity_id" {
  description = "The ID of the user assigned managed identity"
  type        = string
  default     = null
}

variable "key_vault_name" {
  description = "The name of the Key Vault"
  type        = string
  default     = null
}