package test.viewers;

import java.net.URI;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.HexViewer;

public class StartHexViewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(HexViewer.class,new URI("file:///home/ptdeboer/tests/image1.jpg"));
            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
