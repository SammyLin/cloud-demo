package main

import (
	"github.com/pulumi/pulumi-azure-native-sdk/resources"
	"github.com/pulumi/pulumi-azure-native-sdk/web"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// 建立一個測試用的 Resource Group
		rg, err := resources.NewResourceGroup(ctx, "test-rg", &resources.ResourceGroupArgs{
			Location: pulumi.String("eastasia"),
		})
		if err != nil {
			return err
		}

		ctx.Export("resourceGroupName", rg.Name)
		ctx.Export("resourceGroupLocation", rg.Location)

		// 1. Create App Service Plan (Linux)
		plan, err := web.NewAppServicePlan(ctx, AppServicePlanName, &web.AppServicePlanArgs{
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			Kind:              pulumi.String("linux"),
			Reserved:          pulumi.Bool(true), // Linux
			Sku: &web.SkuDescriptionArgs{
				Tier:     pulumi.String("Basic"),
				Name:     pulumi.String(AppServiceSku),
			},
		})
		if err != nil {
			return err
		}

		// 2. Create Web App (Java 21 SE, Linux)
		webApp, err := web.NewWebApp(ctx, WebAppName, &web.WebAppArgs{
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			ServerFarmId:      plan.ID(),
			Kind:              pulumi.String("app,linux"),
			SiteConfig: &web.SiteConfigArgs{
				LinuxFxVersion: pulumi.String(JavaVersion), // Java 21
			},
		})
		if err != nil {
			return err
		}

		// Export Web App URL
		ctx.Export("webAppUrl", pulumi.Sprintf("https://%s.azurewebsites.net", webApp.Name))

		return nil
	})
}
