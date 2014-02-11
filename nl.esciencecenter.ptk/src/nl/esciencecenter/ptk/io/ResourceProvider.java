package nl.esciencecenter.ptk.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public interface ResourceProvider
{
    public URI resolvePathURI(String path) throws URISyntaxException;

    public OutputStream createOutputStream(URI uri) throws IOException;

    public InputStream createInputStream(URI uri) throws IOException;

    public RandomReader createRandomReader(URI uri) throws IOException;

    public RandomWriter createRandomWriter(URI uri) throws IOException;

}
