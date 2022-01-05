def call() {
    script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
            stage('Configure connection to engine') {
                sh '/dxtoolkit/dx_config -convert todxconf -text hostname,$DLPX_ENGINE,80,$DLPX_USER,$DLPX_PASSWORD,true,http -configfile /config/dxtools.conf'
                sh '/dxtoolkit/dx_get_appliance -configfile /config/dxtools.conf'
            }
        }
    }
}
