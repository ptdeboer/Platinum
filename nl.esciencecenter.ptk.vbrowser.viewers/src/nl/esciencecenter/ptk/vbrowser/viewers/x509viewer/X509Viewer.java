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

package nl.esciencecenter.ptk.vbrowser.viewers.x509viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.ptk.data.HashMapList;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.ssl.CertUtil;
import nl.esciencecenter.ptk.ssl.CertificateStore;
import nl.esciencecenter.ptk.ssl.CertificateStoreException;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.viewers.EmbeddedViewer;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class X509Viewer extends EmbeddedViewer implements CertPanelListener
{
    private static final long serialVersionUID = 5397354476414010762L;

    public static final String ADD_METHOD = "addCert";

    public static final String VIEW_METHOD = "viewCert";

    private static String mimeTypes[] =
    {
            "application/x-x509-ca-cert",
            // .crt and .pem can be both user AND CA
            "application/x-pem-file",
            "application/x-x509-pem-file",
            "application/x-x509-crt-file",
            // "application/x-x509-user-cert" = user cert! (not CA)
    };

    // ========================================================================
    //
    // ========================================================================

    private X509Certificate cert;

    private CertPanel caPanel;

    private CertificateStore certUtil;

    public X509Viewer()
    {
        super();
    }

    @Override
    public void doDisposeViewer()
    {
    }

    @Override
    public String[] getMimeTypes()
    {
        return mimeTypes;
    }

    @Override
    public String getViewerName()
    {
        return "ViewerX509";
    }

    @Override
    public void doInitViewer()
    {
        initGUI();
    }

    public void initGUI()
    {
        this.setLayout(new BorderLayout());

        this.caPanel = new CertPanel();
        this.caPanel.setCertPanelListener(this);
        this.add(caPanel, BorderLayout.CENTER);
        Dimension preferredSize = new Dimension(800, 350);
        this.setPreferredSize(preferredSize);
    }

    protected void installCertificate(X509Certificate cert, boolean save) throws Exception
    {
        getCertUtil().addCACertificate(cert, save);
    }

    protected CertificateStore getCertUtil() throws CertificateStoreException
    {
        return this.getResourceHandler().getCertificateStore();
    }

    @Override
    public void doStopViewer()
    {

    }

    //
    // public Vector<ActionMenuMapping> getActionMappings()
    // {
    // ActionMenuMapping addMapping=new ActionMenuMapping(ADD_METHOD, "Add Certificate","certs");
    // ActionMenuMapping viewMapping=new ActionMenuMapping(VIEW_METHOD, "View Certificate","certs");
    //
    // // '/' is not a RE character
    //
    // Pattern txtPatterns[]=new Pattern[mimeTypes.length];
    //
    // for (int i=0;i<mimeTypes.length;i++)
    // {
    // txtPatterns[i]=Pattern.compile(mimeTypes[i]);
    // }
    //
    // addMapping.addMimeTypeMapping(txtPatterns);
    // viewMapping.addMimeTypeMapping(txtPatterns);
    //
    // Vector<ActionMenuMapping> mappings=new Vector<ActionMenuMapping>();
    // mappings.add(addMapping);
    // mappings.add(viewMapping);
    //
    // return mappings;
    // }

    @Override
    public void doStartViewer(VRL vrl, String optionalMethod)
    {
        doUpdate(vrl, optionalMethod);
    }

    @Override
    public void doUpdate(VRL vrl)
    {
        doUpdate(vrl, null);
    }

    public void doUpdate(VRL location, String optMethodName)
    {
        // default to true ?
        boolean add = true;

        if (StringUtil.equals(optMethodName, VIEW_METHOD))
            add = false;

        if (StringUtil.equals(optMethodName, ADD_METHOD))
            add = true;

        if (location == null)
            return;

        addCertificate(location, add);
    }

    public void askCertificate(VRL loc) throws VrsException
    {
        addCertificate(loc, true);
    }

    public void addCertificate(VRL loc, boolean askToAdd)
    {
        try
        {
            cert = instCert(loc);

            String keyIssuers = cert.getIssuerDN().getName();
            String[] mtmp = keyIssuers.split("CN=");

            int index = mtmp[1].indexOf(",");

            keyIssuers = mtmp[1].substring(0, index);

            StringBuffer message = new StringBuffer();

            message.append("Certificate Details:\n");
            message.append("Issuer DN:              " + cert.getIssuerDN() + "\n");
            message.append("Subject DN:             " + cert.getSubjectDN() + "\n");
            message.append("Issuer X500 Principal:  " + cert.getIssuerX500Principal() + "\n");
            message.append("Signature Algorithm:    " + cert.getSigAlgName() + "\n");
            message.append("Type:                   " + cert.getType() + "\n");
            message.append("Version:                " + cert.getVersion() + "\n");
            message.append("Not After:              " + cert.getNotAfter() + "\n");
            message.append("Not Before:             " + cert.getNotBefore() + "\n");
            message.append("Serial Number:          " + cert.getSerialNumber() + "\n");

            if (askToAdd)
            {
                caPanel.setQuestion("You have been asked to trust a new Certificate Authority(CA).\n"
                        + "Accept certificate from '" + keyIssuers + "'?");
            }
            else
            {
                caPanel.setQuestion("Viewing Certificate information.");
                caPanel.setViewOnly(true);
            }

            caPanel.setMessageText(message.toString());

        }
        catch (Exception e)
        {
            caPanel.setMessageText(e.getMessage());
            caPanel.setQuestion("Exception occured");
            notifyException("Failed to add certificate:" + loc, e);
        }
    }

    public boolean isStandaloneViewer()
    {
        return true;
    }

    private X509Certificate instCert(VRL certUri) throws Exception
    {
        String txt = getResourceHandler().readText(certUri, textEncoding);

        // Use hardcoded String to find start of certificate.
        // Current Pem reader is just as simplistic.
        int index = txt.indexOf("-----BEGIN CERTIFICATE");

        if (index >= 0)
        {
            // Get (Expected) DER part
            String derStr = txt.substring(index);
            return CertUtil.createDERCertificateFromString(derStr);
        }
        int len = txt.length();
        if (len > 80)
            len = 80;
        throw new IOException("Couldn't find start of (DER) certificate!\n---\nStarting text:\n" + '"' + txt.substring(0, len) + '"');

    }

    public void optionSelected()
    {
        int opt = caPanel.getOption();
        try
        {
            if (opt == CertPanel.OK)
            {
                installCertificate(cert, true);
            }
            if (opt == CertPanel.TEMPORARY)
            {
                installCertificate(cert, false);
            }

            caPanel.setEnabled(false);

        }
        catch (Exception e)
        {
            notifyException("Adding Certificate Failed", e);
        }

        closeViewer();
    }

    @Override
    public Map<String, List<String>> getMimeMenuMethods()
    {
        String[] mimeTypes = getMimeTypes();

        // Use HashMapList to keep order of menu entries: first is default(!)

        Map<String, List<String>> mappings = new HashMapList<String, List<String>>();

        for (int i = 0; i < mimeTypes.length; i++)
        {
            List<String> list = new StringList(new String[]
            { VIEW_METHOD + ":View Certificate", ADD_METHOD + ":Add Certificate" });
            mappings.put(mimeTypes[i], list);
        }

        return mappings;
    }
}
