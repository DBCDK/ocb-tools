#!groovy

def workerNode = "devel11"
def teamSlackNotice = 'team-x-notice'
def teamSlackWarning = 'team-x-warning'

void notifyOfBuildStatus(final String buildStatus) {
    final String subject = "${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    final String details = """<p> Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""
    emailext(
            subject: "$subject",
            body: "$details", attachLog: true, compressLog: false,
            mimeType: "text/html",
            recipientProviders: [[$class: "CulpritsRecipientProvider"]]
    )
}

pipeline {
    agent { label workerNode }

    tools {
        maven "Maven 3"
    }

    triggers {
        pollSCM("H/03 * * * *")
    }

    options {
        timestamps()
    }

    stages {
        stage("Clear workspace") {
            steps {
                deleteDir()
                checkout scm
            }
        }

        stage("Maven build") {
            steps {
                sh "mvn clean verify pmd:pmd -Dmaven.test.failure.ignore=false"
            }
        }

        stage("Publish pmd results") {
            steps {
                step([$class: 'hudson.plugins.pmd.PmdPublisher', checkstyle: 'target/pmd.xml'])
            }
        }

        stage("Package") {
            steps {
                sh "tar -czf target/dist/ocb-tools-1.0.0.tar.gz target/dist/ocb-tools-1.0.0"
            }
        }

        stage("Archive artifacts") {
            steps {
                archiveArtifacts(artifacts: "target/dist/*.tar.gz")
            }
        }

        stage("Build and deploy docker") {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                sh './bin/build-and-push-dockerimage.sh'
            }
        }

    }

    post {
        unstable {
            notifyOfBuildStatus("build became unstable")
        }
        failure {
            notifyOfBuildStatus("build failed")
        }

        success {
            script {
                if (BRANCH_NAME == 'main') {
                    def dockerImageName = readFile(file: 'docker.out')
                    slackSend(channel: teamSlackNotice,
                            color: 'good',
                            message: "${JOB_NAME} #${BUILD_NUMBER} completed, and pushed ${dockerImageName} to artifactory.",
                            tokenCredentialId: 'slack-global-integration-token')
                }
            }
        }
        fixed {
            script {
                if ("${env.BRANCH_NAME}" == 'main') {
                    slackSend(channel: teamSlackNotice,
                            color: 'good',
                            message: "${env.JOB_NAME} #${env.BUILD_NUMBER} back to normal: ${env.BUILD_URL}",
                            tokenCredentialId: 'slack-global-integration-token')
                }
            }
        }
        failure {
            script {
                if ("${env.BRANCH_NAME}".equals('main')) {
                    slackSend(channel: teamSlackWarning,
                        color: 'warning',
                        message: "${env.JOB_NAME} #${env.BUILD_NUMBER} failed and needs attention: ${env.BUILD_URL}",
                        tokenCredentialId: 'slack-global-integration-token')
                }
            }
        }
    }
}
