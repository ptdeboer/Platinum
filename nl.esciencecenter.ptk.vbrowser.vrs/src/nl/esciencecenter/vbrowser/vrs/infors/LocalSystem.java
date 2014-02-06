package nl.esciencecenter.vbrowser.vrs.infors;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.io.FSNode;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class LocalSystem extends InfoRSNode
{
    protected FSUtil fsUtil = null;

    private InfoResourceNode homeNode;

    public LocalSystem(InfoRootNode infoRootNode) throws VrsException
    {
        super(infoRootNode, InfoConstants.LOCALSYSTEM, InfoRS.createPathVRL(InfoConstants.LOCALSYSTEM));
        fsUtil = FSUtil.getDefault();
        initChilds();
    }

    public String getIconURL(int size)
    {
        return "info/system-128.png";
    }

    protected void initChilds() throws VrsException
    {
        nodes.clear();
        initHome();
        initDrives();
    }

    public InfoResourceNode getHomeNode() throws VrsException
    {
        if (this.homeNode == null)
        {
            homeNode = initHome();
        }
        return homeNode;
    }

    protected InfoResourceNode initHome() throws VrsException
    {
        FSNode home = fsUtil.getUserHomeDir();

        URI uri = home.getURI();
        VRL vrl = new VRL(uri);
        String name = vrl.getPath();
        String subPath = "Home";
        homeNode = CreateLinkNode(subPath, vrl, name, "info/home_folder-48.png");
        this.addNode(homeNode);
        return homeNode;
    }

    protected void initDrives() throws VrsException
    {
        List<FSNode> roots = fsUtil.listRoots();

        int index = 0;

        for (FSNode root : roots)
        {
            URI uri = root.getURI();
            VRL vrl = new VRL(uri);

            String name = vrl.getPath();

            String subPath = "Root " + index++;

            this.addNode(CreateLinkNode(subPath, vrl, name, "info/hdd_mount-128.png"));
        }

    }

    protected InfoResourceNode CreateLinkNode(String subPath, VRL targetVrl, String name, String iconUrl) throws VRLSyntaxException
    {
        VRL logicalVrl = this.createSubNodeVRL(subPath);

        InfoLinkNode node = new InfoLinkNode(this, logicalVrl, targetVrl);
        node.setLogicalName(name);
        node.setIconUrl(iconUrl);
        return node;
    }

    public List<AttributeDescription> getAttributeDescriptions()
    {
        List<AttributeDescription> descs = super.getAttributeDescriptions();
        List<AttributeDescription> resourceAttrs = getResourceAttrDescriptions();
        descs.addAll(resourceAttrs);

        return descs;
    }

    public List<AttributeDescription> getResourceAttrDescriptions()
    {
        ArrayList<AttributeDescription> descs = new ArrayList<AttributeDescription>();
        descs.add(new AttributeDescription(InfoConstants.LOCALSYSTEM_OSTYPE, AttributeType.STRING, false, "LocalSystem OS Type"));
        descs.add(new AttributeDescription(InfoConstants.LOCALSYSTEM_OSVERSION, AttributeType.STRING, false, "LocalSystem OS Version"));
        descs.add(new AttributeDescription(InfoConstants.LOCALSYSTEM_ARCHTYPE, AttributeType.STRING, false, "LocalSystem Architecture"));
        descs.add(new AttributeDescription(InfoConstants.LOCALSYSTEM_HOMEDIR, AttributeType.STRING, false,
                "LocalSystem user home directory"));
        descs.add(new AttributeDescription(InfoConstants.LOCALSYSTEM_JREHOME, AttributeType.STRING, false, "LocalSystem JRE home"));
        descs.add(new AttributeDescription(InfoConstants.LOCALSYSTEM_JREVERSION, AttributeType.STRING, false, "LocalSystem JRE Version"));
        return descs;
    }

    public Attribute getAttribute(String name) throws VrsException
    {
        if (name == null)
            return null;

        Attribute attr = super.getAttribute(name);
        if (attr != null)
            return attr;

        if (name.equals(InfoConstants.LOCALSYSTEM_OSTYPE))
        {
            attr = new Attribute(name, GlobalProperties.getOsName());
        }
        else if (name.equals(InfoConstants.LOCALSYSTEM_ARCHTYPE))
        {
            attr = new Attribute(name, GlobalProperties.getOsArch());
        }
        else if (name.equals(InfoConstants.LOCALSYSTEM_OSVERSION))
        {
            attr = new Attribute(name, GlobalProperties.getOsVersion());
        }
        else if (name.equals(InfoConstants.LOCALSYSTEM_JREHOME))
        {
            attr = new Attribute(name, GlobalProperties.getJavaHome());
        }
        else if (name.equals(InfoConstants.LOCALSYSTEM_JREVERSION))
        {
            attr = new Attribute(name, GlobalProperties.getJavaVersion());
        }
        else if (name.equals(InfoConstants.LOCALSYSTEM_HOMEDIR))
        {
            String path = this.getHomeNode().getVRL().getPath();
            attr = new Attribute(name, path);
        }

        return attr;
    }
}
