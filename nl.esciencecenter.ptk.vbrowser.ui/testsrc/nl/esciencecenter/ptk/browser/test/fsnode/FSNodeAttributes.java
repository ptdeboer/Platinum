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

package nl.esciencecenter.ptk.browser.test.fsnode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.presentation.IPresentable;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSource;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;

/**
 * Combined Attribute+Presentation example for FSNode    
 */
public class FSNodeAttributes implements AttributeSource, IPresentable
{
    public static enum FileAttribute
    {
        ICON,NAME,RESOURCE_TYPE,PATH,BASENAME,DIRNAME,EXTENSION,LENGTH,MIMETYPE,
        MODIFICATION_TIME,MODIFICATION_TIME_STRING,ACCESS_TIME,CREATION_TIME,PERMISSIONS,PERMISSIONS_STRING; 
        // ---
        
        private String name; 
        
        private FileAttribute()
        {
            String enumStr=this.toString(); 
            // use enum as Name
            this.name=enumStr.substring(0,1).toUpperCase()+enumStr.substring(1,enumStr.length()).toLowerCase(); 
        }
        
        public String getName()
        {
            return name;  
        }
        
        // === static=== 
        private static FileAttribute defaultFileAttributes[]=new FileAttribute[]
                    {   ICON,NAME,LENGTH,MIMETYPE,MODIFICATION_TIME_STRING,PERMISSIONS_STRING }; 

        public static List<String> getStringValues()
        {
            FileAttribute[] values = defaultFileAttributes; 
            StringList strValues=new StringList(values.length);
            for (int i=0;i<values.length;i++)
                strValues.add(values[i].getName()); 
            return strValues; 
        }
    }
    
    private static Presentation defaultPresentation;

    static
    {
        initStatic(); 
    }
    
    private static void initStatic()
    {
        defaultPresentation=Presentation.createDefault(); 
        
        defaultPresentation.setPreferredContentAttributeNames(FileAttribute.getStringValues()); 
        defaultPresentation.setIconAttributeName(FileAttribute.ICON.getName());
        
//        Presentation.storeSchemeType(FSNode.FILE_SCHEME,ResourceType.FILE.toString(),defaultPresentation);
//        Presentation.storeSchemeType(FSNode.FILE_SCHEME,ResourceType.DIRECTORY.toString(),defaultPresentation);
    }
    
    // ========================================================================
    
    // ========================================================================
    
    private FSNode anyFile;

    public FSNodeAttributes(FSNode anyFile)
    {
        this.anyFile=anyFile; 
    }
    
    @Override
    public List<String> getAttributeNames()
    {
        return FileAttribute.getStringValues(); 
    }

    @Override
    public Attribute getAttribute(String name)
    {
        if (name==null)
            return null; 
        if (name.equals(""))
            return null; 
        
        if (name.equalsIgnoreCase(""+FileAttribute.RESOURCE_TYPE))
            return new Attribute(name,anyFile.isFile()?"File":"Dir"); 

        if (name.equalsIgnoreCase(""+FileAttribute.NAME))
            return new Attribute(name,anyFile.getBasename());

        if (name.equalsIgnoreCase(""+FileAttribute.BASENAME))
            return new Attribute(name,anyFile.getBasename());

        if (name.equalsIgnoreCase(""+FileAttribute.DIRNAME))
            return new Attribute(name,anyFile.getDirname());

        if (name.equalsIgnoreCase(""+FileAttribute.MIMETYPE))
        {
            String mime=MimeTypes.getDefault().getMimeType(anyFile.getBasename()); 
            return new Attribute(name,mime);
        }
        
        try
        {
            if (name.equalsIgnoreCase(""+FileAttribute.MODIFICATION_TIME))
                return new Attribute(name,anyFile.getModificationTime());
            
            if (name.equals(""+FileAttribute.MODIFICATION_TIME_STRING))
                return new Attribute(name,Presentation.createDate(anyFile.getModificationTime()));
            
            if (name.equalsIgnoreCase(""+FileAttribute.LENGTH))
                return new Attribute(name,anyFile.getFileSize());
        }
        catch (IOException e)
        {
            return new Attribute(name,"?"); 
        }
        
        if (name.equalsIgnoreCase(""+FileAttribute.PATH))
        {
            return new Attribute(name,anyFile.getPathname());
        }
        
        return null; 
    }

    @Override
    public List<Attribute> getAttributes(String names[])
    {
        if (names==null)
            return null;  
        
        ArrayList<Attribute> attrs=new ArrayList<Attribute>(names.length); 
        for (int i=0;i<names.length;i++)
            attrs.add(getAttribute(names[i]));
        return attrs; 
    }

    @Override
    public Presentation getPresentation()
    {
      return defaultPresentation; 
    }

}
