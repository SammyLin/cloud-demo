variable "subscription_id" {
  description = "Azure subscription ID"
  type        = string
}

variable "environment" {
  description = "Environment (dev, tst, prd)"
  type        = string
  default     = "dev"
  validation {
    condition     = contains(["dev", "tst", "prd"], var.environment)
    error_message = "Environment must be one of: dev, tst, prd."
  }
}

variable "region" {
  description = "Azure region abbreviation (twn, hk, sg)"
  type        = string
  default     = "twn"
  validation {
    condition     = contains(["twn", "hk", "sg"], var.region)
    error_message = "Region must be one of: twn, hk, sg."
  }
}

variable "application" {
  description = "Application name"
  type        = string
  default     = "clouddemo"
}

variable "cips" {
  description = "CIPS identifier"
  type        = string
  default     = "mcips"
}

variable "description" {
  description = "Resource description"
  type        = string
  default     = "functions"
}

# Legacy variables for backward compatibility
variable "resource_group_name" {
  description = "Name of the resource group (legacy - will be computed from naming standard)"
  type        = string
  default     = null
}

variable "location" {
  description = "Azure region for resources (legacy - will be computed from region)"
  type        = string
  default     = null
}

variable "function_app_name" {
  description = "Name of the Function App (legacy - will be computed from naming standard)"
  type        = string
  default     = null
}

variable "storage_account_name" {
  description = "Name of the storage account (legacy - will be computed from naming standard)"
  type        = string
  default     = null
}

# Application settings
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
  description = "Name of the Key Vault (legacy - will be computed from naming standard)"
  type        = string
  default     = null
}

variable "api_key_value" {
  description = "The value for the API key secret"
  type        = string
  default     = ""
  sensitive   = true
}

variable "database_connection_value" {
  description = "The value for the database connection secret"
  type        = string
  default     = ""
  sensitive   = true
}

# Cosmos DB variables (optional)
variable "enable_cosmos_db" {
  description = "Enable Cosmos DB for CSV processing workflow"
  type        = bool
  default     = false
}

variable "cosmos_db_account_name" {
  description = "Name of the Cosmos DB account (legacy - will be computed from naming standard)"
  type        = string
  default     = null
}

variable "cosmos_database_name" {
  description = "Name of the Cosmos database"
  type        = string
  default     = "csvdata"
}

variable "cosmos_collection_name" {
  description = "Name of the Cosmos collection"
  type        = string
  default     = "records"
}

variable "data_limit" {
  description = "Maximum number of records to return from API"
  type        = string
  default     = "100"
}

# Computed values based on naming standard
locals {
  # Location mapping
  location_map = {
    twn = "East Asia"
    hk  = "East Asia"
    sg  = "Southeast Asia"
  }

  # Compute resource names based on naming standard
  computed_resource_group_name    = "rg-${var.application}-${var.cips}-${var.environment}-${var.region}-${var.description}"
  computed_function_app_name      = "func-${var.application}-${var.cips}-${var.environment}-${var.region}-citydata"
  computed_storage_account_name   = "sa${substr(var.application, 0, 3)}${var.cips}${var.environment}${var.region}func01"
  computed_key_vault_name         = "kv${substr(var.application, 0, 3)}${var.cips}${var.environment}${var.region}01"
  computed_cosmos_db_account_name = "cosmos-${var.application}-${var.cips}-${var.environment}-${var.region}-data"
  computed_sftp_storage_name      = "sftp${substr(var.application, 0, 3)}${var.cips}${var.environment}${var.region}csv01"
  computed_cosmos_database_name   = "db-${var.application}-${var.cips}-${var.environment}-${var.region}-${var.cosmos_database_name}"
  computed_cosmos_collection_name = "col-${var.application}-${var.cips}-${var.environment}-${var.region}-${var.cosmos_collection_name}"

  # Use computed values or fall back to legacy variables
  resource_group_name    = var.resource_group_name != null ? var.resource_group_name : local.computed_resource_group_name
  location               = var.location != null ? var.location : local.location_map[var.region]
  function_app_name      = var.function_app_name != null ? var.function_app_name : local.computed_function_app_name
  storage_account_name   = var.storage_account_name != null ? var.storage_account_name : local.computed_storage_account_name
  key_vault_name         = var.key_vault_name != null ? var.key_vault_name : local.computed_key_vault_name
  cosmos_db_account_name   = var.cosmos_db_account_name != null ? var.cosmos_db_account_name : local.computed_cosmos_db_account_name
  sftp_storage_name        = local.computed_sftp_storage_name
  cosmos_database_name     = local.computed_cosmos_database_name
  cosmos_collection_name   = local.computed_cosmos_collection_name
  application_insights_name = "appins-${var.application}-${var.cips}-${var.environment}-${var.region}-01"

  # Standard tags
  standard_tags = {
    Environment    = var.environment
    Project        = "Taiwan Demo Functions"
    CIPS           = var.cips
    Application    = var.application
    Region         = var.region
    Owner          = "Cloud Team"
    CostCenter     = "IT-001"
    Version        = "1.0.0"
    DeploymentDate = formatdate("YYYY-MM-DD", timestamp())
  }
}

# SSH Key for SFTP access
variable "sftp_ssh_public_key" {
  description = "SSH public key for SFTP access to storage account"
  type        = string
  default     = ""
}