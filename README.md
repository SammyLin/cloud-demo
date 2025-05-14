# Cross-Cloud Taiwan City Data Service – Infrastructure Demo

This project provides a cross-cloud (multi-cloud) Infrastructure as Code (IaC) foundation for deploying a Java web application that demonstrates Taiwan city/county data. The goal is "write once, deploy anywhere"—enabling automated provisioning and deployment on different cloud providers (Azure, GCP, AWS) via unified interfaces and best practices.

- **Initial focus:** Azure implementation and connectivity verification
- **Architecture:** Designed for future extensibility to GCP and AWS
- **Core features:** Unified resource abstraction, secret/config management, NoSQL database, application hosting, automated deployment
- **Demo app:** Java Spring Boot, with data access abstraction for cloud-native NoSQL backends

## Project Structure

```
cloud-demo/
├── .windsurf/           # Project rules and automation
│   └── rules
├── infrastructure/      # Pulumi IaC source code
│   ├── main.go
│   ├── go.mod
│   ├── go.sum
│   └── Pulumi.yaml
└── README.md            # Project documentation
```

## Prerequisites
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) installed
- [Pulumi CLI](https://www.pulumi.com/docs/get-started/install/) installed
- Go 1.21 or later installed

## Pulumi Configuration & Environment Variables

All deployment variables (such as App Service name, label, Java version, etc.) are managed in Pulumi YAML config files. This allows you to easily switch environments and keep sensitive or environment-specific values out of source code.

- **Edit your stack config:**
  - Copy the example file: `cp infrastructure/Pulumi.dev.yaml.example infrastructure/Pulumi.dev.yaml`
  - Edit `Pulumi.dev.yaml` as needed. Example:

    ```yaml
    config:
      cloud-demo:appServiceLabelValue: Taiwan Cloud
      cloud-demo:webAppName: citydemo-webapp
      cloud-demo:appServicePlanName: citydemo-appserviceplan
      cloud-demo:javaVersion: Java|21
      cloud-demo:appServiceSku: B1
    ```
- **.gitignore:**
  - All `Pulumi.*.yaml` files are ignored except the `Pulumi.dev.yaml.example` template.

## Usage

1. **Login to Azure**

   ```sh
   az login
   ```

2. **Initialize Pulumi project** (first time only)

   ```sh
   pulumi login
   pulumi stack init dev
   # Or use an existing stack. The config file Pulumi.<stack>.yaml will be loaded automatically.

   ```

3. **Deploy the infrastructure and app**

   ```sh
   pulumi up
   # Pulumi will load variables from infrastructure/Pulumi.dev.yaml for the 'dev' stack
   # You can switch stacks (and thus config) using `pulumi stack select <stack>`

   ```

   You should see a resource group created and its name/location exported.

4. **Destroy test resources** (optional)

   ```sh
   pulumi destroy
   ```

---

If you encounter any errors, please provide the error message for troubleshooting assistance.
