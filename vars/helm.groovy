/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String command) {
    def make_helm = libraryResource 'com/dematic/labs/jenkinsfile_library/make_helm'
    writeFile file: 'make_helm', text: make_helm
    sh 'chmod u+x make_helm'
    sh './make_helm ' + command
}

def call(String command, String option) {
    def files = findFiles(glob: '**/Chart.yaml')
    for (int i = 0; i < files.size(); i++) { //https://issues.jenkins-ci.org/browse/JENKINS-26481
        String[] projectAndChartName = projectAndName(files[i].path)
        if (projectAndChartName[0].isEmpty()) {
            call(String.join(' ', command, projectAndChartName[1], option))
        } else {
            call(String.join(' ', '--dir', projectAndChartName[0], command, projectAndChartName[1], option))
        }
    }
}

// Returns the maven (sub-)project path and chart name
def static projectAndName(path) {
    // The path is expected to look like:
    // alert_generator/src/main/helm/charts/truck-alert-generator/Chart.yaml
    // src/main/helm/charts/truck-alert/Chart.yaml
    // <maven project> 'src/main/helm/charts/' <chart name as directory> '/Chart.yaml'
    def targetIndex = path.indexOf('src/main/helm/charts/')

    return [path.substring(0, targetIndex), path.substring(targetIndex + 'src/main/helm/charts/'.length()).minus('/Chart.yaml')]
}
