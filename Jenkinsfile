pipeline {
    agent any

    environment {
        APP_NAME = 'shieldnet-benefits-api'
        IMAGE_NAME = 'shieldnet/benefits-api'
        IMAGE_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'shieldnet-api'
        APP_PORT = '8080'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code from Git...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building application with Maven...'
                dir('app') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                dir('app') {
                    sh 'mvn test'
                }
            }
        }

    post {
        success {
            echo 'ShieldNet NYC Pipeline Complete and DEPLOYED!'
        }
        failure {
            echo 'ShieldNet NYC Pipeline FAILED'
        }
        always {
            sh 'docker image prune -f || true'
        }
    }
}
