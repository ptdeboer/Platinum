package nl.esciencecenter.vbrowser.vrs.io;

import java.nio.file.attribute.BasicFileAttributes;

public interface VFSFileAttributes extends BasicFileAttributes
{

    boolean isHidden();

    boolean isLocal();

    boolean isRemote();

}
