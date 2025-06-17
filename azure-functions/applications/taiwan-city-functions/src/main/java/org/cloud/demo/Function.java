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
import org.cloud.demo.service.CityService;
import org.cloud.demo.service.CsvProcessingService;
import org.cloud.demo.service.DatabaseService;
import org.cloud.demo.service.KeyVaultService;
import org.cloud.demo.service.StorageService;
import org.cloud.demo.util.ResponseUtil;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions Main Class
 * Contains all HTTP triggers and Blob trigger functions
 */
public class Function {
    
    private final ApplicationConfig config;
    private final CityService cityService;
    private final KeyVaultService keyVaultService;
    private final CsvProcessingService csvProcessingService;
    private final DatabaseService databaseService;
    private final StorageService storageService;
    
    public Function() {
        this.config = new ApplicationConfig();
        this.cityService = new CityService(config);
        this.keyVaultService = new KeyVaultService(config);
        this.csvProcessingService = new CsvProcessingService();
        this.databaseService = new DatabaseService(config);
        this.storageService = new StorageService(config);
    }
    
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return ResponseUtil.createErrorResponse(request, HttpStatus.BAD_REQUEST, 
                "Please pass a name on the query string or in the request body");
        } else {
            return ResponseUtil.createSuccessResponse(request, "Hello, " + name, "Request processed successfully");
        }
    }

    @FunctionName("TaiwanCities")
    public HttpResponseMessage getTaiwanCities(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "cities")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Taiwan cities API called.");

        // Log environment variables if debug mode is enabled
        if (config.isDebugMode()) {
            context.getLogger().info("Environment: " + config.getEnvironment());
            context.getLogger().info("API Version: " + config.getApiVersion());
            context.getLogger().info("Debug Mode: " + config.isDebugMode());
            context.getLogger().info("Max Cities Count: " + config.getMaxCitiesCount());
        }

        List<String> cities = cityService.getCities();
        
        return ResponseUtil.createCitiesResponse(request, config.getEnvironment(), 
            config.getApiVersion(), cities);
    }

    @FunctionName("Config")
    public HttpResponseMessage getConfig(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "config")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Config API called.");

        Map<String, String> configData = Map.of(
            "environment", config.getEnvironment() != null ? config.getEnvironment() : "not set",
            "apiVersion", config.getApiVersion() != null ? config.getApiVersion() : "not set",
            "debugMode", String.valueOf(config.isDebugMode()),
            "maxCitiesCount", String.valueOf(config.getMaxCitiesCount())
        );

        return ResponseUtil.createConfigResponse(request, configData);
    }

    @FunctionName("KeyVaultSecrets")
    public HttpResponseMessage getKeyVaultSecrets(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "secrets")
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Key Vault secrets API called.");

        Map<String, Object> secrets = keyVaultService.getKeyVaultSecrets(context);
        
        return ResponseUtil.createSuccessResponse(request, secrets, "Key Vault secrets retrieved successfully");
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