output "account_name" {
  description = "Name of the Cosmos DB account"
  value       = azurerm_cosmosdb_account.main.name
}

output "account_id" {
  description = "ID of the Cosmos DB account"
  value       = azurerm_cosmosdb_account.main.id
}

output "endpoint" {
  description = "Endpoint of the Cosmos DB account"
  value       = azurerm_cosmosdb_account.main.endpoint
}

output "mongodb_endpoint" {
  description = "MongoDB endpoint for Cosmos DB (for managed identity)"
  value       = "mongodb://${azurerm_cosmosdb_account.main.name}.mongo.cosmos.azure.com:10255"
}

output "database_name" {
  description = "Name of the Cosmos database"
  value       = azurerm_cosmosdb_mongo_database.main.name
}

output "collection_name" {
  description = "Name of the Cosmos collection"
  value       = azurerm_cosmosdb_mongo_collection.main.name
}