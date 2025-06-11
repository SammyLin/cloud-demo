resource_group_name    = "taiwan-functions-rg"
location               = "East Asia"
function_app_name      = "taiwan-city-functions"
storage_account_name   = "taiwanfunctionsstorage"

# Enable Key Vault integration
enable_key_vault       = true
key_vault_name         = "taiwan-func-kv-tf"

# Custom environment variables
app_environment        = "development"
api_version           = "v1"
debug_mode            = "true"
max_cities_count      = "15"