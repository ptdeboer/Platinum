package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsIOException;
import nl.esciencecenter.vbrowser.vrs.exceptions.XMLDataException;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRSNode;
import nl.esciencecenter.vbrowser.vrs.infors.InfoResourceNode;
import nl.esciencecenter.vbrowser.vrs.infors.VInfoResourceFolder;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * XML Data Utility to parse and create VRS Data Types from and to XML.
 */
public class XMLData
{
    private static final ClassLogger logger = ClassLogger.getLogger(XMLData.class);

    // ========
    // Instance
    // ========

    protected XmlMapper xmlMapper;

    protected VRSContext vrsContext;

    public XMLData(VRSContext context)
    {
        xmlMapper = new XmlMapper();
        vrsContext = context;
    }

    public String toXML(AttributeSet attrSet) throws Exception
    {
        XMLAttributeSet xmlProps = toXMLAttributeSet(attrSet);
        String xml = xmlMapper.writeValueAsString(xmlProps);
        return xml;
    }

    public XMLAttributeSet toXMLAttributeSet(AttributeSet attrSet) throws XMLDataException
    {
        try
        {
            attrSet = checkSerialization(attrSet);
            XMLAttributeSet xmlProps = new XMLAttributeSet(attrSet);
            return xmlProps;

        }
        catch (Exception e)
        {
            throw new XMLDataException(e.getMessage(), e);
        }
    }

    public VRSProperties checkSerialization(VRSProperties props) throws XMLDataException
    {
        for (String key : props.keySet())
        {
            Object value  = props.get(key);

            // VRL still not 100% serializable, use URI encoding.
            if (value instanceof VRL)
            {
                try
                {
                    java.net.URI uri;
                    uri = ((VRL) value).toURI();
                    value=uri;
                    props.set(key,value);
                }
                catch (URISyntaxException e)
                {
                    throw new XMLDataException(e.getMessage(), e);
                }
            }

            // Attribute value should be serializable:
            if ((value != null) && (!xmlMapper.canSerialize(value.getClass())))
            {
                logger.errorPrintf("XMLMapper can't serialize attribute:'%s' with class:%s\n", key, value.getClass());
                throw new XMLDataException("XMLMapper can not serialize field:'" + key + "' with class=" + value.getClass());
            }

        }

        return props;
    }
    
    public AttributeSet checkSerialization(AttributeSet attrs) throws XMLDataException
    {
        for (String key : attrs.keySet())
        {
            Attribute attr = attrs.get(key);
            AttributeType type = attr.getType();
            Object value = attr.getValue();

            // VRL still not 100% serializable, use URI encoding.
            if (value instanceof VRL)
            {
                try
                {
                    java.net.URI uri;
                    uri = ((VRL) value).toURI();
                    attr.setValue(uri);
                    value = uri;
                }
                catch (URISyntaxException e)
                {
                    throw new XMLDataException(e.getMessage(), e);
                }
            }

            // Attribute value should be serializable:
            if ((value != null) && (!xmlMapper.canSerialize(value.getClass())))
            {
                logger.errorPrintf("XMLMapper can't serialize attribute:<%s>:'%s' with class:%s\n", type, key, value.getClass());
                throw new XMLDataException("XMLMapper can not serialize field:'" + key + "' with class=" + value.getClass());
            }

        }

        return attrs;
    }

    public AttributeSet parseAttributeSet(String xmlString) throws XMLDataException
    {
        try
        {
            XMLAttributeSet xmlAttrs;
            xmlAttrs = xmlMapper.readValue(xmlString, XMLAttributeSet.class);
            AttributeSet attrSet = xmlAttrs.toAttributeSet();
            return attrSet;
        }
        catch (Exception e)
        {
            throw new XMLDataException(e.getMessage(), e);
        }
    }

    public XMLResourceNode createXMLResourceNode(InfoRSNode infoNode, boolean recursive) throws VrsException
    {
        XMLResourceNode rootXmlNode = new XMLResourceNode(infoNode.getResourceType());
        AttributeSet attrSet = infoNode.getAttributeSet();
        rootXmlNode.setXMLAttributes(new XMLAttributeSet(this.checkSerialization(attrSet)));

        if (recursive)
        {
            addSubNodesTo(rootXmlNode, infoNode, recursive);
        }
        return rootXmlNode;
    }

    protected XMLResourceNode addSubNodesTo(XMLResourceNode xmlNode, InfoRSNode infoNode, boolean recursive) throws VrsException, XMLDataException
    {
        List<? extends InfoResourceNode> subNodes = infoNode.listResourceNodes();
        List<XMLResourceNode> xmlSubNodes = null;

        if (subNodes != null)
        {
            xmlSubNodes = new ArrayList<XMLResourceNode>();
            for (InfoResourceNode subNode : subNodes)
            {
                XMLResourceNode subXmlNode = this.createXMLResourceNode(subNode, recursive);
                xmlSubNodes.add(subXmlNode);
            }
        }

        xmlNode.setSubNodes(xmlSubNodes);

        return xmlNode;
    }

    public String toXML(InfoRSNode infoNode) throws VrsException
    {
        XMLResourceNode xmlNode = this.createXMLResourceNode(infoNode, true);
        String xml;

        try
        {
            xml = xmlMapper.writeValueAsString(xmlNode);
            return xml;
        }
        catch (JsonProcessingException e)
        {
            throw new VrsIOException(e);
        }
    }

    /** 
     * Parse XML String which should be serialized ResourceNodes and add these parsed nodes. 
     */
    public void addXMLResourceNodesTo(InfoRSNode parentNode, String xmlString) throws XMLDataException
    {
        try
        {
            XMLResourceNode xmlRootNode = xmlMapper.readValue(xmlString, XMLResourceNode.class);
            addXMLResourceNodesTo(parentNode, xmlRootNode);
        }
        catch (IOException e)
        {
            throw new XMLDataException(e.getMessage(), e);
        }
    }

    /** 
     * Add sub-nodes of XMLResourceNode to actual InfoResourceNode. 
     * Parent node 'xmlRootNode' is not added.  
     */
    protected void addXMLResourceNodesTo(InfoRSNode parentNode, XMLResourceNode xmlRootNode) throws XMLDataException
    {
        List<XMLResourceNode> subNodes = xmlRootNode.getSubNodes();

        if (subNodes == null)
        {
            return;
        }
        
        VInfoResourceFolder resourceFolder;
        
        if (parentNode instanceof VInfoResourceFolder)
        {
            resourceFolder=(VInfoResourceFolder)parentNode; 
        }
        else
        {
            throw new XMLDataException("Incompatible types, cannot add new ResourceNodes to non ResourceFolder type:"+parentNode); 
        }
        
        for (XMLResourceNode xmlNode : subNodes)
        {
            try
            {
                // construct ResourceNode Structure:
                InfoResourceNode subNode = InfoResourceNode.createResourceNode(parentNode, 
                        xmlNode.getResourceType(), 
                        xmlNode.getXMLAttributes().toAttributeSet());
                
                resourceFolder.addSubNode(subNode);
                addXMLResourceNodesTo(subNode, xmlNode);
            }
            catch (Exception e)
            {
                throw new XMLDataException(e.getMessage(), e);
            }
        }
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
            System.err.printf("=== XML Parse Error ===\n%s\n",input);
            throw new RuntimeException(e);
        }
    }

    public String toXML(VRSProperties props) throws Exception
    {
        XMLProperties xmlProps = toXMLProperties(props);
        String xml = xmlMapper.writeValueAsString(xmlProps);
        return xml;
    }

    public XMLProperties toXMLProperties(VRSProperties props) throws XMLDataException
    {
        try
        {
            props = checkSerialization(props);
            XMLProperties xmlProps = new XMLProperties(props);
            return xmlProps;

        }
        catch (Exception e)
        {
            throw new XMLDataException(e.getMessage(), e);
        }
    }

    public VRSProperties parseVRSProperties(String xmlString) throws XMLDataException
    {
        try
        {
            XMLProperties xmlProps;
            xmlProps = xmlMapper.readValue(xmlString, XMLProperties.class);
            VRSProperties props  = xmlProps.toVRSProperties(); 
            return props;
        }
        catch (Exception e)
        {
            throw new XMLDataException(e.getMessage(), e);
        }
    }
    
}
