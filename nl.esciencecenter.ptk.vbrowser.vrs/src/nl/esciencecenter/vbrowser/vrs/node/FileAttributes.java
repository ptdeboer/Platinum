package nl.esciencecenter.vbrowser.vrs.node;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import nl.esciencecenter.ptk.presentation.Presentation;

public abstract class FileAttributes implements BasicFileAttributes
{
    public boolean isSymbolicLink()
    {
        return false; 
    }

    public boolean isHidden()
    {
        return false; 
    }

    public java.util.Date getModificationTimeDate()
    {
        FileTime time = this.lastModifiedTime();
        if (time==null)
        {
            return null;
        }
        return Presentation.createDate(time.toMillis());
    }

    public java.util.Date getCreationTimeDate()
    {
        FileTime time = this.creationTime();
        if (time==null)
        {
            return null;
        }
        return Presentation.createDate(time.toMillis());
    }

    public java.util.Date getLastAccessTimeDate()
    {
        FileTime time = this.lastAccessTime();
        if (time==null)
        {
            return null;
        }
        return Presentation.createDate(time.toMillis());
    }

    @Override
    public boolean isOther()
    {
        return (!isRegularFile() || !isDirectory() || !isSymbolicLink());
    }

    @Override
    public Object fileKey()
    {
        return null;
    }

    @Override
    abstract public FileTime lastModifiedTime(); 

    @Override
    abstract public FileTime lastAccessTime(); 

    @Override
    abstract public FileTime creationTime(); 

    @Override
    abstract public boolean isRegularFile(); 

    @Override
    abstract public boolean isDirectory(); 

    @Override
    abstract public long size(); 


}
