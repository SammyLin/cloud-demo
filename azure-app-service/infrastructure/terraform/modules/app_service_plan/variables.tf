variable "resource_group_name" {
  type = string
}
variable "location" {
  type = string
}
variable "app_service_plan_name" {
  type = string
}
variable "app_service_sku" {
  type = string
  default = "B1"
}
