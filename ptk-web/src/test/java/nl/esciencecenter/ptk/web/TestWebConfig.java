package nl.esciencecenter.ptk.web;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class TestWebConfig {

    // Currently only some basic sanity tests. 

    @Test
    public void TestCreateWebConfigFromURIs() throws URISyntaxException {

        testCreateConfig("http", "pablo", "escobar.net", 8080, "/service",
                "http://pablo@escobar.net:8080/service");
        testCreateConfig("https", "pablo", "escobar.net", 8443, "/service",
                "https://pablo@escobar.net:8443/service");
        testCreateConfig("https", "pablo", "escobar.net", 8443, "service",
                "https://pablo@escobar.net:8443/service");
        testCreateConfig("https", "pablo", "escobar.net", 8443, "service/",
                "https://pablo@escobar.net:8443/service");
        testCreateConfig("https", "pablo", "escobar.net", 8443, "/service/",
                "https://pablo@escobar.net:8443/service");

        testCreateConfig("http", "pablo", "escobar.net", 80, null, "http://pablo@escobar.net:80/");
        testCreateConfig("http", null, "escobar.net", 80, "/service",
                "http://pablo@escobar.net:80/service");
        testCreateConfig("http", "pablo", "escobar.net", 80, null, "http://pablo@escobar.net:80/");
        testCreateConfig("http", null, "escobar.net", 80, null, "http://pablo@escobar.net:80/");

        // null ports ? 
        //testCreateConfig("http","pablo","escobar.net",0,"/service","/service");
        //testCreateConfig("http",null,"escobar.net",0,"/service","/service");

    }

    protected void testCreateConfig(String scheme, String user, String host, int port,
            String servicePath, String expectedUri) throws URISyntaxException {

        URI uri = new URI(expectedUri);
        uri = uri.normalize();

        WebConfig conf = new WebConfig(uri);
        Assert.assertEquals("Scheme must match protocol.", scheme, conf.getProtocol());
        Assert.assertEquals("Hostnames must match.", host, conf.getHostname());
        Assert.assertEquals("Port numbers must match.", port, conf.getPort());

        outPrintf("Service URI=%s\n", conf.getServiceURI());
        Assert.assertEquals("Service URIs must match", uri, conf.getServiceURI());

    }

    protected void outPrintf(String format, Object... args) {
        System.out.printf(format, args);
    }

}
