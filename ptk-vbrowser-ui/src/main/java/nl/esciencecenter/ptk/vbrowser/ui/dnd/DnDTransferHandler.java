/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Default TransgerHandler for ViewNodes.
 */
@Slf4j
public class DnDTransferHandler extends TransferHandler {

    private static final DnDTransferHandler defaultTransferHandler = new DnDTransferHandler();

    public static DnDTransferHandler getDefault() {
        return defaultTransferHandler;
    }

    // === Instance stuff === //

    /**
     * Construct default TransferHandler wich handles VRL Objects
     */
    public DnDTransferHandler() {
        // Default ViewNode Transferer.
    }

    @Override
    public void exportDone(JComponent comp, Transferable data, int action) {
        // this method is called when the export of the Transferable is done.
        // The actual DnD is NOT finished.

        log.debug("exportDone():{}", data);
        log.debug("exportDone action={}", action);
    }

    /*
     * Method is NOT called when canImport(TransferSupport transferSupport) is
     * overriden!
     *
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
     * java.awt.datatransfer.DataFlavor[])
     */
    // Method is NOT called when canImport(TransferSupport transferSupport) is
    // overrriden!
    // ===
    @Deprecated
    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (DataFlavor flav : flavors)
            log.debug("canImport():{}", flav);
        return false;
        // return VTransferData.hasMyDataFlavor(flavors);
    }

    // This method is called i.s.o above one:
    public boolean canImport(TransferSupport transferSupport) {
        log.error("FIXME:canImport():{}", transferSupport);

        DataFlavor[] flavors = transferSupport.getDataFlavors();
        for (DataFlavor flav : flavors) {
            log.error("FIXME:canImport(): -flav:{}", flav);
        }
        // transferSupport.setDropAction(COPY);
        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        log.debug("createTransferable():{}", c);

        if ((c instanceof ViewNodeComponent) == false) {
            log.error("createTransferable(): Error: Not a ViewNodeComponent:{}", c);
            return null;
        }

        return createTransferable((ViewNodeComponent) c);
    }

    protected Transferable createTransferable(ViewNodeComponent c) {
        // Debug("Create Transferable:"+c);
        List<ViewNode> nodes = null;

        ViewNodeContainer parent = c.getViewContainer();

        if (parent != null) {
            // redirect to parent for multi selection!
            nodes = parent.getNodeSelection();
        } else if (c instanceof ViewNodeContainer) {
            // drag initiated from Container! (ResourceTree)
            nodes = ((ViewNodeContainer) c).getNodeSelection();
        } else {
            // stand-alone 'node'
            nodes = new ArrayList();
            nodes.add(c.getViewNode()); // get actual view node i.s.o contains
            // selection.
        }

        if ((nodes != null) && (nodes.size() > 0)) {
            log.debug("createTransferable(): getNodeSelection()={}", nodes.size());

            if (nodes.size() <= 0)
                return null;

            return VRLEntryListTransferable.createFrom(nodes);
        } else {
            log.debug("Tranfer source not recognised:{}", c);
        }

        return null;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        log.debug("exportAsDrag():{}", e);
        super.exportAsDrag(comp, e, action);
    }

    public void exportToClipboard(JComponent comp, Clipboard clipboard, int action) {
        log.debug("exportToClipboard():{}", comp);
        super.exportToClipboard(comp, clipboard, action);
    }

    @Override
    public int getSourceActions(JComponent c) {
        // All Nodes can be Copied, Moved and Linked to
        return COPY_OR_MOVE | DnDConstants.ACTION_LINK;
    }

    /*
     * NOT called if importData(TransferSupport is implemented)(non-Javadoc)
     *
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
     * java.awt.datatransfer.Transferable)
     */
    @Override
    @Deprecated
    public boolean importData(JComponent comp, Transferable data) {
        // This method is directory called when performing CTRL-V

        log.debug("importData():{}", comp);
        if ((comp instanceof ViewNodeComponent) == false) {
            log.error("importData(): Error: Not a ViewNodeComponent:{}", comp);
            return false;
        }

        return DnDUtil.doPasteData(comp, ((ViewNodeComponent) comp).getViewNode(), data,
                DropAction.COPY);
    }

    /*
     * New (java 1.6) version. If this is overridden, the old
     * ImportData(JComponent, Transferable) is NOT called
     *
     * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.
     * TransferSupport)
     */
    @Override
    public boolean importData(TransferSupport support) {
        // This method is called when performing CTRL-V

        Component comp = support.getComponent();
        Transferable data = support.getTransferable();
        // This method is directory called when performing CTRL-V
        log.debug("importData(TransferSupport):{}", comp);
        if ((comp instanceof ViewNodeComponent) == false) {
            log.error("importData(): Error: Not a ViewNodeComponent:{}", comp);
            return false;
        }

        DropAction dndAction;

        if (support.isDrop()) {
            dndAction = DnDUtil.getDropAction(support.getDropAction());
        } else {
            // todo: check for  Cut'n Paste (CTRL-X)! 
            dndAction = DropAction.COPY_PASTE;
        }

        return DnDUtil.doPasteData(comp, ((ViewNodeComponent) comp).getViewNode(), data, dndAction);
    }

}
