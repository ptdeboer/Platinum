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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import nl.esciencecenter.ptk.ssl.CertificateStore.CaCertOptions;
import nl.esciencecenter.ptk.util.logging.PLogger;

/**
 * Static interface to CertificateDialog to add/reject certificates.
 */
public class CertUI {

    private static PLogger logger = PLogger.getLogger(CertUI.class);

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

        String sslErrorMessage = null;

        // Create SSL Socket, but do not start handshake: 
        SSLSocket socket = SslUtil.createSSLSocket(context, host, port, 10000, false);

        try {
            logger.debugPrintf("--- Starting SSL handshake...\n");
            socket.startHandshake();
            socket.close();
            logger.debugPrintf("<<< No errors, certificate is already trusted\n");
            return true;
        } catch (Exception e) {
            Exception certificateException = findCertificateException(e);

            sslErrorMessage = e.getMessage();
            logger.logException(PLogger.DEBUG, e, "<<< Initial SSL Handshake failed. Exception=%s\n", sslErrorMessage);
            logger.debugPrintf("Certificate Exception= %s\n", certificateException);

            if (certificateException == null)
                throw e;
        }

        // get the key chain!
        X509Certificate[] chain = certStore.getSavingTrustManager().getChain();

        if (chain == null) {
            logger.warnPrintf("Could not obtain server certificate chain\n");
            return false;
        }

        logger.debugPrintf("Server sent " + chain.length + " certificate(s):\n");

        String chainMessage = "";// sslerrorMessage;

        int nrKeys = chain.length;

        String keySubjects[] = new String[nrKeys];
        String keyIssuers[] = new String[nrKeys];

        logger.debugPrintf("Total key chain length=%d\n", chain.length);

        for (int i = 0; i < nrKeys; i++) {
            X509Certificate cert = chain[i];
            keySubjects[i] = cert.getSubjectDN().toString();
            keyIssuers[i] = cert.getIssuerDN().toString();

            chainMessage += " --- Certificate [" + (i + 1) + "] ---\n";
            chainMessage += CertUtil.toString(cert, "    ", "\n");
        }

        // String options[]={"yes","no","temporary"};
        int opt = 0;

        if (options.interactive == true) {
            logger.debugPrintf("Asking interactive for:%s\n", host);

            opt = CertificateDialog.showDialog("Certificate Received from: " + host + "\n" + "Accept certificate ?",
                    chainMessage);

            if ((opt == CertificateDialog.NO) || (opt == CertificateDialog.CANCEL))
                return false;
        } else if (options.alwaysAccept == false) {
            logger.debugPrintf("Rejecting Cert. Interactive==false and alwaysAccept==false for host:%s\n", host);
            return false;
        } else {
            logger.debugPrintf("Accepting Certificate. Interactive==false and alwaysAccept==true for host:%s\n", host);
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
            logger.debugPrintf("+++ Adding Key: %s +++ \n", alias);
            logger.debugPrintf(" -  Subject =%s\n", keySubjects[k]);
            logger.debugPrintf(" -  Issuer  =%s\n", keyIssuers[k]);

            certStore.addCertificate(alias, cert, false);
        }

        // interactive save
        if (options.interactive == true) {
            if (opt != CertificateDialog.TEMPORARY) {
                logger.debugPrintf("Accepting Certificate. Interactive==false and alwaysAccept==true for host:%s\n",
                        host);
                certStore.saveKeystore();
            }
        }
        // not interactive:
        else if (options.storeAccepted == true) {
            logger.debugPrintf("Saving keystore after (default) accepting certificate from host:%s\n", host);
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
