/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.sendmailfrompojo;

import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * The purpose of this class is to create a fake trust certificate. It just
 * blindly trusts an unsigned https certificate. This is essential to establish
 * a https connection to a server that issues an unverified https
 * certificate. The methods are reimplemented to just blindly return true
 * 
 * 
 */
 public class TrustEveryone implements TrustManager, X509TrustManager {

   public boolean isServerTrusted(X509Certificate[] certs) {
        return true;
    }

    public boolean isClientTrusted(X509Certificate[] certs) {
        return true;
    }

    public void checkServerTrusted(X509Certificate[] certs, 
                                   String authType) throws
                                   java.security.cert.CertificateException {

        return;
    }

    public void checkClientTrusted(X509Certificate[] certs,
                                   String authType) throws
                                   java.security.cert.CertificateException {
        return;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
