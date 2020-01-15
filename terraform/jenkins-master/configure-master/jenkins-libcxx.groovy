// Paste this script in Jenkins Script Console to configure Azure VM plugin
import com.microsoft.azure.vmagent.builders.*;

def bionicTemplate = new AzureVMTemplateBuilder()
    .withName("libcxxbionic")
    .withLabels("LIBCXX-1804")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_SGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "terraform-bionic")
.withInitScript("sudo gpasswd -a ${ADMIN_USER} docker \n" +
                        "sudo chmod g+rw /var/run/docker.sock")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();

def xenialTemplate = new AzureVMTemplateBuilder()
    .withName("libcxxxenial")
    .withLabels("LIBCXX-1604")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_SGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "terraform-xenial")
        .withInitScript("sudo gpasswd -a ${ADMIN_USER} docker \n" +
                        "sudo chmod g+rw /var/run/docker.sock")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();

def myCloud = new AzureVMCloudBuilder()
    .withCloudName(CLOUD_NAME)
    .withAzureCredentialsId(CREDENTIALS_ID_VMSS)
    .withExistingResourceGroupName(AGENTS_RESOURCE_GROUP)
    .addToTemplates(bionicTemplate)
    .addToTemplates(xenialTemplate)
    .build();

Jenkins.getInstance().clouds.add(myCloud);
