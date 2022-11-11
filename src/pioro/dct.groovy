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




    def get() {

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = [ new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
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
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        def get = new URL("https://uvo1bgogbnn1tfz3ifu.vm.cld.sr/v2/management/accounts").openConnection();

        get.addRequestProperty("Authorization", "apk 36.tTNnxlYPZZtGupmYPKTEXJ3Rlt8UR1le6Z8ZKpxblD81SeVh9siwAOCLZUvIvJjM");

        def getRC = get.getResponseCode();
        println(getRC);
        if (getRC.equals(200)) {
            println(get.getInputStream().getText());
        }

    }
