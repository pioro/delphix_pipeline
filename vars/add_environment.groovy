def call(Map config=[:]) {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    try {
      script {
        /* Requires the Docker Pipeline plugin to be installed */
        docker.image('pioro/dxtoolkit:latest').inside('-u root -w /dxtoolkit -v ${WORKSPACE}:/config') {
          sh "/dxtoolkit/dx_create_env -envname ${config.name} -envtype unix -host ${config.host} -username delphix -authtype password -password delphix -toolkitdir '/home/delphix'"
        }
      }
    } catch (Exception e) {
         echo "Caught ${e.toString()}"
         echo "Stage failed, but we continue"  
    } 
}
