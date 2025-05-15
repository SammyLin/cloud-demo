resource "azurerm_linux_web_app" "this" {
  name                = var.web_app_name
  location            = var.location
  resource_group_name = var.resource_group_name
  service_plan_id     = var.app_service_plan_id

  site_config {
    application_stack {
      java_server         = "JAVA"
      java_server_version = "21"
      java_version        = "21"
    }
  }

  app_settings = {
    APP_INSIDE_LABEL = var.app_service_label_value
  }
}

output "url" {
  value = azurerm_linux_web_app.this.default_hostname
}
