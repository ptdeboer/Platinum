package nl.esciencecenter.ptk.io;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Wrapper for nested URI Syntax Exceptions on File Locations.
 */
public class FileURISyntaxException extends IOException
{
    private static final long serialVersionUID = -5950527403084197333L;

    protected String fileLocation = null;

    public FileURISyntaxException(String message, String location)
    {
        super(message);
        this.fileLocation = location;
    }

    public FileURISyntaxException(String message, String location, URISyntaxException cause)
    {
        super(message, cause);
        this.fileLocation = location;
    }

    public String getFileLocation()
    {
        return fileLocation;
    }

    public String getInput()
    {
        Throwable orgCause = this.getCause();

        if (this.getCause() instanceof URISyntaxException)
        {
            return ((URISyntaxException) orgCause).getInput();
        }
        return null;
    }

}
