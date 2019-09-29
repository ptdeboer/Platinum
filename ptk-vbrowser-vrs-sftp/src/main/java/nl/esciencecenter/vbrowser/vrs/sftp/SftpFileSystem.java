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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.crypt.Secret;
import nl.esciencecenter.ptk.ui.UI;
import nl.esciencecenter.vbrowser.vrs.VCloseable;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceAccessDeniedException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceAlreadyExistsException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceNotFoundException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.io.VShellChannelCreator;
import nl.esciencecenter.vbrowser.vrs.io.VStreamCreator;
import nl.esciencecenter.vbrowser.vrs.node.VFileSystemNode;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpChannel;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpConfig;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SftpEntry;
import nl.esciencecenter.vbrowser.vrs.sftp.jsch.SshSession;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
import nl.esciencecenter.vbrowser.vrs.vrl.VRLUtil;

import nl.piter.vterm.api.ChannelOptions;
import nl.piter.vterm.api.ShellChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SftpFileSystem extends VFileSystemNode implements VStreamCreator,
        VShellChannelCreator, VCloseable {

    private static final Logger logger = LoggerFactory.getLogger(SftpFileSystem.class);

    /** Configuration subdir relative to user home, for example ".ssh" */
    public static final String SSH_USER_CONFIGSUBDIR_PROPERTY = "sshUserConfigSubDir";

    /** User known hosts file relative to user's configuration directory, for example "known_hosts". */
    public static final String SSH_USER_KNOWN_HOSTS_PROPERTY = "sshUserKnownHostsFile";

    /** Users identidyf files, matching the global ResourceSystemInfo property name */
    public static final String SSH_USER_IDENTITY_FILES = ResourceConfigInfo.ATTR_USER_KEY_FILES;

    private SshSession sftpSession;

    private SftpChannel sftpChannel;

    public SftpFileSystem(JSch jsch, VRSContext context, ResourceConfigInfo info, VRL vrl)
            throws VrsException {
        //
        super(context, VRLUtil.getServerVRL(vrl));
        logger.debug("SftpFileSystem:New(): for vrl={}", vrl);
        UI ui = context.getUI();
        UserUI userUI = null;

        if ((ui != null) && (ui.isEnabled())) {
            userUI = new UserUI(ui);
        }

        try {
            // one client per Filesystem
            this.sftpSession = new SshSession(jsch, createSftpConfig(context, info), false);
            if (userUI != null) {
                this.sftpSession.setUserUI(userUI);
            }
            this.sftpSession.connect();
            // one SftpChannel per FileSystem
            this.sftpChannel = sftpSession.createSftpChannel();
            this.sftpChannel.connect();
        } catch (Exception e) {
            throw new VrsException(e.getMessage(), e);
        }
    }

    protected SftpConfig createSftpConfig(VRSContext context, ResourceConfigInfo info) {
        SftpConfig config = new SftpConfig();
        return updateSftpConfig(config, context, info);
    }

    protected SftpConfig updateSftpConfig(SftpConfig config, VRSContext context,
            ResourceConfigInfo info) {
        //
        config.host = info.getServerHostname();
        config.port = info.getServerPort();

        if (config.port <= 0) {
            config.port = 22;
        }
        config.user = info.getUsername();
        Secret pwd = info.getPassword();
        if (pwd != null) {
            config.passwd = pwd.getChars();
        }

        String subDir = info.getProperty(SftpFileSystem.SSH_USER_CONFIGSUBDIR_PROPERTY);
        if (subDir != null) {
            config.userConfigDir = context.getHomeVRL().getPath() + "/" + subDir;
        }
        String knownHosts = info.getProperty(SftpFileSystem.SSH_USER_KNOWN_HOSTS_PROPERTY);
        if (knownHosts != null) {
            config.userConfigDir = context.getHomeVRL().getPath() + "/" + subDir;
        }
        config.privateKeys = new String[] { "id_rsa", "id_dsa" };

        config.sshKnowHostFile = SftpConfig.SSH_USER_KNOWN_HOSTS;
        logger.debug("updateSftpConfig(): config:{}", config);

        return config;
    }

    public SshSession getSftpSession() {
        return sftpSession;
    }

    public SftpChannel getSftpChannel() {
        return this.sftpChannel;
    }

    @Override
    protected SftpPathNode createVFSNode(VRL vrl) throws VrsException {
        return createNode(vrl);
    }

    protected SftpPathNode createNode(VRL vrl) throws VrsException {
        return new SftpPathNode(this, vrl);
    }

    public List<SftpPathNode> listNodes(String remotePath) throws VrsException {
        //
        logger.debug("listNodes():remotePath='{}'", remotePath);

        try {
            List<SftpEntry> entries = sftpChannel.list(remotePath);
            List<SftpPathNode> nodes = new ArrayList<SftpPathNode>();

            for (SftpEntry entry : entries) {
                nodes.add(this.createNode(resolveVRL(remotePath, entry.getFilename())));
            }
            return nodes;
        } catch (Exception e) {
            throw new VrsException(e.getMessage(), e);
        }
    }

    protected VRL resolveVRL(String dirname, String filename) {
        return new VRL(this.getServerVRL().replacePath(dirname + "/" + filename));
    }

    public SftpATTRS fetchSftpAttrs(String remotePath, boolean resolveLink) throws VrsException {
        logger.debug("fetchSftpAttrs():resolveLink,remotePath='{}'",
                resolveLink ? "true" : "false", remotePath);

        try {
            return sftpChannel.statSftpAttrs(remotePath, resolveLink);
        } catch (SftpException e) {
            logger.error("fetchSftpAttrs():remotePath='{}' => SftpException:{}", remotePath,
                    e.getMessage());
            throw convertSftpException(e, "Fetching attributes from:" + remotePath);
        }
    }

    public boolean exists(String remotePath) throws VrsException {
        try {
            return sftpChannel.exists(remotePath);
        } catch (SftpException e) {
            logger.error("exists():remotePath='{}' => SftpException:{}", remotePath, e.getMessage());
            throw new VrsException(e.getMessage(), e);
        }
    }

    // =========================
    // Create/Delete/Rename
    // =========================

    public boolean mkdir(String remotePath, boolean ignoreExisting) throws VrsException {
        logger.debug("mkdir(),ignoreExisting={},remotePath={}", ignoreExisting, remotePath);
        boolean exists = exists(remotePath);

        try {
            if (ignoreExisting == true) {
                if (exists) {
                    return true;
                } else {
                    // continue;
                }
            } else {
                if (exists) {
                    throw new ResourceAlreadyExistsException("Path already exists:" + remotePath,
                            null);
                }
            }

            return sftpChannel.mkdir(remotePath);

        } catch (SftpException e) {
            logger.error("mkdir():remotePath='{}' => SftpException:{}", remotePath, e.getMessage());
            throw convertSftpException(e, "Performing mkdir():exists=" + exists
                    + ",ignoreExisting=" + ignoreExisting + ",remotePath='" + remotePath + "'");
        }
    }

    public boolean delete(String remotePath, boolean isDir, LinkOption[] options)
            throws VrsException {
        try {
            return sftpChannel.delete(remotePath, isDir);
        } catch (SftpException e) {
            logger.error("mkdir():delete='{}' => SftpException:{}", remotePath, e.getMessage());
            throw new VrsException(e.getMessage(), e);
        }
    }

    public VFSPath renameTo(VFSPath sourcePath, VFSPath otherPath) throws VrsException {
        try {
            String otherPathStr = otherPath.getVRL().getPath();
            sftpChannel.rename(sourcePath.getVRL().getPath(), otherPathStr);
            return otherPath;
        } catch (SftpException e) {
            logger.error("mkdir():renameTo='{}' => '{}' => SftpException:{}", sourcePath,
                    otherPath, e.getMessage());
            throw new VrsException(e.getMessage(), e);
        }
    }

    // =========================
    // Stream creation methods
    // =========================

    @Override
    public OutputStream createOutputStream(VRL vrl) throws VrsException {
        return createOutputStream(vrl.getPath(), false);
    }

    @Override
    public InputStream createInputStream(VRL vrl) throws VrsException {
        return createInputStream(vrl.getPath());
    }

    public InputStream createInputStream(String remotePath) throws VrsException {
        try {
            return this.sftpSession.createSftpInputStream(remotePath);
        } catch (SftpException | JSchException e) {
            logger.error("mkdir():createInputStream='{}' => Exception:{}", remotePath,
                    e.getMessage());
            throw new VrsException("Performing createInputStream() on remotePath:'" + remotePath
                    + "'", e);
        }
    }

    public OutputStream createOutputStream(String remotePath, boolean append) throws VrsException {
        try {
            return this.sftpSession.createSftpOutputStream(remotePath, append);
        } catch (JSchException | SftpException e) {
            logger.error("mkdir():remotePath='{}' => Exception:{}", remotePath, e.getMessage());
            throw new VrsException("Performing createOutputStream() on remotePath:" + remotePath, e);
        }

    }

    // =========================
    // Misc.
    // =========================

    private VrsException convertSftpException(SftpException e, String action) {
        switch (e.id) {
            case ChannelSftp.SSH_FX_NO_SUCH_FILE:
                return new ResourceNotFoundException("Resource not found:" + action, e);
            case ChannelSftp.SSH_FX_EOF:
                return new ResourceException("Resource read exception (EOF):" + action, e,
                        "EOF Exception");
            case ChannelSftp.SSH_FX_PERMISSION_DENIED:
                return new ResourceAccessDeniedException("Resource read exception (EOF):" + action,
                        e);
            case ChannelSftp.SSH_FX_FAILURE:
                return new ResourceException("Unknown Failure:" + action, e, "Unknown");

        }

        return new VrsException("SftpExeption:" + action + "\nReason=" + e.getMessage(), e);
    }

    @Override
    public ShellChannel createShellChannel(VRL optionalLocation, ChannelOptions options) throws IOException {
        try {
            ChannelShell shellChannel = this.sftpSession.createShellChannel();
            SshShellChannel sshShellChannel = new SshShellChannel(this.sftpSession, shellChannel,options);
            return sshShellChannel;
        } catch (JSchException e) {
            throw new IOException("Failed to create (SSH)ShellChannel" + e.getMessage(), e);
        }
    }

    @Override
    public boolean close() throws IOException {
        logger.debug("SftpFileSystem closing:" + this);
        this.sftpChannel.close();
        this.sftpSession.close();
        return true;
    }

    public String toString() {
        return "SftpFileSystem:[sftpSession='" + this.sftpSession + "']";
    }

}
