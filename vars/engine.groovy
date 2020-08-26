def call() {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    pipeline {
      agent {
        docker {
            image 'pioro/dxtoolkit:2.4.8'
            args '-w /dxtoolkit -u root'
            reuseNode true
        }
      }
      stages {
        stage('Configure Engine') {
          steps {
            echo "This is engine configuration"
            sh '/dxtoolkit/dx_config -convert todxconf -text hostname,$DLPX_ENGINE,80,$DLPX_USER,$DLPX_PASSWORD,true,http -configfile dxtools.conf'
            sh '/dxtoolkit/dx_get_appliance'
          }
        }
      }
    }
}
