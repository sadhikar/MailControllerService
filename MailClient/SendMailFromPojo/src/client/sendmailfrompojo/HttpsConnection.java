/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.sendmailfrompojo;

import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 *  This class takes care of setting up an HTTPS connection with the generated certificates.
 *
 * 
 */
public class HttpsConnection {
    /**
     * This method attempts to establish an Https connection to a specified servlet url.
     *
     * @param servletUrl is the url of the servlet to which an https connection is to be established.
     *
     * @return a URLConnection object with the connection set.
     * 
     */
    public static URLConnection getConnection(String servletUrl) {
      try {

            /*
             * Create a fake implementation of the HostNameVerifier interface
             * for use with HttpsURLConnection later
             */
            HostnameVerifier verifier = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                } //end verify
            };

            /**
             *  Create an array of TrustManager for use with the init() method
             *  of the SSLContext
             */
            TrustManager[] trustAllCerts = new TrustManager[1];
            /*
             * Create an instance of the fake trust-manager TrustEveryone ,
             */
            TrustManager trustManager = new TrustEveryone();
            /*
             * Add this to the trust manager.
             */
            trustAllCerts[0] = trustManager;
            /*
             * Create an instance of SSL context in order to connect through
             * Https
             */
            SSLContext sslContext = SSLContext.getInstance("SSL");
            /*
             * Initialize the SSLContext with the fake trust manager created
             * earlier. Argument KeyManager and SecureRandom aren't needed for
             * this fake SSL context
             */
            sslContext.init(null, trustAllCerts, null);
            /*
             * Set the SSL factory of this HttpsURLConnection to the fake
             * SSL context created above
             */
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
                                                          .getSocketFactory());
            /*
             * Set the HostNameVerifier of this HttpsURLConnection to the
             * fake HostNameVerifier created earlier
             */
            HttpsURLConnection.setDefaultHostnameVerifier(verifier);

            /*
             * Attempt to establish a conncetion to the servlet URL
             */
            URL url = new URL(servletUrl);
            URLConnection connection = url.openConnection(Proxy.NO_PROXY);
            /*
             * This connection does both input and output; Therefore set the
             * appropriate flags to true.
             */
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            return connection;
      }
      catch(Exception exception){
          System.out.println(exception);
          return null;
      }
    }
}
