# Cursor Rules for Cloud Demo Project

This directory contains Cursor Rules that provide consistent guidance for the Azure Functions project development.

## Available Rules

### 1. Azure Naming Standards (`azure-naming-standard.mdc`)
- **Purpose**: Enforces Azure resource naming conventions based on Public Cloud Platform standards
- **Scope**: All Terraform files (`**/*.tf`, `**/*.tfvars`, etc.)
- **Key Features**:
  - Resource naming patterns for Azure services
  - Tag standards and requirements
  - Environment variable naming conventions
  - Pre-deployment checklist

### 2. Java Azure Functions Development (`java-azure-functions.mdc`)
- **Purpose**: Provides Java development standards for Azure Functions
- **Scope**: Java source files, Maven configuration, and Azure Functions configuration
- **Key Features**:
  - Code style and structure guidelines
  - Azure Functions best practices
  - Security and performance optimization
  - Testing and deployment standards

### 3. Terraform Best Practices (`terraform-best-practices.mdc`)
- **Purpose**: Enforces Terraform development standards and best practices
- **Scope**: All Terraform configuration files
- **Key Features**:
  - Code organization and structure
  - Variable management and validation
  - Security best practices
  - State management guidelines

## How to Use

### Automatic Application
These rules are configured to automatically apply when working with relevant files:
- **Always Apply**: Rules are always included in the model context
- **File Pattern Matching**: Rules are triggered based on file extensions and patterns
- **Project Scope**: Rules apply to the entire cloud-demo project

### Manual Invocation
You can manually reference these rules in conversations:
- `@azure-naming-standard` - For Azure resource naming guidance
- `@java-azure-functions` - For Java development standards
- `@terraform-best-practices` - For Terraform development guidance

## Project-Specific Configuration

### Application Identifiers
- **Application Name**: `twdemo` (Taiwan Demo Application)
- **CIPS Identifier**: `mcips` (Microsoft Cloud Infrastructure Platform Services)
- **Environments**: `dev`, `tst`, `prd`
- **Regions**: `twn`, `hk`, `sg`

### Naming Patterns
All resources follow the pattern:
```
[Resource]-[Application]-[CIPS]-[Environment]-[Region]-[Description]-[Suffix]
```

Example: `rg-twdemo-mcips-dev-twn-functions`

## Development Workflow

### 1. Infrastructure Development
When working with Terraform files:
- Follow the naming standards defined in `azure-naming-standard.mdc`
- Apply best practices from `terraform-best-practices.mdc`
- Use consistent variable naming and validation

### 2. Java Development
When working with Java code:
- Follow the structure and patterns in `java-azure-functions.mdc`
- Implement proper error handling and logging
- Use the defined package structure and naming conventions

### 3. Configuration Management
- Use environment variables for configuration
- Implement proper validation and error handling
- Follow security best practices for sensitive data

## Validation and Compliance

### Pre-deployment Checklist
Before deploying any changes:
- [ ] Resource names follow naming patterns
- [ ] Code follows style guidelines
- [ ] Security best practices are implemented
- [ ] Proper error handling is in place
- [ ] Documentation is updated

### Code Quality
- Use appropriate validation rules
- Follow consistent formatting
- Implement proper testing
- Maintain clear documentation

## Updating Rules

### Adding New Rules
1. Create a new `.mdc` file in this directory
2. Define the rule metadata (description, globs, alwaysApply)
3. Document the rule purpose and scope
4. Update this README with the new rule information

### Modifying Existing Rules
1. Update the rule content as needed
2. Ensure backward compatibility
3. Update documentation if necessary
4. Test the rule with relevant files

## Best Practices

### Rule Design
- Keep rules focused and specific
- Provide clear examples and guidelines
- Use consistent formatting and structure
- Include practical checklists and validations

### Rule Maintenance
- Regularly review and update rules
- Gather feedback from team members
- Ensure rules remain relevant and useful
- Version control all rule changes

## Support and Feedback

For questions or suggestions about these rules:
1. Review the rule documentation
2. Check the examples and guidelines
3. Consult with the development team
4. Update rules based on team feedback

## Related Documentation

- [Cursor Rules Documentation](https://docs.cursor.com/context/rules)
- [Azure Functions Documentation](https://docs.microsoft.com/en-us/azure/azure-functions/)
- [Terraform Best Practices](https://www.terraform.io/docs/cloud/guides/recommended-practices/index.html)