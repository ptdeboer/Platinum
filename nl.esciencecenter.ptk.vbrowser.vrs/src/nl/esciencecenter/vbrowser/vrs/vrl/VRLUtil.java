package nl.esciencecenter.vbrowser.vrs.vrl;

import java.net.URI;
import java.util.List;

public class VRLUtil
{
    
    public static URI[] toURIs(VRL[] vrls)
    {
        URI uris[]=new URI[vrls.length]; 
        for (int i=0;i<vrls.length;i++)
        {
            if (vrls[i]!=null)
            {
                uris[i]=vrls[i].toURINoException();
            }
            else
            {
                uris[i]=null;
            }
        }
        return uris; 
    }

    public static URI[] toURIs(List<VRL> vrls)
    {
        if (vrls==null)
            return null;
        
        URI uris[]=new URI[vrls.size()]; 
        
        for (int i=0;i<vrls.size();i++)
        {
            VRL vrl=vrls.get(i); 
            if (vrl!=null)
            {
                uris[i]=vrls.get(i).toURINoException();
            }
        }
        
        return uris; 
    }

}
