package nl.esciencecenter.ptk.ssl;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.exceptions.CertificateStoreException;
import nl.esciencecenter.ptk.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

public class Test_CertificateStore {
    protected static Secret defaultSecret = new Secret(new char[]{'1', '2', '3', '4', '5'});

    // ==============
    // Helper Methods 
    // ==============

    public static CertificateStore createEmpty() throws CertificateStoreException {
        CertificateStore cert = CertificateStore.createInternal(defaultSecret);
        return cert;
    }

    private void infoPrintf(String format, Object... args) {
        System.out.printf(format, args);
    }

    // ==============
    // Tests  
    // ==============

    @Test
    public void testEmptyCertStore() throws CertificateStoreException {
        // test defaults: 
        CertificateStore certs = createEmpty();
        Assert.assertFalse("Default empty certificate store may not be persistant.", certs.isPersistant());
        Assert.assertTrue("Default empty certificate store may not have a persistant storage location.", StringUtil.isEmpty(certs.getKeyStoreLocation()));

        List<String> aliasses = certs.getAliases();
        Assert.assertTrue("Default empty certificate store may not have aliasses.", ((aliasses == null) || (aliasses.size() <= 0)));
    }

    @Test
    public void testStoreCertificate() throws CertificateStoreException, CertificateEncodingException, NoSuchAlgorithmException {
        // test defaults: 
        CertificateStore certs = createEmpty();

        java.net.URL url = ClassLoader.getSystemResource("test/certificates/TestCert.crt");
        Assert.assertNotNull("FATAL: Could not get test certificate!", url);

        X509Certificate cert = certs.addDERCertificate(url.getPath(), false);
        Assert.assertNotNull("Added Certificate may not be null", cert);

        Principal principal = cert.getSubjectDN();
        infoPrintf(" - subjectDN name=%s\n", principal.getName());

        List<String> aliases = certs.getAliases();
        Assert.assertEquals("CertificateStore with one certicate, must have only one aliasses.", 1, aliases.size());

        String alias = aliases.get(0);
        Certificate cert2 = certs.getCertificate(alias);
        Assert.assertNotNull("Could get Certificate with alias:" + alias, cert2);
        infoPrintf("Added certificate with alias:%s\n", alias);

        if (cert2 instanceof X509Certificate) {
            assertEquals("Certificates from KeyStore with alias:" + alias + " must match.", (X509Certificate) cert2, cert);
        }
    }

    protected void assertEquals(String message, X509Certificate cert1, X509Certificate cert2) throws CertificateEncodingException, NoSuchAlgorithmException {
        Assert.assertEquals(message + ":X509Certificates do not match.", cert1, cert2);

        String certStr1 = CertUtil.toString(cert1, " ", "\n");
        String certStr2 = CertUtil.toString(cert2, " ", "\n");

        Assert.assertEquals("X509Certificate String presenentations do not match.", certStr1, certStr2);

    }

    @Test
    public void testChangePasswordEmptyCertStore() throws CertificateStoreException {
        CertificateStore certs = createEmpty();

        Secret newPassword = new Secret("newPassword".toCharArray());
        certs.changePassword(defaultSecret, newPassword);
        // check
        KeyStore keyStore = certs.getKeyStore();

        // test new password by creating new CertificateStore: 
        CertificateStore newStore = CertificateStore.createFrom(keyStore, newPassword);

        certs.changePassword(newPassword, defaultSecret);

        // test new password by creating new CertificateStore:
        CertificateStore oldStore = CertificateStore.createFrom(keyStore, newPassword);

        // check; 
    }

    // @Test
    public void test_ListCertificates() throws Exception {
        // CertificateStore certs=CertificateStore.fsutil(true);
        // CertificateStore certs=CertificateStore.loadCertificateStore(GlobalProperties.getGlobalUserHome()+"/.vletrc/cacerts","changeit",false,false);
        Secret secret = new Secret("changeit".toCharArray());
        CertificateStore certs = CertificateStore.loadCertificateStore(GlobalProperties.getGlobalUserHome() + "/.vletrc/cacerts", secret, false, false);

        List<String> aliasses = certs.getAliases();

        for (String alias : aliasses) {
            Certificate cert = certs.getCertificate(alias);
            if (cert instanceof X509Certificate) {
                System.out.printf("alias %s=%s", alias, CertUtil.toString((X509Certificate) cert, "  ", "\n"));
            } else {
                System.out.printf("alias %s=Unknown Certificate class:%s\n", alias, cert.getClass());
            }
        }
    }

}
