package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.VRSProperties;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Jackson annotated properties. <br>
 * XML Proxy class for any Map<String,Object> Properties based class.
 */
@JsonPropertyOrder(
{ "propertiesName", "properties" })
public class XMLProperties
{
    protected String setName;

    protected Map<String, Object> properties;

    public XMLProperties()
    {
        // empty, needed for jackson.
    }

    public XMLProperties(String name, Map<String, Object> props)
    {
        this.setName = name;
        this.properties = props;
    }

    public XMLProperties(VRSProperties props)
    {
        this.setName(props.getName());
        this.setProperties(props.getProperties());
    }

    @JacksonXmlProperty(localName = "properties")
    public void setProperties(Map<String, Object> props)
    {
        this.properties = props;
    }

    @JacksonXmlProperty(localName = "properties")
    public Map<String, Object> getProperties()
    {
        return properties;
    }

    @JacksonXmlProperty(localName = "propertiesName", isAttribute = true)
    public void setName(String setName)
    {
        this.setName = setName;
    }

    @JacksonXmlProperty(localName = "propertiesName", isAttribute = true)
    public String getName()
    {
        return this.setName;
    }

    public VRSProperties toVRSProperties()
    {
        return new VRSProperties(this.setName, this.getProperties(), false);
    }
}