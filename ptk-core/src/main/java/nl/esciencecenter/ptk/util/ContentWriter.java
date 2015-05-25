package nl.esciencecenter.ptk.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import nl.esciencecenter.ptk.io.Writable;

/**
 * Content writer util class which can automatically close the OutputStream written to.
 */
public class ContentWriter implements Writable {

    private boolean autoClose;

    private OutputStream outputStream;

    private String charEncoding;

    private IOException closeException;

    public ContentWriter(OutputStream outps) {
        this.outputStream = outps;
        this.charEncoding = ResourceLoader.CHARSET_UTF8;
        this.autoClose = false;
    }

    public ContentWriter(OutputStream outps, boolean autoClose) {
        this.outputStream = outps;
        this.charEncoding = ResourceLoader.CHARSET_UTF8;
        this.autoClose = autoClose;
    }

    public ContentWriter(OutputStream outps, String charEncoding) {
        this.outputStream = outps;
        this.charEncoding = charEncoding;
        this.autoClose = false;
    }

    public ContentWriter(OutputStream outps, String charEncoding, boolean autoClose) {
        this.outputStream = outps;
        this.charEncoding = charEncoding;
        this.autoClose = autoClose;
    }

    public void write(byte[] bytes) throws IOException {
        try {
            outputStream.write(bytes);
            outputStream.flush();
        } finally {
            if (autoClose) {
                autoClose();
            }
        }
    }

    public void write(byte[] bytes, int bufferOffset, int numBytes) throws IOException {
        try {
            outputStream.write(bytes, bufferOffset, numBytes);
            outputStream.flush();
        } finally {
            if (autoClose) {
                autoClose();
            }
        }
    }

    public void write(String text) throws IOException {
        try {
            outputStream.write(text.getBytes(charEncoding));
            outputStream.flush();
        } finally {
            if (autoClose) {
                autoClose();
            }
        }
    }

    public void saveProperties(Properties properties, String comments) throws IOException {
        try {
            properties.store(outputStream, comments);
            outputStream.flush();
        } finally {
            if (autoClose) {
                autoClose();
            }
        }
    }

    public void autoClose() {
        try {
            this.outputStream.close();
        } catch (IOException e) {
            this.closeException = e;
        }
    }

    public Exception getCloseException() {
        return this.closeException;
    }
}
