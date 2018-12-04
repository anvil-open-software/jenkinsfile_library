/*
 * Copyright 2018 Dematic, Corp.
 * Licensed under the MIT Open Source License: https://opensource.org/licenses/MIT
 */

def call(String buildStatus, String stage = '') {
    buildStatus = buildStatus ?: 'SUCCESS'
    if (buildStatus == 'STARTED') {
        return
    }

    def subject = stage + ' ' + env.JOB_NAME.replaceAll('/', ", ") + " [${env.BUILD_NUMBER}]: ${buildStatus}"
    def summary = "${subject}, see the build ${env.BUILD_URL}"

    office365ConnectorSend(message: summary, status: buildStatus, webhookUrl: "${env.GLOBAL_MS_TEAMS_HOOK}")
    slackSend(color: buildStatus == 'SUCCESS' ? 'good' : 'danger', message: summary)

    if (buildStatus == 'FAILED' && !isFeatureBranch()) {
        emailext subject: subject,
                recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                mimeType: 'text/plain',
                body: "project build error is here: ${env.BUILD_URL}"
    }
}
