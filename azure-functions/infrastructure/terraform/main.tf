terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~>3.0"
    }
    time = {
      source  = "hashicorp/time"
      version = "~>0.9"
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

module "key_vault" {
  count                                 = var.enable_key_vault ? 1 : 0
  source                                = "./modules/key_vault"
  name                                  = var.key_vault_name
  resource_group_name                   = module.resource_group.name
  location                              = module.resource_group.location
  api_key_value                         = var.api_key_value
  database_connection_value             = var.database_connection_value
  system_assigned_identity_principal_id = null # Will be added later
}

module "function_app" {
  source                        = "./modules/function_app"
  name                          = var.function_app_name
  resource_group_name           = module.resource_group.name
  location                      = module.resource_group.location
  storage_account_name          = module.storage_account.name
  storage_connection_string     = module.storage_account.connection_string
  enable_key_vault              = var.enable_key_vault
  user_assigned_identity_id     = var.enable_key_vault ? module.key_vault[0].user_assigned_identity_id : null
  key_vault_name                = var.enable_key_vault ? module.key_vault[0].key_vault_name : null
  custom_app_settings           = merge({
    "APP_ENVIRONMENT"   = var.app_environment
    "API_VERSION"       = var.api_version
    "DEBUG_MODE"        = var.debug_mode
    "MAX_CITIES_COUNT"  = var.max_cities_count
    "KEY_VAULT_ENABLED" = tostring(var.enable_key_vault)
  }, var.enable_key_vault ? {
    "KEY_VAULT_NAME"    = var.key_vault_name
  } : {})

  depends_on = [module.key_vault]
}

# Add System Assigned Identity access policy after Function App is created
resource "azurerm_key_vault_access_policy" "function_app_system_identity" {
  count = var.enable_key_vault ? 1 : 0
  
  key_vault_id = module.key_vault[0].key_vault_id
  tenant_id    = module.key_vault[0].key_vault_tenant_id
  object_id    = module.function_app.system_assigned_identity_principal_id

  secret_permissions = [
    "Get",
    "List",
  ]

  depends_on = [module.function_app, module.key_vault]
}