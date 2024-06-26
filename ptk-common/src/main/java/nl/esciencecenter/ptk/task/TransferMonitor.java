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

package nl.esciencecenter.ptk.task;

import lombok.ToString;

import java.net.URI;

/**
 * Transfer Specific Monitor. Adds more meta fields specific for (VRS) File Transfers.
 */
@ToString
public class TransferMonitor extends TaskMonitorAdaptor {

    private static int transferCounter = 0;

    // === instance === //

    private int transferId = 0;
    private URI[] sources;
    private String[] sourceTypes;
    private final URI dest;
    private String destType;
    private final String actionType;
    private int sourcesDone;

    /**
     * Create Transfer monitor for specified URIs to destination URI.
     */
    public TransferMonitor(String action, URI[] sourceUris, URI destVri) {
        super("TransferMonitor", sourceUris.length);
        this.transferId = transferCounter++;
        this.actionType = action;
        this.sources = sourceUris;
        this.dest = destVri;
    }

    /**
     * Create Transfer monitor with optional Resource Types.
     */
    public TransferMonitor(String action, URI[] sourceUris, String[] sourceTypes, URI destVri, String destType) {
        super("TransferMonitor", sourceUris.length);
        this.transferId = transferCounter++;
        this.actionType = action;
        this.sources = sourceUris;
        this.sourceTypes = sourceTypes;
        this.destType = destType;
        this.dest = destVri;
    }

    public String getID() {
        return "transfer:#" + transferId;
    }

    public URI getDestination() {
        return dest;
    }

    public String getDestinationType() {
        return destType;
    }

    public URI getSource() {
        if ((sources != null) && (sources.length > 0))
            return sources[0];
        return null;
    }

    public URI[] getSources() {
        return sources;
    }

    public int getTotalSources() {
        if (sources != null)
            return sources.length;
        return 0;
    }

    public int getSourcesDone() {
        return sourcesDone;
    }

    public void updateSourcesDone(int done) {
        this.sourcesDone = done;
    }

    public String getActionType() {
        return actionType;
    }

    // ===
    // Sub class interface:
    // ===

    /**
     * During a transfer, more sources might be added by the on going transfer process.
     *
     * @param sources - new full list of sources.
     */
    protected void updateSources(URI[] sources) {
        this.sources = sources;
    }

}
