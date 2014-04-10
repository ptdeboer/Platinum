package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.HexViewer;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class StartHexViewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(HexViewer.class,new VRL("file:///home/ptdeboer/tests/image1.jpg"));
            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
