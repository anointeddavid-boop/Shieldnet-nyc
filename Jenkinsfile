pipeline {
    agent any
    environment {
        IMAGE_NAME = 'shieldnet/benefits-api'
        IMAGE_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'shieldnet-api'
        APP_PORT = '8080'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                dir('app') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        stage('Docker Build') {
            steps {
                dir('app') {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                    sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
                }
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push shieldnet/benefits-api:latest
                    '''
                }
            }
        }
        stage('Deploy') {
            steps {
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"
                sh "docker run -d --name ${CONTAINER_NAME} -p ${APP_PORT}:8080 --restart unless-stopped ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }
        stage('Health Check') {
            steps {
                sh 'sleep 15'
                sh "curl -f http://localhost:${APP_PORT}/api/v1/health"
            }
        }
    }
    post {
        success {
            echo 'ShieldNet NYC Pipeline Complete!'
        }
        failure {
            echo 'ShieldNet NYC Pipeline FAILED'
        }
        always {
            sh 'docker image prune -f || true'
        }
    }
}
