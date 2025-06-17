package org.cloud.demo.service;

import org.cloud.demo.config.ApplicationConfig;
import java.util.Arrays;
import java.util.List;

/**
 * City Service Class
 * Manages Taiwan city data and related business logic
 */
public class CityService {
    
    private static final String[] ALL_CITIES = {
        "台北市", "新北市", "桃園市", "台中市", "台南市", "高雄市",
        "基隆市", "新竹市", "嘉義市", "新竹縣", "苗栗縣", "彰化縣",
        "南投縣", "雲林縣", "嘉義縣", "屏東縣", "宜蘭縣", "花蓮縣",
        "台東縣", "澎湖縣", "金門縣", "連江縣"
    };
    
    private final ApplicationConfig config;
    
    public CityService(ApplicationConfig config) {
        this.config = config;
    }
    
    /**
     * Get Taiwan cities list
     * Limited by configured maximum cities count
     */
    public List<String> getCities() {
        int maxCount = config.getMaxCitiesCount();
        int limit = Math.min(maxCount, ALL_CITIES.length);
        return Arrays.asList(ALL_CITIES).subList(0, limit);
    }
    
    /**
     * Get all cities (unlimited)
     */
    public List<String> getAllCities() {
        return Arrays.asList(ALL_CITIES);
    }
    
    /**
     * Get total city count
     */
    public int getTotalCityCount() {
        return ALL_CITIES.length;
    }
    
    /**
     * Check if city exists
     */
    public boolean isCityValid(String cityName) {
        return Arrays.asList(ALL_CITIES).contains(cityName);
    }
    
    /**
     * Search cities by keyword
     */
    public List<String> searchCities(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCities();
        }
        
        return Arrays.stream(ALL_CITIES)
            .filter(city -> city.contains(keyword.trim()))
            .limit(config.getMaxCitiesCount())
            .toList();
    }
} 