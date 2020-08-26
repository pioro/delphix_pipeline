def call() {
    environment {
        DXTOOLKIT_CONF = "$WORKSPACE/dxtools.conf"
    }
    sh '/dxtoolkit/dx_config -convert todxconf -text hostname,$DLPX_ENGINE,80,$DLPX_USER,$DLPX_PASSWORD,true,http -configfile dxtools.conf'
    sh '/dxtoolkit/dx_get_appliance'
}
