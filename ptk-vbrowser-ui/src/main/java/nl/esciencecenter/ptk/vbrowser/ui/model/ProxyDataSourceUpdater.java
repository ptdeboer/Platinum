package nl.esciencecenter.ptk.vbrowser.ui.model;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface ProxyDataSourceUpdater // extends ViewNodeDataSourceUpdater
{
    ProxyDataSource getDataSource();

    /**
     * Update/refresh all relevant data.
     */
    void update();

    /**
     * Update specified resources and optional Attribute Names.
     *
     * @param vrls              - list of resources to update, can be null to indicate all.
     * @param optAttributeNames - update specific attributes, set to null to update all.
     */
    void update(VRL[] vrls, String[] optAttributeNames);

}
