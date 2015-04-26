package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.TextViewer;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class StartTextViewer
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(TextViewer.class,new VRL("file:///home/ptdeboer/tests/testText.txt"));

            // viewStandAlone(null);
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
