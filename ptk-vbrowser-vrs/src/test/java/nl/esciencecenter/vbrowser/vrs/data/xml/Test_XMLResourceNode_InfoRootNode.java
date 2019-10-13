package nl.esciencecenter.vbrowser.vrs.data.xml;

import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.infors.InfoResourceNode;
import nl.esciencecenter.vbrowser.vrs.infors.InfoRootNode;
import nl.esciencecenter.vbrowser.vrs.infors.Test_InfoRS;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import org.junit.Assert;
import org.junit.Test;

public class Test_XMLResourceNode_InfoRootNode {

    @Test
    public void testRootNodeToXML() throws Exception {
        VRSClient vrsClient = Test_InfoRS.initTestClient();
        InfoRootNode rootNode = vrsClient.getInfoRootNode();
        Assert.assertNotNull(rootNode);
        XMLData data = new XMLData(new VRSContext());
        String xml = data.toXML(rootNode);
        System.out.printf(XMLData.prettyFormat(xml, 3));
    }

    @Test
    public void testRootNodeSubNodesToXML() throws Exception {
        VRSClient vrsClient = Test_InfoRS.initTestClient();
        VRSContext ctx = vrsClient.getVRSContext();

        InfoRootNode rootNode = vrsClient.getInfoRootNode();

        rootNode.addResourceLink("links", "GFTP eslt007.local", new VRL(
                "http://eslt007.local:2811/~"), null, false);
        rootNode.addResourceLink("links", "SFTP eslt007.local", new VRL(
                "https://eslt007.local:22/~"), null, false);
        rootNode.addResourceLink("links", "LocalHome:/~", new VRL("file:///~"), null, false);

        rootNode.addResourceLink(null, "GFTP eslt007.local",
                new VRL("http://eslt007.local:2811/~"), null, false);
        rootNode.addResourceLink(null, "SFTP eslt007.local", new VRL("https://eslt007.local:22/~"),
                null, false);
        rootNode.addResourceLink(null, "LocalHome:/~", new VRL("file:///~"), null, false);

        XMLData data = new XMLData(ctx);
        String xml = data.toXML(rootNode);

        System.out.printf(XMLData.prettyFormat(xml, 3));

        InfoResourceNode copyFolder = rootNode.createFolder("XMLCopy");
        data.addXMLResourceNodesTo(copyFolder, xml);
    }

}
