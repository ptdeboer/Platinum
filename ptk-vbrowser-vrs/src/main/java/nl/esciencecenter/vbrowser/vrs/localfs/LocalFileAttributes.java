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

package nl.esciencecenter.vbrowser.vrs.localfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import nl.esciencecenter.vbrowser.vrs.io.VFSFileAttributes;

public class LocalFileAttributes implements VFSFileAttributes
{
    protected BasicFileAttributes attrs;

    public LocalFileAttributes(BasicFileAttributes attrs)
    {
        this.attrs = attrs;
    }

    public boolean isSymbolicLink()
    {
        return attrs.isSymbolicLink();
    }

    public String getSymbolicLinkTarget()
    {
        return null;
    }

    public boolean isHidden()
    {
        return false;
    }

    @Override
    public FileTime lastModifiedTime()
    {
        return attrs.lastModifiedTime();
    }

    @Override
    public FileTime lastAccessTime()
    {
        return attrs.lastAccessTime();
    }

    @Override
    public FileTime creationTime()
    {
        return attrs.creationTime();
    }

    @Override
    public boolean isRegularFile()
    {
        return attrs.isRegularFile();
    }

    @Override
    public boolean isDirectory()
    {
        return attrs.isDirectory();
    }

    @Override
    public boolean isOther()
    {
        return attrs.isOther();
    }

    @Override
    public long size()
    {
        return attrs.size();
    }

    @Override
    public Object fileKey()
    {
        return attrs.fileKey();
    }

    @Override
    public boolean isLocal()
    {
        return true;
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

}
