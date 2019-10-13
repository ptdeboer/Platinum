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

package nl.esciencecenter.vbrowser.vrs.sftp;

import com.jcraft.jsch.SftpATTRS;
import nl.esciencecenter.vbrowser.vrs.io.VFSFileAttributes;

import java.nio.file.attribute.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SftpFileAttributes implements VFSFileAttributes, PosixFileAttributes {

    protected SftpATTRS attrs;

    public SftpFileAttributes(SftpATTRS sftpATTRS) {
        this.attrs = sftpATTRS;
    }

    public boolean isSymbolicLink() {
        return attrs.isLink();
    }

    public boolean isHidden() {
        return false;
    }

    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(attrs.getMTime(), TimeUnit.SECONDS);
    }

    @Override
    public FileTime lastAccessTime() {
        return FileTime.from(attrs.getATime(), TimeUnit.SECONDS);
    }

    @Override
    public FileTime creationTime() {
        return null;
        // FileTime.from(attrs.getATime(),TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isRegularFile() {
        return attrs.isReg();
    }

    @Override
    public boolean isDirectory() {
        return attrs.isDir();
    }

    public boolean isSocket() {
        return attrs.isSock();
    }

    public boolean isFifo() {
        return attrs.isFifo();
    }

    @Override
    public boolean isOther() {
        return (attrs.isReg() == false);
    }

    @Override
    public long size() {
        return attrs.getSize();
    }

    @Override
    public Object fileKey() {
        return null;
    }

    @Override
    public UserPrincipal owner() {
        return null;
    }

    @Override
    public GroupPrincipal group() {
        return null;
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        return null;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

}
