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