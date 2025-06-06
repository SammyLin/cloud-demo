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