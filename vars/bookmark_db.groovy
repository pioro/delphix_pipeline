def call(Map config=[:]) {
    script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
            stage('Preparing VDB') {
                sh "/dxtoolkit/dx_snapshot_db -name '${config.dbname}' -group 'Oracle 19c Virtual Databases'" 
                sh "/dxtoolkit/dx_ctl_bookmarks -name '${config.bookmarkname}' -dbname '${config.dbname}' -action create -timestamp latest"
            }
        }
    }
}

