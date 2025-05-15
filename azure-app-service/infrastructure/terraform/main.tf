terraform {
  required_version = ">= 1.0.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.0"
    }
  }
}

provider "azurerm" {
  features {}
  subscription_id = var.subscription_id
}

module "resource_group" {
  source = "./modules/resource_group"
  resource_group_name = var.resource_group_name
  location           = var.location
}

module "app_service_plan" {
  source = "./modules/app_service_plan"
  resource_group_name = module.resource_group.name
  location           = module.resource_group.location
  app_service_plan_name = var.app_service_plan_name
  app_service_sku    = var.app_service_sku
}

module "web_app" {
  source = "./modules/web_app"
  resource_group_name = module.resource_group.name
  location           = module.resource_group.location
  app_service_plan_id = module.app_service_plan.id
  web_app_name       = var.web_app_name
  java_version       = var.java_version
  app_service_label_value = var.app_service_label_value
}
