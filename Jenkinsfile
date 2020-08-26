@Library('delphix_pipeline') _


pipeline {

   environment { 
        DLPX_ENGINE = "myengine1"
        DLPX_USER = "admin"
        DLPX_PASSWORD = "delphix"
        DXTOOLKIT_CONF = "$WORKSPACE/dxtools.conf"
   }
    
   agent {
      docker { 
         image 'pioro/dxtoolkit:2.4.8' 
         args '-w /dxtoolkit -u root'
         reuseNode true
      }
   }

   stages {
      stage('Create config and check engine') {
         steps {
            sh '/dxtoolkit/dx_config -convert todxconf -text hostname,$DLPX_ENGINE,80,$DLPX_USER,$DLPX_PASSWORD,true,http -configfile dxtools.conf'
            sh '/dxtoolkit/dx_get_appliance'
            script {
    	       engine() 
            }
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
             add_environment slon:"krowa"
         }
      }
      
   }
}
