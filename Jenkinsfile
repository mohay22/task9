pipeline {
    agent any

    environment {
        SONAR_SCANNER = "${tool 'SonarQube Scanner'}/bin/SonarQube Scanner" // Ensure correct tool name
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/mohay22/task9.git' // Your repository
            }
        }

        stage('Setup JDK & Gradle') {
            steps {
                script {
                    def javaHome = tool name: 'JDK17', type: 'jdk'
                    env.JAVA_HOME = javaHome
                    if (isUnix()) {
                        env.PATH = "${javaHome}/bin:${env.PATH}"
                    } else {
                        env.PATH = "${javaHome}\\bin;${env.PATH}"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew clean assembleDebug'
                    } else {
                        bat 'gradlew.bat clean assembleDebug'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew testDebugUnitTest jacocoTestReport' // Add JaCoCo
                    } else {
                        bat 'gradlew.bat testDebugUnitTest jacocoTestReport' // Add JaCoCo
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sqp_92b52f4c370b841cd3415826b33e7ebb7df76c41', variable: 'SONAR_LOGIN')]) {
                        script {
                            // Temporarily echo token for debugging (Remove in production)
                            echo "The SonarQube token is: ${SONAR_LOGIN}"

                            // Define SonarQube properties
                            def sonarProps = """
                                sonar.projectKey=task9
                                sonar.projectName=task9
                                sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/testDebugUnitTestCoverage/testDebugUnitTestCoverage.xml
                            """
                            writeFile file: 'sonar-project.properties', text: sonarProps

                            if (isUnix()) {
                                sh "./gradlew sonarqube -Dsonar.login=$SONAR_LOGIN"
                            } else {
                                bat "gradlew.bat sonarqube -Dsonar.login=%SONAR_LOGIN%"
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploy step - Add Firebase or other distribution steps here'
            }
        }
    }

    post {
        always {
            junit '**/build/test-results/testDebugUnitTest/*.xml'
            archiveArtifacts artifacts: 'app/build/outputs/**/*.apk', fingerprint: true

            // Email notification
            emailext (
                subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """<p>Build Status: ${currentBuild.currentResult}</p>
                         <p>Job: ${env.JOB_NAME} #${env.BUILD_NUMBER}</p>
                         <p>Duration: ${currentBuild.durationString}</p>
                         <p>SonarQube Report: <a href='http://localhost:9000/dashboard?id=task9'>View Report</a></p>
                         <p>Check console output at: <a href='${env.BUILD_URL}console'>${env.BUILD_URL}console</a></p>""",
                to: 'taabunicholas@gmail.com', // Replace with your email
                mimeType: 'text/html'
            )
        }
        success {
            echo '✅ Build & Tests Passed Successfully!'
        }
        failure {
            echo '❌ Build or Tests Failed. Check logs for details.'
        }
    }
}