package nl.esciencecenter.ptk.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface for Shell Channels which have a pty (pseudo-terminal) associated with it.  
 * Whether features are supported depends on the implementing shell channel. 
 */
public interface ShellChannel 
{
    /** 
     * Get stdin OutputStream (to write to remote shell) after channel has connected. 
     */ 
    public OutputStream getStdin();

    /** 
     * Get stdout InputStream (to read from remote shell) after channel has connected. 
     */ 
    public InputStream getStdout();
    
    /** 
     * Get Optional stderr InputStream if supported. Stderr might be mixed with stdout.
     */ 
    public InputStream getStderr(); 

    public void connect() throws IOException; 
    
    public void disconnect(boolean waitForTermination) throws IOException; 

    // === tty/shell Options === 
    
    public String getTermType() throws IOException; 
    
    public boolean setTermType(String type) throws IOException; 
    
    public boolean setTermSize(int col, int row, int wp, int hp) throws IOException; 
    
    /**
     * Returns array of int[2] {col,row} or int[4] {col,row,wp,hp} of remote terminal (pty) size.
     * Return NULL if size couldn't be determined (terminal sizes not supported)  
     */ 
    public int[] getTermSize() throws IOException; 

    // === Life Cycle management === 
    
    public void waitFor() throws InterruptedException;

    public int exitValue();

}
