# Terraform Infrastructure (Azure)

本目錄依照 [Terraform Best Practices](https://www.terraform-best-practices.com/) 設計，負責 cloud-demo Azure 資源的自動化部署。

## 架構

```
terraform/
├── environments/
│   └── dev/
│       └── main.tfvars
├── modules/
│   ├── resource_group/
│   ├── app_service_plan/
│   └── web_app/
├── main.tf
├── variables.tf
├── outputs.tf
└── README.md
```

## 使用方式

### 建議設定 Azure 訂閱 ID 的方法

**不要直接把 subscription_id 寫進 main.tfvars！**

推薦兩種安全方法：
1. 用環境變數：
   ```sh
   export TF_VAR_subscription_id=你的ID
   terraform apply -var-file=environments/dev/main.tfvars
   ```
2. 或建立 `dev.auto.tfvars`（不進 git）：
   ```hcl
   subscription_id = "你的ID"
   ```

---

```sh
cd infrastructure/terraform
terraform init
terraform plan -var-file=environments/dev/main.tfvars
terraform apply -var-file=environments/dev/main.tfvars
```

## 說明
- modules 包含三個子模組，分別建立 Resource Group、App Service Plan、Web App
- environments/dev/main.tfvars 控制 dev 環境參數
- 你可依需求擴充更多模組
