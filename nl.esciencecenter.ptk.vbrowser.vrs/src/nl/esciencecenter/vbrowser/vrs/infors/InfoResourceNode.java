package nl.esciencecenter.vbrowser.vrs.infors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSProperties;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.data.AttributeType;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VStreamAccessable;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoResourceNode extends InfoRSNode implements VStreamAccessable
{
    private static ClassLogger logger = ClassLogger.getLogger(InfoResourceNode.class);

    public static InfoResourceNode createLinkNode(InfoRSNode parent, String logicalName, VRL targetLink, String optIconURL,
            boolean showLinkIcon) throws VRLSyntaxException
    {
        VRL logicalVRL = parent.createSubNodeVRL("node" + parent.getNumNodes());

        InfoResourceNode node = new InfoResourceNode(parent, InfoConstants.RESOURCELINK, logicalVRL);

        node.setTargetVRL(targetLink);
        node.setIconUrl(optIconURL);
        node.setShowLinkIcon(showLinkIcon);
        node.setLogicalName(logicalName);

        return node;
    }

    public static InfoResourceNode createFolderNode(InfoRootNode parentNode, String folderName, String optIconURL)
            throws VRLSyntaxException
    {
        VRL logicalVRL = parentNode.createSubNodeVRL(folderName);
        InfoResourceNode node = new InfoResourceNode(parentNode, InfoConstants.RESOURCEFOLDER, logicalVRL);

        node.setTargetVRL(null);
        node.setIconUrl(optIconURL);
        node.setShowLinkIcon(false);
        node.setLogicalName(folderName);

        return node;
    }

    // ==========
    // Instance
    // ==========

    protected VRSProperties resourceProps = new VRSProperties();

    protected InfoResourceNode(InfoRSNode parent, String type, VRL logicalVRL)
    {
        super(parent, type, logicalVRL);
        this.setLogicalName(logicalVRL.getBasename());
    }

    protected InfoResourceNode(InfoRSNode parent, String type, VRL logicalVRL, VRL targetVRL)
    {
        super(parent, type, logicalVRL);

        this.setTargetVRL(targetVRL);
        this.setIconUrl(null);
        this.setLogicalName(logicalVRL.getBasename());
    }

    protected void setLogicalName(String name)
    {
        this.resourceProps.set(InfoConstants.RESOURCE_NAME, name);
    }

    protected void setIconUrl(String iconUrl)
    {
        this.resourceProps.set(InfoConstants.RESOURCE_ICONURL, iconUrl);
    }

    protected void setShowLinkIcon(boolean val)
    {
        this.resourceProps.set(InfoConstants.RESOURCE_SHOWLINKICON, val);
    }

    public boolean getShowLinkIcon(boolean defaultValue)
    {
        return resourceProps.getBooleanProperty(InfoConstants.RESOURCE_SHOWLINKICON, defaultValue);
    }

    @Override
    public String getIconURL(int size)
    {
        return resourceProps.getStringProperty(InfoConstants.RESOURCE_ICONURL);
    }

    public String getName()
    {
        String name = resourceProps.getStringProperty(InfoConstants.RESOURCE_NAME);

        if (name == null)
        {
            VRL targetVrl = this.getTargetVRL();
            if (targetVrl != null)
            {
                name = "link:" + targetVrl.toString();
            }
            else
            {
                name = getVRL().getBasename();
            }
        }

        return name;
    }

    public VRL getTargetVRL()
    {
        try
        {
            return resourceProps.getVRLProperty(InfoConstants.RESOURCE_TARGETVRL);
        }
        catch (VRLSyntaxException e)
        {
            logger.warnPrintf("Invalid VRL:%s\n", e.getMessage());
            return null;
        }
    }

    public void setTargetVRL(VRL vrl)
    {
        if (vrl == null)
        {
            resourceProps.remove(InfoConstants.RESOURCE_TARGETVRL);
        }
        else
        {
            resourceProps.set(InfoConstants.RESOURCE_TARGETVRL, vrl);
        }
    }

    public List<? extends VPath> list() throws VrsException
    {
        if (isResourceLink())
        {
            VRL vrl = this.getTargetVRL();

            if (vrl == null)
            {
                return nodes;
            }

            VPath node = resolveTarget(vrl);
            if ((node == null) || (node.isComposite() == false))
            {
                return null;
            }

            return node.list();

        }
        else if (isResourceFolder())
        {
            return nodes;
        }
        else
        {
            return nodes;
        }
    }

    public boolean isResourceLink()
    {
        return InfoConstants.RESOURCELINK.equals(getResourceType());
    }

    public boolean isResourceFolder()
    {
        return InfoConstants.RESOURCEFOLDER.equals(getResourceType());
    }

    public VPath resolveTarget(VRL vrl) throws VrsException
    {
        return getInfoRS().getVRSClient().openLocation(vrl);
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
        descs.add(new AttributeDescription(InfoConstants.RESOURCE_TARGETVRL, AttributeType.VRL, true, "Resource Target VRL"));
        descs.add(new AttributeDescription(InfoConstants.RESOURCE_MIMETYPE, AttributeType.STRING, true, "Resource MimeType"));
        descs.add(new AttributeDescription(InfoConstants.RESOURCE_ICONURL, AttributeType.STRING, true, "Resource Icon URL"));
        descs.add(new AttributeDescription(InfoConstants.RESOURCE_SHOWLINKICON, AttributeType.BOOLEAN, true,
                "Resource show (mini) link icon"));

        return descs;
    }

    public Attribute getAttribute(String name) throws VrsException
    {
        if (name == null)
            return null;

        Attribute attr = super.getAttribute(name);
        if (attr != null)
            return attr;

        if (name.equals(InfoConstants.RESOURCE_TARGETVRL))
        {
            attr = new Attribute(name, getTargetVRL());
        }
        else if (name.equals(InfoConstants.RESOURCE_MIMETYPE))
        {
            attr = new Attribute(name, getMimeType());
        }
        else if (name.equals(InfoConstants.RESOURCE_SHOWLINKICON))
        {
            attr = new Attribute(name, getShowLinkIcon(false));
        }
        else if (name.equals(InfoConstants.RESOURCE_ICONURL))
        {
            attr = new Attribute(name, getIconURL(128));
        }

        return attr;
    }

    // ======================================
    // Stream Read/Write Methods (load/save)
    // ========================================

    public OutputStream createOutputStream() throws VrsException
    {
        throw new VrsException("Not now");
    }

    public InputStream createInputStream() throws VrsException
    {
        throw new VrsException("Not now");
    }

}
