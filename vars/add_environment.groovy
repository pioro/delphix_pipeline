def call(Map config=[:]) {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    sh "/dxtoolkit/dx_create_env -envname ${config.name} -envtype unix -host source -username 'oracle' -authtype password -password oracle -toolkitdir '/home/oracle/toolkit' "
}
