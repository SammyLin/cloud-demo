package org.cloud.demo.util;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * Response Utility Class
 * Used for generating unified HTTP responses
 */
public class ResponseUtil {
    
    /**
     * Create successful JSON response
     */
    public static HttpResponseMessage createSuccessResponse(
            HttpRequestMessage<?> request, 
            Object data, 
            String message) {
        
        Map<String, Object> responseBody = Map.of(
            "success", true,
            "message", message != null ? message : "Operation successful",
            "data", data != null ? data : Map.of()
        );
        
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(responseBody)
                .build();
    }
    
    /**
     * Create error JSON response
     */
    public static HttpResponseMessage createErrorResponse(
            HttpRequestMessage<?> request, 
            HttpStatus status, 
            String errorMessage) {
        
        Map<String, Object> responseBody = Map.of(
            "success", false,
            "error", errorMessage != null ? errorMessage : "Operation failed",
            "status", status.value()
        );
        
        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(responseBody)
                .build();
    }
    
    /**
     * Create cities list response
     */
    public static HttpResponseMessage createCitiesResponse(
            HttpRequestMessage<?> request,
            String environment,
            String version,
            List<String> cities) {
        
        Map<String, Object> responseBody = Map.of(
            "environment", environment != null ? environment : "unknown",
            "version", version != null ? version : "unknown",
            "count", cities.size(),
            "cities", cities
        );
        
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(responseBody)
                .build();
    }
    
    /**
     * Create configuration response
     */
    public static HttpResponseMessage createConfigResponse(
            HttpRequestMessage<?> request,
            Map<String, String> config) {
        
        Map<String, Object> responseBody = Map.of("config", config);
        
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(responseBody)
                .build();
    }
    
    /**
     * Create data response
     */
    public static HttpResponseMessage createDataResponse(
            HttpRequestMessage<?> request,
            List<Document> data) {
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"data\": [");
        
        for (int i = 0; i < data.size(); i++) {
            Document doc = data.get(i);
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append("{")
                .append("\"id\": \"").append(doc.getString("id")).append("\",")
                .append("\"number\": ").append(doc.get("number")).append(",")
                .append("\"fileName\": \"").append(doc.getString("fileName")).append("\",")
                .append("\"processedAt\": \"").append(doc.get("processedAt")).append("\",")
                .append("\"updatedAt\": \"").append(doc.get("updatedAt")).append("\"")
                .append("}");
        }
        
        jsonBuilder.append("], \"count\": ").append(data.size()).append("}");
        
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(jsonBuilder.toString())
                .build();
    }
    
    /**
     * Create query response
     */
    public static HttpResponseMessage createQueryResponse(
            HttpRequestMessage<?> request,
            String id,
            String fileName,
            String limit,
            List<Document> data) {
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"query\": {");
        if (id != null) jsonBuilder.append("\"id\": \"").append(id).append("\",");
        if (fileName != null) jsonBuilder.append("\"fileName\": \"").append(fileName).append("\",");
        jsonBuilder.append("\"limit\": ").append(limit != null ? limit : "100");
        jsonBuilder.append("}, \"data\": [");
        
        for (int i = 0; i < data.size(); i++) {
            Document doc = data.get(i);
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append("{")
                .append("\"id\": \"").append(doc.getString("id")).append("\",")
                .append("\"number\": ").append(doc.get("number")).append(",")
                .append("\"fileName\": \"").append(doc.getString("fileName")).append("\",")
                .append("\"processedAt\": \"").append(doc.get("processedAt")).append("\",")
                .append("\"updatedAt\": \"").append(doc.get("updatedAt")).append("\"")
                .append("}");
        }
        
        jsonBuilder.append("], \"count\": ").append(data.size()).append("}");
        
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(jsonBuilder.toString())
                .build();
    }
} 