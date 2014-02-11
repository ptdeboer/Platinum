package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import java.util.List;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class VirtualRootProxyFactory extends ProxyFactory
{
    public static final String VROOT_VRL="virtual:0"; 
    
    private static VirtualRootNode rootNode;
    
    // ========
    // instance
    // ========

    public VirtualRootProxyFactory(BrowserPlatform platform)
    {
        super(platform);
    }

    @Override
    public ProxyNode doOpenLocation(VRL locator) throws ProxyException
    {
        if (locator.toString().equals(VROOT_VRL))
        {
            return this.getRoot();
        }
        
        if (getRoot().hasChild(locator))
        {
            return getRoot().getChild(locator); 
        }
        
        // delegate to ProxyNodeFactories of children: 
        List<? extends ProxyNode> nodes = getRoot().getChilds(); 

        String reasons="=== reasons ===\n";
        for (ProxyNode node:nodes)
        {
            ProxyFactory fac = node.getProxyFactory();
            StringHolder reason=new StringHolder();
            if (fac.canOpen(locator, reason)==true)
            {
                return fac.openLocation(locator); 
            }
            reasons+=reason.value+"\n"; 
        }
        
        throw new ProxyException("Unknown Virtual Node or Node not a child node:"+locator+"\n"+reasons); 
            
    }

    @Override
    public boolean canOpen(VRL locator, StringHolder reasonHolder) 
    {
        if (locator.toString().equals(VROOT_VRL))
            return true;
        
        
        try
        {
            List<? extends ProxyNode> nodes = getRoot().getChilds();
            for (ProxyNode node:nodes)
            {
                if (node.getProxyFactory().canOpen(locator, reasonHolder))
                    return true;
            }
        }
        catch (ProxyException e)
        {
            if (reasonHolder!=null)
            {
                reasonHolder.value="Got Exception:"+e+"\n"+e.getStackTrace();
                return false; 
            }
        }
        
        return false; 
    }

    public VirtualRootNode getRoot() throws ProxyException
    {
        try
        {
            if (rootNode == null)
            {
                rootNode = new VirtualRootNode(this, new VRL(VROOT_VRL));
            }
    
            return rootNode;
        }
        catch (VRLSyntaxException e)
        {
            throw new ProxyException("VRL Syntax Exception:" + e, e);
        }
    }

}