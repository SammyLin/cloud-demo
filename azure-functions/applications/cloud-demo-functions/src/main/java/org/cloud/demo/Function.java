package org.cloud.demo;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.BlobTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.cloud.demo.config.ApplicationConfig;
import org.cloud.demo.model.CsvData;
import org.cloud.demo.model.ProcessingResult;
import org.cloud.demo.service.CsvProcessingService;
import org.cloud.demo.service.DatabaseService;
import org.cloud.demo.service.KeyVaultService;
import org.cloud.demo.service.StorageService;
import org.cloud.demo.util.ResponseUtil;
import org.bson.Document;
import org.cloud.demo.util.ResponseUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions Main Class
 * Contains all HTTP triggers and Blob trigger functions
 */
public class Function {
    
    private final ApplicationConfig config;
    private final KeyVaultService keyVaultService;
    private final CsvProcessingService csvProcessingService;
    private final DatabaseService databaseService;
    private final StorageService storageService;
    
    public Function() {
        this.config = new ApplicationConfig();
        this.keyVaultService = new KeyVaultService(config);
        this.csvProcessingService = new CsvProcessingService();
        this.databaseService = new DatabaseService(config);
        this.storageService = new StorageService(config);
    }
    
    @FunctionName("Echo")
    public HttpResponseMessage echo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "echo")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Echo API called.");

        // Parse query parameter
        final String name = request.getQueryParameters().get("name");

        if (name == null) {
            return ResponseUtil.createErrorResponse(request, HttpStatus.BAD_REQUEST, 
                "Please pass a name on the query string");
        } else {
            return ResponseUtil.createSuccessResponse(request, "Hello, " + name, "Echo request processed successfully");
        }
    }

    @FunctionName("ConfigEnv")
    public HttpResponseMessage getConfigEnv(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "config-env")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Config-env API called.");

        Map<String, String> envConfigData = Map.of(
            "ENV_APP_NAME", System.getenv("ENV_APP_NAME") != null ? System.getenv("ENV_APP_NAME") : "not set",
            "ENV_REGION", System.getenv("ENV_REGION") != null ? System.getenv("ENV_REGION") : "not set"
        );

        return ResponseUtil.createConfigResponse(request, envConfigData);
    }

    @FunctionName("ConfigKv")
    public HttpResponseMessage getConfigKv(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "config-kv")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Config-kv API called.");

        Map<String, Object> kvConfigData = keyVaultService.getKeyVaultSecrets(context);
        
        return ResponseUtil.createSuccessResponse(request, kvConfigData, "Key Vault configuration retrieved successfully");
    }

    @FunctionName("DbTest")
    public HttpResponseMessage testDatabase(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "db-test")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Database test API called.");

        try {
            // Test database connection
            boolean isConnected = databaseService.testConnection(context);
            
            Map<String, Object> dbTestResult = Map.of(
                "connection_status", isConnected ? "connected" : "failed",
                "db_endpoint", System.getenv("DB_ENDPOINT") != null ? System.getenv("DB_ENDPOINT") : "not set",
                "db_database_name", System.getenv("DB_DATABASE_NAME") != null ? System.getenv("DB_DATABASE_NAME") : "not set",
                "db_collection_name", System.getenv("DB_COLLECTION_NAME") != null ? System.getenv("DB_COLLECTION_NAME") : "not set",
                "timestamp", java.time.Instant.now().toString()
            );
            
            return ResponseUtil.createSuccessResponse(request, dbTestResult, "Database test completed");
            
        } catch (Exception e) {
            context.getLogger().severe("Database test failed: " + e.getMessage());
            return ResponseUtil.createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, 
                "Database test failed: " + e.getMessage());
        }
    }

    @FunctionName("CsvBlobProcessor")
    public void processCsvBlob(
            @BlobTrigger(
                name = "csvBlob",
                dataType = "string",
                path = "csv-uploads/{name}",
                connection = "SFTP_STORAGE_CONNECTION")
                String csvContent,
            @BindingName("name") String fileName,
            final ExecutionContext context) {
        
        context.getLogger().info("Processing CSV file: " + fileName + " (" + csvContent.length() + " characters)");
        
        boolean success = false;
        String errorLog = "";
        
        try {
            // Process CSV data
            ProcessingResult result = csvProcessingService.processCsvContent(csvContent, fileName, context);
            
            if (result.isSuccess()) {
                // Save processed data to database
                List<CsvData> processedData = result.getProcessedData();
                if (processedData != null && !processedData.isEmpty()) {
                    try {
                        databaseService.saveCsvData(processedData, context);
                        context.getLogger().info("Successfully saved " + processedData.size() + " records to database");
                    } catch (Exception e) {
                        context.getLogger().severe("Failed to save data to database: " + e.getMessage());
                        // Continue with file processing even if database save fails
                    }
                }
                
                success = true;
                context.getLogger().info("CSV processing completed successfully. Processed " + result.getProcessedCount() + " records from file: " + fileName);
            } else {
                throw new RuntimeException(result.getErrorMessage());
            }

        } catch (Exception e) {
            success = false;
            String errorMessage = "Error processing CSV file '" + fileName + "': " + e.getMessage();
            context.getLogger().severe(errorMessage);
            errorLog = csvProcessingService.generateProcessingLog(fileName, 
                ProcessingResult.failure(errorMessage), e.getMessage());
        }
        
        // Move file to appropriate folder
        try {
            storageService.moveFileAfterProcessing(fileName, csvContent, success, errorLog, context);
        } catch (Exception e) {
            context.getLogger().severe("Failed to move file after processing: " + e.getMessage());
        }
    }

    @FunctionName("DataReader")
    public HttpResponseMessage readData(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "data")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Data reader API called.");

        try {
            List<Document> data = databaseService.readDataFromDatabase(context);
            return ResponseUtil.createDataResponse(request, data);

        } catch (Exception e) {
            context.getLogger().severe("Error reading data: " + e.getMessage());
            return ResponseUtil.createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to read data: " + e.getMessage());
        }
    }

    @FunctionName("QueryData")
    public HttpResponseMessage queryData(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "query")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Query data API called.");

        try {
            // Parse query parameters
            String id = request.getQueryParameters().get("id");
            String fileName = request.getQueryParameters().get("fileName");
            String limitStr = request.getQueryParameters().get("limit");
            
            List<Document> data = databaseService.queryDataFromDatabase(id, fileName, limitStr, context);
            
            return ResponseUtil.createQueryResponse(request, id, fileName, limitStr, data);

        } catch (Exception e) {
            context.getLogger().severe("Error querying data: " + e.getMessage());
            return ResponseUtil.createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to query data: " + e.getMessage());
        }
    }
}