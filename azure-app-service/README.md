# Azure App Service – Infrastructure as Code

This directory provides Infrastructure as Code (IaC) examples for Azure App Service, supporting both Terraform and Pulumi workflows, with automated deployment of Spring Boot applications.

---

## Directory Structure
```
azure-app-service/
├── applications/                # Spring Boot application source code
├── infrastructure/
│   ├── pulumi/                  # Pulumi IaC and deployment scripts
│   │   └── deploy-app.sh
│   └── terraform/               # Terraform IaC and deployment scripts
│       └── deploy-app.sh
└── README.md                    # This documentation file
```

## Application Placement
Place your Spring Boot project under the `applications/` directory. For example:
```
azure-app-service/applications/taiwan-city-demo
```

---

## Terraform Workflow
1. Enter the Terraform directory:
   ```sh
   cd azure-app-service/infrastructure/terraform
   ```
2. Initialize and deploy infrastructure:
   ```sh
   terraform init
   terraform apply -var-file=environments/dev/main.tfvars
   ```
3. Deploy the application:
   ```sh
   ./deploy-app.sh
   ```

## Pulumi Workflow
1. Enter the Pulumi directory:
   ```sh
   cd azure-app-service/infrastructure/pulumi
   ```
2. Initialize and deploy infrastructure:
   ```sh
   pulumi up
   ```
3. Deploy the application:
   ```sh
   ./deploy-app.sh
   ```

---

## About deploy-app.sh
- Each IaC subdirectory contains a `deploy-app.sh` script that will automatically package `applications/taiwan-city-demo` and upload it to the corresponding Azure Web App.
- Each script automatically reads resource names from IaC outputs.

---

## Advanced & Support
- You can extend this structure for multiple environments (dev/prod), or multi-cloud (GCP/AWS) scenarios.
- For CI/CD, automated testing, or multi-language IaC examples, feel free to ask!
