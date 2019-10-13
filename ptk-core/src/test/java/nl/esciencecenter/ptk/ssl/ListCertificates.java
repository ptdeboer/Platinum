package nl.esciencecenter.ptk.ssl;

import nl.esciencecenter.ptk.crypt.Secret;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

public class ListCertificates {

    @Test
    public void test_ListCertificates() throws Exception {
        // CertificateStore certs=CertificateStore.fsutil(true);
        // CertificateStore certs=CertificateStore.loadCertificateStore(GlobalProperties.getGlobalUserHome()+"/.vletrc/cacerts","changeit",false,false);
        Secret secret = new Secret("changeit".toCharArray());
        URL url = ListCertificates.class.getResource("/test/certificates/cacerts");
        Assert.assertNotNull("Can not resolve test certificate store");
        CertificateStore certs = CertificateStore.loadCertificateStore(url.getPath(), secret, false, false);

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
