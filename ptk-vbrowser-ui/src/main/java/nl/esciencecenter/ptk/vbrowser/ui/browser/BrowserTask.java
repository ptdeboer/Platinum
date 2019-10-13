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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.ITaskSource;
import nl.esciencecenter.ptk.task.TaskMonitorAdaptor;

/**
 * ActionTask origination from a (Proxy)Browser
 */
public abstract class BrowserTask extends ActionTask {
    /**
     * Create new task origination from the provided ProxyBrowser. Created task will be linked to
     * this ProxyBrowserController.
     */
    public BrowserTask(ProxyBrowserController browserController, String taskName) {
        super(browserController.getTaskSource(), taskName, new TaskMonitorAdaptor(taskName, 1));
    }

    /**
     * Create new task origination from the provided Browser task source.
     *
     * @param taskSource - optional Browser TaskSource. Can be null.
     * @param taskName   - Descriptive task name.
     */
    public BrowserTask(ITaskSource taskSource, String taskName) {
        super(taskSource, taskName, new TaskMonitorAdaptor(taskName, 1));
    }

    public BrowserTask(ITaskSource taskSource, String taskName, ITaskMonitor monitor) {
        super(taskSource, taskName, monitor);
    }

    @Override
    protected void stopTask() {
        this.getTaskMonitor().logPrintf("*STOP* Received for:%s\n", this);
    }

}
