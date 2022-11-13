package pioro;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


class dct {

    String dct_server;
    String dct_auth;

    dct(dct_server, dct_auth) {
        this.dct_server = dct_server;
        this.dct_auth = dct_auth;
        this.setupSSL();
    }

    @NonCPS
    def setupSSL() {

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = [ new X509TrustManager() {
                @NonCPS
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @NonCPS
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @NonCPS
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        ];

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @NonCPS
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    }


    def get() {

        this.setupSSL()

        def get = new URL("https://uvo1bgogbnn1tfz3ifu.vm.cld.sr/v2/management/accounts").openConnection();

        get.addRequestProperty("Authorization", "apk 36.tTNnxlYPZZtGupmYPKTEXJ3Rlt8UR1le6Z8ZKpxblD81SeVh9siwAOCLZUvIvJjM");

        def outst
        def getRC = get.getResponseCode();
        println(getRC);
        if (getRC.equals(200)) {
            outst = get.getInputStream().getText();
        } else {
            outst = "dupa"
        }

        return outst

    }





    def runGet(String url) {

        def get = new URL(this.dct_server + url).openConnection();
        get.addRequestProperty("Authorization", "apk " + this.dct_auth);

        def outst
        def getRC = get.getResponseCode();
        if (getRC.equals(200)) {
            outst = get.getInputStream().getText();
        } else {
            throw new Exception("runGet Error. RC: " + getRC + " Error: " + get.getErrorStream().getText())
        }

        return outst;

    }


    def getUsers() {
        return this.runGet('/v2/management/accounts')
    }


    def createUser(String username, boolean admin) {
        String payload = '{ "username": ' + username + ',"generate_api_key": true,"is_admin": true }';

    }

    def testerr() {
        throw new Exception("This is an error")
    }


    def runPost(String url, String payload) {
        URL url = new URL(this.dct_server + url);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty ("Authorization", "apk " + this.dct_auth);
        conn.addRequestProperty ("Content-Type", "application/json");
        conn.addRequestProperty ("Accept", "application/json");
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(payload);
        writer.flush();
        writer.close()
        String output;
        def getRC = conn.getResponseCode();
        if (getRC.equals(201) or getRC.equals(200)) {
            output = conn.getInputStream().getText();
        } else {
            output = conn.getErrorStream().getText();
            throw new Exception("runPost Error. RC: " + getRC + " Error: " + output)  
        }
        return output
    }


    def wait_for_job(String job_id) {
        boolean jobrunning = true
        def jsonSlurper = new JsonSlurper();
        def job_obj;
        while(jobrunning) {
            job_obj = jsonSlurper.parseText(runGet('/v2/jobs/' + job_id))
            if ((job_obj.status.equals('RUNNING')) || (job_obj.status.equals('STARTED'))) {
                sleep(10)
            } else {
                jobrunning = false;
            }
        }
        return job_obj
    }

    def create_or_refresh_vdb(String source, String name, String environment_name) {

        vdb_obj = """
        {
            "source_data_id":"$source",
            "name":"$name",
            "database_name":"$name",
            "environment_id":"$environment_name"
        }
        """

        def jsonSlurper = new JsonSlurper()

        payload = """
        {
            "filter_expression": "name EQ '$name'"
        }
        """

        ret_json = this.runPost('/v2/vdbs/search', payload);
        ret_object = jsonSlurper.parseText(ret_json)

        if (ret_object.response_metadata.total == 1) {
            println("mama baze")
            ret_json = runPost("/v2/vdbs/$name/refresh_by_snapshot", '{}');
        } else {
            def generator = new JsonGenerator.Options()
                .excludeNulls()
                .build()
            ret_json = this.runPost('/v2/vdbs/provision_by_snapshot', vdb_obj);
        }

        
        ret_object = jsonSlurper.parseText(ret_json)
        println(ret_json)

        job_stat = this.wait_for_job(ret_object.job.id)

        if (job_stat.status.equals('COMPLETED')) {
            println("vdb created or refreshed")
        } else {
            println("Error in job")
            println(job_stat)
            throw new Exception("create_or_refresh_vdb Error.  Error: " + job_stat)  
        }


    }


}