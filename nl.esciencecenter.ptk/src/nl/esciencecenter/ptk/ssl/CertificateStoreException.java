package nl.esciencecenter.ptk.ssl;

import java.io.IOException;

public class CertificateStoreException extends IOException 
{
    private static final long serialVersionUID = 1L;

    public CertificateStoreException(String message)
    {
        super(message); 
    }
    
    public CertificateStoreException(String message, Throwable cause)
    {
        super(message,cause); 
    }
    
}
