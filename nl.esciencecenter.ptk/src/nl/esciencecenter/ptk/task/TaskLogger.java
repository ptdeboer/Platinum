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

import java.util.logging.Level;

import nl.esciencecenter.ptk.data.StringHolder;
import nl.esciencecenter.ptk.util.logging.FormattingLogger;
import nl.esciencecenter.ptk.util.logging.RecordingLogHandler;

/**
 * Custom logger class for the TaskMonitor. Use java.logging compatible (sub)class.
 */
public class TaskLogger extends FormattingLogger
{
    private RecordingLogHandler handler;

    private Level defaultLevel = INFO;

    public TaskLogger(String name)
    {
        super(name);
        this.handler = new RecordingLogHandler();
        this.addHandler(handler);
        // default level=info, but default logPrintf uses "ALL" anyway:
        this.setLevel(defaultLevel);
    }

    /** Default logPrintf for TaksLogger */
    public void logPrintf(String format, Object... args)
    {
        // Default log level for task logger is INFO.
        log(defaultLevel, format, args);
    }

    public int getLogText(boolean clearLogBuffer, int logEventOffset, StringHolder logTextHolder)
    {
        return handler.getLogText(clearLogBuffer, logEventOffset, logTextHolder);
    }

}