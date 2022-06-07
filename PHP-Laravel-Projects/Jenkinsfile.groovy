pipeline {
    agent any

    stages {
        stage('Git Pull Changes') {
            steps {
                echo 'Pulling Github Changes'
                echo 'git pull'
            }
        }
        stage('Deployment') {
            steps {
                echo 'Deployment is on making...'
                echo 'php artisan config:cache'
            }
        }
    }
}
