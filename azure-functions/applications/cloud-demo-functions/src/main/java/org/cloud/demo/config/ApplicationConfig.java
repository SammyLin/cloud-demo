package org.cloud.demo.config;

import java.util.Optional;

/**
 * Application Configuration Class
 * Manages environment variables for configuration
 */
public class ApplicationConfig {
    
    private static final String DEFAULT_ENVIRONMENT = "unknown";
    private static final String DEFAULT_API_VERSION = "1.0.0";
    private static final boolean DEFAULT_DEBUG_MODE = false;
    private static final int DEFAULT_MAX_CITIES_COUNT = 50;
    private static final boolean DEFAULT_KEY_VAULT_ENABLED = false;
    private static final int DEFAULT_DATA_LIMIT = 100;
    
    /**
     * Get application environment
     */
    public String getEnvironment() {
        return System.getenv("APP_ENVIRONMENT");
    }
    
    /**
     * Get API version
     */
    public String getApiVersion() {
        return System.getenv("API_VERSION");
    }
    
    /**
     * Get debug mode
     */
    public boolean isDebugMode() {
        String debugMode = System.getenv("DEBUG_MODE");
        return "true".equalsIgnoreCase(debugMode);
    }
    
    /**
     * Get maximum cities count
     */
    public int getMaxCitiesCount() {
        String maxCitiesCountStr = System.getenv("MAX_CITIES_COUNT");
        if (maxCitiesCountStr != null && !maxCitiesCountStr.isEmpty()) {
            try {
                return Integer.parseInt(maxCitiesCountStr);
            } catch (NumberFormatException e) {
                return DEFAULT_MAX_CITIES_COUNT;
            }
        }
        return DEFAULT_MAX_CITIES_COUNT;
    }
    
    /**
     * Check if Key Vault is enabled
     */
    public boolean isKeyVaultEnabled() {
        // Key Vault is enabled if KEY_VAULT_NAME environment variable is set
        String keyVaultName = System.getenv("KEY_VAULT_NAME");
        return keyVaultName != null && !keyVaultName.trim().isEmpty();
    }
    
    /**
     * Get Key Vault name
     */
    public Optional<String> getKeyVaultName() {
        return Optional.ofNullable(System.getenv("KEY_VAULT_NAME"));
    }
    
    /**
     * Get Cosmos DB endpoint
     */
    public Optional<String> getCosmosEndpoint() {
        return Optional.ofNullable(System.getenv("DB_ENDPOINT"));
    }
    
    /**
     * Get Cosmos DB database name
     */
    public Optional<String> getCosmosDatabase() {
        return Optional.ofNullable(System.getenv("DB_DATABASE_NAME"));
    }
    
    /**
     * Get Cosmos DB collection name
     */
    public Optional<String> getCosmosCollection() {
        return Optional.ofNullable(System.getenv("DB_COLLECTION_NAME"));
    }
    
    /**
     * Get data limit
     */
    public int getDataLimit() {
        String dataLimitStr = System.getenv("DATA_LIMIT");
        if (dataLimitStr != null && !dataLimitStr.isEmpty()) {
            try {
                return Integer.parseInt(dataLimitStr);
            } catch (NumberFormatException e) {
                return DEFAULT_DATA_LIMIT;
            }
        }
        return DEFAULT_DATA_LIMIT;
    }
    
    /**
     * Get SFTP storage connection string
     */
    public Optional<String> getSftpStorageConnection() {
        return Optional.ofNullable(System.getenv("SFTP_STORAGE_CONNECTION"));
    }
    
    /**
     * Get API key
     */
    public Optional<String> getApiKey() {
        return Optional.ofNullable(System.getenv("KV_API_KEY"));
    }
    
    /**
     * Get database connection string
     */
    public Optional<String> getDatabaseConnection() {
        return Optional.ofNullable(System.getenv("KV_DATABASE_CONNECTION"));
    }
} 