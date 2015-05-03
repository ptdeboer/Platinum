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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Drag and Drop Data conversion methods and DataFlavors.
 */
public class DnDData
{
    private static PLogger logger=PLogger.getLogger(DnDData.class); 
    
    public static final String VBROWSER_VRS_MIMETYPE_PREFIX = VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX;

    public static final String VBROWSER_VFS_MIMETYPE_PREFIX = VRSTypes.VBROWSER_VFS_MIMETYPE_PREFIX;

    /**
     * Any Resource can be exported as VRL.
     */
    public static final String VBROWSER_VRL_MIMETYPE = "application/vbrowser-vrl";

    /**
     * DataFlavor for Actual (Virtual) Files and (Virtual) Directories.
     */
    public static final String VBROWSER_VFSPATH_MIMETYPE = VBROWSER_VFS_MIMETYPE_PREFIX + "-path";

    /**
     * DataFlavor for actual (Virtual) Files only. No directories allowed.
     */
    public static final String VBROWSER_VFSFILE_MIMETYPE = VBROWSER_VFS_MIMETYPE_PREFIX + "-file";

    /**
     * DataFlavor for actual (Virtual) Directories only. No files allowed.
     */
    public static final String VBROWSER_VFSDIRECTORY_MIMETYPE = VBROWSER_VFS_MIMETYPE_PREFIX + "-directory";

    public static class VRLEntry implements Serializable
    {
        private static final long serialVersionUID = -1427923836527803502L;

        public VRL vrl;

        public String resourceType;

        public String mimeType;

        @Override
        public String toString()
        {
            return "VRLEntry:[vrl=" + vrl + ", resourceType=" + resourceType + ", mimeType=" + mimeType + "]";
        }
    }

    public static class VRLEntryList extends ArrayList<VRLEntry> implements Serializable
    {
        private static final long serialVersionUID = -8876989751853728967L;

        public VRLEntryList(int n)
        {
            super(n);
        }

        public List<VRL> toVRLList()
        {
            ArrayList<VRL> vrls = new ArrayList<VRL>();
            for (VRLEntry entry : this)
            {
                vrls.add(entry.vrl);
            }
            return vrls;
        }

    }; 

    /**
     * List of VRLs
     */
    public static DataFlavor flavorVRLEntryList = new DataFlavor(VBROWSER_VRL_MIMETYPE + ";class="
            + DnDUtil.mimeTypeClassName(VRLEntryList.class), "(Array)List<VRL> class");

    /***
     * List of VFS VRls contains only (remote) files or directories.
     */
    public static DataFlavor flavorVFSPaths = new DataFlavor(VBROWSER_VFSPATH_MIMETYPE + ";class="
            + DnDUtil.mimeTypeClassName(VRLEntryList.class), "VFS Paths as as list of VRLs (List<VRL>)");

    /**
     * DataFlavors which can be converted to VRLs
     */
    public static DataFlavor[] dataFlavorsToVRLs = new DataFlavor[]
    {
            DnDFlavors.javaURLFlavor, // single URL not a list. 
            flavorVFSPaths,
            flavorVRLEntryList,
            DnDFlavors.javaFileListFlavor,
            DnDFlavors.javaURIListAsTextFlavor
    };

    /**
     * DataFlavors which can be converted to VFSPaths (VFS VRLs)
     */
    public static DataFlavor[] dataFlavorsToVFSPaths = new DataFlavor[]
    {
            flavorVFSPaths,
            DnDFlavors.javaFileListFlavor
    };

    /**
     * DataFlavors which can be constructed from VRLs.
     */
    public static DataFlavor[] dataFlavorsFromVRLs = new DataFlavor[]
    {
            flavorVRLEntryList,
            DnDFlavors.javaURIListAsTextFlavor,
            DnDFlavors.javaStringFlavor,
            // If plain text is supported, all charsets must be supported. 
            // DnDFlavors.plainTextFlavor 
            // flavorJavaURL, // => VRLs are URIs not URLs 
    };
    
    /**
     * DataFlavors which can be constructed from VFSPaths. JavaFileList might
     * not work if files are remote files.
     */
    public static DataFlavor[] dataFlavorsFromVFSPaths = new DataFlavor[]
    {
            flavorVFSPaths,
            flavorVRLEntryList,
            DnDFlavors.javaURIListAsTextFlavor,
            DnDFlavors.javaStringFlavor,
            //DnDFlavors.plainTextFlavor,
            //DnDFlavors.javaFileListFlavor 
    };

    // ========================================================================
    //
    // ========================================================================
    public static void staticInit()
    {
        for (DataFlavor flav:dataFlavorsFromVFSPaths)
        {
            logger.debugPrintf("- VFSPath DataFlavor=%s\n",flav); 
        }
    }
    
    /**
     * Check whether all of the Transferable data can be converted to one or
     * more VRLs. This method returns false if some data can not be converted to
     * a VRL.
     */
    public static boolean canConvertToVRLs(Transferable t)
    {
        for (DataFlavor flav : dataFlavorsToVRLs)
        {
            if (t.isDataFlavorSupported(flav))
            {
                return true;
            }
        }

        // return true;
        return false;
    }

    /**
     * Check whether all of the Transferable date can be converted to one or
     * more VFSPaths. This could be one ore more (remote) files. This method
     * returns false if some data can not be converted to a VFS Path.
     */
    public static boolean canConvertToVFSPaths(Transferable t)
    {
        // DataFlavor flavors[]=t.getTransferDataFlavors();

        for (DataFlavor flav : dataFlavorsToVFSPaths)
        {
            if (t.isDataFlavorSupported(flav))
            {
                return true;
            }
        }

        // return true;
        return false;
    }

    public static List<VRL> getVRLsFrom(Transferable t) throws VRLSyntaxException, UnsupportedFlavorException,
            IOException
    {
        // X) Drop of a single URL 
        // Note: Firefox/IE might also provide a (java)File ending with .URL containing the actual URL. 
        if (t.isDataFlavorSupported(DnDFlavors.javaURLFlavor))
        {
            java.net.URL url  = (java.net.URL) t.getTransferData(DnDFlavors.javaURLFlavor);
            Vector<VRL> vris = new Vector<VRL>();
            vris.add(new VRL(url));
            return vris;
        }
        
        // Check custom VRLEntryList for internal Drag'n Drop:
        if (t.isDataFlavorSupported(DnDData.flavorVRLEntryList))
        {
            // II: get data:
            VRLEntryList vrls = (VRLEntryList) t.getTransferData(DnDData.flavorVRLEntryList);
            return vrls.toVRLList();
        }

        // List of URIs.
        if (t.isDataFlavorSupported(DnDFlavors.javaURIListAsTextFlavor))
        {
            List<URI> uris = DnDFlavors.getURIList(t, DnDFlavors.javaURIListAsTextFlavor); 
            Vector<VRL> vris = new Vector<VRL>();
            for (URI uri:uris)
            {
                vris.add(new VRL(uri)); 
            }
            return vris; 
        }
        
        // X) Check for drop of (Java)files from native Operating System.
        // Note: Under Windows Firefox/IE might create a local temp file named *.URL with the actual URL in it. 
        // First check or URI/URLs above then for actual (java)Files here. 
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
            List<VRL> vris = DnDData.getJavaFileListVRLs(t);

            // ---
            // Although some browser claim they can export an URL to a JavaFile
            // Some URLs are not actual files. Only return non empty lists!
            // ---

            if (vris != null)
            {
                return vris;
            }
        }
        
        // Default to Text/String: 
        if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            String str = (String) t.getTransferData(DataFlavor.stringFlavor);
            Vector<VRL> vris = new Vector<VRL>();
            vris.add(new VRL(str));
            return vris;
        }

        throw new UnsupportedFlavorException(t.getTransferDataFlavors()[0]);
    }

    /**
     * Handle Java File List. Both windows and KDE can drop actual Java Files.
     * 
     * @throws IOException
     * @throws UnsupportedFlavorException
     */
    public static List<VRL> getJavaFileListVRLs(Transferable t) throws UnsupportedFlavorException, IOException
    {
        java.util.List<File> fileList = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        Iterator<File> iterator = fileList.iterator();

        int len = fileList.size();
        if (len == 0)
        {
            return null;
        }

        List<VRL> vris = new ArrayList<VRL>(len);

        while (iterator.hasNext())
        {
            java.io.File file = (File) iterator.next();
            VRL vrl = new VRL("file", null, file.getAbsolutePath());
            vris.add(vrl);
        }

        return vris;
    }

    public String getText(Transferable t) throws UnsupportedFlavorException, IOException
    {
        if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            String str = (String) t.getTransferData(DataFlavor.stringFlavor);
            return str; 
        }
        
        throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
    }
    
}
