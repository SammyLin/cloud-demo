package org.cloud.demo.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.azure.functions.ExecutionContext;
import org.cloud.demo.config.ApplicationConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Key Vault Service Class
 * Responsible for interactions with Azure Key Vault
 */
public class KeyVaultService {
    
    private final ApplicationConfig config;
    
    public KeyVaultService(ApplicationConfig config) {
        this.config = config;
    }
    
    /**
     * Get Key Vault secrets information
     */
    public Map<String, Object> getKeyVaultSecrets(ExecutionContext context) {
        Map<String, Object> result = new HashMap<>();
        
        if (!config.isKeyVaultEnabled()) {
            result.put("key_vault_enabled", false);
            result.put("message", "Key Vault integration is disabled");
            return result;
        }
        
        result.put("key_vault_enabled", true);
        
        try {
            // Method 1: Using environment variables that reference Key Vault (recommended)
            Map<String, Object> method1Results = new HashMap<>();
            method1Results.put("api_key", maskSecret(config.getApiKey().orElse("not set")));
            method1Results.put("database_connection", maskSecret(config.getDatabaseConnection().orElse("not set")));
            result.put("method1_env_references", method1Results);
            
            // Method 2: Direct Key Vault access using SDK (for demonstration)
            Map<String, Object> method2Results = getDirectKeyVaultSecrets(context);
            result.put("method2_direct_access", method2Results);
            
            result.put("note", "Method 1 uses Key Vault references in app settings, Method 2 uses direct SDK access");
            
        } catch (Exception e) {
            context.getLogger().severe("Error accessing Key Vault: " + e.getMessage());
            result.put("error", "Failed to access Key Vault");
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get secrets directly from Key Vault
     */
    private Map<String, Object> getDirectKeyVaultSecrets(ExecutionContext context) {
        Map<String, Object> results = new HashMap<>();
        
        Optional<String> keyVaultName = config.getKeyVaultName();
        if (keyVaultName.isEmpty()) {
            results.put("api_key", "Key Vault name not configured");
            results.put("database_connection", "Key Vault name not configured");
            return results;
        }
        
        try {
            String keyVaultUrl = "https://" + keyVaultName.get() + ".vault.azure.net/";
            SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

            KeyVaultSecret apiKeySecret = secretClient.getSecret("api-key");
            KeyVaultSecret dbConnectionSecret = secretClient.getSecret("database-connection");
            
            results.put("api_key", maskSecret(apiKeySecret.getValue()));
            results.put("database_connection", maskSecret(dbConnectionSecret.getValue()));
            
        } catch (Exception e) {
            context.getLogger().warning("Direct Key Vault access failed: " + e.getMessage());
            results.put("api_key", "Direct access failed: " + e.getMessage());
            results.put("database_connection", "Direct access failed: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Mask sensitive information
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

    /**
     * Get secret from Key Vault
     * @param secretName Name of the secret
     * @return Secret value
     */
    private String getSecret(String secretName) {
        if (!config.isKeyVaultEnabled()) {
            throw new RuntimeException("Key Vault integration is disabled");
        }
        
        Optional<String> keyVaultName = config.getKeyVaultName();
        if (keyVaultName.isEmpty()) {
            throw new RuntimeException("Key Vault name not configured");
        }
        
        try {
            String keyVaultUrl = "https://" + keyVaultName.get() + ".vault.azure.net/";
            SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

            KeyVaultSecret secret = secretClient.getSecret(secretName);
            return secret.getValue();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get secret '" + secretName + "' from Key Vault: " + e.getMessage(), e);
        }
    }

    /**
     * Get Cosmos DB primary key from Key Vault
     * @return Cosmos DB primary key
     */
    public String getCosmosPrimaryKey() {
        return getSecret("cosmos-primary-key");
    }

    /**
     * Get API key from Key Vault
     * @return API key value
     */
    public String getApiKey() {
        return getSecret("api-key");
    }

    /**
     * Get database connection string from Key Vault
     * @return Database connection string
     */
    public String getDatabaseConnection() {
        return getSecret("database-connection");
    }
} 