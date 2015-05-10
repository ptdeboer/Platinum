package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;

public interface VResourceConfigurable {

    ResourceConfigInfo getResourceConfigInfo() throws VrsException;

    ResourceConfigInfo updateResourceConfigInfo(ResourceConfigInfo info);

}
