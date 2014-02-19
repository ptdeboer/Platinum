package nl.esciencecenter.ptk.ui.widgets;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * Handle drops on the Navigation Bar.
 */
public class URIDropHandler implements DropTargetListener
{
    private static ClassLogger logger=ClassLogger.getLogger(URIDropHandler.class); 
     
    private URIDropTargetLister uriDropTargetListener;

    public URIDropHandler(URIDropTargetLister uriDropListener)
    {
        this.uriDropTargetListener = uriDropListener;
    }

    public void dragEnter(DropTargetDragEvent dtde)
    {
        // accept/reject DataFlavor
        for (DataFlavor flavor : DnDFlavors.uriDataFlavors)
        {
            if (dtde.isDataFlavorSupported(flavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                return;
            }
        }
        dtde.rejectDrag();
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }

    public void dragExit(DropTargetEvent dte)
    {
    }

    public void drop(DropTargetDropEvent dtde)
    {
        // check
        Transferable t = dtde.getTransferable();
        DropTargetContext dtc = dtde.getDropTargetContext();

        List<java.net.URI> uris = null;

        try
        {
            if (DnDFlavors.canConvertToURIs(t.getTransferDataFlavors(),true))
            {
                // first accept drop!
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                uris = DnDFlavors.getURIList(t);

                if ((uris != null) && (uris.size() > 0))
                {
                    uriDropTargetListener.notifyUriDrop(uris);
                    dtde.dropComplete(true);
                    return;
                }
                else
                {
                    logger.warnPrintf("drop(): Could not convert to one or more URIs:%s\n",new ExtendedList<DataFlavor>(t.getTransferDataFlavors())); 
                    dtde.dropComplete(false);
                }
            }
            else
            {
                logger.warnPrintf("drop(): Dropped data is not valid URI\n"); 
                dtde.rejectDrop();
            }
        }
        catch (UnsupportedFlavorException e)
        {
            logger.logException(ClassLogger.ERROR,e,"UnsupportedFlavorException:%s\n",e); 
        }
        catch (IOException e)
        {
            logger.logException(ClassLogger.ERROR,e,"IOException:%s\n",e); 
        }

        dtde.dropComplete(false);
    }

}
