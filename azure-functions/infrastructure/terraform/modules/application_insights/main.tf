# Application Insights for Function App monitoring
resource "azurerm_application_insights" "main" {
  name                = var.name
  location            = var.location
  resource_group_name = var.resource_group_name
  application_type    = "web"

  tags = merge(var.tags, {
    Component = "monitoring"
    Purpose   = "application-insights"
  })
}