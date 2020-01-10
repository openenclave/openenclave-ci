variable "terraform-images" {
  type = map
  default = {
    "acc-ubuntu-18.04" = {
      "os_type"  = "Linux"
      "blob_uri" = "https://oejenkinswesteurope.blob.core.windows.net/disks/jenkins-terraform-ubuntu-18.04.vhd"
    }
    "acc-ubuntu-16.04" = {
      "os_type"  = "Linux"
      "blob_uri" = "https://oejenkinswesteurope.blob.core.windows.net/disks/jenkins-terraform-ubuntu-16.04.vhd"
    }
    "ubuntu-nonSGX" = {
      "os_type"  = "Linux"
      "blob_uri" = "https://oejenkinswesteurope.blob.core.windows.net/disks/jenkins-terraform-ubuntu-nonSGX.vhd"
    }
    "acc-win-2016-SGX" = {
      "os_type"  = "Windows"
      "blob_uri" = "https://oejenkinswesteurope.blob.core.windows.net/disks/jenkins-terraform-win-2016-SGX.vhd"
    }
    "acc-win-2016-dcap" = {
      "os_type"  = "Windows"
      "blob_uri" = "https://oejenkinswesteurope.blob.core.windows.net/disks/jenkins-terraform-win-2016-dcap.vhd"
    }
    "win-2016-nonSGX" = {
      "os_type"  = "Windows"
      "blob_uri" = "https://oejenkinswesteurope.blob.core.windows.net/disks/jenkins-terraform-win-2016-nonSGX.vhd"
    }
  }
}

variable "location" {
  default = "westeurope"
}

variable "resource_group_name" {}

variable "os_disk_size_gb" {
  default = 50
}

resource "azurerm_image" "terraform-image" {
  for_each            = var.terraform-images
  name                = each.key
  location            = var.location
  resource_group_name = var.resource_group_name

  os_disk {
    os_type  = each.value["os_type"]
    os_state = "Generalized"
    blob_uri = each.value["blob_uri"]
    size_gb  = var.os_disk_size_gb
  }
}
