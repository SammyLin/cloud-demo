# Azure Functions 專案架構圖

## 系統概覽

這是一個基於 Azure Functions 的無伺服器應用程式，提供台灣城市資料 API 和 CSV 檔案處理功能。系統支援多種部署方式（Terraform 和 Pulumi）並整合了多個 Azure 服務。

## 架構圖

```mermaid
graph TB
    %% 外部用戶
    User[👤 外部用戶] --> API[🌐 HTTP API]
    
    %% Azure Functions
    API --> FunctionApp[🚀 Azure Function App<br/>taiwan-city-functions]
    
    %% Function App 內部組件
    FunctionApp --> HttpExample[📡 HttpExample<br/>Hello World API]
    FunctionApp --> TaiwanCities[🏙️ TaiwanCities<br/>台灣城市列表 API]
    FunctionApp --> Config[⚙️ Config<br/>配置查詢 API]
    FunctionApp --> KeyVaultSecrets[🔐 KeyVaultSecrets<br/>密鑰管理 API]
    FunctionApp --> DataReader[📊 DataReader<br/>資料讀取 API]
    
    %% Blob 觸發器
    FunctionApp --> CsvBlobProcessor[📁 CsvBlobProcessor<br/>CSV 檔案處理]
    
    %% 儲存服務
    CsvBlobProcessor --> StorageAccount[💾 Azure Storage Account<br/>taiwanfunctionsstorage]
    
    %% Blob 容器
    StorageAccount --> CsvUploads[📤 csv-uploads<br/>上傳容器]
    StorageAccount --> CsvSuccess[✅ csv-success<br/>成功處理容器]
    StorageAccount --> CsvFailure[❌ csv-failure<br/>失敗處理容器]
    
    %% 資料庫服務
    CsvBlobProcessor --> CosmosDB{🔷 Cosmos DB<br/>taiwan-func-cosmos<br/>csvdata/records}
    DataReader --> CosmosDB
    
    %% 密鑰管理
    KeyVaultSecrets --> KeyVault[🗝️ Azure Key Vault<br/>taiwan-func-kv-01]
    
    %% 身份管理
    FunctionApp --> ManagedIdentity[🆔 Managed Identity<br/>系統分配身份]
    ManagedIdentity --> KeyVault
    
    %% 基礎設施管理
    subgraph "Infrastructure as Code"
        Terraform[🔧 Terraform]
        Pulumi[⚡ Pulumi]
    end
    
    Terraform --> FunctionApp
    Pulumi --> FunctionApp
    
    %% 部署流程
    subgraph "部署流程"
        Build[🔨 Maven Build]
        Package[📦 打包部署]
        Deploy[🚀 部署到 Azure]
    end
    
    Build --> Package
    Package --> Deploy
    Deploy --> FunctionApp
    
    %% 樣式設定
    classDef azureService fill:#0078d4,stroke:#005a9e,stroke-width:2px,color:#fff
    classDef function fill:#ff6b35,stroke:#d14500,stroke-width:2px,color:#fff
    classDef storage fill:#00a651,stroke:#007c3a,stroke-width:2px,color:#fff
    classDef security fill:#d13438,stroke:#a4262c,stroke-width:2px,color:#fff
    classDef database fill:#68217a,stroke:#4c1b5a,stroke-width:2px,color:#fff
    classDef iac fill:#ff8c00,stroke:#cc7000,stroke-width:2px,color:#fff
    classDef process fill:#107c10,stroke:#0b5a0b,stroke-width:2px,color:#fff
    
    class FunctionApp,HttpExample,TaiwanCities,Config,KeyVaultSecrets,DataReader,CsvBlobProcessor azureService
    class StorageAccount,CsvUploads,CsvSuccess,CsvFailure storage
    class KeyVault,ManagedIdentity security
    class CosmosDB database
    class Terraform,Pulumi iac
    class Build,Package,Deploy process
```

## 詳細架構說明

### 1. 核心組件

#### Azure Function App
- **名稱**: `taiwan-city-functions`
- **運行時**: Java 17
- **計劃類型**: Consumption (Y1)
- **位置**: East Asia

#### HTTP 觸發函數
1. **HttpExample** - 簡單的 Hello World API
2. **TaiwanCities** - 返回台灣城市列表的 JSON API
3. **Config** - 顯示當前環境變數和配置設定
4. **KeyVaultSecrets** - 從 Key Vault 讀取密鑰
5. **DataReader** - 從 Cosmos DB 讀取資料

#### Blob 觸發函數
- **CsvBlobProcessor** - 處理上傳到 `csv-uploads` 容器的 CSV 檔案

### 2. 儲存服務

#### Azure Storage Account
- **名稱**: `taiwanfunctionsstorage`
- **類型**: Standard LRS
- **用途**: Function App 運行時儲存和 CSV 檔案處理

#### Blob 容器
- **csv-uploads** - 接收上傳的 CSV 檔案
- **csv-success** - 成功處理的檔案
- **csv-failure** - 處理失敗的檔案

### 3. 資料庫服務

#### Cosmos DB (可選)
- **帳戶名稱**: `taiwan-func-cosmos`
- **資料庫**: `csvdata`
- **集合**: `records`
- **用途**: 儲存 CSV 處理後的資料

### 4. 安全服務

#### Azure Key Vault (可選)
- **名稱**: `taiwan-func-kv-01`
- **用途**: 儲存 API 金鑰和資料庫連接字串
- **整合**: 透過 Managed Identity 存取

### 5. 基礎設施管理

#### Terraform 模組
```
modules/
├── resource_group/      # 資源組管理
├── storage_account/     # 儲存帳戶
├── function_app/        # Function App
├── cosmos_db/          # Cosmos DB (可選)
└── key_vault/          # Key Vault (可選)
```

#### Pulumi 支援
- 提供相同的基礎設施部署能力
- 使用 TypeScript/JavaScript 編寫

### 6. 部署流程

1. **基礎設施部署**
   ```bash
   terraform init
   terraform apply
   ```

2. **應用程式部署**
   ```bash
   mvn clean package
   ./deploy-app.sh
   ```

### 7. 環境變數

| 變數名稱 | 描述 | 預設值 |
|---------|------|--------|
| `APP_ENVIRONMENT` | 應用程式環境 | development |
| `API_VERSION` | API 版本 | v1 |
| `DEBUG_MODE` | 除錯模式 | true |
| `MAX_CITIES_COUNT` | 最大城市數量 | 50 |
| `KEY_VAULT_ENABLED` | 啟用 Key Vault | false |
| `COSMOS_DB_ENABLED` | 啟用 Cosmos DB | false |

### 8. API 端點

| 端點 | 方法 | 描述 |
|------|------|------|
| `/api/HttpExample` | GET/POST | Hello World API |
| `/api/cities` | GET | 台灣城市列表 |
| `/api/config` | GET | 配置查詢 |
| `/api/secrets` | GET | Key Vault 密鑰 |
| `/api/data` | GET | Cosmos DB 資料 |

### 9. 資料流程

1. **CSV 處理流程**:
   ```
   上傳 CSV → csv-uploads → CsvBlobProcessor → 驗證/處理 → 
   Cosmos DB 儲存 → 移動到 csv-success/csv-failure
   ```

2. **API 請求流程**:
   ```
   用戶請求 → Azure Functions → 處理邏輯 → 回應
   ```

3. **密鑰管理流程**:
   ```
   Function App → Managed Identity → Key Vault → 密鑰存取
   ```

## 擴展性考量

- **水平擴展**: Azure Functions 自動擴展
- **垂直擴展**: 可升級到 Premium 計劃
- **多區域**: 可部署到多個 Azure 區域
- **監控**: 整合 Azure Monitor 和 Application Insights
- **安全性**: 支援 VNet 整合和私有端點 