package nl.piter.vterm.emulator;

import nl.piter.vterm.api.EmulatorListener;

import java.io.IOException;

/**
 * VT10X/XTerm (VTx) emulator
 */
public interface Emulator {

    void start();

    byte[] getKeyCode(String keystr);

    String getEncoding();

    void signalTerminate();

    void signalHalt(boolean b);

    boolean updateRegion(int nr_columns, int nr_rows, int region_y1, int region_y2);

    void step();

    void setTermType(String type);

    boolean sendSize(int nr_columns, int nr_rows) throws IOException;

    void send(byte[] code) throws IOException;

    void send(byte keychar) throws IOException;

    int[] getRegion();

    void addListener(EmulatorListener listener);

    void removeListener(EmulatorListener listener);

}
