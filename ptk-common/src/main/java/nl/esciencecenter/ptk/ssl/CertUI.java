/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.ssl;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.ssl.CertificateStore.CaCertOptions;
import nl.esciencecenter.ptk.ssl.ui.CertificateJDialog;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Static interface to CertificateDialog to add/reject certificates.
 */
@Slf4j
public class CertUI {

    /**
     * Check whether Exception was caused by a certificate error. Check nested exception stack.
     */
    public static CertificateException findCertificateException(Exception e) {
        Throwable cause = e;
        // analyse exception stack:
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof java.security.cert.CertificateException) {
                return (java.security.cert.CertificateException) cause;
            }
        }

        return null;
    }

    public static boolean interactiveImportCertificate(CertificateStore cacerts, String hostname, int port,
                                                       CaCertOptions options) throws Exception {
        boolean result = interactiveImportCertificate(cacerts, hostname, port, null, options);
        return result;
    }

    /**
     * Interactive ask to accept Certificate or not. If accepted the Certificate Store will contain
     * the accepted cerificate.
     */
    private static boolean interactiveImportCertificate(CertificateStore certStore, String host, int port,
                                                        String optPassphrase, CaCertOptions options) throws Exception {
        // use defaults;

        if (options == null)
            options = certStore.getOptions();

        if (port <= 0)
            port = 443;

        // connect
        SSLContext context = certStore.createSSLContext("SSLv3");

        // Create SSL Socket, but do not start handshake:
        SSLSocket socket = SslUtil.createSSLSocket(context, host, port, 10000, false);

        try {
            log.debug("[Starting SSL handshake]");
            socket.startHandshake();
            socket.close();
            log.debug("- No errors: certificate(s) already trusted.");
            return true;
        } catch (Exception e) {
            Exception certificateException = findCertificateException(e);

            log.error("SSL Handshake failed:" + e.getMessage());
            log.error(e.getMessage(), e);

            if (certificateException == null) {
                throw e;
            }
        }

        // get the key chain!
        X509Certificate[] chain = certStore.getSavingTrustManager().getChain();

        if (chain == null) {
            log.warn("Could not obtain server certificate chain");
            return false;
        }

        log.debug("Server sent " + chain.length + " certificate(s)");

        String chainMessage = "";// sslerrorMessage;

        int nrKeys = chain.length;

        String[] keySubjects = new String[nrKeys];
        String[] keyIssuers = new String[nrKeys];

        log.debug("Total key chain length={}", chain.length);

        for (int i = 0; i < nrKeys; i++) {
            X509Certificate cert = chain[i];
            keySubjects[i] = cert.getSubjectDN().toString();
            keyIssuers[i] = cert.getIssuerDN().toString();

            chainMessage += "--- Certificate [" + (i + 1) + "] ---\n";
            chainMessage += CertUtil.toString(cert, "    ", "\n");
        }

        // String options[]={"yes","no","temporary"};
        int opt = 0;

        if (options.interactive == true) {
            log.debug("Asking interactive for:{}", host);

            opt = CertificateJDialog.showDialog("Certificate Received from: " + host + "\n" + "Accept certificate ?",
                    chainMessage);

            if ((opt == CertificateJDialog.NO) || (opt == CertificateJDialog.CANCEL))
                return false;
        } else if (options.alwaysAccept == false) {
            log.debug("Rejecting Cert. Interactive==false and alwaysAccept==false for host:{}", host);
            return false;
        } else {
            log.debug("Accepting Certificate. Interactive==false and alwaysAccept==true for host:{}", host);
            // continue
        }

        /**
         * Add complete chain. If the certificate already exists with a similar alias, the key will
         * be overwritten.
         */
        for (int k = 0; k < nrKeys; k++) {
            X509Certificate cert = chain[k];
            String alias = createServerKeyID(host, port, k); // host + "-" + (k
            // + 1);
            log.debug("+++ Adding Key: {} +++ \n", alias);
            log.debug(" -  Subject: {}", keySubjects[k]);
            log.debug(" -  Issuer : {}", keyIssuers[k]);

            certStore.addCertificate(alias, cert, false);
        }

        // interactive save
        if (options.interactive == true) {
            if (opt != CertificateJDialog.TEMPORARY) {
                log.debug("Accepting Certificate. Interactive==false and alwaysAccept==true for host:{}",
                        host);
                certStore.saveKeystore();
            }
        }
        // not interactive:
        else if (options.storeAccepted == true) {
            log.debug("Saving keystore after (default) accepting certificate from host:{}", host);
            certStore.saveKeystore();
        }

        // ===
        // bug: must recreate/reinitialize trustManager, when
        // updating keyStore.
        // FIXED in addCertificatecertStore.getTrustManager(true);
        // ===

        return true;
    }

    private static String createServerKeyID(String host, int port, int k) {
        return "" + host + ":" + port + "-" + k;
    }

}
