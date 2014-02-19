package dnd;

import java.awt.dnd.DropTarget;
import java.net.URI;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.ui.widgets.URIDropHandler;
import nl.esciencecenter.ptk.ui.widgets.URIDropTargetLister;

public class DnDTestPanel extends JPanel implements URIDropTargetLister
{

    public DnDTestPanel()
    {
        super();
        initGui();
    }

    protected void initGui()
    {
        DropTarget dt1 = new DropTarget();

        try
        {
            dt1.addDropTargetListener(new URIDropHandler(this));
            this.setDropTarget(dt1);
            this.setTransferHandler(new DnDTestTransferHandler());
        }
        catch (TooManyListenersException e)
        {
            e.printStackTrace();
        }

        addCopyPasteKeymappings(this);

    }

    public static void addCopyPasteKeymappings(JComponent comp)
    {

        // Copy Past Keyboard bindings:
        {
            InputMap imap = comp.getInputMap();
            imap.put(KeyStroke.getKeyStroke("ctrl X"), TransferHandler.getCutAction().getValue(Action.NAME));
            imap.put(KeyStroke.getKeyStroke("ctrl C"), TransferHandler.getCopyAction().getValue(Action.NAME));
            imap.put(KeyStroke.getKeyStroke("ctrl V"), TransferHandler.getPasteAction().getValue(Action.NAME));
        }

        ActionMap map = comp.getActionMap();
        // Use TransferHandler actions:
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

    }

    @Override
    public void notifyUriDrop(List<URI> uris)
    {
        for (URI uri : uris)
        {
            System.out.printf(" - uri=%s\n", uri);
        }
    }

}
