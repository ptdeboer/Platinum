package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * XML Proxy Object for AttributeSet;
 */
public class XMLAttributeSet {

    /**
     * XML Proxy Object for Attribute.
     */
    @JsonPropertyOrder({ "attributeType", "attributeName", "attributeValue", "attributeEnumValues" })
    public static class XMLAttribute {

        public XMLAttribute() {
        }

        public XMLAttribute(Attribute attr) {
            this.attributeType = attr.getType();
            this.attributeName = attr.getName();
            this.attributeValue = attr.getValue();
            this.attributeEnumValues = attr.getEnumValues();
        }

        @JacksonXmlProperty(localName = "attributeName", isAttribute = true)
        public String attributeName;

        @JacksonXmlProperty(localName = "attributeType", isAttribute = true)
        public AttributeType attributeType;

        @JacksonXmlProperty(localName = "attributeEnumValue")
        @JacksonXmlElementWrapper(localName = "attributeEnumValue", useWrapping = false)
        public String attributeEnumValues[];

        @JacksonXmlProperty(localName = "attributeValue")
        public Object attributeValue;

        public Attribute toAttribute() throws Exception {
            if (attributeType == null) {
                throw new NullPointerException("Internal Error:No AttributeType!");
            }

            // when parsed from XML, default type is String.
            if (attributeValue instanceof String) {
                return AttributeUtil.parseFromString(attributeType, attributeName, (String) attributeValue,
                        attributeEnumValues);
            } else {
                return AttributeUtil.createFrom(attributeType, attributeName, attributeValue, attributeEnumValues);
            }
        }

        @Override
        public String toString() {
            return "XMLAttribute[attributeName=" + attributeName + ", attributeType=" + attributeType
                    + ", attributeValue=" + attributeValue + ", attributeEnumValues="
                    + Arrays.toString(attributeEnumValues) + "]";
        }
    }

    @JacksonXmlProperty(localName = "setName", isAttribute = true)
    public String setName;

    // Jackson Note: localName is used as wrapper name for array elements elements, use 'attribute' as array element name and not 'attributes'.
    @JacksonXmlProperty(localName = "attribute")
    @JacksonXmlElementWrapper(localName = "attribute", useWrapping = false)
    public List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

    public XMLAttributeSet() {
    }

    public XMLAttributeSet(AttributeSet attrs) {
        init(attrs.getName(), attrs);
    }

    @JsonIgnore
    protected void init(String name, Map<String, Attribute> attrs) {
        this.setName = name;
        setAttributes(attrs);
    }

    @JsonIgnore
    public void setXMLAttributes(XMLAttribute attrs[]) {
        attributes = new ArrayList<XMLAttribute>();
        for (int i = 0; i < attrs.length; i++) {
            attributes.add(attrs[i]);
        }
    }

    @JsonIgnore
    public void setAttributes(Map<String, Attribute> attrs) {
        if (attrs == null) {
            this.attributes = null;
            return;
        }

        attributes = new ArrayList<XMLAttribute>();

        String keys[] = attrs.keySet().toArray(new String[0]);

        for (int i = 0; i < keys.length; i++) {
            attributes.add(new XMLAttribute(attrs.get(keys[i])));
        }
    }

    @JsonIgnore
    public AttributeSet toAttributeSet() throws Exception {
        AttributeSet attrSet = new AttributeSet(this.setName);

        if ((attributes == null) || (attributes.size() <= 0)) {
            return null;
        }

        for (XMLAttribute attr : attributes) {
            attrSet.put(attr.attributeName, attr.toAttribute());
        }
        return attrSet;
    }

    @JsonIgnore
    public void addAttributes(Attribute attr) {
        this.attributes.add(new XMLAttribute(attr));
    }

}
