pipeline {
    agent any
    // environmental vars
    environment {
        DEPLOYMENT_DOMAIN    = 'https://poc.example.app'
        PROJECT              = 'example-web-poc'
        PROJECT_NEW_VERSION  = 'example-web-poc-new-version'
        PROJECT_PREV_VERSION = 'example-web-poc-prev-version'
    }
    stages {
        stage('notifyDevelopers') {
            // This simply sends notifications to developers in Telegram to notify the start of the Job
            steps{
                withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER}\n<b>Build Status</b>: Pending ⌛\nThe result will be delivered to you when Jenkins Job is Done :D"
                ''')
                }
            }
        }
        stage('Build') {
            //This builds the NodeJS
            steps {
                git branch: 'main', credentialsId: 'xxxx-xxxx-xxxx-xxxx', url: 'git@gitlab.example.com:/omaraladin/example-api'
                sh 'npm install'
                sh 'rm -rf ${PROJECT_NEW_VERSION}.tar.gz'
                //Creating a sendable artifact of the NodeJS application
                sh 'tar czf ${PROJECT_NEW_VERSION}.tar.gz *'
            }
        }
        stage('Unit Test') {
            steps {
                //Here the developer should be tasked to prepare some unit-testing artifacts and provide as an input to Ops guys, then he/she applies in the pipeline stages
                echo "Someone should finish me"
            }
        }
        stage('DeployApi2') {
            //This sends and deploy to NodeJS
            steps {
                echo "Uncomment my lines when finish"
                //Deploy App to servers, to find more go to filesystem /var/lib/jenkins/ansible_playbooks in this Controller node
                //include this inside Ansible Playbook sh 'scp -i /var/lib/jenkins/.ssh/id_dev-jenkins -P 2812 -o StrictHostKeyChecking=no ${PROJECT_NEW_VERSION}.tar.gz root@node.example.com:/root/'   
                //Now it is better to put the deployment process logic to an ansible playbook
                sh 'ansible-playbook -i ${ANSIBLE_PATH}/inventory/devNode.hosts --private-key=${ANSIBLE_DEV_PRIVATE_KEY} ${ANSIBLE_PATH}/dev_playbooks/deployNodeDev.yaml'
            }
        }        
    }
    //Post-build control, deciding what to do in each Pipeline exit status
    post {
        //Case Succeeded
        success {
            withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER}\n<b>Build Status:</b>: SUCCESS ✅ \n<b>Check your changes at with an Incognito tab at: </b>\n${DEPLOYMENT_DOMAIN}"
                ''')
            }
        }
        //Case Failed
        failure {
            withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {            
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project</b> : ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER} \n<b>Build Status:</b> FAILED ❌"
                    ''')            
            }
        }
        //Case Unstable
        unstable {
            withCredentials([string(credentialsId: 'testChatId', variable: 'CHATID'), string(credentialsId: 'botToken', variable: 'TOKEN')]) {            
                sh ('''
                    curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage -d chat_id=${CHATID} -d parse_mode="HTML" -d text="<b>Project:</b> ${JOB_NAME}\n<b>Build NO:</b> ${BUILD_NUMBER}\n<b>Build Status:</b> UNSTABLE ❗ \nReview your changes at:\n${DEPLOYMENT_DOMAIN}"
                ''')
            }
        }
    }
}
