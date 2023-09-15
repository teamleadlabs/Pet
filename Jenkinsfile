pipeline{
    agent{label 'java'}
    tools {
        maven 'maven 3.9.4'
    }
    stages{
        stage('VCS'){
            steps{
                   git url: 'https://github.com/Cloud-and-devops-notes/spring-petclinic-jenkins.git',
                       branch: 'spcbranch'
            }
        }
        stage('Build ') {
            steps {
                sh 'ls'
                sh 'mvn --version'
                sh 'mvn package'
            }
        }
        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('SONAR_CLOUD') {
            sh 'mvn clean package sonar:sonar -Dsonar.organization=sridhardevops  -Dsonar.token=94fa8a4b44707c54a1aae9c4894e0cd8bf0d18d6 -Dsonar.host.url=https://sonarcloud.io -Dsonar.projectKey=sridhardevops006'
            
            }
        }
        }
        stage('nexus'){
            steps{
                
                nexusArtifactUploader artifacts: [[artifactId: 'spring-petclinic', classifier: '', file: '/home/ubuntu/workspace/NEW-NEXUS-SPC-MVN/target/spring-petclinic-3.1.0-SNAPSHOT.jar', type: 'jar']], credentialsId: 'nexus', groupId: 'org.springframework.samples', nexusUrl: '35.153.16.224:8081', nexusVersion: 'nexus3', protocol: 'http', repository: 'maven-snapshots', version: '3.1.0-SNAPSHOT'                                    
            }

        }  
        stage('artifact') {
           steps {
            archiveArtifacts artifacts: '**/target/spring-petclinic-3.1.0-SNAPSHOT.jar',
                         onlyIfSuccessful: true
            junit testResults: '**/surefire-reports/TEST-*.xml'
           }
        } 
        stage('artifact build'){
          steps{
            sh 'docker image build -t spc-mvn .'
            sh 'docker image list'

          } 
        } 
        stage('docker login'){
            steps{   
        withCredentials([string(credentialsId: 'DOCKER_HUB_PASSWORD1',variable: 'SRIDHAR')]) {
         sh 'docker login -u sridhar006 -p $SRIDHAR'  
         }
            }
        }
        stage('docker push image '){
            steps{
                sh 'docker image tag spc-mvn sridhar006/spc-mvn:${BUILD_ID}'
                sh 'docker push sridhar006/spc-mvn:${BUILD_ID}'
                
            }
        }        
        // stage("kubernetes deployment"){
        //    steps{ 
        //   sh 'kubectl apply -f deployement.yaml'
      }

      }  
 
        
    
    







































