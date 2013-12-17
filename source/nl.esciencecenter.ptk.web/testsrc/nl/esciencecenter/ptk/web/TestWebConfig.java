package nl.esciencecenter.ptk.web;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class TestWebConfig
{
    
    // Currently only some basic sanity tests. 
 
    @Test
    public void TestCreateWebConfigFromURIs() throws URISyntaxException
    {
        
        testCreateConfig("http","pablo","escobar.net",8080,"/service","/service");
        testCreateConfig("https","pablo","escobar.net",8443,"/service","/service");
        testCreateConfig("https","pablo","escobar.net",8443,"service","/service");
        testCreateConfig("https","pablo","escobar.net",8443,"service/","/service");
        testCreateConfig("https","pablo","escobar.net",8443,"/service/","/service");

        testCreateConfig("http","pablo","escobar.net",80,null,"/");
        testCreateConfig("http",null,"escobar.net",80,"/service","/");
        testCreateConfig("http","pablo","escobar.net",80,null,"/");
        testCreateConfig("http",null,"escobar.net",80,null,"/");
        
        // null ports ? 
        //testCreateConfig("http","pablo","escobar.net",0,"/service","/service");
        //testCreateConfig("http",null,"escobar.net",0,"/service","/service");

    }
    
    protected void testCreateConfig(String scheme, String user,String host, int port, String servicePath,String resolvedServicePath) throws URISyntaxException
    {
        String uriStr=scheme+"://";
        
        if (user!=null)
            uriStr+=user+"@";
        
        uriStr+=host;
        
        if (port>0)
            uriStr+=":"+port; 
    
        if (servicePath==null)
        {
            uriStr+="/";
        }
        else if (servicePath.startsWith("/")==false) 
        {
            uriStr+="/"+servicePath;
        }
        else
        {
            uriStr+=servicePath;
        }
        
        URI uri=new URI(uriStr); 
        uri=uri.normalize(); 
        
        WebConfig conf=new WebConfig(uri); 
        Assert.assertEquals("Scheme must match protocol.",scheme,conf.getProtocol());  
        Assert.assertEquals("Hostnames must match.",host,conf.getHostname());  
        Assert.assertEquals("Port numbers must match.",port,conf.getPort());   
        Assert.assertEquals("Service paths must match.",resolvedServicePath,conf.getServicePath());    
        Assert.assertEquals("Port numbers must match.",port,conf.getPort());   

        outPrintf("Service URI=%s\n",conf.getServiceURI());
        Assert.assertEquals("Service URIs must match",uri,conf.getServiceURI()); 
        
    }
    
    
    protected void outPrintf(String format,Object... args)
    {
        System.out.printf(format,args); 
    }
    
}
