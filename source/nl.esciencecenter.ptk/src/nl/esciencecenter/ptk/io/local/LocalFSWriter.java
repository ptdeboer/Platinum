package nl.esciencecenter.ptk.io.local;

import java.io.IOException;
import java.io.RandomAccessFile;

import nl.esciencecenter.ptk.io.RandomWriter;

public class LocalFSWriter implements RandomWriter
{

    private LocalFSNode fsNode;

    public LocalFSWriter(LocalFSNode node)
    {
        fsNode=node;
    }

    @Override
    public void writeBytes(long fileOffset, byte[] buffer, int bufferOffset,  int nrBytes) throws IOException
    {
        RandomAccessFile afile = null;

        try
        {
            afile = new RandomAccessFile(fsNode.toJavaFile(), "rw");
            afile.seek(fileOffset);
            afile.write(buffer, bufferOffset, nrBytes);
            afile.close(); // MUST CLOSE !
            // if (truncate)
            // afile.setLength(fileOffset+nrBytes);
            return;// if failed, some exception occured !
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (afile!=null)
            {
                try
                {
                    // Must close between Reads! (not fast but ensures consistency between reads). 
                    afile.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    @Override
    public void close() throws IOException
    {
        
    }
    

}
