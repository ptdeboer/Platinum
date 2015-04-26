package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.event.VRSEventListener;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent.VRSEventType;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Listen to Proxy Events and update the ProxyNode cache. 
 */
public class ProxyNodeCacheUpdater implements VRSEventListener
{
    protected ProxyFactory factory; 
    
    public ProxyNodeCacheUpdater(ProxyFactory proxyFactory)
    {
        factory=proxyFactory;
    }

    @Override
    public void notifyVRSEvent(VRSEvent e)
    {
        VRSEventType type = e.getType(); 
        VRL parentVrl=e.getParent();
        VRL vrls[]=e.getResources(); 
        
        switch (type)
        {
            case RESOURCES_ADDED: 
            case RESOURCES_DELETED: 
            case RESOURCES_RENAMED: 
            case REFRESH_RESOURCES:
            case ATTRIBUTES_CHANGED:
            {
                if (parentVrl!=null)
                {
                    factory.refreshChilds(parentVrl);
                }
                for (VRL vrl:vrls)
                {
                    factory.refreshNode(vrl); 
                }
                break; 
            }
            default:
            {
                break;
            }
        }
    }

}
