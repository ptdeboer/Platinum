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

package nl.esciencecenter.vbrowser.vrs.task;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.task.ActionTask;
import nl.esciencecenter.ptk.task.TaskWatcher;

/**
 * ActionTask Watcher for VRSTasks.
 */
@Slf4j
public class VRSTaskWatcher extends TaskWatcher {

    private static VRSTaskWatcher instance = null;

    public static VRSTaskWatcher getTaskWatcher() {
        if (instance == null) {
            instance = new VRSTaskWatcher("VRSTaskWatcher");
        }

        return instance;
    }

    public VRSTaskWatcher(String name) {
        super(name);
    }

    @Override
    public void notifyTaskStarted(ActionTask actionTask) {
        log.error("+++ notifyTaskStarted:{}", actionTask);
        super.notifyTaskStarted(actionTask);
    }

    @Override
    public void notifyTaskTerminated(ActionTask actionTask) {
        log.error("--- notifyTaskTerminated:{}", actionTask);
        super.notifyTaskTerminated(actionTask);
    }

}
