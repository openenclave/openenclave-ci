// Copyright (c) Open Enclave SDK contributors.
// Licensed under the MIT License.

properties([parameters([string(name: 'OPENENCLAVE_REPOSITORY_NAME', defaultValue: 'openenclave/openenclave'),
                        string(name: 'OPENENCLAVE_BRANCH_NAME', defaultValue: 'master'),
                        booleanParam(name: 'FULL_TEST_SUITE', defaultValue: false)])])

stage("Test Openenclave") {
    build job: '/CI-CD_Infrastructure/OpenEnclave-Testing',
        parameters: [string(name: 'REPOSITORY_NAME', value: params.OPENENCLAVE_REPOSITORY_NAME),
                     string(name: 'BRANCH_NAME', value: params.OPENENCLAVE_BRANCH_NAME),
                     string(name: 'OECI_LIB_VERSION', value: "PR-${ghprbPullId}"),
                     booleanParam(name: 'FULL_TEST_SUITE', value: FULL_TEST_SUITE)]
}
