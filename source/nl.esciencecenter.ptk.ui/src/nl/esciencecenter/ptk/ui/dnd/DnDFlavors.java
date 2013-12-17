package nl.esciencecenter.ptk.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.net.URIUtil;

public class DnDFlavors
{

    public static DataFlavor flavorURIList = new DataFlavor("text/uri-list;class=java.lang.String", "uri list");
    
    /** "text/plain" mimetype. */ 
    public static DataFlavor plainText=new DataFlavor("text/plain;representationclass=java.lang.String","plain text"); 
            
    public static DataFlavor javaFileListFlavor = DataFlavor.javaFileListFlavor; 

    public static DataFlavor stringFlavor = DataFlavor.stringFlavor;

    public static List<URI> getURIList(Transferable transferable, DataFlavor uriFlavor) throws UnsupportedFlavorException, IOException
    {
        
        List<URI> uris=new ArrayList<URI>(); 
            
        if (uriFlavor==DnDFlavors.flavorURIList)
        {
            String urilist = (String) transferable.getTransferData(uriFlavor);
            uris = URIUtil.parseURIList(urilist,";"); 
        }
        else if (uriFlavor==DnDFlavors.javaFileListFlavor)
        {
            List<java.io.File> fileList = ( List<java.io.File>)transferable.getTransferData(uriFlavor);
        
            for (int i=0;i<fileList.size();i++)
            {
                //debugPrintf(">>> adding File:%s\n",fileList.get(i));
                uris.add(fileList.get(i).toURI());
            }
        }
        
        return uris;
    } 
    
}
