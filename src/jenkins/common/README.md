Open Enclave CI
================
This library contains common Jenkins pipeline functions to be used by https://github.com/microsoft/openenclave

Usage
================
- In your Jenkins instance define a [Global Pipeline Library](https://jenkins.io/doc/book/pipeline/shared-libraries/) that points to this repo (for the purpose of this doc we'll name it 'OpenEnclaveCommon')
- In your pipeline script simply import the library
```groovy
@Library("OpenEnclaveCommon") _
oe = new jenkins.common.Openenclave()
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
