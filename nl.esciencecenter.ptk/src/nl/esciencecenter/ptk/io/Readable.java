package nl.esciencecenter.ptk.io;

import java.io.IOException;

public interface Readable
{
    public int read(byte buffer[], int bufferOffset, int numBytes) throws IOException;
}