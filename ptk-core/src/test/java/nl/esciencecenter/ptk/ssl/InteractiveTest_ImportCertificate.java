package nl.esciencecenter.ptk.ssl;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.ssl.CertificateStore.CaCertOptions;

public class InteractiveTest_ImportCertificate {

    public static void main(String[] args) {
        CertificateStore cacerts;
        try {
            cacerts = CertificateStore.loadCertificateStore("/home/piter/.vrsrc/cacerts", new Secret(CertificateStore.DEFAULT_PASSPHRASE.toCharArray()), true, true);
            String hostname = "bioboost-virt.science.ru.nl";
            int port = 443;

            CaCertOptions options = new CaCertOptions();
            options.interactive = true;
            options.storeAccepted = true;

            CertUI.interactiveImportCertificate(cacerts, hostname, port, options);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
