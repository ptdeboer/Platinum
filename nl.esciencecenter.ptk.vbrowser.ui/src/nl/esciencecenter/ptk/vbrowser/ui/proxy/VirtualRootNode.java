package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VirtualRootNode extends ProxyNode
{
    protected String name="Root"; 
    
    protected List<ProxyNode> childs=new ArrayList<ProxyNode>();
    
    protected VirtualRootNode(ProxyFactory factory, VRL proxyLocation)
    {
        super(factory, proxyLocation);
    }

    @Override
    protected String doGetName() throws ProxyException
    {
        return name; 
    }

    @Override
    protected String doGetResourceType() throws ProxyException
    {
        return "Root"; 
    }

    @Override
    protected String doGetResourceStatus() throws ProxyException
    {
        return null;
    }

    @Override
    protected String doGetMimeType() throws ProxyException
    {
        return null;
    }

    @Override
    protected boolean doGetIsComposite() throws ProxyException
    {
        return true; 
    }

    @Override
    protected List<? extends ProxyNode> doGetChilds(int offset, int range, LongHolder numChildsLeft) throws ProxyException
    {
        return ProxyNode.subrange(childs,offset,range); 
    }

    @Override
    protected ProxyNode doGetParent() throws ProxyException
    {
        return this;
    }

    @Override
    protected List<String> doGetChildTypes() throws ProxyException
    {
        StringList list=new StringList(); 
        
        for (int i=0;i<childs.size();i++)
        {
            list.add(childs.get(i).getResourceType()); 
        }
        return list; 
    }

    @Override
    protected List<String> doGetAttributeNames() throws ProxyException
    {
        return null;
    }

    @Override
    protected List<Attribute> doGetAttributes(List<String> names) throws ProxyException
    {
        return null;
    }

    @Override
    protected Presentation doGetPresentation()
    {
        return null;
    }

    public void addChild(ProxyNode node)
    {
        if (childs==null)
            childs=new ArrayList<ProxyNode>(); 
        
       this.childs.add(node); 
    }

    public void setChilds(List<ProxyNode> nodes)
    {
       this.childs=new ArrayList<ProxyNode>(nodes);
    }

    public boolean hasChild(VRL locator)
    {
        return (this.getChild(locator)!=null); 
    }

    public ProxyNode getChild(VRL locator)
    {
        for (ProxyNode node:this.childs)
        {
            if (node.hasLocator(locator))
                return node; 
        }
        
        return null; 
    }
    

}
