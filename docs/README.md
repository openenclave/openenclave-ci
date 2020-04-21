Open Enclave CI
================
This library contains common Jenkins pipeline functions to be used by https://github.com/openenclave/openenclave

Usage
================
- In your Jenkins instance define a [Global Pipeline Library](https://jenkins.io/doc/book/pipeline/shared-libraries/) that points to this repo (for the purpose of this doc we'll name it 'OpenEnclaveCommon')
- In your pipeline script simply import the library
- OECI_LIB_VERSION can be any branch or PR from openenclave-ci repository (master, PR-XX)
```groovy
OECI_LIB_VERSION = env.OECI_LIB_VERSION ?: "master"
oe = library("OpenEnclaveCommon@${OECI_LIB_VERSION}").jenkins.common.Openenclave.new()
```
- Use the functions
```groovy
stage("test1") {
  node("linux") {
    cleanWs()
    def task = """
                echo "Hello World!"
                """
    oe.Run("clang-7", task)
  }
}
```
