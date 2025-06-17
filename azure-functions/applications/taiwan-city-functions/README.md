# Taiwan City Functions - Azure Functions Application

This is an Azure Functions-based Taiwan city data processing application that demonstrates how to build maintainable and scalable serverless applications.

## Project Structure

```
src/main/java/org/cloud/demo/
├── config/
│   └── ApplicationConfig.java          # Application configuration management
├── model/
│   ├── CsvData.java                    # CSV data model
│   └── ProcessingResult.java           # Processing result model
├── service/
│   ├── CityService.java                # City data service
│   ├── CsvProcessingService.java       # CSV processing service
│   ├── DatabaseService.java            # Database operations service
│   ├── KeyVaultService.java            # Key Vault service
│   └── StorageService.java             # Blob Storage service
├── util/
│   └── ResponseUtil.java               # HTTP response utility class
└── Function.java                       # Azure Functions main class
```

## Architecture Design Principles

### 1. Single Responsibility Principle (SRP)
Each class has a clear responsibility:
- **Configuration Classes**: Manage environment variables and configuration
- **Service Classes**: Handle business logic
- **Model Classes**: Represent data structures
- **Utility Classes**: Provide common functionality

### 2. Dependency Injection
Use constructor injection for dependencies to improve testability:
```java
public class CityService {
    private final ApplicationConfig config;
    
    public CityService(ApplicationConfig config) {
        this.config = config;
    }
}
```

### 3. Error Handling
Unified error handling mechanism:
```java
try {
    // Business logic
} catch (Exception e) {
    context.getLogger().severe("Error: " + e.getMessage());
    return ResponseUtil.createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, 
        "Operation failed: " + e.getMessage());
}
```

## Feature Modules

### 1. City Data API (`/api/cities`)
- Provides Taiwan city list
- Supports environment variable configuration limits
- Includes metadata information

### 2. Configuration Query API (`/api/config`)
- Displays current application configuration
- Used for debugging and monitoring

### 3. Key Vault Integration (`/api/secrets`)
- Secure key management
- Supports two access methods: environment variable references and direct SDK access

### 4. CSV File Processing
- Blob trigger automatically processes uploaded CSV files
- Data validation and error handling
- Automatic file classification (success/failure)

### 5. Data Query APIs
- `/api/data`: Read all data
- `/api/query`: Support conditional queries

## Configuration Management

### Environment Variables
```bash
# Application configuration
APP_ENVIRONMENT=dev
API_VERSION=1.0.0
DEBUG_MODE=true
MAX_CITIES_COUNT=50

# Azure service configuration
COSMOS_ENDPOINT=https://your-cosmos-account.documents.azure.com:443/
COSMOS_DATABASE=your-database
COSMOS_COLLECTION=your-collection
SFTP_STORAGE_CONNECTION=your-storage-connection-string

# Key Vault configuration
KEY_VAULT_ENABLED=true
KEY_VAULT_NAME=your-key-vault-name
API_KEY=your-api-key
DATABASE_CONNECTION=your-database-connection-string
```

## Security

### 1. Sensitive Information Protection
- Use Azure Key Vault to store keys
- Mask sensitive information in logs
- Avoid hardcoded configuration values

### 2. Input Validation
- CSV file format validation
- Data type checking
- Size limit controls

## Monitoring and Logging

### 1. Structured Logging
```java
context.getLogger().info("Processing file: " + fileName + " (" + fileSize + " bytes)");
context.getLogger().warning("Invalid data format at line " + lineNumber);
context.getLogger().severe("Database connection failed: " + error.getMessage());
```

### 2. Error Tracking
- Detailed error information logging
- Error log file generation
- Failure handling process

## Deployment

### 1. Local Development
```bash
# Install dependencies
mvn clean install

# Run locally
mvn azure-functions:run
```

### 2. Azure Deployment
```bash
# Deploy to Azure
mvn azure-functions:deploy
```

## Testing

### 1. Unit Testing
```bash
mvn test
```

### 2. Integration Testing
```bash
mvn verify
```

## Best Practices

### 1. Code Organization
- Package by feature modules
- Clear naming conventions
- Appropriate comments and documentation

### 2. Error Handling
- Unified error response format
- Appropriate logging
- Graceful degradation

### 3. Performance Optimization
- Connection pool management
- Batch operations
- Timely resource release

### 4. Maintainability
- Modular design
- Dependency injection
- Unit test coverage

## Extension Guide

### Adding New Features
1. Create corresponding service classes
2. Add new endpoints in Function class
3. Update configuration classes (if needed)
4. Add unit tests
5. Update documentation

### Adding New Data Sources
1. Create new Repository classes
2. Implement data access interfaces
3. Update service classes
4. Add configuration items

## Troubleshooting

### Common Issues
1. **Configuration Errors**: Check environment variable settings
2. **Connection Issues**: Verify Azure service connection strings
3. **Permission Issues**: Confirm managed identity permissions
4. **Performance Issues**: Check database queries and connection pool configuration

### Debugging Tips
1. Enable debug mode to view detailed logs
2. Use Azure Application Insights for monitoring
3. Check Azure Functions logs
4. Verify database connections and queries

## Contributing Guidelines

1. Follow existing code style
2. Add appropriate tests
3. Update documentation
4. Code review before submission

## License

This project is licensed under the MIT License. 