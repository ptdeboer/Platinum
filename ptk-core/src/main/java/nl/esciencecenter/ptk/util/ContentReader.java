package nl.esciencecenter.ptk.util;

import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.io.Readable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Content reader util class which can automatically close the OutputStream written to.
 */
public class ContentReader implements Readable {

    private final boolean autoClose;
    private final InputStream inputStream;
    private final String charEncoding;


    public ContentReader(InputStream inps) {
        this.inputStream = inps;
        this.charEncoding = ResourceLoader.CHARSET_UTF8;
        this.autoClose = false;
    }

    public ContentReader(InputStream inps, boolean autoClose) {
        this.inputStream = inps;
        this.charEncoding = ResourceLoader.CHARSET_UTF8;
        this.autoClose = autoClose;
    }

    public ContentReader(InputStream inps, String charEncoding) {
        this.inputStream = inps;
        this.charEncoding = charEncoding;
        this.autoClose = false;
    }

    public ContentReader(InputStream inps, String charEncoding, boolean autoClose) {
        this.inputStream = inps;
        this.charEncoding = charEncoding;
        this.autoClose = autoClose;
    }

    public byte[] readBytes() throws IOException {
        return IOUtil.readAll(inputStream, autoClose);
    }

    public String readString() throws IOException {
        return new String(IOUtil.readAll(inputStream, autoClose), charEncoding);
    }

    @Override
    public int read(byte[] buffer, int bufferOffset, int numBytes) throws IOException {
        return this.inputStream.read(buffer, bufferOffset, numBytes);
    }

    public Properties loadProperties() throws IOException {
        try {
            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } finally {
            if (autoClose) {
                IOUtil.autoClose(inputStream);
            }
        }
    }

}
