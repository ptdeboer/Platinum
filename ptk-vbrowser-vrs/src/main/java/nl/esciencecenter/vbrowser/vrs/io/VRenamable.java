package nl.esciencecenter.vbrowser.vrs.io;

import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

/**
 * Renamable interface for resources. This interface uses logical names, for VPath renames see
 * VPathRenamable.
 */
public interface VRenamable {

    /**
     * Rename this (virtual) resource.
     *
     * @param newNameOrPath - logical name or relative path.
     * @return update path if rename was successful.
     */
    VPath renameTo(String newNameOrPath) throws VrsException;

}
