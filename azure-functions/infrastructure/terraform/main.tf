terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~>3.0"
    }
  }
}

provider "azurerm" {
  features {}
  subscription_id = var.subscription_id
}

module "resource_group" {
  source   = "./modules/resource_group"
  name     = var.resource_group_name
  location = var.location
}

module "storage_account" {
  source              = "./modules/storage_account"
  name                = var.storage_account_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
}

module "function_app" {
  source                     = "./modules/function_app"
  name                       = var.function_app_name
  resource_group_name        = module.resource_group.name
  location                   = module.resource_group.location
  storage_account_name       = module.storage_account.name
  storage_connection_string  = module.storage_account.connection_string
}