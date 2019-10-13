package nl.esciencecenter.ptk.io;

import java.io.IOException;

public interface Readable {

    int read(byte[] buffer, int bufferOffset, int numBytes) throws IOException;

}