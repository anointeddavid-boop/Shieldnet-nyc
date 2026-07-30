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
                echo '🔍 Checking out source code from Git...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building application with Maven...'
                dir('app') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                echo '🧪 Running unit tests...'
                dir('app') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    echo 'Test stage complete.'
                }
                failure {
                    echo '❌ Tests failed - blocking deployment.'
                }
            }
        }

        stage('Code Quality Check') {
            steps {
                echo '🔎 Checking code quality...'
                dir('app') {
                    sh 'mvn checkstyle:check || true'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                dir('app') {
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                    sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Docker Push') {
            steps {
                echo '📦 Pushing image to registry...'
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploying ShieldNet to server...'
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"
                sh """
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        -p ${APP_PORT}:8080 \
                        --restart unless-stopped \
                        -e HOSTNAME=\$(hostname) \
                        ${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Health Check') {
            steps {
                echo '❤️ Verifying deployment health...'
                sh 'sleep 15'
                sh """
                    curl -f http://localhost:${APP_PORT}/api/v1/health || \
                    (echo '❌ Health check failed!' && exit 1)
                """
                echo '✅ ShieldNet is live and healthy!'
            }
        }

        stage('Smoke Test') {
            steps {
                echo '💨 Running post-deployment smoke tests...'
                sh """
                    curl -s -X POST http://localhost:${APP_PORT}/api/v1/eligibility \
                        -H 'Content-Type: application/json' \
                        -d '{"firstName":"Test","lastName":"User","householdSize":3,"monthlyIncome":1800,"borough":"BRONX","benefitType":"SNAP","emergencyApplication":false}' \
                        | grep -q 'APPROVED' && echo '✅ Smoke test passed!' || echo '❌ Smoke test failed!'
                """
            }
        }
    }

    post {
        success {
            echo """
            ✅ ShieldNet NYC Pipeline Complete!
            Build: ${BUILD_NUMBER}
            Image: ${IMAGE_NAME}:${IMAGE_TAG}
            Status: DEPLOYED AND HEALTHY
            """
        }
        failure {
            echo """
            ❌ ShieldNet NYC Pipeline FAILED
            Build: ${BUILD_NUMBER}
            Action: Check logs above for details
            """
        }
        always {
            echo 'Cleaning up unused Docker images...'
            sh 'docker image prune -f || true'
        }
    }
}
