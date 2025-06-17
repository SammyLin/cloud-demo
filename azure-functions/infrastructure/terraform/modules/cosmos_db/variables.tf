variable "name" {
  description = "Name of the Cosmos DB account"
  type        = string
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
}

variable "location" {
  description = "Azure region for the Cosmos DB account"
  type        = string
}

variable "database_name" {
  description = "Name of the Cosmos database"
  type        = string
}

variable "collection_name" {
  description = "Name of the Cosmos collection"
  type        = string
}

