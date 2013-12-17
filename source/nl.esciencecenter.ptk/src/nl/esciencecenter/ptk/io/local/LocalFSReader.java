package nl.esciencecenter.ptk.io.local;

import java.io.IOException;
import java.io.RandomAccessFile;

import nl.esciencecenter.ptk.io.RandomReader;

public class LocalFSReader implements RandomReader
{
    protected LocalFSNode fsNode; 
    
    public LocalFSReader(LocalFSNode node) throws IOException
    {
        this.fsNode=node;
    }

    public int readBytes(long fileOffset, byte[] buffer, int bufferOffset, int nrBytes) throws IOException
    {
        RandomAccessFile afile = null;
        
        try
        {
            afile = new RandomAccessFile(fsNode.toJavaFile(), "r");
            afile.seek(fileOffset);
            int nrRead = afile.read(buffer, bufferOffset, nrBytes);
         
            return nrRead;
        }
        catch (IOException e)
        {
            throw new IOException("Could open location for reading:" + this,e);
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
    public long getLength() throws IOException
    {
        return fsNode.getFileSize(); 
    }

}
