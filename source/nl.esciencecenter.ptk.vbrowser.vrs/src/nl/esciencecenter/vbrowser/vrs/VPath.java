package nl.esciencecenter.vbrowser.vrs;

import java.util.List;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.data.AttributeDescription;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VPath
{
    public VRL getVRL();

    /** 
     * Return short name or logical name. 
     * Default is basename of VRL. 
     * @return short name or logical name of this resource. 
     */
    public String getName(); 
        
    public String getResourceType() throws VrsException; 

    public VResourceSystem VResourceSystem() throws VrsException; 

    public VRL resolvePathVRL(String path) throws VrsException; 

    public VPath resolvePath(String path) throws VrsException; 
 
    /**
     * Return parent VPath of this VPath. 
     * Default implementation returns directory name.  
     * @return Logical Parent (V)Path of this VPath. 
     * @throws VrsException
     */
    public VPath getParent() throws VrsException;
    
    public boolean isComposite() throws VrsException;
    
    public String getIconURL(int size) throws VrsException; 

    public String getMimeType() throws VrsException;
    
    public String getResourceStatus() throws VrsException;

    public List<AttributeDescription> getAttributeDescriptions(); 

    public List<Attribute> getAttributes(List<String> names) throws VrsException;

    public List<String> getAttributeNames() throws VrsException;

    public List<? extends VPath> list() throws VrsException;

    public List<String> getChildNodeResourceTypes() throws VrsException; 
 
}
