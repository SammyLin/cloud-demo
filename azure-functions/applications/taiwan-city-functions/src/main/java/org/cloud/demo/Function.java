package org.cloud.demo;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class Function {
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
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
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

        // Read environment variables
        String appEnvironment = System.getenv("APP_ENVIRONMENT");
        String apiVersion = System.getenv("API_VERSION");
        String debugMode = System.getenv("DEBUG_MODE");
        String maxCitiesCountStr = System.getenv("MAX_CITIES_COUNT");

        // Log environment variables if debug mode is enabled
        if ("true".equalsIgnoreCase(debugMode)) {
            context.getLogger().info("Environment: " + appEnvironment);
            context.getLogger().info("API Version: " + apiVersion);
            context.getLogger().info("Debug Mode: " + debugMode);
            context.getLogger().info("Max Cities Count: " + maxCitiesCountStr);
        }

        String[] allCities = {
            "台北市", "新北市", "桃園市", "台中市", "台南市", "高雄市",
            "基隆市", "新竹市", "嘉義市", "新竹縣", "苗栗縣", "彰化縣",
            "南投縣", "雲林縣", "嘉義縣", "屏東縣", "宜蘭縣", "花蓮縣",
            "台東縣", "澎湖縣", "金門縣", "連江縣"
        };

        // Apply max cities limit from environment variable
        int maxCitiesCount = 50; // default
        if (maxCitiesCountStr != null && !maxCitiesCountStr.isEmpty()) {
            try {
                maxCitiesCount = Integer.parseInt(maxCitiesCountStr);
            } catch (NumberFormatException e) {
                context.getLogger().warning("Invalid MAX_CITIES_COUNT value: " + maxCitiesCountStr);
            }
        }

        // Limit cities based on environment variable
        String[] cities = allCities;
        if (maxCitiesCount > 0 && maxCitiesCount < allCities.length) {
            cities = java.util.Arrays.copyOf(allCities, maxCitiesCount);
        }

        // Build response with metadata
        String citiesJson = String.join(",", java.util.Arrays.stream(cities)
            .map(city -> "\"" + city + "\"")
            .toArray(String[]::new));

        String responseBody = String.format(
            "{\"environment\": \"%s\", \"version\": \"%s\", \"count\": %d, \"cities\": [%s]}",
            appEnvironment != null ? appEnvironment : "unknown",
            apiVersion != null ? apiVersion : "unknown",
            cities.length,
            citiesJson
        );

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(responseBody)
                .build();
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

        // Read all environment variables
        String appEnvironment = System.getenv("APP_ENVIRONMENT");
        String apiVersion = System.getenv("API_VERSION");
        String debugMode = System.getenv("DEBUG_MODE");
        String maxCitiesCount = System.getenv("MAX_CITIES_COUNT");

        String responseBody = String.format(
            "{\"config\": {\"environment\": \"%s\", \"apiVersion\": \"%s\", \"debugMode\": \"%s\", \"maxCitiesCount\": \"%s\"}}",
            appEnvironment != null ? appEnvironment : "not set",
            apiVersion != null ? apiVersion : "not set",
            debugMode != null ? debugMode : "not set",
            maxCitiesCount != null ? maxCitiesCount : "not set"
        );

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(responseBody)
                .build();
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

        // Check if Key Vault is enabled
        String keyVaultEnabled = System.getenv("KEY_VAULT_ENABLED");
        boolean isKeyVaultEnabled = "true".equalsIgnoreCase(keyVaultEnabled);

        if (!isKeyVaultEnabled) {
            String responseBody = "{\"message\": \"Key Vault integration is disabled\", \"key_vault_enabled\": false}";
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(responseBody)
                    .build();
        }

        try {
            // Method 1: Using environment variables that reference Key Vault (recommended)
            String apiKeyFromEnv = System.getenv("API_KEY");
            String dbConnectionFromEnv = System.getenv("DATABASE_CONNECTION");

            // Method 2: Direct Key Vault access using SDK (for demonstration)
            String keyVaultName = System.getenv("KEY_VAULT_NAME");
            String apiKeyDirect = null;
            String dbConnectionDirect = null;

            if (keyVaultName != null && !keyVaultName.isEmpty()) {
                String keyVaultUrl = "https://" + keyVaultName + ".vault.azure.net/";
                try {
                    SecretClient secretClient = new SecretClientBuilder()
                        .vaultUrl(keyVaultUrl)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildClient();

                    KeyVaultSecret apiKeySecret = secretClient.getSecret("api-key");
                    KeyVaultSecret dbConnectionSecret = secretClient.getSecret("database-connection");
                    
                    apiKeyDirect = apiKeySecret.getValue();
                    dbConnectionDirect = dbConnectionSecret.getValue();
                } catch (Exception e) {
                    context.getLogger().warning("Direct Key Vault access failed: " + e.getMessage());
                    apiKeyDirect = "Direct access failed: " + e.getMessage();
                    dbConnectionDirect = "Direct access failed: " + e.getMessage();
                }
            } else {
                apiKeyDirect = "Key Vault name not configured";
                dbConnectionDirect = "Key Vault name not configured";
            }

            String responseBody = String.format(
                "{\"key_vault_enabled\": true, \"secrets\": {" +
                "\"method1_env_references\": {" +
                "\"api_key\": \"%s\", " +
                "\"database_connection\": \"%s\"" +
                "}, " +
                "\"method2_direct_access\": {" +
                "\"api_key\": \"%s\", " +
                "\"database_connection\": \"%s\"" +
                "}, " +
                "\"note\": \"Method 1 uses Key Vault references in app settings, Method 2 uses direct SDK access\"" +
                "}}",
                apiKeyFromEnv != null ? maskSecret(apiKeyFromEnv) : "not set",
                dbConnectionFromEnv != null ? maskSecret(dbConnectionFromEnv) : "not set",
                apiKeyDirect != null ? maskSecret(apiKeyDirect) : "not set",
                dbConnectionDirect != null ? maskSecret(dbConnectionDirect) : "not set"
            );

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(responseBody)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error accessing Key Vault: " + e.getMessage());
            String errorResponse = String.format(
                "{\"error\": \"Failed to access Key Vault\", \"message\": \"%s\", \"key_vault_enabled\": true}",
                e.getMessage()
            );
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(errorResponse)
                    .build();
        }
    }

    /**
     * Masks sensitive information for logging/display purposes
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return "not set";
        }
        if (secret.length() <= 4) {
            return "****";
        }
        return secret.substring(0, 2) + "****" + secret.substring(secret.length() - 2);
    }
}