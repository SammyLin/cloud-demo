package org.cloud.demo.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.cloud.demo.config.ApplicationConfig;
import org.cloud.demo.model.CsvData;
import org.cloud.demo.service.KeyVaultService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database Service Class
 * Responsible for interactions with Cosmos DB
 */
public class DatabaseService {
    
    private final ApplicationConfig config;
    
    public DatabaseService(ApplicationConfig config) {
        this.config = config;
    }
    
    /**
     * Create MongoDB client (using Key Vault + primary key)
     */
    private MongoClient createMongoClientWithPrimaryKey(ExecutionContext context) {
        try {
            Optional<String> cosmosEndpoint = config.getCosmosEndpoint();
            if (cosmosEndpoint.isEmpty()) {
                throw new RuntimeException("Cosmos DB endpoint not configured");
            }
            
            String endpoint = cosmosEndpoint.get();
            String accountName = endpoint.replace("https://", "").replace(".documents.azure.com:443/", "");
            
            // Get primary key from Key Vault
            KeyVaultService keyVaultService = new KeyVaultService(config);
            String primaryKey = keyVaultService.getCosmosPrimaryKey();
            if (primaryKey == null || primaryKey.isEmpty()) {
                throw new RuntimeException("Cosmos DB primary key not found in Key Vault");
            }
            
            String mongoConnectionString = String.format(
                "mongodb://%s:%s@%s.mongo.cosmos.azure.com:10255/?ssl=true&replicaSet=globaldb&retrywrites=false&maxIdleTimeMS=120000&appName=@%s@&authSource=admin",
                accountName, primaryKey, accountName, accountName
            );
            
            context.getLogger().info("Creating MongoDB client with Key Vault secret, endpoint: " + mongoConnectionString);
            
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(mongoConnectionString))
                .build();
                
            return MongoClients.create(settings);
            
        } catch (Exception e) {
            context.getLogger().severe("Failed to create MongoDB client: " + e.getMessage());
            throw new RuntimeException("Failed to create MongoDB client", e);
        }
    }
    
    /**
     * Save CSV data to database
     */
    public void saveCsvData(List<CsvData> csvDataList, ExecutionContext context) {
        Optional<String> databaseName = config.getCosmosDatabase();
        Optional<String> collectionName = config.getCosmosCollection();
        
        if (databaseName.isEmpty() || collectionName.isEmpty()) {
            throw new RuntimeException("Cosmos DB database or collection name not configured");
        }
        
        try (MongoClient mongoClient = createMongoClientWithPrimaryKey(context)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName.get());
            MongoCollection<Document> collection = database.getCollection(collectionName.get());
            
            int savedCount = 0;
            for (CsvData csvData : csvDataList) {
                try {
                    Document document = new Document()
                        .append("id", csvData.getId())
                        .append("number", csvData.getNumber())
                        .append("fileName", csvData.getFileName())
                        .append("processedAt", csvData.getProcessedAt())
                        .append("updatedAt", Instant.now());
                    
                    Document filter = new Document("id", csvData.getId());
                    Document update = new Document("$set", document);
                    UpdateOptions options = new UpdateOptions().upsert(true);
                    
                    collection.updateOne(filter, update, options);
                    savedCount++;
                    
                } catch (Exception e) {
                    context.getLogger().warning("Failed to save record, ID: " + csvData.getId() + ", Error: " + e.getMessage());
                }
            }
            
            context.getLogger().info("Successfully saved " + savedCount + " records to database");
            
        } catch (Exception e) {
            context.getLogger().severe("Failed to save data to database: " + e.getMessage());
            throw new RuntimeException("Database operation failed", e);
        }
    }
    
    /**
     * Read data from database
     */
    public List<Document> readDataFromDatabase(ExecutionContext context) {
        Optional<String> databaseName = config.getCosmosDatabase();
        Optional<String> collectionName = config.getCosmosCollection();
        
        if (databaseName.isEmpty() || collectionName.isEmpty()) {
            throw new RuntimeException("Cosmos DB database or collection name not configured");
        }
        
        List<Document> results = new ArrayList<>();
        
        try (MongoClient mongoClient = createMongoClientWithPrimaryKey(context)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName.get());
            MongoCollection<Document> collection = database.getCollection(collectionName.get());
            
            int limit = config.getDataLimit();
            
            collection.find()
                .sort(new Document("updatedAt", -1))
                .limit(limit)
                .into(results);
                
            context.getLogger().info("Retrieved " + results.size() + " records from database");
            
        } catch (Exception e) {
            context.getLogger().severe("Failed to read data from database: " + e.getMessage());
            throw new RuntimeException("Database read failed", e);
        }
        
        return results;
    }
    
    /**
     * Query data from database
     */
    public List<Document> queryDataFromDatabase(String id, String fileName, String limitStr, ExecutionContext context) {
        Optional<String> databaseName = config.getCosmosDatabase();
        Optional<String> collectionName = config.getCosmosCollection();
        
        if (databaseName.isEmpty() || collectionName.isEmpty()) {
            throw new RuntimeException("Cosmos DB database or collection name not configured");
        }
        
        List<Document> results = new ArrayList<>();
        
        try (MongoClient mongoClient = createMongoClientWithPrimaryKey(context)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName.get());
            MongoCollection<Document> collection = database.getCollection(collectionName.get());
            
            // Build query conditions
            Document filter = new Document();
            if (id != null && !id.trim().isEmpty()) {
                filter.append("id", id.trim());
            }
            if (fileName != null && !fileName.trim().isEmpty()) {
                filter.append("fileName", new Document("$regex", fileName).append("$options", "i"));
            }
            
            // Set limit
            int limit = config.getDataLimit();
            if (limitStr != null && !limitStr.isEmpty()) {
                try {
                    limit = Integer.parseInt(limitStr);
                } catch (NumberFormatException e) {
                    context.getLogger().warning("Invalid limit value: " + limitStr);
                }
            }
            
            collection.find(filter)
                .sort(new Document("updatedAt", -1))
                .limit(limit)
                .into(results);
                
            context.getLogger().info("Retrieved " + results.size() + " records from database with filter: " + filter.toJson());
            
        } catch (Exception e) {
            context.getLogger().severe("Failed to query database: " + e.getMessage());
            throw new RuntimeException("Database query failed", e);
        }
        
        return results;
    }
} 