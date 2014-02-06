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
