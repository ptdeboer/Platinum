package nl.esciencecenter.vbrowser.vrs.data.xml;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

@Slf4j
public class Test_XMLAttributeSet {

    @Test
    public void testXMLAttributeSet() throws Exception {
        testXMLAttributeSet("field1", "value2");
        testXMLAttributeSet("field2", "1");
        testXMLAttributeSet("field3", "3.3");
        testXMLAttributeSet("field4", "http://host.domain:8443/helloWorld");
        testXMLAttributeSet("field5", "http://host.domain:8443/helloWorld?query#fragment");
        testXMLAttributeSet(
                "field5",
                AttributeUtil.createEnumerate("enumField", new String[]{"enum1", "enum2",
                        "enum3", "enum4", "enum2"}, "enum2"));

        testXMLAttributeSet("intField", new Integer(3));
        testXMLAttributeSet("floatField", new Float(3.14));
        testXMLAttributeSet("doubleField", new Double(3.14));
        testXMLAttributeSet("uriField", new java.net.URI(
                "https://host.domain:8443/helloWorld?Query#Fragment"));

    }

    @Test
    public void testXMLAttributeSetOrder() throws Exception {
        testXMLAttributeSetOrder("field1", "field2", "field3", "field4");
        testXMLAttributeSetOrder("field4", "field3", "field2", "field1");
        testXMLAttributeSetOrder("a", "b", "c", "d");
        testXMLAttributeSetOrder("b", "a", "d", "c");
        testXMLAttributeSetOrder("d", "c", "b", "a");
        testXMLAttributeSetOrder("c", "d", "a", "b");
    }

    private void testXMLAttributeSet(String name, Object value) throws Exception {
        Attribute attr = AttributeUtil.createFrom(name, value);

        AttributeSet attrs = new AttributeSet("Test");

        attrs.set(attr);

        XMLData data = new XMLData(new VRSContext());
        String xml = data.toXML(attrs);

        log.debug("---xml---\n{}", XMLData.prettyFormat(xml, 3));

        AttributeSet newSet = data.parseAttributeSet(xml);
        compare(attrs, newSet, true);
    }

    private void testXMLAttributeSetOrder(String... fieldNames) throws Exception {
        AttributeSet attrs = new AttributeSet("Test");
        for (String field : fieldNames) {
            attrs.set(field, "value" + field + "");
        }

        XMLData data = new XMLData(new VRSContext());
        String xml = data.toXML(attrs);

        log.debug("---xml---\n{}", XMLData.prettyFormat(xml, 3));

        AttributeSet parsed = data.parseAttributeSet(xml);

        compare(attrs, parsed, false);
    }

    protected static void compare(AttributeSet original, AttributeSet others,
                                  boolean compareStringValuesOnly) {
        Set<String> orgKeys = original.keySet();
        Set<String> otherKeys = others.keySet();

        for (String key : orgKeys) {
            Attribute org = original.get(key);
            Attribute other = others.get(key);

            Assert.assertNotNull("Original field value is NOT defined:" + key, org);
            Assert.assertNotNull("Duplicate field value is NOT defined:" + key, other);

            // compare String values;  
            Assert.assertEquals("Field:" + key + " doesn't not match original value",
                    org.getStringValue(), other.getStringValue());

            if (compareStringValuesOnly == false) {
                // disable editable false for now:
                //other.setEditable(false);
                //org.setEditable(false); 

                // use equals and compare: 
                Assert.assertTrue("Field values for:'" + key + "' do not match: expected:" + org
                        + ", got:" + other, equals(org, other));
            }
        }

        // Check order of fields: 
        Assert.assertEquals("Number of fields don't match", orgKeys.size(), otherKeys.size());

        String[] arr1 = orgKeys.toArray(new String[0]);
        String[] arr2 = otherKeys.toArray(new String[0]);
        for (int i = 0; i < arr1.length; i++) {
            Assert.assertEquals("Field numbers #" + i + " don't match", arr1[i], arr2[i]);
        }
    }

    public static boolean equals(Attribute org, Attribute other) {
        //todo: better check for non strict equivalent attributes.
        boolean same = org.getName().equals(other.getName());
        same = same && org.getValue().equals(other.getValue());
        return same;
    }

    @Test
    public void testVRLProperties() throws Exception {
        testXMLAttributeSet("vrlField", new VRL("http://host.domain/helloWorld"));
        testXMLAttributeSet("vrlField", new VRL("http://host.domain:8080/helloWorld"));
        testXMLAttributeSet("vrlField", new VRL("http://host.domain:8080/helloWorld/Aap"));
        testXMLAttributeSet("vrlField", new VRL("https://host.domain/helloWorld?Query#Fragment"));
        testXMLAttributeSet("vrlField", new VRL(
                "https://host.domain:8443/helloWorld?Query#Fragment"));
        testXMLAttributeSet("vrlField", new VRL(
                "https://host.domain:8443/helloWorld?Query#Fragment"));
    }


}
