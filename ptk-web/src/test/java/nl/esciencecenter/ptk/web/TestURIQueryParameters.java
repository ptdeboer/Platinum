package nl.esciencecenter.ptk.web;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public class TestURIQueryParameters {

    @Test
    public void TestBasicEncoding() throws UnsupportedEncodingException, URISyntaxException {
        // common parameter formats: 
        testSingleParameter("name", "value", "name=value");
        testSingleParameter("name", "comma,value", "name=comma%2Cvalue");
        testSingleParameter("name", "dotted.value", "name=dotted.value");
        testSingleParameter("name", "slashed/value", "name=slashed%2Fvalue");
        testSingleParameter("name", "min-value", "name=min-value");

        // Special: URI Syntax: Space is URI encoded to "+" and "+" to %2B !
        // Note that this differs from java URLEncoding as this uses web form encoding. 

        testSingleParameter("name", "Spaced Value", "name=Spaced+Value");
        testSingleParameter("name", "plus+value", "name=plus%2Bvalue");
    }

    protected void testSingleParameter(String name, String value, String expectedString)
            throws UnsupportedEncodingException, URISyntaxException {
        URIQueryParameters pars = new URIQueryParameters();
        pars.put(name, value);
        Assert.assertEquals("Encoded parameters do not match.", expectedString,
                pars.toQueryString());

        // Decode single parameter and check with java.net.URI implementation. 
        // note that Appache's HTTPClient has better URI encoding and decoding ! 
        // use URIBuilder if possible. 
        //java.net.URI dummy=new java.net.URI("http://nohost/spaced%20path?"+pars.toQueryString());
        //String uriQuery=dummy.getQuery(); 
        //Assert.assertEquals("URI Decoded parameters do not match.", name+"="+value,uriQuery); 

    }

}
