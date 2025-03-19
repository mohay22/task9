pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                // Checkout your Android app repo
                git 'https://github.com/yourusername/your-android-app-repo.git'
                echo 'Checked out code'
            }
        }
        stage('Build') {
            steps {
                // Build your Android app
                sh './gradlew clean build'
                echo 'Build completed'
            }
        }
        stage('Test') {
            steps {
                // Run tests
                sh './gradlew test'
                echo 'Tests completed'
            }
        }
    }
    post {
        always {
            // Always notify, regardless of success or failure
            echo 'Pipeline finished, sending notifications'
            // Archive build artifacts (e.g., APK) for reference
            archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', allowEmptyArchive: true
        }
        success {
            // Email notification on success
            emailext (
                subject: "SUCCESS: ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                body: """
                The build for ${env.JOB_NAME} (#${env.BUILD_NUMBER}) succeeded!
                Duration: ${currentBuild.durationString}
                Check details: ${env.BUILD_URL}
                APK available at: ${env.BUILD_URL}artifact/app/build/outputs/apk/
                """,
                to: 'your.email@example.com',
                mimeType: 'text/plain',
                attachLog: true // Attach build log for debugging
            )
            // Slack notification on success
            slackSend (
                color: 'good',
                message: """
                SUCCESS: ${env.JOB_NAME} Build #${env.BUILD_NUMBER} completed successfully!
                Duration: ${currentBuild.durationString}
                <${env.BUILD_URL}|View Build> | <${env.BUILD_URL}artifact/app/build/outputs/apk/|Download APK>
                """
            )
        }
        failure {
            // Email notification on failure
            emailext (
                subject: "FAILURE: ${env.JOB_NAME} Build #${env.BUILD_NUMBER}",
                body: """
                The build for ${env.JOB_NAME} (#${env.BUILD_NUMBER}) failed.
                Duration: ${currentBuild.durationString}
                Check details: ${env.BUILD_URL}
                Console output: ${env.BUILD_URL}console
                Please investigate the issue.
                """,
                to: 'your.email@example.com',
                mimeType: 'text/plain',
                attachLog: true // Attach build log for debugging
            )
            // Slack notification on failure
            slackSend (
                color: 'danger',
                message: """
                FAILURE: ${env.JOB_NAME} Build #${env.BUILD_NUMBER} failed!
                Duration: ${currentBuild.durationString}
                <${env.BUILD_URL}|View Build> | <${env.BUILD_URL}console|View Console>
                """
            )
        }
    }
}