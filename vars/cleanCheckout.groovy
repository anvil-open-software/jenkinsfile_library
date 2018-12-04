/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */
def call(String branchName = null) {
    checkout([
            $class: 'GitSCM',
            extensions: scm.extensions +  [[$class: 'CleanBeforeCheckout']],
            userRemoteConfigs: scm.userRemoteConfigs
    ])
    if (branchName != null) {
        sh "git checkout ${branchName}"
    }
}
