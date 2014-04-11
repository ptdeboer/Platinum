package nl.esciencecenter.ptk.io;

import java.io.IOException;

public interface Writable
{
    public void write(byte buffer[], int bufferOffset, int numBytes) throws IOException;
}