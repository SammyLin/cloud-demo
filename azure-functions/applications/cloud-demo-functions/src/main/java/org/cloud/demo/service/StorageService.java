package org.cloud.demo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import org.cloud.demo.config.ApplicationConfig;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Storage Service Class
 * Responsible for Azure Blob Storage operations
 */
public class StorageService {
    
    private final ApplicationConfig config;
    
    public StorageService(ApplicationConfig config) {
        this.config = config;
    }
    
    /**
     * Move file to appropriate folder after processing
     */
    public void moveFileAfterProcessing(String fileName, String fileContent, boolean success, 
                                       String errorLog, ExecutionContext context) {
        Optional<String> storageConnectionString = config.getSftpStorageConnection();
        if (storageConnectionString.isEmpty()) {
            context.getLogger().severe("SFTP storage connection string not configured");
            throw new RuntimeException("Storage configuration missing");
        }
        
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString.get())
                .buildClient();

            String targetContainer = success ? "csv-success" : "csv-failure";
            String sourceContainer = "csv-uploads";
            
            // Get container clients
            BlobContainerClient sourceContainerClient = blobServiceClient.getBlobContainerClient(sourceContainer);
            BlobContainerClient targetContainerClient = blobServiceClient.getBlobContainerClient(targetContainer);
            
            // Create target container if it doesn't exist
            if (!targetContainerClient.exists()) {
                targetContainerClient.create();
                context.getLogger().info("Created container: " + targetContainer);
            }
            
            // Copy file to target container
            BlobClient sourceBlobClient = sourceContainerClient.getBlobClient(fileName);
            BlobClient targetBlobClient = targetContainerClient.getBlobClient(fileName);
            
            // Upload file content to target container
            byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            targetBlobClient.upload(new ByteArrayInputStream(fileBytes), fileBytes.length, true);
            context.getLogger().info("Moved file " + fileName + " to " + targetContainer + " container");
            
            // Create error log file if processing failed
            if (!success && errorLog != null && !errorLog.trim().isEmpty()) {
                String errorFileName = getFileNameWithoutExtension(fileName) + "_failure.log";
                BlobClient errorBlobClient = targetContainerClient.getBlobClient(errorFileName);
                
                byte[] errorBytes = errorLog.getBytes(StandardCharsets.UTF_8);
                errorBlobClient.upload(new ByteArrayInputStream(errorBytes), errorBytes.length, true);
                context.getLogger().info("Created error log: " + errorFileName);
            }
            
            // Delete original file from csv-uploads
            if (sourceBlobClient.exists()) {
                sourceBlobClient.delete();
                context.getLogger().info("Deleted original file from " + sourceContainer + " container");
            }
            
        } catch (Exception e) {
            context.getLogger().severe("Error moving file: " + e.getMessage());
            throw new RuntimeException("Failed to move file after processing", e);
        }
    }
    
    /**
     * Get filename without extension
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String containerName, String fileName, ExecutionContext context) {
        Optional<String> storageConnectionString = config.getSftpStorageConnection();
        if (storageConnectionString.isEmpty()) {
            return false;
        }
        
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString.get())
                .buildClient();
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            
            return blobClient.exists();
        } catch (Exception e) {
            context.getLogger().warning("Error checking file existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete file
     */
    public boolean deleteFile(String containerName, String fileName, ExecutionContext context) {
        Optional<String> storageConnectionString = config.getSftpStorageConnection();
        if (storageConnectionString.isEmpty()) {
            return false;
        }
        
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString.get())
                .buildClient();
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            
            if (blobClient.exists()) {
                blobClient.delete();
                context.getLogger().info("Successfully deleted file: " + fileName + " from container: " + containerName);
                return true;
            } else {
                context.getLogger().warning("File does not exist: " + fileName + " in container: " + containerName);
                return false;
            }
        } catch (Exception e) {
            context.getLogger().severe("Error deleting file: " + e.getMessage());
            return false;
        }
    }
} 