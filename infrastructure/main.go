package main

import (
	"github.com/pulumi/pulumi-azure-native-sdk/resources"
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
		return nil
	})
}
