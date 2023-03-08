def call(Map config=[:]) {
    script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
            stage('Preparing VDB') {
                sh "/dxtoolkit/dx_provision_vdb -sourcename db19 -dbname '${config.dbname}' -group 'Oracle 19c Virtual Databases' -targetname ${config.dbname} -creategroup -environment singlenode -type oracle -envinst '/u01/app/oracle/product/19.0.0/dbhome_1'"
            }
            stage("Set dataset") {
                when { expression { ${config.bookmark} != 'full' } }
                steps {
                    sh "/dxtoolkit/dx_ctl_js_container -action restore -timestamp 1pctdata  -container_name $DLPX_CONTAINER -template_name $DLPX_TEMPLATE"
                }
            }
        }
    }
}

