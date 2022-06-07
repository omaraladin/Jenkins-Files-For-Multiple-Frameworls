pipeline {
    agent any
    environment {
        DEPLOYMENT_DOMAIN    = 'https://poc.example.app'
        PROJECT              = 'example-web-poc'
        PROJECT_NEW_VERSION  = 'example-web-poc-new-version'
        PROJECT_PREV_VERSION = 'example-web-poc-prev-version'
    }
    stages {
        stage('notifyDevelopers') {
            // This simply sends notifications to developers in Telegram
            steps{
                withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER}\n<b>Build Status</b>: Pending ⌛\nThe result will be delivered to you when Jenkins Job is Done :D"
                ''')
                }
            }
        }
        stage('Build') {
            //Thi builds the NodeJS API
            steps {
                git branch: 'main', credentialsId: 'xxxx-xxxx-xxxx-xxxx', url: 'git@gitlab.example.com:/omaraladin/example-api'
                sh 'npm install'
                sh 'rm -rf ${PROJECT_NEW_VERSION}.tar.gz'
                sh 'tar czf ${PROJECT_NEW_VERSION}.tar.gz *'
            }
        }
    }
    post {
        success {
            withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER}\n<b>Build Status:</b>: SUCCESS ✅ \n<b>Check your changes at with an Incognito tab at: </b>\n${DEPLOYMENT_DOMAIN}"
                ''')
            }
        }
        failure {
            withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {            
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER} \n<b>Build Status:</b> FAILED ❌"
                    ''')            
            }
        }
        unstable {
            withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {            
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project:</b> ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER}\n<b>Build Status:</b> UNSTABLE ❗ \nReview your changes at:\n${DEPLOYMENT_DOMAIN}"
                ''')
            }
        }
    }
}
