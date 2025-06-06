package main

import (
	"github.com/pulumi/pulumi-azure-native-sdk/resources"
	"github.com/pulumi/pulumi-azure-native-sdk/storage"
	"github.com/pulumi/pulumi-azure-native-sdk/web"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		cfg := config.New(ctx, "azure-functions-demo")
		resourceGroupName := cfg.Require("resourceGroupName")
		functionAppName := cfg.Require("functionAppName")
		storageAccountName := cfg.Require("storageAccountName")
		location := cfg.Require("location")

		// Create Resource Group
		rg, err := resources.NewResourceGroup(ctx, resourceGroupName, &resources.ResourceGroupArgs{
			Location: pulumi.String(location),
		})
		if err != nil {
			return err
		}

		// Create Storage Account (required for Azure Functions)
		storageAccount, err := storage.NewStorageAccount(ctx, storageAccountName, &storage.StorageAccountArgs{
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			Sku: &storage.SkuArgs{
				Name: pulumi.String("Standard_LRS"),
			},
			Kind: pulumi.String("StorageV2"),
		})
		if err != nil {
			return err
		}

		// Get storage connection string
		storageKeys := storage.ListStorageAccountKeysOutput(ctx, storage.ListStorageAccountKeysOutputArgs{
			ResourceGroupName: rg.Name,
			AccountName:       storageAccount.Name,
		})

		storageConnectionString := pulumi.Sprintf("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
			storageAccount.Name,
			storageKeys.Keys().Index(pulumi.Int(0)).Value())

		// Create App Service Plan for Functions (Consumption plan)
		appServicePlan, err := web.NewAppServicePlan(ctx, functionAppName+"-plan", &web.AppServicePlanArgs{
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			Kind:              pulumi.String("FunctionApp"),
			Sku: &web.SkuDescriptionArgs{
				Tier: pulumi.String("Dynamic"),
				Name: pulumi.String("Y1"),
			},
		})
		if err != nil {
			return err
		}

		// Create Function App
		functionApp, err := web.NewWebApp(ctx, functionAppName, &web.WebAppArgs{
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			ServerFarmId:      appServicePlan.ID(),
			Kind:              pulumi.String("FunctionApp"),
			SiteConfig: &web.SiteConfigArgs{
				AppSettings: web.NameValuePairArray{
					web.NameValuePairArgs{
						Name:  pulumi.String("AzureWebJobsStorage"),
						Value: storageConnectionString,
					},
					web.NameValuePairArgs{
						Name:  pulumi.String("FUNCTIONS_EXTENSION_VERSION"),
						Value: pulumi.String("~4"),
					},
					web.NameValuePairArgs{
						Name:  pulumi.String("FUNCTIONS_WORKER_RUNTIME"),
						Value: pulumi.String("java"),
					},
					web.NameValuePairArgs{
						Name:  pulumi.String("WEBSITE_RUN_FROM_PACKAGE"),
						Value: pulumi.String("1"),
					},
				},
				JavaVersion: pulumi.String("17"),
			},
		})
		if err != nil {
			return err
		}

		// Export outputs
		ctx.Export("resourceGroupName", rg.Name)
		ctx.Export("functionAppName", functionApp.Name)
		ctx.Export("functionAppUrl", pulumi.Sprintf("https://%s.azurewebsites.net", functionApp.Name))
		ctx.Export("storageAccountName", storageAccount.Name)

		return nil
	})
}
