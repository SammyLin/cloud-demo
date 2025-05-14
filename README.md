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

## Usage

1. **Login to Azure**

   ```sh
   az login
   ```

2. **Initialize Pulumi project** (first time only)

   ```sh
   pulumi login
   pulumi stack init dev
   ```

3. **Deploy the test resource**

   ```sh
   pulumi up
   ```

   You should see a resource group created and its name/location exported.

4. **Destroy test resources** (optional)

   ```sh
   pulumi destroy
   ```

---

If you encounter any errors, please provide the error message for troubleshooting assistance.
