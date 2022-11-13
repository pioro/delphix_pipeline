def call(Map dct=[:]) {
    script {
        dct.dctobj.runGET('/v2/management/accounts') 
    }
}