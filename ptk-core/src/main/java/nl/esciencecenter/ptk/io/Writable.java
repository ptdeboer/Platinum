package nl.esciencecenter.ptk.io;

import java.io.IOException;

/**
 * Interface for resources which can be written to.
 */
public interface Writable {

    void write(byte[] buffer, int bufferOffset, int numBytes) throws IOException;

}