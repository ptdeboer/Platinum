/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.vbrowser.ui.dnd;

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeDnDHandler.DropAction;

/**
 * Default TransgerHandler for ViewNodes.
 */
public class DnDTransferHandler extends TransferHandler
{
    private static ClassLogger logger=ClassLogger.getLogger(DnDTransferHandler.class,DnDUtil.dndLogger); 
    
    private static final long serialVersionUID = -115960212645219778L;

    private static DnDTransferHandler defaultTransferHandler = new DnDTransferHandler();

    public static DnDTransferHandler getDefault()
    {
        return defaultTransferHandler;
    }

    // === Instance stuff === //

    /** Construct default TransferHandler wich handles VRL Objects */
    public DnDTransferHandler()
    {
        // Default ViewNode Transferer.
    }

    @Override
    public void exportDone(JComponent comp, Transferable data, int action)
    {
        // this method is called when the export of the Transferable is done.
        // The actual DnD is NOT finished.

        logger.debugPrintf("exportDone():%s\n",data);
        logger.debugPrintf("exportDone action=%s\n",action);
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
    public boolean canImport(JComponent c, DataFlavor[] flavors)
    {
        for (DataFlavor flav : flavors)
            logger.debugPrintf("canImport():%s\n",flav);
        return false;
        // return VTransferData.hasMyDataFlavor(flavors);
    }

    // This method is called i.s.o above one:
    public boolean canImport(TransferSupport transferSupport)
    {
        logger.errorPrintf("FIXME:canImport():%s\n", transferSupport);
        
        DataFlavor[] flavors = transferSupport.getDataFlavors();
        for (DataFlavor flav : flavors)
        {
            logger.errorPrintf("FIXME:canImport(): -flav:%s\n", flav);
        }
        // transferSupport.setDropAction(COPY);
        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        logger.debugPrintf("createTransferable():%s\n", c);

        if ((c instanceof ViewNodeComponent) == false)
        {
            logger.errorPrintf("createTransferable(): Error: Not a ViewNodeComponent:%s\n", c);
            return null;
        }

        return createTransferable((ViewNodeComponent) c);
    }

    protected Transferable createTransferable(ViewNodeComponent c)
    {
        // Debug("Create Transferable:"+c);
        ViewNode[] nodes = null;

        ViewNodeContainer parent = c.getViewContainer();

        if (parent != null)
        {
            // redirect to parent for multi selection!
            nodes = parent.getNodeSelection();
        }
        else if (c instanceof ViewNodeContainer)
        {
            // drag initiated from Container! (ResourceTree)
            nodes = ((ViewNodeContainer) c).getNodeSelection();
        }
        else
        {
            // stand-alone 'node'
            nodes = new ViewNode[1];
            nodes[0] = c.getViewNode(); // get actual view node i.s.o contains
                                        // selection.
        }

        if ((nodes != null) && (nodes.length > 0))
        {
            logger.debugPrintf("createTransferable(): getNodeSelection()=%d\n", nodes.length);

            if (nodes.length <= 0)
                return null;

            return VRLEntryListTransferable.createFrom(nodes);
        }
        else
        {
            logger.debugPrintf("Tranfer source not recognised:%s\n", c);
        }

        return null;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action)
    {
        logger.debugPrintf("exportAsDrag():%s\n",e);
        super.exportAsDrag(comp, e, action);
    }

    public void exportToClipboard(JComponent comp, Clipboard clipboard, int action)
    {
        logger.debugPrintf("exportToClipboard():%s\n",comp);
        super.exportToClipboard(comp, clipboard, action);
    }

    @Override
    public int getSourceActions(JComponent c)
    {
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
    public boolean importData(JComponent comp, Transferable data)
    {
        // This method is directory called when performing CTRL-V

        logger.debugPrintf("importData():%s\n", comp);
        if ((comp instanceof ViewNodeComponent) == false)
        {
            logger.errorPrintf("importData(): Error: Not a ViewNodeComponent:%s\n", comp);
            return false;
        }

        return DnDUtil.doPasteData(comp, ((ViewNodeComponent) comp).getViewNode(), data, DropAction.COPY);
    }

    /*
     * New (java 1.6) version. If this is overridden, the old
     * ImportData(JComponent, Transferable) is NOT called
     * 
     * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.
     * TransferSupport)
     */
    @Override
    public boolean importData(TransferSupport support)
    {
        // This method is called when performing CTRL-V

        Component comp = support.getComponent();
        Transferable data = support.getTransferable();
        // This method is directory called when performing CTRL-V
        logger.debugPrintf("importData(TransferSupport):%s\n", comp);
        if ((comp instanceof ViewNodeComponent) == false)
        {
            logger.errorPrintf("importData(): Error: Not a ViewNodeComponent:%s\n", comp);
            return false;
        } 
        
        DropAction dndAction;
        
        if (support.isDrop())
        {
            dndAction = DnDUtil.getDropAction(support.getDropAction());
        }
        else
        {
            // todo: check for  Cut'n Paste (CTRL-X)! 
            dndAction = DropAction.COPY_PASTE;
        }
        
        return DnDUtil.doPasteData(comp, ((ViewNodeComponent) comp).getViewNode(), data, dndAction);
    }

}
