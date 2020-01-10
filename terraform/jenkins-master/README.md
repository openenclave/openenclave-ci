# Example to create Jenkins server for Openenclave:
 This project is using terraform workspaces. 
 Workspaces are needed to keep state files separate , and to create unique naming resources with same terraform code

 The following Azure terraform modules are used to create resources:
### Terraform AzureRM Network
Used to create Azure VNet and subnets
- [Terraform Registry](https://registry.terraform.io/modules/Azure/network/azurerm)
- [Github](https://github.com/Azure/terraform-azurerm-network)

### Terraform AzureRM Compute
Used to create the VM for Jenkins Master with a custom [cloud-init template](cloud-init.tpl) 
- [Terraform Registry](https://registry.terraform.io/modules/Azure/compute/azurerm)
- [Github](https://github.com/Azure/terraform-azurerm-compute)

### Cloud-init template
Terraform will render the cloud-init template replacing the variables with their values.
[Cloud-init](cloud-init.tpl) will:
 * Format and mount the datadisk
 * Create a service file for Jenkins
 * Install Docker
 * Install Nginx and configure Jenkins site
 * Install and configure LetsEncrypt certificates
 * Install and configure LetsEncrypt certbot certificate autorenewal
 * Start Jenkins


# Steps to perform to create a Jenkins server in the workspace named "public":

### Prepare your environment 
Create a variables file for your environment in the variables folder.
Any variables we define here will override the defaults from [variables.tf](variables.tf) file
In our example we want to change:
 * Location of our Resource Group
 * VNet address space
 * VM Size
 * Add custom tags
 
Ex:
[oe-jenkins-public.tfvars](variables/oe-jenkins-public.tfvars)

### Use Azure CLI to login
Terraform will use the default Azure credentials when managing Azure resources
(Install and configure Terraform) [https://docs.microsoft.com/en-us/azure/developer/terraform/install-configure]
```bash
az login
az account set --subscription "xxxx-xxxx-xxxxx-xxx"
```

### Create a planfile
In this step we are going to:
 * Initialize Terraform, retrieving all used modules and setting up the backend.
 * Switch to the existing "public" workspace , or create a new one if it doesn't exist
 * Generate a Terraform plan file using our custom variables file.

```bash
terraform init
terraform workspace select public || terraform workspace new public
terraform plan --var-file=variables/oe-jenkins-public.tfvars -out planfile
```

### Apply the plan
We check the plan to make sure we are applying only the desired changes.
If we are satisfied with the plan, we apply all changes from the plan with the following command:

```bash
terraform apply planfile
```

### Accessing Jenkins Master
After terraform is complete we can access the Jenkins master on the DNS name from the Terraform output.
To retrieve the initial Jenkins Admin password, login to the VM using the private SSH key and run the following command:
```bash
docker logs jenkins
```



## Extra Tips:

### You can override any variable by defining an environment variable with the same name , prefixed with "TF_VAR_"
In our example we want to use a custom path to the oeadmin SSH public key.
This path is specific to the current user and should not be commited into Git.

Define oeadmin_ssh_pub_key variable , the key that will be attached to admin user
```bash
export TF_VAR_ssh_key=/path/to/public/key
```

### Use existing Azure Resources
Terraform can import resources created outside of Terraform using the [import command](https://www.terraform.io/docs/import/index.html)

Import your existing resources before running the Plan.
```bash
# Use a pre-existing VNET
terraform import module.network.azurerm_virtual_network.vnet /subscriptions/<SUBSCRIPTION_ID>/resourceGroups/<RESOURCE_GROUP_NAME>/providers/Microsoft.Network/virtualNetworks/<VNET_NAME>

# Use a pre-existing Resource Group
terraform import azurerm_resource_group.jenkins-rg.name <EXISTING_RESOURCE_GROUP_NAME>
```


### Configure the backend, or use custom AWS credentials
The [config.tf](config.tf) file contains all the configuration for the backend used to store Terraform state files.
You can also add custom parameters to the "azurerm" provider.

More info can be found at:
[Terraform Backend documentation](https://www.terraform.io/docs/backends/types/azurerm.html)
[Terraform AzureRM provider documentation](https://www.terraform.io/docs/providers/azurerm/index.html)

### Destroy all resources
```bash
terraform destroy
```
