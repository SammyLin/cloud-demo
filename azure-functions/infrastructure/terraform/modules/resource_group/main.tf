resource "azurerm_resource_group" "main" {
  name     = var.name
  location = var.location

  tags = {
    Environment = "dev"
    Project     = "Taiwan Functions Demo"
  }
}