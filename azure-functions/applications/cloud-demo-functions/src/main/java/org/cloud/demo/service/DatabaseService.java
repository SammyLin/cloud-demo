package org.cloud.demo.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpMethod;
import com.microsoft.azure.functions.ExecutionContext;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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
     * Create MongoDB client - tries Service Connector first, then falls back to Key Vault
     */
    private MongoClient createMongoClient(ExecutionContext context) {
        // Check if Service Connector is available
        String serviceConnectorEndpoint = System.getenv("AZURE_COSMOS_RESOURCEENDPOINT");
        if (serviceConnectorEndpoint != null && !serviceConnectorEndpoint.isEmpty()) {
            context.getLogger().info("Service Connector detected, using Service Connector connection");
            return createMongoClientWithServiceConnector(context);
        } else {
            context.getLogger().info("Service Connector not detected, falling back to Key Vault connection");
            return createMongoClientWithPrimaryKey(context);
        }
    }
    
    /**
     * Create MongoDB client using Service Connector with Managed Identity
     */
    private MongoClient createMongoClientWithServiceConnector(ExecutionContext context) {
        try {
            // Get Service Connector environment variables
            String cosmosEndpoint = System.getenv("AZURE_COSMOS_RESOURCEENDPOINT");
            String listConnectionStringUrl = System.getenv("AZURE_COSMOS_LISTCONNECTIONSTRINGURL");
            
            if (cosmosEndpoint == null || cosmosEndpoint.isEmpty()) {
                throw new RuntimeException("AZURE_COSMOS_RESOURCEENDPOINT not found - Service Connector may not be configured");
            }
            
            context.getLogger().info("Using Service Connector with endpoint: " + cosmosEndpoint);
            
            // Try to get connection string using Managed Identity and Azure Management API
            if (listConnectionStringUrl != null && !listConnectionStringUrl.isEmpty()) {
                context.getLogger().info("Attempting to get connection string from: " + listConnectionStringUrl);
                
                try {
                    DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
                    
                    // Make HTTP request to get connection strings
                    HttpClient httpClient = HttpClient.createDefault();
                    HttpRequest request = new HttpRequest(HttpMethod.POST, listConnectionStringUrl);
                    request.setHeader("Authorization", "Bearer " + credential.getToken(
                        new TokenRequestContext().addScopes("https://management.azure.com/.default")
                    ).block().getToken());
                    request.setHeader("Content-Type", "application/json");
                    
                    HttpResponse response = httpClient.send(request).block();
                    
                    if (response.getStatusCode() == 200) {
                        String responseBody = response.getBodyAsString().block();
                        context.getLogger().info("Successfully retrieved connection strings");
                        
                        // Parse the response to extract MongoDB connection string
                        // Response format: {"connectionStrings":[{"connectionString":"mongodb://...","description":"Primary MongoDB Connection String"}]}
                        if (responseBody.contains("mongodb://")) {
                            int startIndex = responseBody.indexOf("mongodb://");
                            int endIndex = responseBody.indexOf("\"", startIndex);
                            String mongoConnectionString = responseBody.substring(startIndex, endIndex);
                            
                            context.getLogger().info("Using retrieved MongoDB connection string");
                            
                            MongoClientSettings settings = MongoClientSettings.builder()
                                .applyConnectionString(new com.mongodb.ConnectionString(mongoConnectionString))
                                .build();
                                
                            return MongoClients.create(settings);
                        }
                    } else {
                        context.getLogger().warning("Failed to get connection string, status: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    context.getLogger().warning("Failed to retrieve connection string via API: " + e.getMessage());
                }
            }
            
            // Fallback: Try direct connection (this might not work without authentication)
            String accountName = cosmosEndpoint.replace("https://", "").replace(".documents.azure.com:443/", "");
            String mongoConnectionString = String.format(
                "mongodb://%s.mongo.cosmos.azure.com:10255/?ssl=true&replicaSet=globaldb&retrywrites=false&maxIdleTimeMS=120000&appName=@%s@",
                accountName, accountName
            );
            
            context.getLogger().info("Attempting fallback MongoDB connection");
            
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(mongoConnectionString))
                .build();
                
            return MongoClients.create(settings);
            
        } catch (Exception e) {
            context.getLogger().severe("Failed to create MongoDB client with Service Connector: " + e.getMessage());
            throw new RuntimeException("Failed to create MongoDB client", e);
        }
    }

    /**
     * Create MongoDB client (using Key Vault + primary key) - LEGACY METHOD
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
        
        try (MongoClient mongoClient = createMongoClient(context)) {
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
        
        try (MongoClient mongoClient = createMongoClient(context)) {
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
        
        try (MongoClient mongoClient = createMongoClient(context)) {
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
    
    /**
     * Test database connection
     */
    public boolean testConnection(ExecutionContext context) {
        try {
            Optional<String> databaseName = config.getCosmosDatabase();
            Optional<String> collectionName = config.getCosmosCollection();
            
            if (databaseName.isEmpty() || collectionName.isEmpty()) {
                context.getLogger().warning("Database configuration not found");
                return false;
            }
            
            try (MongoClient mongoClient = createMongoClient(context)) {
                MongoDatabase database = mongoClient.getDatabase(databaseName.get());
                MongoCollection<Document> collection = database.getCollection(collectionName.get());
                
                // Simple ping test
                collection.countDocuments();
                
                context.getLogger().info("Database connection test successful");
                return true;
                
            } catch (Exception e) {
                context.getLogger().severe("Database connection test failed: " + e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            context.getLogger().severe("Database connection test error: " + e.getMessage());
            return false;
        }
    }
} 