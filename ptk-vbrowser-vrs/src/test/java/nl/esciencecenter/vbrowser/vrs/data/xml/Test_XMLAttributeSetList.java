package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.exceptions.XMLDataException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test_XMLAttributeSetList {
    private static final Logger logger = LoggerFactory.getLogger(Test_XMLAttributeSetList.class);

    public static final Object testSet1[][][] = {//
        { { "stringField", "stringValue" },//
            { "integerField", new Integer(1) },//
            { "doubleField", new Double(1) },//
            { "booleanField", new Boolean(true) } },//
    };

    public static final Object nill[][][] = { { { "objectField", new Object() } } };//

    // ======= 
    // Helpers 
    // =======

    protected List<AttributeSet> createAttributeSetList(Object objects[][][]) {
        int numSets = objects.length;

        ArrayList<AttributeSet> setList = new ArrayList<AttributeSet>();

        for (int i = 0; i < numSets; i++) {
            AttributeSet set = new AttributeSet("set:" + i);
            Object[][] setObjects = objects[i];
            int numAttrs = setObjects.length;
            //
            for (int j = 0; j < numAttrs; j++) {
                Attribute attr = AttributeUtil.createFrom(setObjects[j][0].toString(), setObjects[j][1]);
                set.put(attr);
            }
            setList.add(set);
        }

        return setList;
    }

    protected void testXMLAttributeSetSingle(String name, Object value) throws Exception {
        Attribute attr = AttributeUtil.createFrom(name, value);
        AttributeSet attrs = new AttributeSet("Test");
        attrs.set(attr);

        List<AttributeSet> list = new ArrayList<AttributeSet>();
        list.add(attrs);

        testXMLAttributeSetList(list);
    }

    protected List<AttributeSet> testXMLAttributeSetList(List<AttributeSet> list) throws XMLDataException {
        XMLData data = new XMLData(new VRSContext());
        String xml = data.toXML("test", list);

        logger.info("---xml---\n{}\n", XMLData.prettyFormat(xml, 3));
        StringHolder nameH = new StringHolder();
        List<AttributeSet> newSetList = data.parseAttributeSetList(xml, nameH);
        compare(list, newSetList, true);
        return newSetList;
    }

    protected void compare(List<AttributeSet> setList, List<AttributeSet> otherSetList,//
            boolean compareStringValuesOnly) {

        Assert.assertEquals("Sizes of lists must match", setList.size(), otherSetList.size());

        for (int index = 0; index < setList.size(); index++) {
            AttributeSet set = setList.get(index);
            AttributeSet otherSet = setList.get(index);
            Assert.assertEquals("Set names must match", set.getName(), otherSet.getName());
            Test_XMLAttributeSet.compare(set, otherSet, compareStringValuesOnly);
        }
    }

    // ===== 
    // Tests 
    // =====

    //@Test
    public void testXMLAttributeSetListSingle() throws Exception {

        testXMLAttributeSetSingle("field1", "value2");
        testXMLAttributeSetSingle("field2", "1");
        testXMLAttributeSetSingle("field3", "3.3");
        testXMLAttributeSetSingle("field4", "http://host.domain:8443/helloWorld");
        testXMLAttributeSetSingle("field5", "http://host.domain:8443/helloWorld?query#fragment");
        String enums[] = new String[] { "enum1", "enum2", "enum3", "enum4", "enum2" };
        testXMLAttributeSetSingle("field5", AttributeUtil.createEnumerate("enumField", enums, "enum2"));
    }

    //@Test
    public void testXMLAttributeSetList1() throws Exception {
        AttributeSet set = new AttributeSet("testSet1");

        int n = 10;

        for (int i = 0; i < n; i++) {
            set.set(new Attribute("name:" + i, "value:" + i));
        }

        List<AttributeSet> list = new ArrayList<AttributeSet>();
        list.add(set);

        List<AttributeSet> xmlSet = testXMLAttributeSetList(list);
        Assert.assertEquals("SetList must have one set.", 1, xmlSet.size());
        AttributeSet newSet = xmlSet.get(0);

        Test_XMLAttributeSet.compare(set, newSet, false);
    }

    @Test
    public void testXMLAttributeSetList_testSet1() throws Exception {

        List<AttributeSet> setList = createAttributeSetList(testSet1);
        List<AttributeSet> newList = testXMLAttributeSetList(setList);

    }

}
