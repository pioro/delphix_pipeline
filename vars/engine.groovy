def call() {
    environment {
        DXTOOLKIT_CONF = "${env.WORKSPACE}/dxtools.conf"
    }
    echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL} ${env.WORKSPACE}"
    sh '/dxtoolkit/dx_config -convert todxconf -text hostname,$DLPX_ENGINE,80,$DLPX_USER,$DLPX_PASSWORD,true,http -configfile dxtools.conf'
    sh '/dxtoolkit/dx_get_appliance'
}
