pipeline {
    agent any  // Utiliza cualquier agente disponible

    stages {
        stage('Checkout') {  // Etapa para gestionar el repositorio
            steps {
                script {
                    echo 'Actualizando o clonando repositorio en VM1'  // Mensaje informativo
                    // Verifica si el directorio 'dds-deploy' existe
                    if (fileExists('dds-deploy')) {
                        dir('dds-deploy') {  // Cambia al directorio si existe
                            sh "git pull origin main"  // Actualiza el repositorio
                        }
                    } else {
                        // Si no existe, clona el repositorio desde GitHub
                        sh "git clone https://github.com/VOrtigosa/dds-deploy.git"
                    }
                }
            }
        }

        stage('Start Agent') {
            steps {
                // Encender el agente
                sh '''
                    # Descargar el archivo agent.jar
                    curl -sO http://192.168.184.130:8080/jnlpJars/agent.jar
                    
                    # Iniciar el agente de Jenkins
                    java -jar agent.jar -url http://192.168.184.130:8080/ -secret 009e3ebe3700f5488028589d961010367cda907258b11925238f2b54d1198fea -name "VM2 - CentOS - Producción" -workDir "/home/produ"
                    '''
                
                // Esperar un tiempo fijo para que el agente se inicie
                sleep(30) // Esperar 30 segundos
            }
        }
        
        stage('Checkout Remoto') {
            agent { label 'minikubeJava' }
            steps {
                script {
                    echo 'Actualizando o clonando repositorio en VM2'  // Mensaje informativo
                    // Verifica si el directorio 'dds-deploy' existe
                    if (fileExists('dds-deploy')) {
                        dir('dds-deploy') {  // Cambia al directorio si existe
                            sh "git pull origin main"  // Actualiza el repositorio
                        }
                    } else {
                        // Si no existe, clona el repositorio desde GitHub
                        sh "git clone https://github.com/VOrtigosa/dds-deploy.git"
                    }
                }
            }
        }

        stage('SonarQube Analysis') {  // Etapa para análisis con SonarQube
            steps {
                script {
                    def mvn = tool 'Default Maven'  // Obtiene la instalación de Maven
                    echo 'Testing with Maven'
                    dir('dds-deploy') {
                        withSonarQubeEnv('sonarQube') {  // Nombre de la configuración de SonarQube en Jenkins
                            sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=com.ejemplo:mi-proyecto"  // Conexión con el proyecto
                        }
                    }
                }
            }
        }

        stage('Test') {  // Etapa para ejecutar pruebas
            steps {
                script {
                    def mvn = tool 'Default Maven'  // Obtiene la instalación de Maven
                    dir('dds-deploy') {
                        sh "${mvn}/bin/mvn clean test" // Corre los tests
                    }
                }
            }
        }
        
        stage('Docker Build') {  // Etapa de Build
            agent { label 'minikubeJava' }
            steps {
                script {
                    echo 'Building Docker Image'
                    dir('dds-deploy') {
                        sh 'ls -l'  // Muestra el contenido del directorio
                        sh 'docker build -t vortigosa/app-libros:lts .'
                    }
                }
            }
        }

        stage('Login to Docker') {
            agent { label 'minikubeJava' }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-login', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                    }
                }
            }
        }

        stage('Docker Push') {  // Etapa para subir la imagen a Docker
            agent { label 'minikubeJava' }
            steps {
                script {
                    echo 'Pushing Docker Image'
                    sh '''
                        docker tag vortigosa/app-libros:lts vortigosa/app-libros:lts
                        docker push vortigosa/app-libros:lts
                    '''
                }
            }
        }

        stage('Deploy') {  // Etapa de Deploy
            agent { label 'minikubeJava' }
            steps {
                script {
                    echo 'Ejecutando deploy'
                    dir('/home/produ/workspace'){
                        sh '''
                        alias kubectl="minikube kubectl --" 
                        kubectl rollout restart deployment app-libros-deployment
                        '''
                    }
                }
            }
        }
    }
}
