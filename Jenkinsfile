pipeline {
    agent any

    environment {
        SPRING_PROFILES_ACTIVE = 'test'
        DATASOURCE_URL = "jdbc:postgresql://postgres-ci:5432/${env.POSTGRES_DB}"
        DATASOURCE_USERNAME = "${env.POSTGRES_USER}"
        DATASOURCE_PASSWORD = "${env.POSTGRES_PASSWORD}"
        JWT_SECRET = "${env.JWT_SECRET}"
        MAIL_USERNAME = "${env.MAIL_USERNAME}"
        MAIL_PASSWORD = "${env.MAIL_PASSWORD}"
        APP_FRONTEND_URL = 'http://localhost:4174'
        DOCKER_IMAGE = "${env.DOCKER_IMAGE_NAME ?: 'ochogwuprince/skytouch-app'}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        skipDefaultCheckout(true)
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

        stage('Build & Push Image') {
            when {
                changeset "src/**"  // ← only build if src files changed
            }
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'docker-registry-credentials',
                        usernameVariable: 'REGISTRY_USER',
                        passwordVariable: 'REGISTRY_PASS'
                )]) {
                    sh '''
                        docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} -t ${DOCKER_IMAGE}:latest .
                        echo "$REGISTRY_PASS" | docker login -u "$REGISTRY_USER" --password-stdin
                        docker push ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                    '''
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