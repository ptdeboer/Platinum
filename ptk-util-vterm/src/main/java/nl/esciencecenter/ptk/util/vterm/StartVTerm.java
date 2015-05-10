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

package nl.esciencecenter.ptk.util.vterm;

import java.io.IOException;
import java.net.URI;

import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.exec.ShellChannel;

public class StartVTerm {

    public static void main(String[] arg) {
        startVTerm();
    }

    public static void startVTerm() {
        startVTerm(new VTermChannelProvider(), null, null);
    }

    public static VTerm startVTerm(ShellChannel shellChan) {
        return startVTerm(new VTermChannelProvider(), null, shellChan);
    }

    public static VTerm startVTerm(URI loc) {
        return startVTerm(new VTermChannelProvider(), loc, null);
    }

    public static VTerm startVTerm(VTermChannelProvider channelProvider,
            final URI optionalLocation, final ShellChannel shellChan) {
        final VTerm term = new VTerm(channelProvider);

        // always create windows during Swing Event thread 
        Runnable creator = new Runnable() {
            public void run() {

                // center on screen
                term.setLocationRelativeTo(null);
                term.setVisible(true);
                term.showSplash();
                term.requestFocus();

                term.updateFrameSize();
                if (shellChan != null) {
                    term.setShellChannel(shellChan);
                    term.startSession();
                }

                if (optionalLocation != null) {
                    try {
                        term.openLocation(optionalLocation);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        };

        SwingUtilities.invokeLater(creator);

        return term;

        /*
         * { Insets insets = frame.getInsets(); int width =
         * awtTerm.getTermWidth(); int height = awtTerm.getTermHeight(); width +=
         * (insets.left + insets.right); height += (insets.top + insets.bottom);
         * frame.setSize(width, height); }
         */
    }

}
