package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.x509viewer.X509Viewer;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class StartX509Viewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(X509Viewer.class,new VRL("file:///home/ptdeboer/tests/cert.pem"));
            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
