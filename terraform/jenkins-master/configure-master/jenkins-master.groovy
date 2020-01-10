// Paste this script in Jenkins Script Console to configure Azure VM plugin
import com.microsoft.azure.vmagent.builders.*;

def bionicTemplate = new AzureVMTemplateBuilder()
    .withName("accbionic")
    .withLabels("ACC-1804")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_SGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "acc-ubuntu-18.04")
        .withInitScript("sudo gpasswd -a ${ADMIN_USER} docker \n" +
                        "sudo chmod g+rw /var/run/docker.sock")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();

def xenialTemplate = new AzureVMTemplateBuilder()
    .withName("accxenial")
    .withLabels("ACC-1604")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_SGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "acc-ubuntu-16.04")
        .withInitScript("sudo gpasswd -a ${ADMIN_USER} docker \n" +
                        "sudo chmod g+rw /var/run/docker.sock")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();


def ubuntunonSGXTemplate = new AzureVMTemplateBuilder()
    .withName("ubuntunonsgx")
    .withLabels("nonSGX")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_NONSGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "ubuntu-nonSGX")
        .withInitScript("sudo gpasswd -a ${ADMIN_USER} docker \n" +
                        "sudo chmod g+rw /var/run/docker.sock")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();

def win2016Template = new AzureVMTemplateBuilder()
    .withName("accwin2016sgx")
    .withLabels("SGXFLC-Windows")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_SGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "acc-win-2016-SGX")
        .withOsType("Windows")
        .withLaunchMethod("SSH")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();

def win2016dcapTemplate = new AzureVMTemplateBuilder()
    .withName("accwin2016dcap")
    .withLabels("SGXFLC-Windows-DCAP")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_SGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "acc-win-2016-dcap")
        .withOsType("Windows")
        .withLaunchMethod("SSH")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();

def win2016nonSGXTemplate = new AzureVMTemplateBuilder()
    .withName("accwin2016nonsgx")
    .withLabels("nonSGX-Windows")
    .withLocation(AGENTS_LOCATION)
    .withVirtualMachineSize(VM_SIZE_NONSGX)
    .withExistingStorageAccount(AGENTS_STORAGE_ACCOUNT)
    .withUsageMode("Only build jobs with label expressions matching this node")
    .addNewAdvancedImage()
        .withCustomManagedImage(AGENTS_IMAGE_PREFIX + "win-2016-nonSGX")
        .withOsType("Windows")
        .withLaunchMethod("SSH")
    .endAdvancedImage()
    .withAdminCredential(CREDENTIALS_ID_ADMIN)
    .build();


def myCloud = new AzureVMCloudBuilder()
    .withCloudName(CLOUD_NAME)
    .withAzureCredentialsId(CREDENTIALS_ID_VMSS)
    .withExistingResourceGroupName(AGENTS_RESOURCE_GROUP)
    .addToTemplates(bionicTemplate)
    .addToTemplates(xenialTemplate)
    .addToTemplates(ubuntunonSGXTemplate)
    .addToTemplates(win2016Template)
    .addToTemplates(win2016dcapTemplate)
    .addToTemplates(win2016nonSGXTemplate)
    .build();

Jenkins.getInstance().clouds.add(myCloud);
