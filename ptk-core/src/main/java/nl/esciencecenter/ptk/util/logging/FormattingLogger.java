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

package nl.esciencecenter.ptk.util.logging;

//Only allow default java imports. Almost all classes refer to this class!
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Legacy Java Logger Facade
 */
public class FormattingLogger extends Logger {

    // ==================
    // Class Fields 
    // ==================

    public static final String DEFAULT_RESOURCEBUNDLENAME = "nl.nlesc.ptk.logging";

    public static final Level ALL = Level.ALL;
    public static final Level DEBUG_FULL = Level.FINEST;
    public static final Level DEBUG = Level.FINE;
    public static final Level INFO = Level.INFO;
    public static final Level WARN = Level.WARNING;
    public static final Level ERROR = Level.SEVERE;
    public static final Level FATAL = ERROR;
    public static final Level NONE = Level.OFF;

    // ==================
    // Instance 
    // ==================

    protected FormattingLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    protected FormattingLogger(String name) {
        // todo: resourcebundle names 
        super(name, null);
    }

    // === Level Methods === 
    public void setLevelToDebug() {
        this.setLevel(DEBUG);
    }

    public void setLevelToInfo() {
        this.setLevel(INFO);
    }

    public void setLevelToWarn() {
        this.setLevel(WARN);
    }

    public void setLevelToError() {
        this.setLevel(ERROR);
    }

    public void setLevelToFatal() {
        this.setLevel(FATAL);
    }

    public void setLevelToNone() {
        this.setLevel(NONE);
    }

    /**
     * Clears log level so that the effective log level is inherited from the Parent logger;
     */
    public void setLevelToParent() {
        Logger parent = this.getParent();

        if (parent != null) {
            this.setLevel(parent.getLevel());
        }
    }

    /**
     * Whether specified level will be logged, or the level value is higher or equal then the
     * current loglevel.
     */
    public boolean hasEffectiveLevel(Level level) {
        return this.isLoggable(level);
    }

    /** Whether DEBUG level is enabled. */
    public boolean isLevelDebug() {
        return isLoggable(FormattingLogger.DEBUG);
    }

    // ========================================================================
    // Actual Log Methods 
    // ========================================================================

    //Java say this is the method override 
    public void log(LogRecord record) {
        // Default is to bounce it up the the parent handler!
        super.log(record); // bounce up ?  
    }

    public void logPrintf(Level level, String format, Object... args) {
        log(level, format, args);
    }

    public void debugPrintf(String format, Object... args) {
        this.log(DEBUG, format, args);
    }

    public void infoPrintf(String format, Object... args) {
        this.log(INFO, format, args);
    }

    public void warnPrintf(String format, Object... args) {
        this.log(WARN, format, args);
    }

    public void errorPrintf(String format, Object... args) {
        this.log(ERROR, format, args);
    }

    /** Print fatal message. Append end of line character */
    public void fatal(String msg) {
        super.log(FATAL, msg + "\n");
    }

    public void logException(Level level, Throwable e, String format, Object... args) {
        if (this.isLoggable(level) == false) // slow check!
            return;

        // create custom LogRecord!
        LogRecord lr = new LogRecord(level, format);
        lr.setLoggerName(this.getName());
        lr.setParameters(args);
        lr.setThrown(e);
        log(lr);
    }

    // Don't forget to add a handler which outputs the actual log messages ! 
    public synchronized void addHandler(Handler handler) throws SecurityException {
        super.addHandler(handler);
    }

}
