package nl.esciencecenter.vbrowser.vrs.sftp.jsch;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;

/**
 * An SftpEntry symbolizes a remote Sftp file or directory. <br>
 * It wrappes around a (ChannelSftp) LsEntry Object.
 */
public class SftpEntry {

    final private LsEntry lsEntry;

    protected SftpEntry(LsEntry entry) {
        if (entry == null)
            throw new NullPointerException("Fatal: LsEntry can not be null");
        this.lsEntry = entry;
    }

    /**
     * @return The filename is the last part of the path.
     */
    public String getFilename() {
        return lsEntry.getFilename();
    }

    /**
     * @return LSEntry text as reported by the remote filesystem. Typically this the output from a
     *         remote 'ls -l' command.
     */
    public String getLSText() {
        return lsEntry.getLongname();
    }

    /**
     * Actual SftpATTRS object.
     * 
     * @return
     */
    public SftpATTRS getAttrs() {
        return lsEntry.getAttrs();
    }

    public boolean isDir() {
        SftpATTRS attrs = lsEntry.getAttrs();
        return ((attrs != null) && (attrs.isDir()));
    }

    public boolean isFile() {
        SftpATTRS attrs = lsEntry.getAttrs();
        return ((attrs != null) && (attrs.isDir()));
    }

    public String toString() {
        String str = "SftpEntry:[";
        str += "filename:'" + getFilename() + "',";
        str += "lsText:'" + getLSText() + "',";
        str += "sftpAttrs:'" + getAttrs() + "'";
        str += "]";
        return str;
    }

}
