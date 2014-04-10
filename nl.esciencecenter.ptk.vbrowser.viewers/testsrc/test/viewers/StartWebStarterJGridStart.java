package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.JavaWebStarter;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;


public class StartWebStarterJGridStart
{
    // === Main ===

    public static void main(String args[])
    {
        // Global.setDebug(true);
        
        try
        {
            ViewerTests.testViewer(JavaWebStarter.class,new VRL("http://ca.dutchgrid.nl/start/jgridstart.jnlp"));
        }
        catch (Exception e)
        {
            System.out.println("***Error: Exception:" + e);
            e.printStackTrace();
        }

    }

    
}
