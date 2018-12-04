/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(Closure body = {}) {
    properties([
            buildDiscarder(logRotator(numToKeepStr: '5')),
            gitLabConnection('gitlab')
    ])

    timestamps {
        def config = [X11: false, skipSonar: false, skipTests: false]
        // evaluate the body block, and collect configuration into the object
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()

        echo "Starting build with " + config.toMapString()

        currentBuild.result = 'SUCCESS'
        def currentProjectVersion
        try {
            parallel( // provided that two builds can actually run at the same time without conflicts...
                    'build': {
                        node {
                            ansiColor('xterm') {
                                stage('build') {
                                    cleanCheckout()
                                    gitlabCommitStatus(name: 'build') {
                                        currentProjectVersion = readMavenPom().version
                                        maven('', currentProjectVersion, config.X11, config.skipTests)
                                    }
                                }
                            }
                        }
                    },
                    'sonar': {
                        if (config.skipSonar || isFeatureBranch()) {
                            echo "Skipping sonar tests on ${env.BRANCH_NAME}"
                            return
                        }

                        node {
                            ansiColor('xterm') {
                                stage('sonar') {
                                    cleanCheckout()
                                    gitlabCommitStatus(name: 'sonar') {
                                        withSonarQubeEnv('dlabs') {
                                            currentProjectVersion = readMavenPom().version
                                            maven("clean verify ${env.SONAR_MAVEN_GOAL} -Dsonar.host.url=${env.SONAR_HOST_URL} -Pjacoco".toString(), currentProjectVersion, config.X11, config.skipTests)
                                        }
                                    }
                                }
                            }
                        }
                    }
            )

            if (!isReleasable(currentProjectVersion)) {
                return
            }

            def userAbortedRelease = false
            def releaseVersion
            stage('Continue to Release') {
                try {
                    notifyBuild(currentBuild.result, 'build')
                    milestone label: 'preReleaseConfirmation'
                    timeout(time: 1, unit: 'DAYS') {
                        releaseVersion = input(
                                message: 'Publish ? This will fail if there are any SNAPSHOT dependencies left!',
                                parameters: [
                                        [name        : 'version',
                                         defaultValue: toReleaseVersion(currentProjectVersion),
                                         description : 'Release version',
                                         $class      : 'hudson.model.StringParameterDefinition']
                                ]
                        )
                    }
                    milestone label: 'postReleaseConfirmation'
                } catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e) {
                    userAbortedRelease = true
                }
            }

            if (userAbortedRelease) {
                return
            }

            node {
                ansiColor('xterm') {
                    stage('release') {
                        cleanCheckout()
                        gitlabCommitStatus(name: 'release') {
                            sh "git tag v${releaseVersion}"

                            def descriptor = Artifactory.mavenDescriptor()
                            descriptor.version = releaseVersion
                            descriptor.failOnSnapshot = true
                            descriptor.transform()

                            maven('', releaseVersion)
                            gitPush('--tags')
                            currentBuild.displayName += ", " + releaseVersion

                            cleanCheckout(env.BRANCH_NAME)
                            def snapshotVersion = nextSnapshotVersionFor(releaseVersion)

                            descriptor = Artifactory.mavenDescriptor()
                            descriptor.version = snapshotVersion
                            descriptor.transform()
                            sh "git commit -a -m '[CD] change version to ${snapshotVersion}'"
                            gitPush()
                        }
                    }
                }
            }
        } catch (err) {
            currentBuild.result = "FAILED"
            echo err.toString()
            throw err
        } finally {
            notifyBuild(currentBuild.result)
        }
    }
}
