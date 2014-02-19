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
import java.util.Vector;

import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.ui.dnd.DnDFlavors;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDData.VRLEntry;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.DnDData.VRLEntryList;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * List of Resource VRLs including some type information.
 */
public class VRLEntryListTransferable implements Transferable
{

    public static VRLEntryListTransferable createFrom(ViewNode[] nodes)
    {
        if (nodes == null)
        {
            return null;
        }

        int n = nodes.length;
        VRLEntryList vris = new VRLEntryList(n);
        for (int i = 0; i < n; i++)
        {
            VRLEntry entry = new VRLEntry();
            entry.vrl = nodes[i].getVRL();
            entry.resourceType = nodes[i].getResourceType();
            entry.mimeType = nodes[i].getMimeType();
            vris.add(entry);
        }
        return new VRLEntryListTransferable(vris);
    }

    // ========================================================================
    // Instance
    // ========================================================================

    protected VRLEntryList vris = null;

    public VRLEntryListTransferable(VRLEntryList vris)
    {
        this.vris = vris;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (!isDataFlavorSupported(flavor))
        {
            throw new UnsupportedFlavorException(flavor);
        }

        if (flavor.equals(DnDData.flavorVFSPaths))
        {
            if (isAllVFSPaths())
            {
                return vris;
            }
            else
            {
                DnDUtil.errorPrintf("Requested for VFSPaths but not all VRLs are VFS Paths!\n");
            }
            return vris; // filter ?
        }

        if (flavor.equals(DnDData.flavorVRLEntryList))
        {
            return vris;
        }

        //
        // KDE and Web browser drag and drop: ask for URIs as semicolon
        // separated String list.
        //
        if ((flavor.equals(DnDFlavors.uriListAsTextFlavor)) || (flavor.equals(DataFlavor.stringFlavor)))
        {
            // export as newline separated string
            // to mimic KDE's newline separated uriList flavor !
            String sepStr = "\n";

            // String flavor: use ';' as separator ! ;
            if (flavor.equals(DataFlavor.stringFlavor))
                sepStr = ";";

            // I can export local file
            String urisstr = "";

            for (int i=0;i<vris.size();i++)
            {
                VRL vri = vris.get(i).vrl;
                // local files are dropped:
                if (vri.hasScheme(FSNode.FILE_SCHEME))
                {
                    // create local file path (leave out hostname!)
                    urisstr += "file://" + vri.getPath();
                }
                else
                {
                    urisstr += vri.toString();
                }
                
                if (i+1<vris.size())
                {
                    urisstr+=sepStr; 
                }
                
            }
            return urisstr;
        }

        if (flavor.equals(DataFlavor.javaFileListFlavor))
        {
            java.util.Vector<File> fileList = new Vector<File>();

            for (VRLEntry ref : vris)
            {
                VRL vri = ref.vrl;

                if (vri.hasScheme(FSNode.FILE_SCHEME))
                {
                    File file = new File(vri.getPath());
                    fileList.add(file);
                }
                else
                {
                    DnDUtil.errorPrintf("Cannot export remote file as local file flavor:%s\n", vri);
                    ;// cannot export remote file as local files !
                }
            }
            return fileList;
        }

        DnDUtil.errorPrintf("VRLEntryList:DataFlavor not supported:%s\n", flavor);
        throw new UnsupportedFlavorException(flavor); 
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        boolean isVFSPathList = allVFSPaths(vris);

        if (isVFSPathList)
        {
            // explicitly include VFS Paths:
            return DnDData.dataFlavorsFromVFSPaths;
        }
        else
        {
            // Mixed URIs or non File type nodes: can only export as VRLs
            return DnDData.dataFlavorsFromVRLs;
        }
    }

    public boolean isAllVFSPaths()
    {
        return this.allVFSPaths(vris);
    }

    private boolean allVFSPaths(VRLEntryList vris)
    {
        for (VRLEntry entry : vris)
        {
            boolean vfsType = StringUtil.equals(entry.resourceType, VRSTypes.FILE_TYPE, VRSTypes.DIR_TYPE);
            if (!vfsType)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        DnDUtil.debugPrintf("VRLEntryList:isDataFlavorSupported:%s\n", flavor);

        for (DataFlavor flav : getTransferDataFlavors())
        {
            if (flav.equals(flavor))
            {
                return true;
            }
        }
        // return true;
        return false;
    }

    public String toString()
    {
        String str = "{vriList:[";

        for (int i = 0; i < vris.size(); i++)
        {
            str += vris.get(i);
            if (i + 1 < vris.size())
                str += ",";
        }

        return str + "]}";
    }

}
