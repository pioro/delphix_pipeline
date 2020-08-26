@Library('delphix_pipeline') _


pipeline {

   environment { 
        DLPX_ENGINE = "myengine1"
        DLPX_USER = "admin"
        DLPX_PASSWORD = "delphix"
        DXTOOLKIT_CONF = "/config/dxtools.conf"
   }
    
   agent {
      label 'oracle19'
   }

   stages {

      stage('Configure Engine') {
         agent {
            docker {
                  image 'pioro/dxtoolkit:2.4.8'
                  args '-u root -w /dxtoolkit -v ${PWD}:/config'
                  label 'master'
            }
         }
         steps {
            engine()
         }
      }

      stage('a nie docker') {
         steps {
            echo 'Hello World'
            sh 'ls -l '
            echo 'Hello World 2nd' 
            sh "echo 'SLON 2' >> plik "
         }
      }

      stage('Discover environment') {
         agent {
            docker {
                  image 'pioro/dxtoolkit:2.4.8'
                  args '-u root -w /dxtoolkit -v ${PWD}:/config'
                  label 'master'
            }
         }
         steps {
            add_environment name:"source"
         }
      }
      
   }
}
