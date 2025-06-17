resource "azurerm_storage_account" "main" {
  name                     = var.name
  resource_group_name      = var.resource_group_name
  location                 = var.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
  
  # Required for Azure Functions
  min_tls_version                   = "TLS1_2"
  shared_access_key_enabled         = true
  public_network_access_enabled     = true
  allow_nested_items_to_be_public   = true
  cross_tenant_replication_enabled  = true
  https_traffic_only_enabled        = false  # Allow HTTP for Functions
  
  # Enable SFTP support
  is_hns_enabled    = true   # Hierarchical namespace required for SFTP
  sftp_enabled      = true   # Enable SFTP
  
  # Enable blob access for Functions
  blob_properties {
    versioning_enabled = false
  }

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}

# 手動創建文件共享，以避免權限問題
resource "azurerm_storage_share" "function_content" {
  name                 = "share-${var.name}-content"
  storage_account_name = azurerm_storage_account.main.name
  quota                = 5120  # 5GB
  
  depends_on = [azurerm_storage_account.main]
}

