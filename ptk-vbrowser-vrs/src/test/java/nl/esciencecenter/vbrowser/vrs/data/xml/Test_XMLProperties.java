package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.io.IOException;
import java.util.Set;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.junit.Assert;
import org.junit.Test;

public class Test_XMLProperties
{

    @Test
    public void testXMLVRSProperties() throws Exception
    {
        testXMLVRSProperties("field1", "value2");
        testXMLVRSProperties("field2", "1");
        testXMLVRSProperties("field3", "3.3");
        testXMLVRSProperties("field4", "http://host.domain:8443/helloWorld");
        testXMLVRSProperties("field5", "http://host.domain:8443/helloWorld?query#fragment");

        testXMLVRSProperties("intField", new Integer(3));
        testXMLVRSProperties("floatField", new Float(3.14));
        testXMLVRSProperties("doubleField", new Double(3.14));
        testXMLVRSProperties("uriField", new java.net.URI("https://host.domain:8443/helloWorld?Query#Fragment"));

    }

    @Test
    public void testXMLVRSPropertiesOrder() throws Exception
    {
        testXMLVRSPropertiesOrder("field1", "field2", "field3", "field4");
        testXMLVRSPropertiesOrder("field4", "field3", "field2", "field1");
        testXMLVRSPropertiesOrder("a", "b", "c", "d");
        testXMLVRSPropertiesOrder("b", "a", "d", "c");
        testXMLVRSPropertiesOrder("d", "c", "b", "a");
        testXMLVRSPropertiesOrder("c", "d", "a", "b");
    }

    private void testXMLVRSProperties(String name, Object value) throws Exception
    {
        VRSProperties attrs = new VRSProperties("Test");
        attrs.set(name, value);

        XMLData data = new XMLData(new VRSContext());
        String xml = data.toXML(attrs);

        outPrintf("---xml---\n%s\n", XMLData.prettyFormat(xml, 3));

        VRSProperties newSet = data.parseVRSProperties(xml);
        compare(attrs, newSet, true);
    }

    private void testXMLVRSPropertiesOrder(String... fieldNames) throws Exception
    {
        VRSProperties attrs = new VRSProperties("Test");
        for (String field : fieldNames)
        {
            attrs.set(field, "value" + field + "");
        }

        XMLData data = new XMLData(new VRSContext());
        String xml = data.toXML(attrs);

        outPrintf("---xml---\n%s\n", XMLData.prettyFormat(xml, 3));

        VRSProperties parsed = data.parseVRSProperties(xml);

        compare(attrs, parsed, true);
    }

    protected void compare(VRSProperties original, VRSProperties others, boolean checkStringValuesOnly)
    {
        Set<String> orgKeys = original.keySet();
        Set<String> otherKeys = others.keySet();

        for (String key : orgKeys)
        {
            Object org = original.get(key);
            Object other = others.get(key);

            Assert.assertNotNull("Original field value is NOT defined:" + key, org);
            Assert.assertNotNull("Duplicate field value is NOT defined:" + key, other);

            // compare String values;
            Assert.assertEquals("Field:" + key + " doesn't not match original value", org.toString(), other.toString());

            if (checkStringValuesOnly == false)
            {
                // disable editable falg for now:
                // other.setEditable(false);
                // org.setEditable(false);

                // use equals and compare:
                Assert.assertTrue("Field:" + key + " doesn't not match original value. original=" + org + ",other=" + other,
                        org.equals(other));
            }
        }

        // Check order of fields:
        Assert.assertEquals("Number of fields don't match", orgKeys.size(), otherKeys.size());

        String arr1[] = orgKeys.toArray(new String[0]);
        String arr2[] = otherKeys.toArray(new String[0]);
        for (int i = 0; i < arr1.length; i++)
        {
            Assert.assertEquals("Field numbers #" + i + " don't match", arr1[i], arr2[i]);
        }
    }

    @Test
    public void testVRLProperties() throws Exception
    {
        testXMLVRSProperties("vrlField", new VRL("http://host.domain/helloWorld"));
        testXMLVRSProperties("vrlField", new VRL("http://host.domain:8080/helloWorld"));
        testXMLVRSProperties("vrlField", new VRL("http://host.domain:8080/helloWorld/Aap"));
        testXMLVRSProperties("vrlField", new VRL("https://host.domain/helloWorld?Query#Fragment"));
        testXMLVRSProperties("vrlField", new VRL("https://host.domain:8443/helloWorld?Query#Fragment"));
        testXMLVRSProperties("vrlField", new VRL("https://host.domain:8443/helloWorld?Query#Fragment"));

    }

    public static void outPrintf(String format, Object... args)
    {
        System.out.printf(format, args);
    }

}
