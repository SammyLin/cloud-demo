package org.cloud.demo;

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
}