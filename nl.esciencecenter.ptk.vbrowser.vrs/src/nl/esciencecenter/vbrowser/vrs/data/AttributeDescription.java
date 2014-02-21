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

package nl.esciencecenter.vbrowser.vrs.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.esciencecenter.ptk.data.HashSetList;
import nl.esciencecenter.ptk.data.StringList;

public class AttributeDescription
{
    public static List<AttributeDescription> createList(StringList list, AttributeType type, boolean editable)
    {
        ArrayList<AttributeDescription> descs=new ArrayList<AttributeDescription>(); 
        
        for (String name:list)
        {
            descs.add(new AttributeDescription(name,type,editable,"Attribute "+name));
             
        }
        return null;
    }
    
    protected String name; 
    
    protected Set<AttributeType> allowedTypes;  
    
    protected boolean isEditable; 
    
    protected String descriptionText=null; 
    
    AttributeDescription(String name)
    {
        this.name=name; 
        this.allowedTypes=new HashSetList<AttributeType>(); 
        this.allowedTypes.add(AttributeType.ANY); 
        this.isEditable=false; 
    }
    
    public AttributeDescription(String name, AttributeType type, boolean editable,String description)
    {
        this.name=name; 
        this.allowedTypes=new HashSetList<AttributeType>(); 
        this.allowedTypes.add(type); 
        this.isEditable=false; 
        this.descriptionText=description;
    }

    public AttributeDescription(String name, AttributeType[] types, boolean editable)
    {
        this.name=name; 
        this.allowedTypes=new HashSetList<AttributeType>(); 
        for (AttributeType type:types)
        {
            this.allowedTypes.add(type);
        }
        this.isEditable=false; 
    }

    public String getName()
    {
        return name;
    }
    
    public Set<AttributeType> getAllowedTypes()
    {
        return allowedTypes; 
    }
    
    public boolean isEditable()
    {
        return isEditable;
    }


}
