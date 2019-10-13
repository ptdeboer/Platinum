package nl.esciencecenter.vbrowser.vrs.data.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Annotated XML Proxy Object for a List of AttributeSets.
 */
public class XMLAttributeSetList {

    @JacksonXmlProperty(localName = "setGroupName", isAttribute = true)
    protected String setGroupName;

    // Jackson Note: localName is used as wrapper name for array/list elements, use 'attributeSet' as array element name 
    // and not 'attributeSetList'.
    @JacksonXmlProperty(localName = "attributeSet")
    @JacksonXmlElementWrapper(localName = "attributeSet", useWrapping = false)
    protected List<XMLAttributeSet> attributeSetList = null;

    public XMLAttributeSetList() {
        init("", new ArrayList<AttributeSet>());
    }

    public XMLAttributeSetList(String groupName) {
        init(groupName, new ArrayList<AttributeSet>());
    }

    public XMLAttributeSetList(String groupName, List<AttributeSet> attrSets) {
        init(groupName, attrSets);
    }

    @JsonIgnore
    public String getSetGroupName() {
        return this.setGroupName;
    }

    @JsonIgnore
    protected void init(String name, List<AttributeSet> attrSets) {
        attributeSetList = new ArrayList<XMLAttributeSet>();
        this.setGroupName = name;
        setAttributes(attrSets);
    }

    @JsonIgnore
    public void setAttributes(List<AttributeSet> attrSets) {
        for (int i = 0; i < attrSets.size(); i++) {
            attributeSetList.add(new XMLAttributeSet(attrSets.get(i)));
        }
    }

    @JsonIgnore
    public void addAtttributeSet(AttributeSet attrSet) {
        this.attributeSetList.add(new XMLAttributeSet(attrSet));
    }

    @JsonIgnore
    public List<AttributeSet> toAttributeSetList() throws Exception {
        List<AttributeSet> list = new ArrayList<AttributeSet>();
        for (int i = 0; i < this.attributeSetList.size(); i++) {
            list.add(this.attributeSetList.get(i).toAttributeSet());
        }
        return list;
    }

}
