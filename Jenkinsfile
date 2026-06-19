pipeline {
    agent any

    environment {
        CONTAINER_HOST = 'unix:///var/run/podman.sock'
        SPRING_PROFILES_ACTIVE = 'test'
        DATASOURCE_URL = 'jdbc:postgresql://postgres-ci:5432/skytouch_db'
        DATASOURCE_USERNAME = 'postgres'
        DATASOURCE_PASSWORD = 'postgres'
        JWT_SECRET = 'test-jwt-secret-for-ci-only-must-be-long-enough'
        MAIL_USERNAME = 'test@example.com'
        MAIL_PASSWORD = 'test'
        APP_FRONTEND_URL = 'http://localhost:4174'
        DOCKER_IMAGE = "${env.DOCKER_REGISTRY ?: ''}${env.DOCKER_REGISTRY ? '/' : ''}${env.DOCKER_IMAGE_NAME ?: 'skytouch-app'}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x mvnw && ./mvnw clean test -B'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -DskipTests -B'
            }
        }

        stage('Build Image') {
            steps {
                script {
                    def tag = "${DOCKER_IMAGE}:${env.BUILD_NUMBER}"
                    sh "podman build -t ${tag} -t ${DOCKER_IMAGE}:latest ."
                }
            }
        }

        stage('Push Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'dev'
                    expression { env.TAG_NAME?.startsWith('v') }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-credentials',
                    usernameVariable: 'REGISTRY_USER',
                    passwordVariable: 'REGISTRY_PASS'
                )]) {
                    sh '''
                        if [ -n "$DOCKER_REGISTRY" ]; then
                          echo "$REGISTRY_PASS" | podman login "$DOCKER_REGISTRY" -u "$REGISTRY_USER" --password-stdin
                        else
                          echo "$REGISTRY_PASS" | podman login -u "$REGISTRY_USER" --password-stdin
                        fi
                        podman push ${DOCKER_IMAGE}:latest
                        podman push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                    '''
                    script {
                        if (env.TAG_NAME) {
                            sh "podman tag ${DOCKER_IMAGE}:latest ${DOCKER_IMAGE}:${env.TAG_NAME}"
                            sh "podman push ${DOCKER_IMAGE}:${env.TAG_NAME}"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
