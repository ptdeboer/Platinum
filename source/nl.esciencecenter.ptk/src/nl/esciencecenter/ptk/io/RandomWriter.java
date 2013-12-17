package nl.esciencecenter.ptk.io;

import java.io.IOException;

public interface RandomWriter
{
    public void writeBytes(long fileOffset, byte buffer[], int bufferOffset, int nrBytes) throws IOException;

    public void close() throws IOException;
}
