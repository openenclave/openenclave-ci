variable "resource_group_name" {
  description = "Name of the resource group to create"
  default     = "OE-Jenkins-terraform"
}

variable "storage_account_name" {
  description = "Jenkins agents storage account name"
  default     = "agentsterraform"
}

variable "dns_prefix" {
  description = "Jenkins Master DNS name"
  default     = "oe-jenkins"
}

variable "location" {
  description = "The location/region where the core network will be created. The full list of Azure regions can be found at https://azure.microsoft.com/regions"
  default     = "westeurope"
}

variable "vnet_name" {
  description = "Name of the vnet to create"
  default     = "OE-Jenkins-terraform"
}

variable "address_space" {
  description = "The address space that is used by the virtual network."
  default     = "10.0.0.0/16"
}

variable "subnet_prefixes" {
  description = "The address prefix to use for the subnet."
  default     = ["10.0.1.0/24"]
}

variable "subnet_names" {
  description = "A list of public subnets inside the vNet."
  default     = ["subnet1"]
}

variable "admin_username" {
  description = "The admin username of the VM that will be deployed"
  default     = "oeadmin"
}

variable "tags" {
  description = "The tags to associate with your network and subnets."

  default = {
    environment = "Test"
    application = "Openenclave"
  }
}

variable "vm_size" {
  description = "Specifies the size of the virtual machine."
  default     = "Standard_DS1_V2"
}

variable "ssh_key" {
  description = "Path to the public key to be used for ssh access to the VM.  Only used with non-Windows vms and can be left as-is even if using Windows vms. If specifying a path to a certification on a Windows machine to provision a linux vm use the / in the path versus backslash. e.g. c:/home/id_rsa.pub"
  default     = "~/.ssh/id_rsa.pub"
}

variable "custom_vnet_rg" {
  description = "This variable is only used when we want to import a pre-existing VNet which is part of a different resource group"
  default     = ""
}
