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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Drag and Drop Data conversion methods and DataFlavors.
 */
public class DnDData
{
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

    public static class VRLEntry
    {
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
     * For URIs/VRLs the String representation is one or more URI Strings
     * separated by a ';'
     */
    public static DataFlavor flavorString = DataFlavor.stringFlavor;

    /**
     * Java File List for local Files. This allow dragging and dropping files
     * from and to the native operating system.
     */
    public static DataFlavor flavorJavaFileList = DataFlavor.javaFileListFlavor;

    /**
     * In KDE this is a list of URI seperated by newlines;
     */
    public static DataFlavor flavorURIList = new DataFlavor("text/uri-list;class=java.lang.String", "URI list");

    /**
     * Default binary stream mime-type.
     */
    public static DataFlavor octetStreamDataFlavor = new DataFlavor("application/octet-stream;class=java.io.InputStream", "octetStream");

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
    public static DataFlavor[] dataFlavorsToVRL = new DataFlavor[]
    {
            flavorVFSPaths,
            flavorVRLEntryList,
            flavorJavaFileList,
            flavorURIList
    };

    /**
     * DataFlavors which can be converted to VFSPaths (VFS VRLs)
     */
    public static DataFlavor[] dataFlavorsToVFSPaths = new DataFlavor[]
    {
            flavorVFSPaths,
            flavorJavaFileList
    };

    /**
     * DataFlavors which can be constructed from VRLs.
     */
    public static DataFlavor[] dataFlavorsFromVRL = new DataFlavor[]
    {
            flavorVRLEntryList,
            flavorURIList,
            flavorString,
    };

    /**
     * DataFlavors which can be constructed from VFSPaths. JavaFileList might
     * not work if files are remote files.
     */
    public static DataFlavor[] dataFlavorsFromVFSPaths = new DataFlavor[]
    {
            flavorVFSPaths,
            flavorVRLEntryList,
            flavorJavaFileList,
            flavorURIList,
            flavorString,
    };

    // ========================================================================
    //
    // ========================================================================

    /**
     * Check whether all of the Transferable data can be converted to one or
     * more VRLs. This method returns false if some data can not be converted to
     * a VRL.
     */
    public static boolean canConvertToVRLs(Transferable t)
    {
        for (DataFlavor flav : dataFlavorsToVRL)
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
     * Check whether all of teh Transferable date can be converted to one or
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
        // Check custom VRLEntryList for internal Drag'n Drop:
        if (t.isDataFlavorSupported(DnDData.flavorVRLEntryList))
        {
            // II: get data:
            VRLEntryList vrls = (VRLEntryList) t.getTransferData(DnDData.flavorVRLEntryList);
            return vrls.toVRLList();
        }
        // Check for drop of (Java)files from native Operating System. Works
        // under windows:
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
            List<VRL> vris = DnDData.getJavaFileListVRLs(t);

            // ---
            // Although some browser claim they can export an URL to a JavaFile
            // Some URLs are not actual files. Only return non empty lists!
            // ---

            if (vris != null)
                return vris;
        }

        // List of URIs.
        if (t.isDataFlavorSupported(DnDData.flavorURIList))
        {
            String urilist = (String) t.getTransferData(DnDData.flavorURIList);

            Scanner scanner = new Scanner(urilist.trim());

            Vector<VRL> vris = new Vector<VRL>();

            while (scanner.hasNextLine())
            {
                String lineStr = scanner.nextLine();

                try
                {
                    vris.add(new VRL(lineStr));
                }
                catch (VRLSyntaxException e)
                {
                    DnDUtil.errorPrintf("DnDData: Failed to parse:%s\nException=%s\n", lineStr, e);
                    // Be robust: continue;
                }
            }

            return vris;
        }

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
        
        return null; 
    }
    
}
