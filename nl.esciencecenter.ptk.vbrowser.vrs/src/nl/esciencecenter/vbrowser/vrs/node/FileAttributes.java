/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

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
