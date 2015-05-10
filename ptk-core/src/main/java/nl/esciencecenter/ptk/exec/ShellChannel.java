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

package nl.esciencecenter.ptk.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Generic interface for Shell Channels which have a pty (pseudo-terminal) associated with it.
 * <p>
 * Whether features are supported depends on the implementing shell channel.
 */
public interface ShellChannel {
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

    /**
     * @return terminal type, for example "vt100" or "xterm".
     * @throws IOException
     */
    public String getTermType() throws IOException;

    /**
     * Tries to set terminal type to underlaying shell channel. For example ror example "vt100" or
     * "xterm".
     * 
     * @return true of terminal type was succesfuly updated. False if terminal type is not
     *         supported.
     * @throws IOException
     */
    public boolean setTermType(String type) throws IOException;

    /**
     * Tries to set terminal size to underlaying shell channel.
     * 
     * @return true of terminal type was succesfuly updated. False if terminal type is not
     *         supported.
     * @throws IOException
     */
    public boolean setTermSize(int numColumns, int numRows, int wp, int hp) throws IOException;

    /**
     * Returns array of int[2] {col,row} or int[4] {col,row,wp,hp} of remote terminal (pty) size.
     * Return NULL if size couldn't be determined (terminal sizes not supported)
     */
    public int[] getTermSize() throws IOException;

    /**
     * Wait until shell has finished.
     * 
     * @throws InterruptedException
     */
    public void waitFor() throws InterruptedException;

    /**
     * Exit value if shell process.
     */
    public int exitValue();

}
