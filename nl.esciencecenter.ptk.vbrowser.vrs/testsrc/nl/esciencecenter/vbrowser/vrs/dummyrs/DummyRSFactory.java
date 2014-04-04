package nl.esciencecenter.vbrowser.vrs.dummyrs;

import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.VResourceSystemFactory;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.vrl.VRLUtil;

public class DummyRSFactory implements VResourceSystemFactory
{
    protected String schemes[]=new String[]{"dummy"}; 
    
    public static final String dummyIntPar="dummy.intpar.value"; 
    public static final String dummyStringPar="dummy.stringpar.value"; 
    public static final String dummyBoolPar="dummy.boolpar.value"; 
    public static final String dummyEnumPar="dummy.enumpar.value"; 
    
    public static final String attrNames[]={
            dummyIntPar,
            dummyStringPar,
            dummyBoolPar,
            dummyEnumPar
        };
    
    @Override
    public String[] getSchemes()
    {
        return schemes;
    }

    @Override
    public String createResourceSystemId(VRL vrl)
    {   
        return VRLUtil.getServerVRL(vrl).toString(); 
    }

    @Override
    public ResourceSystemInfo updateResourceInfo(VRSContext context, ResourceSystemInfo info, VRL vrl)
    {
        info.setIfNotSet(new Attribute(dummyIntPar,1)); 
        info.setIfNotSet(new Attribute(dummyStringPar,"stringValue")); 
        info.setIfNotSet(new Attribute(dummyBoolPar,true)); 
        info.setIfNotSet(new Attribute(dummyEnumPar,new String[]{"enum1","enum2"},0)); 
        
        for (String name:attrNames)
        {   
            info.getAttributeSet().setEditable(name,true);
        }
        
        return info;
    }

    @Override
    public VResourceSystem createResourceSystemFor(VRSContext context, ResourceSystemInfo info, VRL vrl) throws VrsException
    {
        return new DummyRS(context,info,vrl); 
    }

}
