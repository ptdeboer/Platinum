package uitests.dnd;

import nl.esciencecenter.ptk.ui.widgets.URIDropHandler;
import nl.esciencecenter.ptk.ui.widgets.URIDropTargetLister;

import javax.swing.*;
import java.awt.dnd.DropTarget;
import java.net.URI;
import java.util.List;
import java.util.TooManyListenersException;

public class DnDTestPanel extends JPanel implements URIDropTargetLister {

    public DnDTestPanel() {
        super();
        initGui();
    }

    protected void initGui() {
        DropTarget dt1 = new DropTarget();

        try {
            dt1.addDropTargetListener(new URIDropHandler(this));
            this.setDropTarget(dt1);
            this.setTransferHandler(new DnDTestTransferHandler());
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }

        addCopyPasteKeymappings(this);

    }

    public static void addCopyPasteKeymappings(JComponent comp) {

        // Copy Past Keyboard bindings:
        {
            InputMap imap = comp.getInputMap();
            imap.put(KeyStroke.getKeyStroke("ctrl X"),
                    TransferHandler.getCutAction().getValue(Action.NAME));
            imap.put(KeyStroke.getKeyStroke("ctrl C"),
                    TransferHandler.getCopyAction().getValue(Action.NAME));
            imap.put(KeyStroke.getKeyStroke("ctrl V"),
                    TransferHandler.getPasteAction().getValue(Action.NAME));
        }

        ActionMap map = comp.getActionMap();
        // Use TransferHandler actions:
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());

    }

    @Override
    public void notifyUriDrop(List<URI> uris) {
        for (URI uri : uris) {
            System.out.printf(" - uri=%s\n", uri);
        }
    }

}
