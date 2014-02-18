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

public class DnDData
{
    public static final String VBROWSER_VRS_MIMETYPE_PREFIX = VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX;

    public static final String VBROWSER_VFS_MIMETYPE_PREFIX = VRSTypes.VBROWSER_VFS_MIMETYPE_PREFIX;

    /**
     * Any Resource van be exported as VRL.
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
     * Always allow String. For URIs/VRLs the String representation is one or
     * more URI Strings separated by a ';'
     */
    public static DataFlavor flavorString = DataFlavor.stringFlavor;

    /**
     * Java File List for local Files. This allow dragging and dropping files
     * from and to the native operating system.
     */
    public static DataFlavor flavorJavaFileList = DataFlavor.javaFileListFlavor;

    /**
     * In KDE this is a newline separators list of URIs
     */
    public static DataFlavor flavorURIList = new DataFlavor("text/uri-list;class=java.lang.String", "URI list");

    public static DataFlavor octetStreamDataFlavor = new DataFlavor("application/octet-stream;class=java.io.InputStream", "octetStream");

    public static DataFlavor flavorVRLEntryList = new DataFlavor(VBROWSER_VRL_MIMETYPE + ";class="
            + DnDUtil.mimeTypeClassName(VRLEntryList.class), "(Array)List<VRL> class");

    public static DataFlavor flavorVFSPaths = new DataFlavor(VBROWSER_VFSPATH_MIMETYPE + ";class="
            + DnDUtil.mimeTypeClassName(VRLEntryList.class), "VFS Paths as as list of VRLs (List<VRL>)");

    public static DataFlavor[] dataFlavorsVRL = new DataFlavor[]
    {
            flavorVRLEntryList,
            flavorJavaFileList,
            flavorURIList,
            flavorString,
    };

    public static DataFlavor[] dataFlavorsVFSPaths = new DataFlavor[]
    {
            flavorVFSPaths,
            flavorJavaFileList
    };

    public static DataFlavor[] dataFlavorsVRLAndVFSPaths = new DataFlavor[]
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

    // === Object === //

    /**
     * DataFlavors from which VRL(s) can be imported !
     */
    public static boolean canConvertToVRLs(Transferable t)
    {
        for (DataFlavor flav : dataFlavorsVRLAndVFSPaths)
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
     * DataFlavors from which VFS VRL(s) can be imported !
     */
    public static boolean canConvertToVFSPaths(Transferable t)
    {
        // DataFlavor flavors[]=t.getTransferDataFlavors();

        for (DataFlavor flav : dataFlavorsVFSPaths)
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
        else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
            List<VRL> vris = DnDData.getJavaFileListVRLs(t);
            return vris;
        }
        // List of URIs.
        else if (t.isDataFlavorSupported(DnDData.flavorURIList))
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
        // else if <checkSupportedMimeType>
        // {
        // Default MimeTypes Here. Check for binary/images/etc.
        // }
        // Default to String:
        else if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            String str = (String) t.getTransferData(DataFlavor.stringFlavor);
            Vector<VRL> vris = new Vector<VRL>();
            vris.add(new VRL(str));
            return vris;

        }

        throw new UnsupportedFlavorException(t.getTransferDataFlavors()[0]);
    }

    /**
     * Handle Java File List
     * 
     * @throws IOException
     * @throws UnsupportedFlavorException
     */
    public static List<VRL> getJavaFileListVRLs(Transferable t) throws UnsupportedFlavorException, IOException
    {
        java.util.List<File> fileList = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        Iterator<File> iterator = fileList.iterator();

        int len = fileList.size();

        List<VRL> vris = new ArrayList<VRL>(len);

        while (iterator.hasNext())
        {
            java.io.File file = (File) iterator.next();

            // Debug("name="+file.getName());
            // Debug("url="+file.toURI().toString());
            // Debug("path="+file.getAbsolutePath());

            VRL vrl = new VRL("file", null, file.getAbsolutePath());
            // String type=(file.isDirectory()?VRS.DIR_TYPE:VRS.FILE_TYPE);

            // String
            // mimeType=UIGlobal.getMimeTypes().getMimeType(vrl.getPath());

            vris.add(vrl);
        }

        return vris;
    }

}
