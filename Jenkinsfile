@Library('delphix_pipeline') _


pipeline {

   environment { 
        DLPX_ENGINE = "myengine1"
        DLPX_USER = "admin"
        DLPX_PASSWORD = "delphix"
        DXTOOLKIT_CONF = "$WORKSPACE/dxtools.conf"
   }
    
   agent {
      label 'master'
   }

   stages {

      stage('Discover environment 0') {
         steps {
            engine()
         }
      }

      stage('a nie docker') {
         agent {
            label 'oracle19'
         }
         steps {
            echo 'Hello World'
            sh 'ls -l '
            echo 'Hello World 2nd' 
            sh "echo 'SLON 2' >> plik "
         }
      }

      stage('Discover environment') {
         steps {
            echo 'Hello World'
            sh 'ls -l '
         }
      }
      
   }
}
