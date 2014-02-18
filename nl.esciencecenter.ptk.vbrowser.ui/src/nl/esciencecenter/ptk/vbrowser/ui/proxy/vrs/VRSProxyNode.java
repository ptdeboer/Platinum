package nl.esciencecenter.ptk.vbrowser.ui.proxy.vrs;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.presentation.IPresentable;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.presentation.VRSPresentation;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import nl.esciencecenter.vbrowser.vrs.VPath;

/** 
 * VRS ProxyNode 
 */
public class VRSProxyNode extends ProxyNode
{
    private static final ClassLogger logger=ClassLogger.getLogger(VRSProxyNode.class); 
    
    private VPath vnode;

    public VRSProxyNode(VRSProxyFactory vrsProxyFactory, VPath vnode,VRL locator) throws ProxyException, URISyntaxException
    {
        super(vrsProxyFactory,locator);
        this.vnode=vnode; 
    }
    
    protected VRSProxyFactory factory()
    {
        return (VRSProxyFactory)this.getProxyFactory(); 
    }

    protected void doPrefetchAttributes() throws ProxyException
    {
        super.doPrefetchAttributes();         
       
//        if (vnode instanceof VResourceLink)
//        {
//        	// assume true for now: 
//        	 this.cache.setIsComposite(true);  
//        }
    }
    
    
    @Override
    protected VRSProxyNode doGetParent() throws ProxyException
    {
        VPath parent;
        
        try
        {
            parent = vnode.getParent();
            if (parent==null)
                return null; 
           
            return new VRSProxyNode(this.getProxyFactory(),parent,new VRL(parent.getVRL().toURI()));
        }
        catch (Exception e)
        {
            throw createProxyException("Couldn't get parent of:"+locator,e); 
        } 
    }

    @Override
    protected List<? extends ProxyNode> doGetChilds(int offset, int range,LongHolder numChildsLeft) throws ProxyException
    {
    	logger.debugPrintf("doGetChilds:%s\n",this); 
    	 
        try
        {
        	// check links first: 
        	if (isResourceLink())
        	{
        		return resolve().doGetChilds(offset,range,numChildsLeft);  
        	}
        	else
        	{ 
        		if (vnode.isComposite()==false)  
        		{
        			return null; 
        		}
        		
        		List<? extends VPath> nodes = vnode.list();

            	return subrange(createNodes(nodes),offset,range); 
        	}
        	
        }
        catch (Exception e)
        {
        	throw createProxyException("Couldn't get childs of:"+locator,e); 
        }
         
    }
    
    protected VRSProxyNode resolve() throws ProxyException
    {
    	logger.debugPrintf("resolve():%s\n",this.vnode); 

    	return this; 
    	
//    	if ((vnode instanceof VResourceLink)==false)
//    		return this; 
//    	
//    	VRL vrl;
//    	
//		try 
//		{
//			vrl = ((VResourceLink)vnode).getTargetLocation();
//	    	VRSProxyNode node = factory()._openLocation(vrl); 
//	    	debug("Resolved to:"+node);
//	    	return node; 
//		}
//		catch (Exception e) 
//		{
//			throw createProxyException("Failed to resolve node:"+this.vnode,e); 
//		}
    }
    
    protected List<VRSProxyNode> createNodes(List<? extends VPath> nodes) throws ProxyException
    {
    	if (nodes==null)
    	{
    		return null; 
    	}
    	
        int len=nodes.size();  
        
        ArrayList<VRSProxyNode> pnodes=new ArrayList<VRSProxyNode>(len); 
        for (int i=0;i<len;i++)
        {
            pnodes.add(createNode(nodes.get(i)));
        }
        return pnodes; 
    }

    protected VRSProxyNode createNode(VPath node) throws ProxyException
    {
        try
        {
            return new VRSProxyNode(factory(),node,new VRL(node.getVRL().toURI()));
        }
        catch (Exception e)
        {
            throw createProxyException("Error creating proxy node from:"+node,e);  
        }
    }
    
    @Override
    protected String doGetIconURL(String status,int size) throws ProxyException
    {
        String url; 
        
//    	if (vnode instanceof LogicalResourceNode)
//    	{
//    	    url=((LogicalResourceNode)vnode).getTargetIconURL();   
//    	}
//    	else
    	{
    	    try
            {
                url = vnode.getIconURL(size);
            }
            catch (VrsException e)
            {
                throw new ProxyException(e.getMessage(),e); 
            }  
    	}
    	
    	return url; 
    }

	@Override
	public VRSProxyFactory getProxyFactory()
	{
		return (VRSProxyFactory)super.getProxyFactory();  
	}
	
	protected boolean isResourceLink()
    {
	    // at startup this might occur. 
        if (vnode.getVRL()==null) 
        {
        	return false; 
        }
//        if (vnode instanceof VResourceLink)
//            return true; 

        // All .vlink AND .vrsx files are ResourceLinks ! 
        if (vnode.getVRL().isVLink() == true)
            return true;
        
        return false;
    }
	
	public String toString()
	{
	    return "<VRSProxyNode:"+getResourceType()+":"+getVRL(); 
	}

	@Override
	protected String doGetMimeType() throws ProxyException
	{
	    String mimeType=null; 
	    try 
        {
	        mimeType=vnode.getMimeType(); 
        }
        catch (Exception e) 
        {
            throw new ProxyException("Couldn't determine mime type of:"+vnode,e); 
        } 
        
//        if (vnode instanceof LogicalResourceNode)
//        {
//            LogicalResourceNode lnode=(LogicalResourceNode)vnode; 
//            mimeType=lnode.getTargetMimeType(); 
//        }
        
        return mimeType; 
	}

	@Override
	protected boolean doGetIsComposite() throws ProxyException 
	{
	    boolean isComposite;
        try
        {
            isComposite = vnode.isComposite();
        }
        catch (VrsException e)
        {
           throw new ProxyException(e.getMessage(),e); 
        }
	    
//	    if (vnode instanceof LogicalResourceNode)
//        {
//            LogicalResourceNode lnode=(LogicalResourceNode)vnode; 
//            try 
//            {
//                isComposite=lnode.getTargetIsComposite(true);
//            }
//            catch (Exception e)
//            {
//                throw createProxyException("Error checking LogicalResourceNode:"+lnode,e); 
//            }
//        }
	    
	    return isComposite; 
	}

    @Override
    protected List<String> doGetChildTypes() throws ProxyException
    {
        try
        {
            return vnode.getChildResourceTypes(); 
        }
        catch (VrsException e)
        {
            throw new ProxyException(e.getMessage(),e); 
        }
    }

    @Override 
    public VRSViewNodeDnDHandler getViewNodeDnDHandlerFor(ViewNode viewNode)
    {
    	return factory().getVRSProxyDnDHandler(viewNode);
    }
    
	// ========================================================================
	// Misc 
	// ========================================================================
	
	private ProxyException createProxyException(String msg, Exception e) 
	{
	    return new ProxyException(msg+"\n"+e.getMessage(),e); 
    }

    @Override
    protected String doGetName()
    {
    	return vnode.getName(); 
    }
    
    @Override
    protected String doGetResourceType() throws ProxyException
    {   
        try
        {
            return vnode.getResourceType();
        }
        catch (VrsException e)
        {
            throw new ProxyException(e.getMessage(),e); 
        } 
    }

    @Override
    protected String doGetResourceStatus() throws ProxyException
    {
        try 
        {
            return this.vnode.getResourceStatus();
        }
        catch (Exception e)
        {
            throw createProxyException("Couldn't get status of:"+vnode,e);  
        }
    }

    @Override
    protected List<String> doGetAttributeNames() throws ProxyException
    {
        try
        {
            return vnode.getAttributeNames();
        }
        catch (VrsException e)
        {
           throw new ProxyException(e.getMessage(),e); 
        } 
    }
    
	@Override
    protected List<Attribute> doGetAttributes(List<String> names) throws ProxyException
    {
        try
        {
            return vnode.getAttributes(names);
        }
        catch (Exception e)
        {
            throw new ProxyException("Couldn't get attributes\n"+e.getMessage(),e); 
        } 
   }
    
    @Override
    protected Presentation doGetPresentation() throws ProxyException
    {
        if (vnode instanceof IPresentable)
        {
            return ((IPresentable) vnode).getPresentation();
        }
        
        String type;
        try
        {
            type = vnode.getResourceType();
            VRL vrl=vnode.getVRL(); 
            return VRSPresentation.getPresentationFor(vrl.getScheme(),vrl.getHostname(),type, true);
        }
        catch (VrsException e)
        {
            throw new ProxyException(e.getMessage(),e); 
        }  
    }

    // resolve target path and return as PNode. 
    public ProxyNode getTargetPNode()
    {
        return null;
    }

}