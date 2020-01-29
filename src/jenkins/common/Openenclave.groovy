#!/usr/bin/groovy
// Copyright (c) Open Enclave SDK contributors.
// Licensed under the MIT License.

package jenkins.common;

String dockerBuildArgs(String... args) {
    String argumentString = ""
    for(arg in args) {
        argumentString += " --build-arg ${arg}"
    }
    return argumentString
}

String dockerImage(String tag, String dockerfile = ".jenkins/Dockerfile", String buildArgs = "") {
    return docker.build(tag, "${buildArgs} -f ${dockerfile} .")
}

def ContainerRun(String imageName, String compiler, String task, String runArgs="") {
    docker.withRegistry("https://oejenkinscidockerregistry.azurecr.io", "oejenkinscidockerregistry") {
        def image = docker.image(imageName)
        image.pull()
        image.inside(runArgs) {
            dir("${WORKSPACE}/build") {
                Run(compiler, task)
            }
        }
    }
}

def azureEnvironment(String task, String imageName = "oetools-deploy:latest") {
    withCredentials([usernamePassword(credentialsId: 'SERVICE_PRINCIPAL_OSTCLAB',
                                      passwordVariable: 'SERVICE_PRINCIPAL_PASSWORD',
                                      usernameVariable: 'SERVICE_PRINCIPAL_ID'),
                     string(credentialsId: 'OSCTLabSubID', variable: 'SUBSCRIPTION_ID'),
                     string(credentialsId: 'TenantID', variable: 'TENANT_ID')]) {
        docker.withRegistry("https://oejenkinscidockerregistry.azurecr.io", "oejenkinscidockerregistry") {
            def image = docker.image(imageName)
            image.pull()
            image.inside {
                sh """#!/usr/bin/env bash
                      set -o errexit
                      set -o pipefail
                      source /etc/profile
                      ${task}
                   """
            }
        }
    }
}

def runTask(String task) {
    dir("${WORKSPACE}/build") {
        sh """#!/usr/bin/env bash
                set -o errexit
                set -o pipefail
                source /etc/profile
                ${task}
            """
    }
}

def Run(String compiler, String task, String compiler_version = "") {
    def c_compiler
    def cpp_compiler
    switch(compiler) {
        case "cross":
            // In this case, the compiler is set by the CMake toolchain file. As
            // such, it is not necessary to specify anything in the environment.
            runTask(task)
            return
        case "clang":
            c_compiler = "clang"
            cpp_compiler = "clang++"
            break
        case "gcc":
            c_compiler = "gcc"
            cpp_compiler = "g++"
            break
        default:
            // This is needed for backwards compatibility with the old
            // implementation of the method.
            c_compiler = "clang"
            cpp_compiler = "clang++"
            compiler_version = "7"
    }
    if (compiler_version) {
        c_compiler += "-${compiler_version}"
        cpp_compiler += "-${compiler_version}"
    }
    withEnv(["CC=${c_compiler}","CXX=${cpp_compiler}"]) {
        runTask(task);
    }
}

def deleteRG(List resourceGroups, String imageName = "oetools-deploy:latest") {
    stage("Delete ${resourceGroups.toString()} resource groups") {
        resourceGroups.each { rg ->
            withEnv(["RESOURCE_GROUP=${rg}"]) {
                dir("${WORKSPACE}/.jenkins/provision") {
                    azureEnvironment("./cleanup.sh", imageName)
                }
            }
        }
    }
}

def emailJobStatus(String status) {
    emailext (
      to: '$DEFAULT_RECIPIENTS',      
      subject: "[Jenkins Job ${status}] ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
      body: """            
            <p>               
            For additional logging details for this job please check: 
            <a href="${env.BUILD_URL}">${env.JOB_NAME} - ${env.BUILD_NUMBER}</a>
            </p>
            """,
      recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']],
      mimeType: 'text/html'     
    )
}

return this