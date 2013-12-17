package test.viewers;

import java.net.URI;

import nl.esciencecenter.ptk.vbrowser.viewers.x509viewer.X509Viewer;

public class StartX509Viewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(X509Viewer.class,new URI("file:///home/ptdeboer/tests/cert.pem"));
            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
