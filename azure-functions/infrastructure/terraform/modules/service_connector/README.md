# Service Connector Module

這個模組建立 Azure Service Connector 來連接 Function App 和 Cosmos DB，使用 Managed Identity 進行身份驗證。

## 功能

- 使用 System-assigned Managed Identity 進行身份驗證
- 支援 Java 客戶端類型
- 可選的 Key Vault 秘密存儲
- 自動配置連接字串和環境變數

## 使用方法

```hcl
module "service_connector" {
  source          = "./modules/service_connector"
  connection_name = "my-cosmos-connection"
  function_app_id = module.function_app.id
  cosmos_db_id    = module.cosmos_db.account_id
  key_vault_id    = module.key_vault.key_vault_id  # 可選
  
  tags = {
    Environment = "dev"
    Purpose     = "cosmos-db-connection"
  }
}
```

## 環境變數

Service Connector 會自動在 Function App 中設定以下環境變數：

- `AZURE_COSMOS_*` - Cosmos DB 連接相關變數
- `AZURE_CLIENT_ID` - Managed Identity 客戶端 ID
- 其他連接字串和配置參數

## Managed Identity 權限

Service Connector 會自動配置：
- Function App 的 System-assigned Managed Identity
- Cosmos DB 的適當 RBAC 角色
- Key Vault 存取權限（如果啟用）

## 注意事項

- 支援 Cosmos DB MongoDB API
- 使用 System-assigned Managed Identity 進行身份驗證
- 不需要手動配置連接字串或密鑰