package org.cloud.demo.model;

import java.util.List;

/**
 * Processing Result Model Class
 * Represents the result of CSV processing
 */
public class ProcessingResult {
    
    private final int processedCount;
    private final int skippedCount;
    private final boolean success;
    private final String errorMessage;
    private final List<CsvData> processedData;
    
    public ProcessingResult(int processedCount, int skippedCount, boolean success, String errorMessage, List<CsvData> processedData) {
        this.processedCount = processedCount;
        this.skippedCount = skippedCount;
        this.success = success;
        this.errorMessage = errorMessage;
        this.processedData = processedData;
    }
    
    /**
     * Create success result
     */
    public static ProcessingResult success(int processedCount, int skippedCount) {
        return new ProcessingResult(processedCount, skippedCount, true, null, null);
    }
    
    /**
     * Create success result with processed data
     */
    public static ProcessingResult success(int processedCount, int skippedCount, List<CsvData> processedData) {
        return new ProcessingResult(processedCount, skippedCount, true, null, processedData);
    }
    
    /**
     * Create failure result
     */
    public static ProcessingResult failure(String errorMessage) {
        return new ProcessingResult(0, 0, false, errorMessage, null);
    }
    
    // Getters
    public int getProcessedCount() {
        return processedCount;
    }
    
    public int getSkippedCount() {
        return skippedCount;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public List<CsvData> getProcessedData() {
        return processedData;
    }
    
    @Override
    public String toString() {
        return "ProcessingResult{" +
                "processedCount=" + processedCount +
                ", skippedCount=" + skippedCount +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", processedData=" + (processedData != null ? processedData.size() + " records" : "null") +
                '}';
    }
} 