package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.ptk.net.URIFactory;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.node.VResourceSystemNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Info Resource System. 
 * Browse logical resources and (remote) URLs. 
 * 
 * @author Piter T. de Boer
 */
public class InfoRS extends VResourceSystemNode // implements VStreamCreator
{

    public static VRL createPathVRL(String path)
    {
        path = URIFactory.uripath("/" + path);
        return new VRL("info", null, 0, path);
    }

    private InfoRootNode rootNode = null;

    private VRSClient vrsClient;

    public InfoRS(VRSContext context) throws VrsException
    {
        super(context, new VRL("info:/"));
        vrsClient = new VRSClient(this.getVRSContext());
    }

    protected InfoRootNode getRootNode() throws VrsException
    {
        if (rootNode == null)
        {
            initRootNode();
        }

        return rootNode;
    }

    protected void initRootNode() throws VrsException
    {
        rootNode = new InfoRootNode(this);
    }

    @Override
    public InfoRSNode resolvePath(VRL vrl) throws VrsException
    {
        if (!vrl.getScheme().equals("info"))
        {
            throw new VrsException("Can only handle 'info:' nodes:" + vrl);
        }

        String paths[] = vrl.getPathElements();

        int n = 0;
        if (paths != null)
            n = paths.length;

        InfoRootNode root = getRootNode();

        if (n == 0 || StringUtil.equals(vrl.getPath(), null, "", "/"))
        {
            return root;
        }

        return root.getNode(vrl);
    }

    protected VRSClient getVRSClient()
    {
        return vrsClient;
    }

}
