/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

#!groovy

library("jenkinsfile_library@$BRANCH_NAME")

properties([
        buildDiscarder(logRotator(numToKeepStr: '5')),
        gitLabConnection('gitlab')
])

timestamps {
    if (env.BRANCH_NAME != 'master') {
        echo "Skipping build on feature branch"
        return
    }

    try {
        node {
            ansiColor('xterm') {
                stage('release') {
                    cleanCheckout()
                    gitlabCommitStatus(name: 'release') {
                        def version = readChangelog().version
                        sh "git tag v" + version
                        gitPush('--tags')
                        currentBuild.displayName += ", " + version
                    }
                }
            }
        }
    } finally {
        notifyBuild(currentBuild.result)
    }
}
