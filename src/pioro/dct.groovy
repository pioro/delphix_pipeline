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





    def runGET(String url) {

        def get = new URL(this.dct_server + url).openConnection();
        get.addRequestProperty("Authorization", "apk " + this.dct_auth);

        def outst
        def getRC = get.getResponseCode();
        if (getRC.equals(200)) {
            outst = get.getInputStream().getText();
        } else {
            throw new Exception("This is an in runGet. RC: " + getRC + " Error: " + get.getErrorStream().getText())
        }

        return outst;

    }


    def getUsers() {
        return this.runGet('/v2/management/accounts')
    }

    def testerr() {
        throw new Exception("This is an error")
    }

}