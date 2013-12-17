package test.viewers;

import java.net.URI;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.ImageViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.TextViewer;

public class StartTextViewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(TextViewer.class,new URI("file:///home/ptdeboer/tests/testText.txt"));

            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
