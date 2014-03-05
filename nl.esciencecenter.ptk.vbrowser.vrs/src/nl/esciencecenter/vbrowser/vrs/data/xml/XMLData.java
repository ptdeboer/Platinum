package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * XML Data Utility to parse and create VRS Data Types from and to XML.
 */
public class XMLData
{
    private static final ClassLogger logger=ClassLogger.getLogger(XMLData.class); 
    
    // ========
    // Instance
    // ========
    
    protected XmlMapper xmlMapper;

    /**
     * Jackson annotated properties. <br>
     * XML Proxy class for any Map<String,Object> Properties based class.
     */
    @JsonPropertyOrder({ "propertiesName", "properties" })
    public static class XMLProperties
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

        @JacksonXmlProperty(localName = "propertiesName")
        public void setName(String setName)
        {
            this.setName = setName;
        }

        @JacksonXmlProperty(localName = "propertiesName")
        public String getName()
        {
            return this.setName;
        }
    }

    public XMLData()
    {
        xmlMapper = new XmlMapper();
    }

    public String toXML(VRSProperties vrsProps) throws IOException
    {
        Map<String, Object> props = vrsProps.getProperties();
        
        canSerialize(props); 
        
        XMLProperties xmlProps = new XMLProperties(vrsProps.getName(),props);
        String xml = xmlMapper.writeValueAsString(xmlProps);

        return xml;
    }

    private void canSerialize(Map<String, Object> props) throws IOException
    {
        for (String key:props.keySet())
        {
            Object value=props.get(key); 
            if (!xmlMapper.canSerialize(value.getClass()))
            {
                logger.errorPrintf("XMLMapper can't serialize field:'%s' with class:%s\n", key,value.getClass()); 
                throw new IOException("XMLMapper can not serialize field:'"+key+"' with class="+value.getClass());
            }
        }
    }

    public VRSProperties createVRSProperties(String xmlString) throws JsonParseException, JsonMappingException, IOException
    {
        XMLProperties xmlProps = xmlMapper.readValue(xmlString, XMLProperties.class);
        Map<String, Object> props = xmlProps.getProperties();
        VRSProperties vrsProps = new VRSProperties(xmlProps.getName(),props);

        return vrsProps;
    }

    public static String prettyFormat(String input, int indent)
    {
        try
        {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
