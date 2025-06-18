import * as pulumi from "@pulumi/pulumi";
import * as azure from "@pulumi/azure-native";

const config = new pulumi.Config("azure-functions-demo");
const resourceGroupName = config.require("resourceGroupName");
const functionAppName = config.require("functionAppName");
const storageAccountName = config.require("storageAccountName");
const location = config.require("location");

// Application Insights configuration
const applicationInsightsName = config.get("applicationInsightsName") || `${functionAppName}-insights`;

// Optional Key Vault configuration
const enableKeyVault = config.getBoolean("enableKeyVault") || false;
const keyVaultName = config.get("keyVaultName");

// Optional Cosmos DB configuration
const enableCosmosDb = config.getBoolean("enableCosmosDb") || false;
const cosmosDbAccountName = config.get("cosmosDbAccountName") || `${functionAppName}-cosmos`;
const cosmosDatabaseName = config.get("cosmosDatabaseName") || "csvdata";
const cosmosCollectionName = config.get("cosmosCollectionName") || "records";

// Create Resource Group
const rg = new azure.resources.ResourceGroup(resourceGroupName, {
    location: location,
});

// Create Application Insights
const appInsights = new azure.insights.Component(applicationInsightsName, {
    resourceGroupName: rg.name,
    location: rg.location,
    applicationType: "web",
    kind: "web",
    retentionInDays: 90,
    samplingPercentage: 100,
    ingestionMode: "ApplicationInsights", // Use ApplicationInsights mode instead of LogAnalytics
    tags: {
        Environment: "dev",
        Project: "Taiwan Functions Demo",
    },
});

// Create Storage Account (required for Azure Functions)
const storageAccount = new azure.storage.StorageAccount(storageAccountName, {
    resourceGroupName: rg.name,
    location: rg.location,
    sku: {
        name: "Standard_LRS",
    },
    kind: "StorageV2",
});

// Get storage connection string
const storageKeys = azure.storage.listStorageAccountKeysOutput({
    resourceGroupName: rg.name,
    accountName: storageAccount.name,
});

const storageConnectionString = pulumi.interpolate`DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};AccountKey=${storageKeys.keys[0].value};EndpointSuffix=core.windows.net`;

// Create separate SFTP-enabled storage account for CSV processing
const sftpStorageAccountName = config.get("sftpStorageAccountName") || `${storageAccountName}sftp`;
const sftpStorageAccount = new azure.storage.StorageAccount(sftpStorageAccountName, {
    resourceGroupName: rg.name,
    location: rg.location,
    sku: {
        name: "Standard_LRS",
    },
    kind: "StorageV2",
    isHnsEnabled: true, // Required for SFTP
    isSftpEnabled: true, // Enable SFTP
    minimumTlsVersion: "TLS1_2",
    allowSharedKeyAccess: true,
    publicNetworkAccess: "Enabled",
    allowBlobPublicAccess: true,
    enableHttpsTrafficOnly: false,
    tags: {
        Environment: "dev",
        Project: "Taiwan Functions Demo",
        Component: "sftp-storage",
        Purpose: "CSV file processing",
    },
});

// Get SFTP storage connection string
const sftpStorageKeys = azure.storage.listStorageAccountKeysOutput({
    resourceGroupName: rg.name,
    accountName: sftpStorageAccount.name,
});

const sftpStorageConnectionString = pulumi.interpolate`DefaultEndpointsProtocol=https;AccountName=${sftpStorageAccount.name};AccountKey=${sftpStorageKeys.keys[0].value};EndpointSuffix=core.windows.net`;

// Create Blob Containers for CSV processing workflow on SFTP storage
const csvUploadContainer = new azure.storage.BlobContainer("csv-uploads", {
    resourceGroupName: rg.name,
    accountName: sftpStorageAccount.name,
    containerName: "csv-uploads",
    publicAccess: "None",
});

const csvSuccessContainer = new azure.storage.BlobContainer("csv-success", {
    resourceGroupName: rg.name,
    accountName: sftpStorageAccount.name,
    containerName: "csv-success",
    publicAccess: "None",
});

const csvFailureContainer = new azure.storage.BlobContainer("csv-failure", {
    resourceGroupName: rg.name,
    accountName: sftpStorageAccount.name,
    containerName: "csv-failure",
    publicAccess: "None",
});

// Create Cosmos DB resources if enabled
let cosmosAccount: azure.documentdb.DatabaseAccount | undefined;
let serviceConnector: azure.servicelinker.Linker | undefined;

if (enableCosmosDb) {
    // Create Cosmos DB Account with MongoDB API
    cosmosAccount = new azure.documentdb.DatabaseAccount(cosmosDbAccountName, {
        resourceGroupName: rg.name,
        location: rg.location,
        databaseAccountOfferType: "Standard",
        kind: "MongoDB",
        consistencyPolicy: {
            defaultConsistencyLevel: "Session",
        },
        locations: [{
            locationName: rg.location,
            failoverPriority: 0,
        }],
        capabilities: [{
            name: "EnableMongo",
        }],
        tags: {
            Environment: "dev",
            Project: "Taiwan Functions Demo",
        },
    });
}

let userAssignedIdentity: azure.managedidentity.UserAssignedIdentity | undefined;
let vault: azure.keyvault.Vault | undefined;

// Conditionally create Key Vault resources
if (enableKeyVault && keyVaultName) {
    // Create User Assigned Managed Identity
    userAssignedIdentity = new azure.managedidentity.UserAssignedIdentity(`${functionAppName}-identity`, {
        resourceGroupName: rg.name,
        location: rg.location,
    });

    // Get current tenant ID
    const currentConfig = azure.authorization.getClientConfig();
    
    // Create Key Vault with proper access policies for deployment and deletion
    vault = new azure.keyvault.Vault(keyVaultName, {
        location: rg.location,
        resourceGroupName: rg.name,
        properties: {
            tenantId: currentConfig.then(config => config.tenantId),
            enabledForTemplateDeployment: true,
            enableRbacAuthorization: false,
            sku: {
                family: "A",
                name: azure.keyvault.SkuName.Standard,
            },
            accessPolicies: [
                // User-assigned identity access policy
                {
                    tenantId: currentConfig.then(config => config.tenantId),
                    objectId: userAssignedIdentity.principalId,
                    permissions: {
                        secrets: ["Get", "List"],
                    },
                },
                // Current user/service principal access policy for management operations
                {
                    tenantId: currentConfig.then(config => config.tenantId),
                    objectId: currentConfig.then(config => config.objectId),
                    permissions: {
                        secrets: ["Get", "List", "Set", "Delete", "Recover", "Backup", "Restore"],
                        keys: ["Get", "List", "Update", "Create", "Import", "Delete", "Recover", "Backup", "Restore"],
                        certificates: ["Get", "List", "Update", "Create", "Import", "Delete", "Recover", "Backup", "Restore", "ManageContacts", "ManageIssuers", "GetIssuers", "ListIssuers", "SetIssuers", "DeleteIssuers"],
                    },
                },
            ],
        },
    });

    // Create sample secrets in Key Vault
    new azure.keyvault.Secret("api-key-secret", {
        resourceGroupName: rg.name,
        vaultName: vault.name,
        secretName: "api-key",
        properties: {
            value: "your-secret-api-key-value",
        },
    });

    new azure.keyvault.Secret("database-connection-secret", {
        resourceGroupName: rg.name,
        vaultName: vault.name,
        secretName: "database-connection",
        properties: {
            value: "server=example.com;database=mydb;user=myuser;password=secretpassword",
        },
    });
}

// Create App Service Plan for Functions (Consumption plan)
const appServicePlan = new azure.web.AppServicePlan(`${functionAppName}-plan`, {
    resourceGroupName: rg.name,
    location: rg.location,
    kind: "FunctionApp",
    sku: {
        tier: "Dynamic",
        name: "Y1",
    },
});

// Base app settings
let appSettings: any[] = [
    {
        name: "AzureWebJobsStorage",
        value: storageConnectionString,
    },
    {
        name: "FUNCTIONS_EXTENSION_VERSION",
        value: "~4",
    },
    {
        name: "FUNCTIONS_WORKER_RUNTIME",
        value: "java",
    },
    {
        name: "WEBSITE_RUN_FROM_PACKAGE",
        value: "1",
    },
    {
        name: "AzureWebJobsDisableHomepage",
        value: "true",
    },
    // Application Insights settings
    {
        name: "APPLICATIONINSIGHTS_CONNECTION_STRING",
        value: appInsights.connectionString,
    },
    // Environment variables for /api/config-env
    {
        name: "ENV_APP_NAME",
        value: "cloud-demo-functions",
    },
    {
        name: "ENV_REGION",
        value: config.get("region") || "twn",
    },
];

// Add Cosmos DB settings if enabled (for /api/db-test)
if (enableCosmosDb && cosmosAccount) {
    appSettings = appSettings.concat([
        {
            name: "DB_ENDPOINT",
            value: cosmosAccount.documentEndpoint,
        },
        {
            name: "DB_DATABASE_NAME",
            value: cosmosDatabaseName,
        },
        {
            name: "DB_COLLECTION_NAME",
            value: cosmosCollectionName,
        },
    ]);
}

// Configure managed identity
let identity: any;
if (enableKeyVault && userAssignedIdentity) {
    // When Key Vault is enabled, use both System and User Assigned identities
    identity = {
        type: "SystemAssigned,UserAssigned",
        userAssignedIdentities: userAssignedIdentity.id.apply(id => ({ [id]: {} })),
    };
    
    // Add Key Vault settings for /api/config-kv
    appSettings = appSettings.concat([
        {
            name: "KV_API_KEY",
            value: pulumi.interpolate`@Microsoft.KeyVault(VaultName=${vault!.name};SecretName=api-key)`,
        },
        {
            name: "KV_DATABASE_CONNECTION",
            value: pulumi.interpolate`@Microsoft.KeyVault(VaultName=${vault!.name};SecretName=database-connection)`,
        },
        {
            name: "KEY_VAULT_NAME",
            value: vault!.name,
        },
    ]);
} else {
    // When Key Vault is disabled, use only System Assigned identity
    identity = {
        type: "SystemAssigned",
    };
}

// Create Function App
const functionApp = new azure.web.WebApp(functionAppName, {
    resourceGroupName: rg.name,
    location: rg.location,
    serverFarmId: appServicePlan.id,
    kind: "FunctionApp",
    identity: identity,
    siteConfig: {
        appSettings: appSettings,
        javaVersion: "17",
        // Note: alwaysOn is not supported in Consumption plan (Y1)
    },
});

// Create Service Connector between Function App and Cosmos DB using Managed Identity
if (enableCosmosDb && cosmosAccount) {
    const subscription = azure.authorization.getClientConfig();
    serviceConnector = new azure.servicelinker.Linker("cosmosdb_connection", {
        resourceUri: pulumi.interpolate`/subscriptions/${subscription.then((sub: any) => sub.subscriptionId)}/resourceGroups/${rg.name}/providers/Microsoft.Web/sites/${functionApp.name}`,
        targetService: {
            type: "AzureResource",
            id: cosmosAccount.id,
        },
        authInfo: {
            authType: "systemAssignedIdentity",
        },
        clientType: "java",
    }, {
        dependsOn: [functionApp, cosmosAccount],
    });
}

// Note: System-assigned identity access policy managed separately via output command
// This approach provides better control and avoids circular dependencies
// The current user already has full permissions for Key Vault operations including deletion

// Prepare outputs object
const outputs: any = {
    resourceGroupName: rg.name,
    functionAppName: functionApp.name,
    functionAppUrl: pulumi.interpolate`https://${functionApp.name}.azurewebsites.net`,
    storageAccountName: storageAccount.name,
    sftpStorageAccountName: sftpStorageAccount.name,
    systemAssignedIdentityPrincipalId: functionApp.identity.apply(id => id?.principalId),
    applicationInsightsName: appInsights.name,
    applicationInsightsConnectionString: appInsights.connectionString,
};

// Add Key Vault outputs if enabled
if (enableKeyVault && vault) {
    outputs.keyVaultName = vault.name;
    outputs.keyVaultUri = vault.properties.apply(p => p?.vaultUri);
    outputs.userAssignedIdentityId = userAssignedIdentity!.id;
    outputs.keyVaultAccessPolicyCommand = pulumi.interpolate`az keyvault set-policy --name ${vault.name} --object-id ${functionApp.identity.apply(id => id?.principalId)} --secret-permissions get list`;
}

// Add Cosmos DB outputs if enabled
if (enableCosmosDb && cosmosAccount) {
    outputs.cosmosAccountName = cosmosAccount.name;
    outputs.cosmosAccountEndpoint = cosmosAccount.documentEndpoint;
    outputs.cosmosDatabaseName = cosmosDatabaseName;
    outputs.cosmosCollectionName = cosmosCollectionName;
    outputs.serviceConnectorName = serviceConnector?.name;
}

// Add CSV container outputs
outputs.csvUploadContainerName = csvUploadContainer.name;
outputs.csvSuccessContainerName = csvSuccessContainer.name;
outputs.csvFailureContainerName = csvFailureContainer.name;

// Export all outputs
export = outputs;