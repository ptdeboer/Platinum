package nl.esciencecenter.ptk.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.net.URIUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

public class DnDFlavors
{
    private static ClassLogger logger = ClassLogger.getLogger(DnDFlavors.class);

    // ===================================
    // URI/URL Flavors
    // ===================================
    public static DataFlavor javaURIListAsTextFlavor = new DataFlavor("text/uri-list;class=java.lang.String", "URI List");

    /**
     * Firefox under Windows can drop a single (java.net)URL
     */
    public static DataFlavor javaURLFlavor = new DataFlavor("application/x-java-url;class=java.net.URL", "Single URL");

    public static DataFlavor javaFileListFlavor = DataFlavor.javaFileListFlavor;

    /**
     * In KDE this is a list of URI Strings separated by newlines;
     */
    public static DataFlavor uriListAsTextFlavor = new DataFlavor("text/uri-list;class=java.lang.String", "URI list");

    // ===================================
    // Content or other mime types Flavors
    // ===================================

    /**
     * Default "text/plain" mime-type DataFlavor.
     * 
     * <pre>
     *     mimeType            = "text/plain"
     *     representationClass = java.lang.String
     * </pre>
     */
    public static DataFlavor plainTextFlavor = new DataFlavor("text/plain;representationclass=java.lang.String", "plain text");

    /**
     * Actual serialized java.lang.String object.
     * 
     * <pre>
     *     mimeType           = "application/x-java-serialized-object"
     *     representationClass = java.lang.String
     * </pre>
     */
    public static DataFlavor javaStringFlavor = DataFlavor.stringFlavor;

    /**
     * Rich Text Format (RTF).
     * 
     * <pre>
     *     mimetype            = 'text/rtf'
     *     representationClass = java.nio.ByteBuffer
     * </pre>
     */
    public static DataFlavor rtfTextAsByteBuffer = new DataFlavor("text/rtf;class=java.nio.ByteBuffer", "RFT ByteBuffer");

    /**
     * The "text/html" mime-type compatible DataFlavor;
     */
    public static DataFlavor htmlFlavor = new DataFlavor("text/html;class=java.lang.String", "HTML Text");

    /**
     * Default binary ('octet') stream mime-type.
     */
    public static DataFlavor octetStreamDataFlavor = new DataFlavor("application/octet-stream;class=java.io.InputStream", "octetStream");

    // ====
    // Windows specific DataFlavors
    // ====

    // mimetype=text/plain;representationclass=java.io.InputStream;charset=windows-1252
    // public static DataFlavor windows1252TextInputStream = new DataFlavor("text/plain;class=java.io.InputStream;charset=windows-1252","Windows 1252 Text InputStream");

    /**
     * Flavors in order of preference which might be converted to URI(s).
     */
    public static DataFlavor uriDataFlavors[] =
    {
            DnDFlavors.javaURLFlavor,
            DnDFlavors.javaURIListAsTextFlavor,
            DnDFlavors.javaFileListFlavor,
            DnDFlavors.plainTextFlavor,
            DnDFlavors.javaStringFlavor
    };

    public static List<URI> getURIList(Transferable transferable) throws UnsupportedFlavorException, IOException
    {
        for (DataFlavor flav : uriDataFlavors)
        {
            if (transferable.isDataFlavorSupported(flav))
            {
                List<URI> uris = getURIList(transferable, flav);
                if (logger.hasDebugLevel())
                {
                    logger.debugPrintf("getURIList()=%s\n", new ExtendedList<URI>(uris));
                }
                return uris;
            }
        }

        throw new IOException("Couldn't get URI(s) from this transferable:" + transferable);
    }

    public static List<URI> getURIList(Transferable transferable, DataFlavor uriFlavor) throws UnsupportedFlavorException, IOException
    {
        List<URI> uris = new ArrayList<URI>();

        // Single URL:
        if (uriFlavor.equals(DnDFlavors.javaURLFlavor))
        {
            java.net.URL url = (java.net.URL) transferable.getTransferData(javaURLFlavor);
            try
            {
                uris.add(url.toURI());
                return uris;
            }
            catch (URISyntaxException e)
            {
                throw new IOException("Failed to parse URL:" + url, e);
            }
        }

        // List or URIs.
        if (uriFlavor.equals(DnDFlavors.javaURIListAsTextFlavor))
        {
            String urilist = (String) transferable.getTransferData(uriFlavor);
            uris = URIUtil.parseURIList(urilist, ";");
            return uris;
        }

        // List of Files:
        if (uriFlavor.equals(DnDFlavors.javaFileListFlavor))
        {
            List<java.io.File> fileList = (List<java.io.File>) transferable.getTransferData(uriFlavor);

            for (int i = 0; i < fileList.size(); i++)
            {
                // debugPrintf(">>> adding File:%s\n",fileList.get(i));
                uris.add(fileList.get(i).toURI());
            }
            return uris;
        }

        // Default to String (list) parsing:
        if ((uriFlavor.equals(javaStringFlavor)) || (uriFlavor.equals(plainTextFlavor)))
        {
            String txt = (String) transferable.getTransferData(uriFlavor);
            uris = new ArrayList<java.net.URI>();

            try
            {
                uris.add(new URI(txt));
                return uris;
            }
            catch (URISyntaxException e)
            {
                throw new IOException("Failed to parse (URI) String:" + txt);
            }
        }

        throw new UnsupportedFlavorException(uriFlavor);
    }

    public static boolean canConvertToURIs(DataFlavor[] flavors, boolean includeStringFlavors)
    {
        for (DataFlavor flav : flavors)
        {
            if (canConvertToURIs(flav))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean canConvertToURIs(DataFlavor flavor)
    {
        if (flavor == null)
        {
            return false;
        }

        for (DataFlavor flav : uriDataFlavors)
        {
            if (flavor.equals(flav))
            {
                logger.debugPrintf("Can convert to URI:%s\n", flav);
                return true;
            }
        }
        return false;
    }

    public static ByteBuffer getRTFText(Transferable t) throws UnsupportedFlavorException, IOException
    {
        ByteBuffer buffer = (java.nio.ByteBuffer) t.getTransferData(rtfTextAsByteBuffer);
        return buffer;
    }

    public static String getRTFTextAsString(Transferable t) throws UnsupportedFlavorException, IOException
    {
        return new String(getRTFText(t).array());
    }

}
