def call(Map config=[:]) {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    try {
      sh "/dxtoolkit/dx_create_env -envname ${config.name} -envtype unix -host ${config.host} -username '${config.username}' -authtype password -password '${config.password}' -toolkitdir '/home/oracle/toolkit' "
    } catch (Exception e) {
         echo "Caught ${e.toString()}"
         echo "Stage failed, but we continue"  
    } 
}
