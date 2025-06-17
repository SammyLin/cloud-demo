# Azure Functions 專案資源命名標準

## 目的
本文件依據「公有雲平台資源命名標準」建立Azure Functions專案資源命名規範，確保各項Azure資源命名一致性及作業執行的正確性。

## 適用範圍
本標準適用於Azure Functions專案中所有Azure資源，包括但不限於：
- Resource Groups
- Function Apps
- Storage Accounts
- Key Vaults
- Cosmos DB
- Virtual Networks
- Managed Identities
- 其他相關Azure服務

## 核心命名模式

### 基本格式
```
[資源]-[應用]-[CIPS]-[環境]-[區域]-[描述]-[後綴]
```

### 命名元素說明
- **全小寫**：除非資源類型特殊要求，否則使用全小寫字母
- **連字符**：使用連字符（-）作為分隔符
- **字元限制**：避免使用特殊字元（連字符除外）
- **長度控制**：總長度控制在63字元以內（特殊資源除外）
- **可讀性**：名稱應當易於理解和閱讀

## 專案特定命名規範

### 應用程式識別
- **應用名稱**：`taiwancity` (台灣城市資料處理應用)
- **CIPS識別**：`mcips` (Microsoft Cloud Infrastructure Platform Services)
- **環境**：`dev` (開發), `tst` (測試), `prd` (生產)
- **區域**：`twn` (台灣), `hk` (香港), `sg` (新加坡)

### Azure資源命名標準

#### 1. Resource Group
```
rg-[應用]-[CIPS]-[環境]-[區域]-[描述]
```
**範例**：
- `rg-taiwancity-mcips-dev-twn-functions`
- `rg-taiwancity-mcips-prd-twn-functions`

#### 2. Function App
```
func-[應用]-[CIPS]-[環境]-[區域]-[描述]
```
**範例**：
- `func-taiwancity-mcips-dev-twn-citydata`
- `func-taiwancity-mcips-prd-twn-citydata`

#### 3. Storage Account (24字元限制)
```
[資源][應用][CIPS][環境][區域][描述][序號]
```
**範例**：
- `satwmcipsdevtwnfunc01`
- `satwmcipsprdtwnfunc01`

#### 4. Key Vault
```
kv-[應用]-[CIPS]-[環境]-[區域]-[描述]
```
**範例**：
- `kv-taiwancity-mcips-dev-twn-secrets`
- `kv-taiwancity-mcips-prd-twn-secrets`

#### 5. Cosmos DB Account
```
cosmos-[應用]-[CIPS]-[環境]-[區域]-[描述]
```
**範例**：
- `cosmos-taiwancity-mcips-dev-twn-data`
- `cosmos-taiwancity-mcips-prd-twn-data`

#### 6. Managed Identity
```
id-[應用]-[CIPS]-[環境]-[描述]
```
**範例**：
- `id-taiwancity-mcips-dev-funcaccess`
- `id-taiwancity-mcips-prd-funcaccess`

#### 7. Virtual Network
```
vnet-[應用]-[CIPS]-[環境]-[區域]-[描述]
```
**範例**：
- `vnet-taiwancity-mcips-dev-twn-core`
- `vnet-taiwancity-mcips-prd-twn-core`

#### 8. Subnet
```
[VNet名稱]-snet-[描述]
```
**範例**：
- `vnet-taiwancity-mcips-dev-twn-core-snet-functions`
- `vnet-taiwancity-mcips-dev-twn-core-snet-database`

## 特殊資源命名

### SFTP Storage Account
由於SFTP功能需要特殊配置，建議使用：
```
sftp-[應用]-[CIPS]-[環境]-[區域]-[描述]
```
**範例**：
- `sftp-taiwancity-mcips-dev-twn-csv`
- `sftp-taiwancity-mcips-prd-twn-csv`

### Blob Containers
```
[描述]-[環境]-[序號]
```
**範例**：
- `csv-uploads-dev-01`
- `csv-success-prd-01`
- `csv-failure-prd-01`

## 環境變數命名

### 應用程式設定
```
[服務]_[設定項目]_[環境]
```
**範例**：
- `APP_ENVIRONMENT`
- `API_VERSION`
- `DEBUG_MODE`
- `COSMOS_DB_ENABLED`
- `KEY_VAULT_ENABLED`

### 連接字串
```
[服務]_CONNECTION_STRING
```
**範例**：
- `COSMOS_CONNECTION_STRING`
- `STORAGE_CONNECTION_STRING`
- `SFTP_STORAGE_CONNECTION`

## 標籤 (Tags) 標準

### 必要標籤
```json
{
  "Environment": "[dev|tst|prd]",
  "Project": "Taiwan City Functions",
  "CIPS": "mcips",
  "Application": "taiwancity",
  "Region": "[twn|hk|sg]",
  "Owner": "Cloud Team",
  "CostCenter": "IT-001"
}
```

### 可選標籤
```json
{
  "Version": "1.0.0",
  "DeploymentDate": "YYYY-MM-DD",
  "MaintenanceWindow": "Sunday 02:00-04:00",
  "BackupRetention": "30 days"
}
```

## 命名檢查清單

### 部署前檢查
- [ ] 資源名稱符合命名模式
- [ ] 長度在限制範圍內
- [ ] 使用正確的分隔符
- [ ] 避免特殊字元
- [ ] 標籤完整且正確
- [ ] 環境變數命名一致

### 特殊資源檢查
- [ ] Storage Account名稱不超過24字元
- [ ] 全域唯一資源名稱不重複
- [ ] Managed Identity名稱符合IAM標準
- [ ] VNet和Subnet命名關係清晰

## 範例配置

### Development環境完整範例
```hcl
# Resource Group
resource_group_name = "rg-taiwancity-mcips-dev-twn-functions"

# Function App
function_app_name = "func-taiwancity-mcips-dev-twn-citydata"

# Storage Account
storage_account_name = "satwmcipsdevtwnfunc01"

# Key Vault
key_vault_name = "kv-taiwancity-mcips-dev-twn-secrets"

# Cosmos DB
cosmos_db_account_name = "cosmos-taiwancity-mcips-dev-twn-data"

# SFTP Storage
sftp_storage_name = "sftp-taiwancity-mcips-dev-twn-csv"
```

### Production環境完整範例
```hcl
# Resource Group
resource_group_name = "rg-taiwancity-mcips-prd-twn-functions"

# Function App
function_app_name = "func-taiwancity-mcips-prd-twn-citydata"

# Storage Account
storage_account_name = "satwmcipsprdtwnfunc01"

# Key Vault
key_vault_name = "kv-taiwancity-mcips-prd-twn-secrets"

# Cosmos DB
cosmos_db_account_name = "cosmos-taiwancity-mcips-prd-twn-data"

# SFTP Storage
sftp_storage_name = "sftp-taiwancity-mcips-prd-twn-csv"
```

## 注意事項

1. **全域唯一性**：某些Azure資源名稱必須全域唯一，部署前請確認名稱可用性
2. **長度限制**：Storage Account限制24字元，其他資源通常限制63字元
3. **字元限制**：避免使用特殊字元，僅允許連字符作為分隔符
4. **環境區分**：確保不同環境的資源名稱有明確區分
5. **一致性**：同一專案內所有資源應遵循相同的命名模式

## 更新記錄

| 版本 | 日期 | 更新內容 | 更新者 |
|------|------|----------|--------|
| 1.0.0 | 2024-01-XX | 初始版本 | Cloud Team | 