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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.net.URIUtil;
import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;

/** 
 * Handle drops on the Navigation Bar. 
 */
public class URIDropHandler implements DropTargetListener
{

    /** 
     * Flavors in order of preference 
     */ 
    public static DataFlavor uriDataFlavors[]={DnDFlavors.javaFileListFlavor,DnDFlavors.flavorURIList,DnDFlavors.stringFlavor};
    
    private URIDropTargetLister uriDropTargetListener;

    public URIDropHandler(URIDropTargetLister uriDropListener)
    {
        this.uriDropTargetListener=uriDropListener;
    }

    public void dragEnter(DropTargetDragEvent dtde)
    {
        // accept/reject DataFlavor
        for (DataFlavor flavor:uriDataFlavors)
        {
            if (dtde.isDataFlavorSupported(flavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                return ; 
            }
        }
        dtde.rejectDrag(); 
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        
        // dtde.rejectDrag(); 
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

            // Note: 'javaFileListFlavor' might be supported when URIs are dropped 
            // but still won't return correct transfer data as "http" URIs are not Files. 
            // Check URI list first. 
            
            if ( t.isDataFlavorSupported(DnDFlavors.flavorURIList))
            {
                // Check URI(s)
                debugPrintf(">>>%s\n","flavorURIList");
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                
                uris=DnDFlavors.getURIList(t,DnDFlavors.flavorURIList);
                
                dtde.getDropTargetContext().dropComplete(true);

                if ((uris!=null) && (uris.size()>0))
                {
                	uriDropTargetListener.notifyUriDrop(uris);
                	dtde.dropComplete(true);
                	return;
                }
            }
            else if (t.isDataFlavorSupported(DnDFlavors.javaFileListFlavor))
            {
            	// (Java) Files dropped native browser: 
                debugPrintf(">>>%s\n","javaFileListFlavor");
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                
                uris=DnDFlavors.getURIList(t,DnDFlavors.javaFileListFlavor);
    
                if ((uris!=null) && (uris.size()>0))
                {
                    uriDropTargetListener.notifyUriDrop(uris);
                    dtde.dropComplete(true);
                    return;
                }
                
                uris=null; // continue; 
            }
            else if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
                // Convert to text: 
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                String txt = (String) t.getTransferData(DataFlavor.stringFlavor);
                dtde.getDropTargetContext().dropComplete(true);
                uris = new ArrayList<java.net.URI>();

                try
                {
                    uris.add(new URI(txt));
                    uriDropTargetListener.notifyUriDrop(uris);
                    dtde.dropComplete(true);
                    return; 
                }
                catch (URISyntaxException e)
                {
                    // uriDropTargetListener.notifyDnDDropText(txt);
                    dtde.rejectDrop();
                    return; 
                }
            }
            
        }
        catch (UnsupportedFlavorException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        dtde.dropComplete(false);

    }

    private void debugPrintf(String format, Object... args)
    {
        
    }

}
