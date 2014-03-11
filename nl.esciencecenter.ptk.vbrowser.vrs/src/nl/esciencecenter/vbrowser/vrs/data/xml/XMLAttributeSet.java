package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * XML Proxy Object for AttributeSet;
 */
public class XMLAttributeSet
{
    /**
     * XML Proxy Object for Attribute.
     */
    @JsonPropertyOrder(
    { "attributeType", "attributeName", "attributeValue", "attributeEnumValues" })
    public static class XMLAttribute
    {

        public XMLAttribute()
        {
        }

        public XMLAttribute(Attribute attr)
        {
            this.attributeType = attr.getType();
            this.attributeName = attr.getName();
            this.attributeValue = attr.getValue();
            this.attributeEnumValues = attr.getEnumValues();
        }

        @JacksonXmlProperty(localName = "attributeName", isAttribute = true)
        public String attributeName;

        @JacksonXmlProperty(localName = "attributeType", isAttribute = true)
        public AttributeType attributeType;

        @JacksonXmlProperty(localName = "attributeValue")
        public Object attributeValue;

        @JacksonXmlProperty(localName = "attributeEnumValues")
        public String attributeEnumValues[];

        public Attribute toAttribute() throws Exception
        {
            if (attributeType==null)
            {
                throw new NullPointerException("Internal Error:No AttributeType!"); 
            }
            
            // when parsed from XML, default type is String.
            if (attributeValue instanceof String)
            {
                return AttributeUtil.parseFromString(attributeType, attributeName, (String) attributeValue, attributeEnumValues);
            }
            else
            {
                return AttributeUtil.createFrom(attributeType, attributeName, attributeValue, attributeEnumValues);
            }
        }
        
        @Override
        public String toString()
        {
            return "XMLAttribute[attributeName=" + attributeName 
                    + ", attributeType=" + attributeType 
                    + ", attributeValue=" + attributeValue 
                    + ", attributeEnumValues=" 
                    + Arrays.toString(attributeEnumValues) + "]";
        }

    }

    @JacksonXmlProperty(localName = "setName", isAttribute = true)
    protected String setName;

    @JacksonXmlProperty(localName = "attributes")
    protected XMLAttribute attributes[] = null; 

    public XMLAttributeSet()
    {
    }

    public XMLAttributeSet(AttributeSet attrs)
    {
        init(attrs.getName(), attrs);
    }

    @JacksonXmlProperty(localName = "setName", isAttribute = true)
    public void setSetName(String name)
    {
        this.setName = name;
    }

    @JacksonXmlProperty(localName = "setName", isAttribute = true)
    public String getSetName()
    {
        return setName;
    }

    @JsonIgnore
    protected void init(String name, Map<String, Attribute> attrs)
    {
        this.setName = name;
        setAttributes(attrs);
    }

    @JacksonXmlProperty(localName = "attributes")
    public void setXMLAttributes(XMLAttribute attrs[])
    {
        attributes=attrs; 
    }

    @JacksonXmlProperty(localName = "attributes")
    public XMLAttribute[] getXMLAttributes()
    {
        return attributes;
    }

    @JsonIgnore
    public void setAttributes(Map<String, Attribute> attrs)
    {
        if (attrs==null)
        {
            this.attributes=null; 
        }
        
        attributes=new XMLAttribute[attrs.size()]; 

        String keys[]=attrs.keySet().toArray(new String[0]); 
        
        for (int i=0;i<keys.length;i++)
        {
            attributes[i]=new XMLAttribute(attrs.get(keys[i])); 
        }
    }

    @JsonIgnore
    public AttributeSet toAttributeSet() throws Exception
    {
        AttributeSet attrSet = new AttributeSet(this.getSetName());
        
        if ((attributes==null) || (attributes.length<=0)) 
        {
            return null; 
        }
        
        for (XMLAttribute attr:attributes) 
        {
            attrSet.put(attr.attributeName,attr.toAttribute());
        }

        return attrSet;
    }

}
