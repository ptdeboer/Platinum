package test.viewers;

import java.net.URI;

import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.viewerplugin.ViewerPlugin;

public class ViewerTests
{

    public static void testViewer(Class<? extends ViewerPlugin> class1, URI uri)
    {
        ViewerFrame frame = ViewerFrame.startViewer(class1, uri);
        
    }

}
