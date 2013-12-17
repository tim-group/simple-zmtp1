package com.timgroup.io;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FullyBufferedOutputStream extends FilterOutputStream {

    private final OutputStream realOut;

    public FullyBufferedOutputStream(OutputStream realOut) {
        super(new ByteArrayOutputStream());
        this.realOut = realOut;
    }

    @Override
    public void flush() throws IOException {
        ByteArrayOutputStream buf = (ByteArrayOutputStream) out;
        try {
            buf.writeTo(realOut);
            realOut.flush();
        } finally {
            buf.reset();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        realOut.close();
    }
}
