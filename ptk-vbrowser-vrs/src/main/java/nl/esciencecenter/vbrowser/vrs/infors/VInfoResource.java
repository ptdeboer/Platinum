package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Shared interface for ResourceLinks and ResourceFolders
 */
public interface VInfoResource {

    boolean isResourceLink();

    boolean isResourceFolder();

    VRL getTargetVRL();

    /**
     * Create new InfoRS node of specified resourceType from persistent Attributes.
     */
    VInfoResourcePath createSubNode(String resourceType, AttributeSet infoAttributes) throws VrsException;

    VInfoResourcePath createFolder(String name) throws VrsException;

    VInfoResourcePath createResourceLink(VRL targetVRL, String logicalName) throws VrsException;

    AttributeSet getInfoAttributes() throws VrsException;

}
