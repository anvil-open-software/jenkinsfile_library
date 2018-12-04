/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String options="") {
    sshagent([scm.userRemoteConfigs[0].credentialsId]) {
        sh "git push " + options
    }
}
