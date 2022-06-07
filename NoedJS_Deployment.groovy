pipeline {
    agent any
    environment {
        DEPLOYMENT_DOMAIN    = 'https://poc.examplevpn.app'
        PROJECT              = 'example-web-poc'
        PROJECT_NEW_VERSION  = 'example-web-poc-new-version'
        PROJECT_PREV_VERSION = 'example-web-poc-prev-version'
    }
    stages {
        stage('notifyDevelopers') {
            // This simply sends notifications to developers in Telegram
            steps{
                withCredentials([string(credentialsId: 'chatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME} \
                    <b>Build NO:</b> : ${BUILD_NUMBER} \
                    <b>Build Status</b>: Pending &#8987; \
                    The result will be delivered to you when Jenkins Job is Done :D"
                ''')
                }
            }
        }
        stage('Build') {
            //Thi builds the NodeJS API
            steps {
                git branch: 'main', credentialsId: '323bb75a-97c0-4f32-809d-c8c4748b584f', url: 'git@192.168.0.201:/root/example-api'
                sh 'npm install'
                sh 'rm -rf ${PROJECT_NEW_VERSION}.tar.gz'
                sh 'tar czf ${PROJECT_NEW_VERSION}.tar.gz *'
            }
        }
    }
    post {
        success {
            withCredentials([string(credentialsId: 'chatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME} \
                    <b>Build NO:</b> : ${BUILD_NUMBER} \
                    <b>Build Status</b>: SUCCESS &#9989; \
                    Check your changes at with an Incognito tab at: \
                    ${DEPLOYMENT_DOMAIN}"
                ''')
            }
            echo 'This has worked well!'
            echo "${DEPLOYMENT_DOMAIN}"
            echo "${PROJECT}"
            echo "${PROJECT_NEW_VERSION}"
            echo "${PROJECT_PREV_VERSION}"
        }
        failure {
            withCredentials([string(credentialsId: 'chatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {            
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME} \
                    <b>Build NO:</b> : ${BUILD_NUMBER} \
                    <b>Build Status</b>: FAILED &#10060;"
                ''')            
            }
        }
        unstable {
            withCredentials([string(credentialsId: 'chatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {            
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME} \
                    <b>Build NO:</b> : ${BUILD_NUMBER} \
                    <b>Build Status</b>: UNSTABLE &#10071;
                    Review your changes at: \
                    ${DEPLOYMENT_DOMAIN}"
                ''')
            }
        }
    }
}
