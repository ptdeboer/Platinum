package nl.esciencecenter.ptk.vbrowser.ui.model;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface ProxyDataSourceUpdater // extends ViewNodeDataSourceUpdater
{
    public ProxyDataSource getDataSource(); 

    /** 
     * Update/refresh all relevant data.  
     */
    public void update(); 

    /** 
     * Update specified resources and optional Attribute Names. 
     * @param vrls - list of resources to update, can be null to indicate all. 
     * @param optAttributeNames - update specific attributes, set to null to update all. 
     */
    public void update(VRL vrls[],String[] optAttributeNames);
    
}
