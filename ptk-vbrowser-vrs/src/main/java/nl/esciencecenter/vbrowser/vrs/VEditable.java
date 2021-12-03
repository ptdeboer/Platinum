package nl.esciencecenter.vbrowser.vrs;

import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

public interface VEditable {
    /**
     * returns true if the caller has the permissions to edit this resource.
     * This means the setAttribute(s) method(s) are allowed.
     * The default implementation for a VFSNode is to check whether
     * it is writable
     */
    boolean isEditable() throws VrsException;

    /**
     * Sets a list of attributes. Returns true if all attributes could be set.
     */
    boolean setAttributes(Attribute[] attrs) throws VrsException;

    /**
     * Set single attribute. Return true if attribute was set.
     */
    boolean setAttribute(Attribute attr) throws VrsException;

}
