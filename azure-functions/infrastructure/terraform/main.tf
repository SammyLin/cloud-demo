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
  name     = local.resource_group_name
  location = local.location
}

module "application_insights" {
  source              = "./modules/application_insights"
  name                = local.application_insights_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
  tags                = local.standard_tags
}

module "storage_account" {
  source              = "./modules/storage_account"
  name                = local.storage_account_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
}

# Create separate SFTP-enabled storage account for CSV processing
resource "azurerm_storage_account" "sftp_storage" {
  name                     = local.sftp_storage_name
  resource_group_name      = module.resource_group.name
  location                 = module.resource_group.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"

  # Enable SFTP support
  is_hns_enabled = true # Required for SFTP
  sftp_enabled   = true # Enable SFTP

  # Basic security settings
  min_tls_version                 = "TLS1_2"
  shared_access_key_enabled       = true
  public_network_access_enabled   = true
  allow_nested_items_to_be_public = true
  https_traffic_only_enabled      = false

  tags = merge(local.standard_tags, {
    Component = "sftp-storage"
    Purpose   = "CSV file processing"
  })
}

# Create SFTP user
resource "azurerm_storage_account_local_user" "sftp_user" {
  name                 = "sftp${substr(var.application, 0, 3)}${var.cips}${var.environment}${var.region}user"
  storage_account_id   = azurerm_storage_account.sftp_storage.id
  ssh_key_enabled      = true
  ssh_password_enabled = false
  home_directory       = "csv-uploads"

  ssh_authorized_key {
    key         = var.sftp_ssh_public_key
    description = "Demo SSH key for SFTP access"
  }

  permission_scope {
    permissions {
      create = true
      delete = true
      list   = true
      read   = true
      write  = true
    }
    service       = "blob"
    resource_name = "csv-uploads"
  }
}

# Create blob containers for CSV processing workflow on SFTP storage
resource "azurerm_storage_container" "csv_uploads" {
  name                  = "csv-uploads"
  storage_account_name  = azurerm_storage_account.sftp_storage.name
  container_access_type = "private"
}

resource "azurerm_storage_container" "csv_success" {
  name                  = "csv-success"
  storage_account_name  = azurerm_storage_account.sftp_storage.name
  container_access_type = "private"
}

resource "azurerm_storage_container" "csv_failure" {
  name                  = "csv-failure"
  storage_account_name  = azurerm_storage_account.sftp_storage.name
  container_access_type = "private"
}

module "cosmos_db" {
  count               = var.enable_cosmos_db ? 1 : 0
  source              = "./modules/cosmos_db"
  name                = local.cosmos_db_account_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
  database_name       = local.cosmos_database_name
  collection_name     = local.cosmos_collection_name
}

module "key_vault" {
  count                                 = var.enable_key_vault ? 1 : 0
  source                                = "./modules/key_vault"
  name                                  = local.key_vault_name
  resource_group_name                   = module.resource_group.name
  location                              = module.resource_group.location
  api_key_value                         = var.api_key_value
  database_connection_value             = var.database_connection_value
  system_assigned_identity_principal_id = null # Will be added later
}

module "function_app" {
  source                                  = "./modules/function_app"
  name                                    = local.function_app_name
  resource_group_name                     = module.resource_group.name
  location                                = module.resource_group.location
  storage_account_name                    = module.storage_account.name
  storage_connection_string               = module.storage_account.connection_string
  storage_account_access_key              = module.storage_account.primary_access_key
  enable_key_vault                        = var.enable_key_vault
  key_vault_name                          = var.enable_key_vault ? local.key_vault_name : null
  application_insights_connection_string  = module.application_insights.connection_string
  custom_app_settings = merge({
    "APP_ENVIRONMENT"   = var.app_environment
    "API_VERSION"       = var.api_version
    "DEBUG_MODE"        = var.debug_mode
    "MAX_CITIES_COUNT"  = var.max_cities_count
    "KEY_VAULT_ENABLED" = tostring(var.enable_key_vault)
    "COSMOS_DB_ENABLED" = tostring(var.enable_cosmos_db)
    # Add SFTP storage connection for blob processing
    "SFTP_STORAGE_CONNECTION" = "DefaultEndpointsProtocol=https;AccountName=${azurerm_storage_account.sftp_storage.name};AccountKey=${azurerm_storage_account.sftp_storage.primary_access_key};EndpointSuffix=core.windows.net"
    }, var.enable_key_vault ? {
    "KEY_VAULT_NAME" = local.key_vault_name
    } : {}, var.enable_cosmos_db ? {
    "COSMOS_ENDPOINT"   = module.cosmos_db[0].endpoint
    "COSMOS_DATABASE"   = module.cosmos_db[0].database_name
    "COSMOS_COLLECTION" = module.cosmos_db[0].collection_name
    "DATA_LIMIT"        = var.data_limit
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

# Service Connector between Function App and Cosmos DB using Managed Identity
module "service_connector" {
  count           = var.enable_cosmos_db ? 1 : 0
  source          = "./modules/service_connector"
  connection_name = "cosmosdb_connection"
  function_app_id = module.function_app.id
  cosmos_db_id    = module.cosmos_db[0].account_id
  key_vault_id    = var.enable_key_vault ? module.key_vault[0].key_vault_id : null

  tags = merge(local.standard_tags, {
    Component = "service-connector"
    Purpose   = "cosmos-db-connection"
  })

  depends_on = [module.function_app, module.cosmos_db]
}