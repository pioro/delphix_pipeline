def call(Map config=[:]) {
    script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
            stage('Preparing VDB') {
                sh "/dxtoolkit/dx_provision_vdb -sourcename db19 -dbname '${config.dbname}' -group 'Oracle 19c Virtual Databases' -targetname ${config.dbname} -creategroup -environment singlenode -type oracle -envinst '/u01/app/oracle/product/19.0.0/dbhome_1' -timestamp '${config.bookmarkname}' -redoGroup 3 -redoSize 50"
                sh "/dxtoolkit/dx_ctl_js_container -action create -container_def 'Oracle 19c Virtual Databases,${config.dbname}' -container_name ${config.dbname} -template_name Myapp_template -container_owner admin -dontrefresh"
            }
            stage('Take pre-test snapshot') {
                sh """
                /dxtoolkit/dx_ctl_js_bookmarks -action create -bookmark_time latest -bookmark_name "${config.dbname}-before-test"  -container_name ${config.dbname} -template_name Myapp_template
                /dxtoolkit/dx_ctl_js_bookmarks -bookmark_name "${config.dbname}-before-test" -action share
                """
            }
        }
    }
}

