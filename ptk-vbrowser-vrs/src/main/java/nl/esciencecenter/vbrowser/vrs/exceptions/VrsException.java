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

package nl.esciencecenter.vbrowser.vrs.exceptions;

/**
 * Super class of all VRS Exceptions.
 * <p>
 * The Class VrsException provides more high-level information about the Exception which occurred
 * and hides the original System Exception.
 * <p>
 * It it recommend to wrap low level exceptions and nested them into more descriptive Exceptions
 * providing extra information from the underlying implementation. <br>
 */
public class VrsException extends Exception {

    protected VrsException() {
        super();
    }

    public VrsException(Throwable cause) {
        super(cause);
    }

    public VrsException(String message) {
        super(message);
    }

    public VrsException(String message, Throwable cause) {
        super(message, cause);
    }

    public String toString() {
        String message_txt = "";
        Throwable parent = null;
        Throwable current = this;
        int index = 0;

        do {
            if (index == 0) {
                message_txt = this.getClass().getCanonicalName() + ":" + getMessage();
            } else {
                message_txt += "\n--- Nested Exception Caused By [" + index + "] ---\n";
                message_txt += current.getClass().getName() + ":" + current.getMessage();
            }

            // get next in exception chain:
            parent = current;
            current = current.getCause();
            index++;

        } while ((current != null) && (current != parent) && (index < 100));

        return (message_txt);
    }

    public String toStringPlusStacktrace() {
        return this + "\n ---Stack Trace --- \n" + getChainedStackTraceText(this);
    }

    /**
     * Returns the stacktrace, including nested Exceptions as single String
     */
    public static String getChainedStackTraceText(Throwable e) {
        String text = "";
        Throwable parent = null;
        Throwable current = e;
        int index = 0;

        // === get whole exception chain:

        do {
            if (index > 0)
                text += "--- Nested Exception Caused By: ---\n";

            text += "Exception[" + index + "]:" + current.getClass().getName() + "\n";
            text += "message=" + current.getMessage() + "\n";

            StackTraceElement[] els = current.getStackTrace();

            if (els != null)
                for (int i = 0; i < els.length; i++)
                    text += "[" + i + "]" + els[i] + "\n";

            // get next in exception chain:
            parent = current;
            current = current.getCause();
            index++;
        } while ((current != null) && (current != parent) && (index < 100));

        return text;
    }

}
