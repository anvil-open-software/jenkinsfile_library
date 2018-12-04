/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String goals, String version, boolean x11 = false, boolean skipTests = false) {
    if (x11) {
        withEnv(['DBUS_SESSION_BUS_ADDRESS=/dev/null']) {
            wrap([$class: 'Xvfb', autoDisplayName: true, parallelBuild: true]) {
                maven(goals, version, false, skipTests)
            }
        }
        return
    }

    def artifactory = Artifactory.server 'artifactory'

    docker.withTool('17.12.0-ce') {
        docker.withRegistry('https://us.gcr.io', 'jenkins-docker') {
            configFileProvider([configFile(fileId: 'simple-maven-settings', variable: 'MAVEN_USER_SETTINGS')]) {
                def mavenRuntime = Artifactory.newMavenBuild()
                mavenRuntime.tool = 'maven353'
                mavenRuntime.resolver server: artifactory, releaseRepo: 'maven-dlabs', snapshotRepo: 'maven-dlabs'
                mavenRuntime.deployer server: artifactory, releaseRepo: 'maven-dlabs-release', snapshotRepo: 'maven-dlabs-snapshot'
                mavenRuntime.deployer.deployArtifacts = isPublishable(version) && !goals.contains('sonar')

                try {
                    def skipTestsOption = ''
                    if (skipTests) {
                        skipTestsOption = ' -DskipTests'
                    }
                    if (!isSnapshotVersion(version)) {
                        skipTestsOption = ' -DskipTests -Darguments="-DskipTests"'
                    }
                    helm('lint', '')
                    def buildInfo = mavenRuntime.run pom: 'pom.xml', goals: "-B -s ${MAVEN_USER_SETTINGS} -Dmaven.repo.local=.m2 ${skipTestsOption} ${goals}".toString()

                    if (mavenRuntime.deployer.deployArtifacts) {
                        helm('package', version)
                        helm('publish', '')


                        def files = findFiles(glob: '**/target/docker/**/tmp/docker-build.tar')
                        for (int i = 0; i < files.size(); i++) { //https://issues.jenkins-ci.org/browse/JENKINS-26481
                            def imageNameAndFilePath = imageAndPaths(files[i].path)
                            if (imageNameAndFilePath.isEmpty()) {
                                continue
                            }
                            def filePath = ''
                            for (int j = 1; j < imageNameAndFilePath.size(); j++) {
                                if (fileExists(imageNameAndFilePath[j])) {
                                    filePath = imageNameAndFilePath[j]
                                    break
                                }
                            }
                            if (filePath.isEmpty()) {
                                // there is an image, yet it is not at a standard location... test image?
                                continue
                            }
                            // TODO: DLABS-1694
                            // dockerFingerprintFrom(dockerfile: filePath, image: docker.image(imageNameAndFilePath[0]).id)
                            sh "docker push ${imageNameAndFilePath[0]}"
                        }

                        // TODO: DLABS-1907
                        // buildInfo.number += isSnapshotVersion(version) ? '-Snapshot' : '-Release'
                        // artifactory.publishBuildInfo buildInfo
                    }
                } finally {
                    junit allowEmptyResults: true, testResults: '**/target/*-reports/TEST-*.xml'
                }
            }
        }
    }
}

// Returns the labeled name of the image and candidate relative paths to the Dockerfile
// The caller must select the first relative path for which it finds a file
def static imageAndPaths(path) {
    // The path is expected to look like:
    // mysql_5.6/target/docker/us.gcr.io/dlabs-dev-primary/mysql_5.6/latest/tmp/docker-build.tar
    // <maven project> '/target/docker/' <docker image name as directories> '/' <docker label as a directory> '/tmp/docker-build.tar'
    def targetIndex = path.indexOf('/target/docker/')

    def imagePath = path.substring(targetIndex + '/target/docker/'.length()).minus('/tmp/docker-build.tar')
    if (imagePath.endsWith("/latest")) {
        // convention: test images always have the 'latest' label
        return []
    }
    def separatorBeforeVersion = imagePath.lastIndexOf('/')
    def imageName = imagePath.substring(0, separatorBeforeVersion) + ':' + imagePath.substring(separatorBeforeVersion + 1)

    return [imageName] + locateDockerFile(path.substring(0, targetIndex) + '/src/main/docker/', imageName)
}

// returns potential path for the dockerfile given an image name.
// this will not find the dockerfile for test images on purpose, as they are not published.
// the dockerfile is typically in the src/main/docker/ directory of the project,
// but some projects produce multiple docker images. They are then in sub-directories,
// eg src/main/docker/execution/ or src/main/docker/pno/
static def locateDockerFile(String dockerfileDir, String imageName) {
    return [dockerfileDir + 'Dockerfile', dockerfileDir + simpleImageName(imageName) + '/Dockerfile']
}

// extract the base name from a qualified and tagged image name
// given "us.gcr.io/dlabs-dev-primary/execution:1.1.1-SNAPSHOT",
// returns "execution"
static def simpleImageName(String name) {
    def separatorBeforeName = name.lastIndexOf('/')
    def separatorBeforeTag = name.indexOf(':')
    return name.substring(separatorBeforeName + 1, separatorBeforeTag)
}
