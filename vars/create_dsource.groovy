def call(Map config=[:]) {
    try {
      script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
          sh "/dxtoolkit/dx_ctl_dsource -type oracle -sourcename '${config.dbname}' -sourceinst /u01/app/oracle/product/19.0.0/dbhome_1 -sourceenv '${config.env}' -source_os_user delphix -dbuser delphix -password delphix -group 'Oracle 19c Sources' -logsync yes -creategroup -dsourcename ${config.dbname} -action create"
        }
      }
    } catch (Exception e) {
         echo "Caught ${e.toString()}"
         echo "Stage failed, but we continue"  
    } 
}
