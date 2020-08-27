def call(Map config=[:]) {
    try {
      sh "dx_ctl_dsource -action create -group 'Untitled' -creategroup -dsourcename '${config.dbname}'  -type oracle -sourcename '${config.dbname}' -sourceinst '/u01/app/oracle/product/19c/db1' -sourceenv '${config.env}' -source_os_user 'oracle' -dbuser delphixdb -password delphixdb -logsync yes"
    } catch (Exception e) {
         echo "Caught ${e.toString()}"
         echo "Stage failed, but we continue"  
    } 
}
