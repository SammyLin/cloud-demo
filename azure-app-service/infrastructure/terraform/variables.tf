variable "resource_group_name" {
  description = "Resource Group 名稱"
  type        = string
}

variable "location" {
  description = "Azure 區域"
  type        = string
}

variable "app_service_plan_name" {
  description = "App Service Plan 名稱"
  type        = string
}

variable "app_service_sku" {
  description = "App Service Plan SKU"
  type        = string
  default     = "B1"
}

variable "web_app_name" {
  description = "Web App 名稱"
  type        = string
}

variable "java_version" {
  description = "Web App Java 版本 (LinuxFxVersion)"
  type        = string
  default     = "JAVA|21-java21"
}

variable "app_service_label_value" {
  description = "Web App APP_INSIDE_LABEL 內容"
  type        = string
}

variable "subscription_id" {
  description = "Azure Subscription ID"
  type        = string
}

