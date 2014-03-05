package test.vrs.data.xml;

import java.io.IOException;
import java.util.Set;

import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.xml.XMLData;

import org.junit.Assert;
import org.junit.Test;

public class TestXMLData
{
    
    @Test 
    public void testVRSProperties() throws Exception
    {
        testVRSProperties("field1","value2");
        testVRSProperties("field2","1");
        testVRSProperties("field3","3.3");
        testVRSProperties("field4","http://host.domain:8443/helloWorld");
        testVRSProperties("field5","http://host.domain:8443/helloWorld?query#fragment");

        testVRSProperties("intField",new Integer(3));
        testVRSProperties("floatField",new Float(3.14));
        testVRSProperties("doubleField",new Double(3.14));
        testVRSProperties("uriField",new java.net.URI("https://host.domain:8443/helloWorld?Query#Fragment"));
        
    }
    
    @Test 
    public void testVRSPropertiesOrder() throws Exception
    {
        testVRSPropertiesOrder("field1","field2","field3","field4");  
        testVRSPropertiesOrder("field4","field3","field2","field1");
        testVRSPropertiesOrder("a","b","c","d");
        testVRSPropertiesOrder("b","a","d","c");
        testVRSPropertiesOrder("d","c","b","a");
        testVRSPropertiesOrder("c","d","a","b");
    }

    private void testVRSProperties(String name, Object value) throws IOException
    {
        VRSProperties props=new VRSProperties("Test"); 
        props.set(name,value); 
        XMLData data=new XMLData(); 
        String xml=data.toXML(props); 
        
        outPrintf("---xml---\n%s\n",XMLData.prettyFormat(xml,3));   
        
        VRSProperties newProps = data.createVRSProperties(xml);
        
        compare(props,newProps,true);
    }
    
    private void testVRSPropertiesOrder(String... fieldNames) throws IOException
    {
        VRSProperties props=new VRSProperties("Test"); 
        for (String field:fieldNames)
        {
            props.set(field,"value"+ field+"");
        }
        
        XMLData data=new XMLData(); 
        String xml=data.toXML(props); 
        
        outPrintf("---xml---\n%s\n",XMLData.prettyFormat(xml,3));   
        
        VRSProperties newProps = data.createVRSProperties(xml);
        
        compare(props,newProps,true);
    }
    
    private void compare(VRSProperties original, VRSProperties others,boolean checkStringValuesOnly)
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

    public static void outPrintf(String format,Object... args)
    {
        System.out.printf(format,args);
    }
    
}
