/*
 * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
 * Copyright 2012-2013 Netherlands eScience Center.
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

/**
 * Helper class for local execution of script/commands etc. Is Factory class for
 * LocalProcess.
 * 
 * @author P.T. de Boer
 */
public class LocalExec
{

    /**
     * Execute command and return {stdout,stderr,status} triple as String array.
     * <p>
     * The String array cmds[] holds the command and argument to execute.
     * cmds[0] is the command to execute and cmds[1],...,cmds[n] are the
     * arguments. Method blocks until process has terminated!
     * <p>
     * Methods returns String array result[] which has at:
     * <li>result[0] complete output of stdout
     * <li>result[1] complete output of stderr;
     * <li>result[2] has the exit value in String value.
     * <p>
     * This method assumes no big output of text. Resulting String array (or
     * array elements) might be null upon error.
     */
    public static String[] execute(String cmds[]) throws IOException
    {
        String result[] = new String[3];

        {
            // PRE: new empty process:
            LocalProcess proc = new LocalProcess();
            // capture stderr, stdout
            proc.setCaptureOutput(true, true);

            // Execute command and wait:
            proc.execute(cmds, true);

            // POST:
            // get stdout,stderr
            String stdout = proc.getStdout();
            String stderr = proc.getStderr();
            int exit = proc.getExitValue();

            result[0] = stdout;
            result[1] = stderr;
            result[2] = "" + exit;

            dispose(proc);

            return result;
        }
    }

    private static void dispose(LocalProcess proc)
    {

        proc.dispose();
    }

    /**
     * Execute cmds[0] and returns LocalProcess object.
     * 
     * Returns Process object of terminated process or when wait=false the
     * Process object of running process.
     * 
     * @param wait
     *            - wait until process completes. if false the Actual runnign
     *            LocalProcess is returned.
     */
    public static LocalProcess execute(String cmds[], boolean wait) throws IOException
    {
        // new empty process:
        LocalProcess proc = new LocalProcess();
        // capture stderr, stdout
        proc.setCaptureOutput(true, true);
        // execute command and optionally wait:
        proc.execute(cmds, wait);

        return proc;
    }

}
