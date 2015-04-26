package dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.swing.TransferHandler;

import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;

public class DnDTestTransferHandler extends TransferHandler
{
    private static final long serialVersionUID = 6542323857970218067L;

    public boolean importData(TransferSupport support) 
    {
        outPrintf("importData()");

        try
        {
            DataFlavor[] flavs = support.getDataFlavors(); 
            for (DataFlavor flav:flavs)
            {
                outPrintf(" - importData() flav:%s\n",flav); 
            }
            
            Transferable data = support.getTransferable(); 
            
            if (data.isDataFlavorSupported(DnDFlavors.rtfTextAsByteBuffer))
            {
                String rtfStr = DnDFlavors.getRTFTextAsString(data);
                outPrintf("--- RTF Text ---\n%s\n",rtfStr); 
            }
            
            if (DnDFlavors.canConvertToURIs(data.getTransferDataFlavors(), true))
            {
                List<URI> uris = DnDFlavors.getURIList(data); 
                for (URI uri:uris)
                {
                    outPrintf(" - importData(): -uri=%s\n",uri); 
                }
            }
        }
        catch (UnsupportedFlavorException | IOException e)
        {
            e.printStackTrace();
        } 
        
        return true; 
    }
    
    public static void outPrintf(String format, Object... args)
    {
        System.out.printf(format,args); 
    }
    
}
