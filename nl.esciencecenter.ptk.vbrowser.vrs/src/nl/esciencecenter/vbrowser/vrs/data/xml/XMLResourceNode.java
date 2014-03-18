package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * XML proxy class for the ResourceNode family.<br>
 * This class represent the persistent part of a ResourceNode and functions as
 * Factory class to create the actual ResourcNode sub-class.
 */
@JsonPropertyOrder({ "resourceType", "infoAttributes", "subNodes" })
public class XMLResourceNode
{
    @JacksonXmlProperty(localName = "infoAttributes")
    protected XMLAttributeSet infoAttributes;

    // Use unwrapped elements, and use 'subNode' as elements name. Do not use 'subNodes' as element name nor as element wrapper.  
    @JacksonXmlProperty(localName = "subNode")
    @JacksonXmlElementWrapper(localName = "subNode", useWrapping = false)
    protected ArrayList<XMLResourceNode> subNodes;

    @JacksonXmlProperty(localName = "resourceType", isAttribute = true)
    protected String resourceType;

    public XMLResourceNode()
    {
    }

    public XMLResourceNode(String resourceType)
    {
        this.resourceType = resourceType;
    }

    @JacksonXmlProperty(localName = "resourceType", isAttribute = true)
    public void setResourceType(String type)
    {
        resourceType = type;
    }

    @JacksonXmlProperty(localName = "resourceType", isAttribute = true)
    public String getResourceType()
    {
        return resourceType;
    }
    
    @JsonIgnore // use ArrayList<> field 
    public void setSubNodes(Collection<? extends XMLResourceNode> nodes)
    {
        if (nodes == null)
        {
            // clear
            this.subNodes = null;
        }
        else
        {
            // clone
            this.subNodes = new ArrayList<XMLResourceNode>();
            this.subNodes.addAll(nodes);
        }
    }
    
    @JsonIgnore // use ArrayList<> field 
    public List<XMLResourceNode> getSubNodes()
    {
        return subNodes;
    }

    @JacksonXmlProperty(localName = "infoAttributes")
    public void setXMLAttributes(XMLAttributeSet xmlAttrs)
    {
        this.infoAttributes = xmlAttrs;
    }

    @JacksonXmlProperty(localName = "infoAttributes")
    public XMLAttributeSet getXMLAttributes()
    {
        return infoAttributes;
    }

}
