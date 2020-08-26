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
      stage('Discover environment') {
         steps {
            sh '/dxtoolkit/dx_create_env -envname "source" -envtype unix -host source -username "oracle" -authtype password -password oracle -toolkitdir "/home/oracle/toolkit" '
         }
      }
      
   }
}
