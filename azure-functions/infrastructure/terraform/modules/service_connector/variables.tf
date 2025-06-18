variable "connection_name" {
  description = "Name of the service connection"
  type        = string
}

variable "function_app_id" {
  description = "Resource ID of the Function App"
  type        = string
}

variable "cosmos_db_id" {
  description = "Resource ID of the Cosmos DB account"
  type        = string
}

variable "key_vault_id" {
  description = "Resource ID of the Key Vault for secret storage"
  type        = string
  default     = null
}

variable "tags" {
  description = "Tags to apply to the service connection"
  type        = map(string)
  default     = {}
}