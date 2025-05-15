package main

import (
	"github.com/pulumi/pulumi-azure-native-sdk/resources"
	"github.com/pulumi/pulumi-azure-native-sdk/web"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		cfg := config.New(ctx, "cloud-demo")
		resourceGroupName := cfg.Require("resourceGroupName")
		appServicePlanName := cfg.Require("appServicePlanName")
		webAppName := cfg.Require("webAppName")
		appServiceSku := cfg.Require("appServiceSku")
		javaVersion := cfg.Require("javaVersion")
		appServiceLabelValue := cfg.Require("appServiceLabelValue")

		// 建立一個測試用的 Resource Group
		rg, err := resources.NewResourceGroup(ctx, resourceGroupName, &resources.ResourceGroupArgs{
			Location: pulumi.String("eastasia"),
		})
		if err != nil {
			return err
		}

		ctx.Export("resourceGroupName", rg.Name)
		ctx.Export("resourceGroupLocation", rg.Location)

		// 1. Create App Service Plan (Linux)
		plan, err := web.NewAppServicePlan(ctx, appServicePlanName, &web.AppServicePlanArgs{
			Name:              pulumi.String(appServicePlanName),
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			Kind:              pulumi.String("linux"),
			Reserved:          pulumi.Bool(true), // Linux
			Sku: &web.SkuDescriptionArgs{
				Tier: pulumi.String("Basic"),
				Name: pulumi.String(appServiceSku),
			},
		})
		if err != nil {
			return err
		}

		// 2. Create Web App (Java 21 SE, Linux)
		webApp, err := web.NewWebApp(ctx, webAppName, &web.WebAppArgs{
			Name:              pulumi.String(webAppName),
			ResourceGroupName: rg.Name,
			Location:          rg.Location,
			ServerFarmId:      plan.ID(),
			Kind:              pulumi.String("app,linux"),
			SiteConfig: &web.SiteConfigArgs{
				LinuxFxVersion: pulumi.String(javaVersion), // Java 21
				AppSettings: web.NameValuePairArray{
					web.NameValuePairArgs{
						Name:  pulumi.String("APP_INSIDE_LABEL"),
						Value: pulumi.String(appServiceLabelValue),
					},
				},
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
