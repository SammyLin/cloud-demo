# Azure Functions 簡化架構圖

## 核心架構

```mermaid
graph LR
    %% 用戶層
    User[👤 用戶] --> API[🌐 API Gateway]
    
    %% Azure Functions 層
    API --> Functions[🚀 Azure Functions]
    
    %% 函數層
    Functions --> HttpAPI[📡 HTTP APIs]
    Functions --> BlobAPI[📁 Blob Trigger]
    
    %% 儲存層
    BlobAPI --> Storage[💾 Azure Storage]
    Storage --> Containers[📦 Blob Containers]
    
    %% 資料層
    BlobAPI --> Database[(🗄️ Cosmos DB)]
    HttpAPI --> Database
    
    %% 安全層
    Functions --> Security[🔐 Key Vault]
    
    %% 部署層
    subgraph "IaC"
        Terraform[🔧 Terraform]
        Pulumi[⚡ Pulumi]
    end
    
    Terraform --> Functions
    Pulumi --> Functions
    
    %% 樣式
    classDef userLayer fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef functionLayer fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef storageLayer fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef dataLayer fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef securityLayer fill:#ffebee,stroke:#c62828,stroke-width:2px
    classDef deployLayer fill:#fff8e1,stroke:#f57f17,stroke-width:2px
    
    class User,API userLayer
    class Functions,HttpAPI,BlobAPI functionLayer
    class Storage,Containers storageLayer
    class Database dataLayer
    class Security securityLayer
    class Terraform,Pulumi deployLayer
```

## 組件說明

### 🚀 Azure Functions
- **HttpExample**: Hello World API
- **TaiwanCities**: 台灣城市列表 API  
- **Config**: 配置查詢 API
- **CsvBlobProcessor**: CSV 檔案處理器

### 💾 Azure Storage
- **csv-uploads**: 接收上傳檔案
- **csv-success**: 成功處理檔案
- **csv-failure**: 失敗處理檔案

### 🗄️ Cosmos DB (可選)
- 儲存 CSV 處理後的資料
- 支援結構化查詢

### 🔐 Key Vault (可選)
- 安全儲存密鑰和連接字串
- 透過 Managed Identity 存取

### 🔧 Infrastructure as Code
- **Terraform**: 使用 HCL 語法
- **Pulumi**: 使用 TypeScript/JavaScript

## 資料流程

```
1. 用戶請求 → Azure Functions → 回應
2. CSV 上傳 → Blob Storage → 觸發處理 → Cosmos DB
3. 密鑰存取 → Managed Identity → Key Vault
``` 