# Create Cosmos DB Account with MongoDB API
resource "azurerm_cosmosdb_account" "main" {
  name                = var.name
  location            = var.location
  resource_group_name = var.resource_group_name
  offer_type          = "Standard"
  kind                = "MongoDB"

  consistency_policy {
    consistency_level = "Session"
  }

  geo_location {
    location          = var.location
    failover_priority = 0
  }

  capabilities {
    name = "EnableMongo"
  }

  capabilities {
    name = "MongoDBv3.4"
  }

  capabilities {
    name = "mongoEnableDocLevelTTL"
  }

  tags = {
    Environment = "dev"
    Purpose     = "CSV Processing"
  }
}


# Create Cosmos DB Database
resource "azurerm_cosmosdb_mongo_database" "main" {
  name                = var.database_name
  resource_group_name = var.resource_group_name
  account_name        = azurerm_cosmosdb_account.main.name
}

# Create Cosmos DB Collection
resource "azurerm_cosmosdb_mongo_collection" "main" {
  name                = var.collection_name
  resource_group_name = var.resource_group_name
  account_name        = azurerm_cosmosdb_account.main.name
  database_name       = azurerm_cosmosdb_mongo_database.main.name

  # Define index for better query performance
  index {
    keys   = ["_id"]
    unique = true
  }

  index {
    keys   = ["id"]
    unique = true
  }

  index {
    keys = ["updatedAt"]
  }
}