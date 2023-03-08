def call(Map config=[:]) {
    script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
            stage('Preparing VDB') {
                sh "/dxtoolkit/dx_refresh_db -name '${config.dbname}' "
            }
        }
    }
}
