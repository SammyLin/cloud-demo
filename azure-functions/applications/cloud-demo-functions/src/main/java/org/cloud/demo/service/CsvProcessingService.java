package org.cloud.demo.service;

import com.microsoft.azure.functions.ExecutionContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.cloud.demo.model.CsvData;
import org.cloud.demo.model.ProcessingResult;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Processing Service Class
 * Responsible for parsing and validating CSV files
 */
public class CsvProcessingService {
    
    private static final String[] REQUIRED_HEADERS = {"ID", "number"};
    
    /**
     * Process CSV content and return processed data
     */
    public ProcessingResult processCsvContent(String csvContent, String fileName, ExecutionContext context) {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            return ProcessingResult.failure("CSV content is empty");
        }
        
        context.getLogger().info("Starting to process CSV file: " + fileName + " (" + csvContent.length() + " characters)");
        
        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
            try (CSVParser csvParser = new CSVParser(new StringReader(csvContent), csvFormat)) {
                
                // Validate CSV headers
                if (!validateHeaders(csvParser)) {
                    return ProcessingResult.failure("CSV file must contain 'ID' and 'number' columns. Found headers: " + csvParser.getHeaderNames());
                }
                
                List<CsvData> validRecords = new ArrayList<>();
                int skippedCount = 0;
                
                for (CSVRecord record : csvParser) {
                    try {
                        CsvData csvData = parseRecord(record, fileName);
                        if (csvData != null) {
                            validRecords.add(csvData);
                        } else {
                            skippedCount++;
                        }
                    } catch (Exception e) {
                        skippedCount++;
                        String warning = "Error processing record at line " + record.getRecordNumber() + ": " + e.getMessage();
                        context.getLogger().warning(warning);
                    }
                }
                
                context.getLogger().info("CSV processing completed. Processed " + validRecords.size() + " records, skipped " + skippedCount + " records");
                
                if (validRecords.isEmpty()) {
                    return ProcessingResult.failure("No valid records were processed");
                }
                
                return ProcessingResult.success(validRecords.size(), skippedCount, validRecords);
                
            }
        } catch (IOException e) {
            context.getLogger().severe("Error parsing CSV: " + e.getMessage());
            return ProcessingResult.failure("CSV parsing failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate CSV headers
     */
    private boolean validateHeaders(CSVParser csvParser) {
        List<String> headerNames = csvParser.getHeaderNames();
        for (String requiredHeader : REQUIRED_HEADERS) {
            if (!headerNames.contains(requiredHeader)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Parse single record
     */
    private CsvData parseRecord(CSVRecord record, String fileName) {
        String id = record.get("ID");
        String numberStr = record.get("number");
        
        // Validate required fields
        if (id == null || id.trim().isEmpty() || numberStr == null || numberStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            double number = Double.parseDouble(numberStr.trim());
            return new CsvData(id.trim(), number, fileName);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Generate processing log
     */
    public String generateProcessingLog(String fileName, ProcessingResult result, String errorDetails) {
        StringBuilder log = new StringBuilder();
        log.append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
           .append(" - Processing file: ").append(fileName).append("\n");
        
        if (result.isSuccess()) {
            log.append("Status: Success\n");
            log.append("Processed records: ").append(result.getProcessedCount()).append("\n");
            log.append("Skipped records: ").append(result.getSkippedCount()).append("\n");
        } else {
            log.append("Status: Failed\n");
            log.append("Error message: ").append(result.getErrorMessage()).append("\n");
            if (errorDetails != null && !errorDetails.trim().isEmpty()) {
                log.append("Detailed error: ").append(errorDetails).append("\n");
            }
        }
        
        return log.toString();
    }
} 