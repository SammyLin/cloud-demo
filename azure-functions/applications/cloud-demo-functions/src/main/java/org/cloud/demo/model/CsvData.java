package org.cloud.demo.model;

import java.time.Instant;

/**
 * CSV Data Model Class
 * Represents data records processed from CSV files
 */
public class CsvData {
    
    private String id;
    private Double number;
    private String fileName;
    private Instant processedAt;
    private Instant updatedAt;
    
    public CsvData() {
        // Default constructor
    }
    
    public CsvData(String id, Double number, String fileName) {
        this.id = id;
        this.number = number;
        this.fileName = fileName;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Double getNumber() {
        return number;
    }
    
    public void setNumber(Double number) {
        this.number = number;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Instant getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "CsvData{" +
                "id='" + id + '\'' +
                ", number=" + number +
                ", fileName='" + fileName + '\'' +
                ", processedAt=" + processedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 