package test.viewers;

import nl.esciencecenter.ptk.vbrowser.viewers.PluginRegistry;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerContext;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerFrame;
import nl.esciencecenter.ptk.vbrowser.viewers.ViewerPlugin;
import nl.esciencecenter.ptk.vbrowser.viewers.events.ViewerEventDispatcher;
import nl.esciencecenter.ptk.vbrowser.viewers.vrs.ViewerResourceLoader;
import nl.esciencecenter.vbrowser.vrs.VRS;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class ViewerTests {
    private static PluginRegistry viewerRegistry;

    public static PluginRegistry getViewerRegistry() {
        if (viewerRegistry == null) {
            viewerRegistry = new PluginRegistry(new ViewerResourceLoader(new VRSContext(),null));
        }

        return viewerRegistry;
    }

    public static ViewerFrame startViewer(Class<? extends ViewerPlugin> class1, VRL vrl)
            throws VrsException {
        ViewerPlugin newViewer = getViewerRegistry().createViewer(class1);
        ViewerFrame frame = createViewerFrame(newViewer, true);

        ViewerContext context = new ViewerContext(getViewerRegistry(), null, null, vrl, true);
        context.setViewerEventDispatcher(new ViewerEventDispatcher(true));
        newViewer.initViewer(context);

        newViewer.startViewer(vrl, null);
        frame.setVisible(true);

        return frame;
    }

    public static ViewerFrame createViewerFrame(ViewerPlugin newViewer, boolean initViewer) {

        ViewerFrame frame = new ViewerFrame(newViewer);
        if (initViewer) {
            newViewer.initViewer(new ViewerContext(getViewerRegistry()));
        }
        frame.pack();
        frame.setSize(frame.getPreferredSize());
        // frame.setSize(800,600);

        return frame;
    }

    public static void testViewer(Class<? extends ViewerPlugin> class1, VRL vrl)
            throws VrsException {
        ViewerFrame frame = startViewer(class1, vrl);
        System.out.printf("ViewerFrame is visible:%s\n", frame.isVisible());
    }

}
