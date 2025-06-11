import * as pulumi from "@pulumi/pulumi";
import * as azure from "@pulumi/azure-native";

const config = new pulumi.Config("azure-functions-demo");
const resourceGroupName = config.require("resourceGroupName");
const functionAppName = config.require("functionAppName");
const storageAccountName = config.require("storageAccountName");
const location = config.require("location");

// Optional Key Vault configuration
const enableKeyVault = config.getBoolean("enableKeyVault") || false;
const keyVaultName = config.get("keyVaultName");

// Create Resource Group
const rg = new azure.resources.ResourceGroup(resourceGroupName, {
    location: location,
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
    const currentConfig = azure.authorization.getClientConfigOutput();
    
    // Create Key Vault with proper access policies for deployment and deletion
    vault = new azure.keyvault.Vault(keyVaultName, {
        location: rg.location,
        resourceGroupName: rg.name,
        properties: {
            tenantId: currentConfig.tenantId,
            enabledForTemplateDeployment: true,
            enableRbacAuthorization: false,
            sku: {
                family: "A",
                name: azure.keyvault.SkuName.Standard,
            },
            accessPolicies: [
                // User-assigned identity access policy
                {
                    tenantId: currentConfig.tenantId,
                    objectId: userAssignedIdentity.principalId,
                    permissions: {
                        secrets: ["Get", "List"],
                    },
                },
                // Current user/service principal access policy for management operations
                {
                    tenantId: currentConfig.tenantId,
                    objectId: currentConfig.objectId,
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
    // Custom environment variables
    {
        name: "APP_ENVIRONMENT",
        value: config.get("appEnvironment") || "development",
    },
    {
        name: "API_VERSION",
        value: config.get("apiVersion") || "v1",
    },
    {
        name: "DEBUG_MODE",
        value: config.get("debugMode") || "false",
    },
    {
        name: "MAX_CITIES_COUNT",
        value: config.get("maxCitiesCount") || "50",
    },
    {
        name: "KEY_VAULT_ENABLED",
        value: enableKeyVault.toString(),
    },
];

// Configure managed identity
let identity: any;
if (enableKeyVault && userAssignedIdentity) {
    // When Key Vault is enabled, use both System and User Assigned identities
    identity = {
        type: "SystemAssigned,UserAssigned",
        userAssignedIdentities: userAssignedIdentity.id.apply(id => ({ [id]: {} })),
    };
    
    // Add Key Vault settings to appSettings
    appSettings = appSettings.concat([
        {
            name: "API_KEY",
            value: pulumi.interpolate`@Microsoft.KeyVault(VaultName=${vault!.name};SecretName=api-key)`,
        },
        {
            name: "DATABASE_CONNECTION",
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
    },
});

// Note: System-assigned identity access policy managed separately via output command
// This approach provides better control and avoids circular dependencies
// The current user already has full permissions for Key Vault operations including deletion

// Prepare outputs object
const outputs: any = {
    resourceGroupName: rg.name,
    functionAppName: functionApp.name,
    functionAppUrl: pulumi.interpolate`https://${functionApp.name}.azurewebsites.net`,
    storageAccountName: storageAccount.name,
    systemAssignedIdentityPrincipalId: functionApp.identity.apply(id => id?.principalId),
};

// Add Key Vault outputs if enabled
if (enableKeyVault && vault) {
    outputs.keyVaultName = vault.name;
    outputs.keyVaultUri = vault.properties.apply(p => p?.vaultUri);
    outputs.userAssignedIdentityId = userAssignedIdentity!.id;
    outputs.keyVaultAccessPolicyCommand = pulumi.interpolate`az keyvault set-policy --name ${vault.name} --object-id ${functionApp.identity.apply(id => id?.principalId)} --secret-permissions get list`;
}

// Export all outputs
export = outputs;