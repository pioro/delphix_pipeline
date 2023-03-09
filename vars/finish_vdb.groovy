def call(Map config=[:]) {
    script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
            steps {
                sh "/dxtoolkit/dx_ctl_db -name '${config.dbname}' -action disable"
            }
        }
    }
}

