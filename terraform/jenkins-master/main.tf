resource "azurerm_resource_group" "jenkins-rg" {
  name     = "${var.resource_group_name}-${terraform.workspace}"
  location = var.location
}

module "network" {
  source              = "Azure/network/azurerm"
  resource_group_name = var.custom_vnet_rg == "" ? azurerm_resource_group.jenkins-rg.name : var.custom_vnet_rg
  vnet_name           = var.vnet_name
  address_space       = var.address_space
  subnet_prefixes     = var.subnet_prefixes
  subnet_names        = var.subnet_names
  tags                = var.tags
}

data "template_cloudinit_config" "jenkins-master" {
  gzip          = true
  base64_encode = true

  part {
    content_type = "text/cloud-config"
    content = templatefile("${path.module}/cloud-init.tpl",
      {
        jenkins_master_dns = "${var.dns_prefix}-${terraform.workspace}"
        location           = var.location
      }
    )
  }
}

module "jenkins-master" {
  source              = "Azure/compute/azurerm"
  vm_os_simple        = "UbuntuServer"
  public_ip_dns       = ["${var.dns_prefix}-${terraform.workspace}"]
  vm_hostname         = "jenkins-master-${terraform.workspace}"
  vm_size             = var.vm_size
  vnet_subnet_id      = element(module.network.vnet_subnets, 0)
  resource_group_name = azurerm_resource_group.jenkins-rg.name
  admin_username      = var.admin_username
  enable_ssh_key      = true
  ssh_key             = var.ssh_key
  custom_data         = data.template_cloudinit_config.jenkins-master.rendered
  nb_data_disk        = 1
  data_disk_size_gb   = 200
}

resource "azurerm_storage_account" "agents" {
  name                     = "${var.storage_account_name}${terraform.workspace}"
  resource_group_name      = azurerm_resource_group.jenkins-rg.name
  location                 = var.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  depends_on               = [module.jenkins-master]
}
