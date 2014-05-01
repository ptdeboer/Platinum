package nl.esciencecenter.vbrowser.vrs.data.xml;

import java.io.IOException;
import java.util.Set;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.data.AttributeUtil;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import org.junit.Assert;
import org.junit.Test;

public class Test_XMLData
{
    
    @Test 
    public void testVRSProperties() throws Exception
    {
        testXMLAttributeSet("field1","value2");
        testXMLAttributeSet("field2","1");
        testXMLAttributeSet("field3","3.3");
        testXMLAttributeSet("field4","http://host.domain:8443/helloWorld");
        testXMLAttributeSet("field5","http://host.domain:8443/helloWorld?query#fragment");

        testXMLAttributeSet("intField",new Integer(3));
        testXMLAttributeSet("floatField",new Float(3.14));
        testXMLAttributeSet("doubleField",new Double(3.14));
        testXMLAttributeSet("uriField",new java.net.URI("https://host.domain:8443/helloWorld?Query#Fragment"));
        
    }
    
    @Test 
    public void testXMLAttributeSetOrder() throws Exception
    {
        testVRSPropertiesOrder("field1","field2","field3","field4");  
        testVRSPropertiesOrder("field4","field3","field2","field1");
        testVRSPropertiesOrder("a","b","c","d");
        testVRSPropertiesOrder("b","a","d","c");
        testVRSPropertiesOrder("d","c","b","a");
        testVRSPropertiesOrder("c","d","a","b");
    }

//    private void testVRSProperties(String name, Object value) throws Exception
//    {
//        VRSProperties props=new VRSProperties("Test"); 
//        props.set(name,value); 
//        XMLData data=new XMLData(); 
//        String xml=data.toXML(props); 
//        
//        outPrintf("---xml---\n%s\n",XMLData.prettyFormat(xml,3));   
//        
//        VRSProperties newProps = data.createVRSProperties(xml);
//        
//        compare(props,newProps,true);
//    }
    
    private void testXMLAttributeSet(String name, Object value) throws Exception
    {
        AttributeSet attrs=new AttributeSet("Test");
        Attribute attr=AttributeUtil.createFrom(name, value); 
        attrs.set(attr); 
        
        XMLData data=new XMLData(new VRSContext()); 
        String xml=data.toXML(attrs); 
        
        outPrintf("---xml---\n%s\n",XMLData.prettyFormat(xml,3));   
        
        AttributeSet newSet =  data.parseAttributeSet(xml); 
        compare(attrs,newSet,true);
    }
    
    private void testVRSPropertiesOrder(String... fieldNames) throws Exception
    {
        AttributeSet attrs=new AttributeSet("Test"); 
        for (String field:fieldNames)
        {
            attrs.set(field,"value"+ field+"");
        }
        
        XMLData data=new XMLData(new VRSContext()); 
        String xml=data.toXML(attrs); 
        
        outPrintf("---xml---\n%s\n",XMLData.prettyFormat(xml,3));   
        
        AttributeSet parsed = data.parseAttributeSet(xml);
        
        compare(attrs,parsed,true);
    }
    
    protected void compare(VRSProperties original, VRSProperties others,boolean checkStringValuesOnly)
    {
        Set<String> orgKeys = original.keySet(); 
        Set<String> otherKeys = others.keySet(); 
        
        for (String key:orgKeys)
        {
            Object org=original.get(key);
            Object other=others.get(key); 
            
            Assert.assertNotNull("Original field value is NOT defined:"+key,org);
            Assert.assertNotNull("Duplicate field value is NOT defined:"+key,other); 
            
            if (checkStringValuesOnly)
            {
                Assert.assertEquals("Field:"+key+" doesn't not match original value",org.toString(),other.toString());
            }
            else
            {
                Assert.assertEquals("Field:"+key+" doesn't not match original value.",org,other); 
                Assert.assertTrue("Field:"+key+" doesn't not match original value. original="+org+",other="+other,org.equals(other)); 
            }
        }
        
        // Check order of fields: 
        Assert.assertEquals("Number of fields don't match", orgKeys.size(),otherKeys.size()); 
        
        String arr1[]=orgKeys.toArray(new String[0]);
        String arr2[]=otherKeys.toArray(new String[0]);
        for (int i=0;i<arr1.length;i++)
        {
            Assert.assertEquals("Field numbers #"+i+" don't match", arr1[i],arr2[i]); 
        }
        
    }

    private void compare(AttributeSet original, AttributeSet others,boolean checkStringValuesOnly)
    {
        Set<String> orgKeys = original.keySet(); 
        Set<String> otherKeys = others.keySet(); 
        
        for (String key:orgKeys)
        {
            Attribute org=original.get(key);
            Attribute other=others.get(key); 
            
            Assert.assertNotNull("Original field value is NOT defined:"+key,org);
            Assert.assertNotNull("Duplicate field value is NOT defined:"+key,other);
            
            // compare String values;  
            Assert.assertEquals("Field:"+key+" doesn't not match original value",org.getStringValue(),other.getStringValue());
            
            if (checkStringValuesOnly==false)
            {
                // disable editable falg for now:
                //other.setEditable(false);
                //org.setEditable(false); 

                // use equals and compare: 
                Assert.assertTrue("Field:"+key+" doesn't not match original value. original="+org+",other="+other,org.equals(other)); 
            }
        }
        
        // Check order of fields: 
        Assert.assertEquals("Number of fields don't match", orgKeys.size(),otherKeys.size()); 
        
        String arr1[]=orgKeys.toArray(new String[0]);
        String arr2[]=otherKeys.toArray(new String[0]);
        for (int i=0;i<arr1.length;i++)
        {
            Assert.assertEquals("Field numbers #"+i+" don't match", arr1[i],arr2[i]); 
        }
    }
    
    @Test 
    public void testVRLProperties() throws Exception
    {
        testXMLAttributeSet("vrlField",new VRL("https://host.domain:8443/helloWorld?Query#Fragment"));
    }
    
    public static void outPrintf(String format,Object... args)
    {
        System.out.printf(format,args);
    }
    
}
