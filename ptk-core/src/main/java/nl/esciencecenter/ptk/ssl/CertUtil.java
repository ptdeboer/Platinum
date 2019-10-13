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

import nl.esciencecenter.ptk.exceptions.FileURISyntaxException;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertUtil {

    /**
     * Create DER Encoded Certificate from String. Certificate String needs to be between:
     *
     * <pre>
     * -----BEGIN CERTIFICATE-----
     * ...
     * -----END CERTIFICATE-----
     * </pre>
     *
     * @return X509Certificate
     * @throws UnsupportedEncodingException
     * @throws CertificateException
     */
    public static X509Certificate createDERCertificateFromString(String derEncodedString)
            throws CertificateException {
        byte[] bytes = derEncodedString.getBytes(StandardCharsets.US_ASCII); // plain aksii
        ByteArrayInputStream binps = new ByteArrayInputStream(bytes);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x590 = (X509Certificate) cf.generateCertificate(binps);

        return x590;
    }

    /**
     * Load .pem Certificate file
     *
     * @return X509Certificate
     * @throws IOException
     * @throws FileURISyntaxException
     * @throws CertificateException
     */
    public static X509Certificate loadPEMCertificate(String filename) throws FileURISyntaxException, IOException,
            CertificateException {

        String pemStr = FSUtil.fsutil().readText(filename);

        int index = pemStr.indexOf("-----BEGIN CERTIFICATE");

        if (index < 0)
            throw new IOException("Couldn't find start of (DER) certificate!\n---\n" + pemStr);

        // Get DER part
        String derStr = pemStr.substring(index);
        return createDERCertificateFromString(derStr);
    }

    /**
     * Load DER encoded Certificate .cer .crt .der
     *
     * @return X509Certificate
     * @throws IOException
     * @throws FileURISyntaxException
     * @throws CertificateException
     */
    public static X509Certificate loadDERCertificate(String filename) throws FileURISyntaxException, IOException,
            CertificateException {

        FSUtil fsUtil = FSUtil.fsutil();
        InputStream finps = fsUtil.createInputStream(fsUtil.newFSPath(filename));

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x590 = (X509Certificate) cf.generateCertificate(finps);

        return x590;
    }

    public static String toString(X509Certificate cert, String indent, String eolStr) throws NoSuchAlgorithmException,
            CertificateEncodingException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest sha2 = MessageDigest.getInstance("SHA-256");
        sha1.update(cert.getEncoded());
        sha2.update(cert.getEncoded());

        String certStr;
        certStr = indent + "Subject DN:" + cert.getSubjectDN() + eolStr;
        certStr += indent + "Issuer DN :" + cert.getIssuerDN() + eolStr;
        certStr += indent + "SHA1      :" + StringUtil.toHexString(sha1.digest(), true) + eolStr;
        certStr += indent + "SHA2(256) :" + StringUtil.toHexString(sha2.digest(), true) + eolStr;
        certStr += indent + "Not before:" + cert.getNotBefore()+ eolStr;
        certStr += indent + "Not after :" + cert.getNotAfter()+ eolStr;

        return certStr;
    }

}
