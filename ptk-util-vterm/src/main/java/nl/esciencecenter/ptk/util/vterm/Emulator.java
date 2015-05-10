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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import nl.esciencecenter.ptk.ui.charpane.ICharacterTerminal;

/**
 * Emulator Super class
 * 
 * Currenlty EmulatorVT100 is a vt100/xterm based terminal
 * 
 * @author P.T. de Boer
 */
public abstract class Emulator {
    private Vector<EmulatorListener> listeners = new Vector<EmulatorListener>();

    private String encoding = "UTF-8";

    ICharacterTerminal term = null;
    VT10xTokenizer tokenizer = null;
    private OutputStream outputStream;
    protected InputStream errorInput;
    protected boolean isConnected = false;
    protected String termType;

    protected int nr_columns;
    protected int nr_rows;

    public Emulator(ICharacterTerminal term, InputStream inps, OutputStream outps) {
        setTerm(term);
        setInputStream(inps);
        this.outputStream = outps;
        this.nr_columns = term.getColumnCount();
        this.nr_rows = term.getRowCount();

    }

    /** Reset states, but do NO disconnect */
    public void reset() {
        nr_columns = term.getColumnCount();
        nr_rows = term.getRowCount();
    }

    void setTerm(ICharacterTerminal term) {
        this.term = term;
    }

    void setInputStream(InputStream inps) {
        this.tokenizer = new VT10xTokenizer(this, inps);
    }

    byte single[] = new byte[1];

    public void send(byte b) throws IOException {
        single[0] = b;
        send(single);
    }

    public void send(byte[] code) throws IOException {
        this.outputStream.write(code);
        this.outputStream.flush();
    }

    /**
     * Check whether there is text from stderr which is connected to the Terminal implementation.
     */
    protected void readErrorStream() throws IOException {
        if (errorInput == null)
            return;

        int MAX = 1024;
        byte buf[] = new byte[MAX + 1];

        if (this.errorInput.available() > 0) {
            int size = this.errorInput.available();

            if (size > MAX)
                size = 1024;

            int numread = errorInput.read(buf, 0, size);

            buf[numread] = 0;

            String str = new String(buf, 0, numread);
            System.err.println(str);
        }

    }

    public void setErrorInput(InputStream errorStream) {
        this.errorInput = errorStream;
    }

    protected void setConnected(boolean val) {
        isConnected = val;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /** Set/send new TERM type */
    public void setTermType(String type) {
        this.termType = type;
    }

    public String getTermType() {
        return termType;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void addListener(EmulatorListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(EmulatorListener listener) {
        this.listeners.remove(listener);
    }

    protected void fireGraphModeEvent(int type, String text) {
        for (EmulatorListener listener : listeners) {
            listener.notifyGraphMode(type, text);
        }
    }

    protected void fireResizedEvent(int columns, int rows) {
        for (EmulatorListener listener : listeners) {
            listener.notifyResized(columns, rows);
        }
    }

    // ======================
    // 
    // ======================

    protected Object haltMutex = new Object();

    protected Object terminateMutex = new Object();

    protected boolean signalHalt = false;

    protected boolean signalTerminate = false;

    public void signalHalt(boolean val) {
        this.signalHalt = val;

        synchronized (haltMutex) {
            if (val == false)
                haltMutex.notifyAll();
        }
    }

    public void step() {
        // when halted, a notify will execute one step in the terminal 
        synchronized (haltMutex) {
            haltMutex.notifyAll();
        }
    }

    public void signalTerminate() {
        this.signalTerminate = true;

        synchronized (terminateMutex) {
            terminateMutex.notifyAll();
        }
    }

    // ===
    // Abstract Interface 
    // ===

    /** Start emulator, read form inputstream and handle Emulator events */
    public abstract void start();

    /** returns bytes which must be send for the specified (AWT) keycode */
    public abstract byte[] getKeyCode(String keystr);

    public abstract Object[][] getTokenTable();

    /** Set/send new size */
    public abstract void sendSize(int nr_columns, int nr_rows);

}
