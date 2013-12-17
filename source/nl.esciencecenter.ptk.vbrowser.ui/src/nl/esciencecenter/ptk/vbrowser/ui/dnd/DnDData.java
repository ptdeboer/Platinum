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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class DnDData
{
    
    // public static class ViewNodeList extends ArrayList<ViewNode> {};
    //
    // public static DataFlavor ViewNodeDataFlavor = new
    // DataFlavor(ViewNode.class, "ViewNode class");
    //
    // public static DataFlavor ViewNodeListDataFlavor = new
    // DataFlavor(ViewNodeList.class, "List<ViewNode> class");
    //
    
    public static class VRLList extends ArrayList<VRL> {};

    /** One ore more URIstrings seperated by a ';' */
    public static DataFlavor flavorString = DataFlavor.stringFlavor;

    public static DataFlavor flavorJavaFileList = DataFlavor.javaFileListFlavor;

    /**
     * In KDE this is a newline seperators list of URIs
     */
    public static DataFlavor flavorURIList = new DataFlavor("text/uri-list;class=java.lang.String", "uri list");

    public static DataFlavor octetStreamDataFlavor = new DataFlavor(
            "application/octet-stream;class=java.io.InputStream", "octetStream");

    // public static DataFlavor VRLFlavor = new DataFlavor(VRL.class,
    // "VRL class");

    public static DataFlavor flavorVRLList = new DataFlavor(VRLList.class, "(Array)List<VRL> class");

    public static DataFlavor[] dataFlavorsVRL = new DataFlavor[]
        {
            // ViewNodeDataFlavor,
            // ViewNodeListDataFlavor,
            // VRLFlavor,
            flavorVRLList, 
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
        // DataFlavor flavors[]=t.getTransferDataFlavors();

        for (DataFlavor flav : dataFlavorsVRL)
            if (t.isDataFlavorSupported(flav))
                return true;

        // return true;
        return false;
    }

    // public static boolean isVRLListFlavor(DataFlavor[] flavors)
    // {
    // DataFlavor[] dataFlavors = dataFlavorsVRL;
    //
    // for (int i = 0; i < flavors.length; i++)
    // {
    // for (int j = 0; j < dataFlavors.length; j++)
    //
    // if (dataFlavors[j].equals(flavors[i]))
    // {
    // return true;
    // }
    //
    // }
    // return false;
    // // return true;
    // }

    public static List<VRL> getVRLsFrom(Transferable t) throws VRLSyntaxException, UnsupportedFlavorException,
            IOException
    {
        // Known URI/File type flavors:

        // if (t.isDataFlavorSupported(DnDData.ViewNodeListDataFlavor))
        // {
        // // II: get data:
        // ViewNode refs[] = (ViewNode[])
        // t.getTransferData(DnDData.ViewNodeListDataFlavor);
        // return toVRLs(refs);
        // }
        // else if (t.isDataFlavorSupported(DnDData.ViewNodeDataFlavor))
        // {
        // // II: get data:
        // ViewNode refs[] = new ViewNode[1];
        // refs[0] = (ViewNode) t.getTransferData(DnDData.ViewNodeDataFlavor);
        // return toVRLs(refs);
        // }
        if (t.isDataFlavorSupported(DnDData.flavorVRLList))
        {
            // II: get data:
            List<VRL> vris = (List<VRL>) t.getTransferData(DnDData.flavorVRLList);
            return vris;
        }
        // else if (t.isDataFlavorSupported(DnDData.VRLFlavor))
        // {
        // // II: get data:
        // List<VRL> vris=new ArrayList<VRL>(0);
        // vris.add((VRL)t.getTransferData(DnDData.VRLFlavor));
        // return vris;
        // }
        // drops from Windows create these objects (thanks to swing) !:
        else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
            List<VRL> vris = DnDData.getJavaFileListVRLs(t);
            return vris;
        }
        //
        // 
        //
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
        //
        // Default MimeTypes :
        // Todo: DataStreams, etc:

        else if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            String str = (String) t.getTransferData(DataFlavor.stringFlavor);
            Vector<VRL> vris = new Vector<VRL>();
            vris.add(new VRL(str));
            return vris;

        }

        throw new UnsupportedFlavorException(t.getTransferDataFlavors()[0]);
    }

    private static List<VRL> toVRLs(ViewNode[] refs)
    {
        List<VRL> list = new ArrayList<VRL>(refs.length);
        for (int i = 0; i < refs.length; i++)
            list.add(refs[i].getVRL());

        return list;
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
