package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.ImageViewer;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class StartImageViewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(ImageViewer.class,new VRL("file:///home/ptdeboer/tests/image1.jpg"));

            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
