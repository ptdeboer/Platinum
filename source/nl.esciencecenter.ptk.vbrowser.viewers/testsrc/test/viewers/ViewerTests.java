package test.viewers;

import java.net.URI;

import nl.esciencecenter.ptk.vbrowser.viewers.internal.HexViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.internal.TextViewer;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPanel;


public class ViewerTests
{

    public static void testViewer(Class<? extends ViewerPanel> class1, URI uri)
    {
        ViewerFrame frame = ViewerFrame.startViewer(class1, uri); 
        
        
    }

}
