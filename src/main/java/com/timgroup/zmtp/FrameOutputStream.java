package com.timgroup.zmtp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FrameOutputStream extends FilterOutputStream {

    private static final int FLAG_MORE = 1;

    public FrameOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        long frameLen = len + 1L;
        writeStretchy(frameLen);
        out.write(flags(false));
        out.write(b, off, len);
    }

    private void writeStretchy(long frameLen) throws IOException {
        if (frameLen < 0xff) {
            out.write((int) frameLen);
        } else {
            out.write(0xff);
            writeLong(frameLen);
        }
    }

    private void writeLong(long x) throws IOException {
        out.write((byte) (x >>> 56));
        out.write((byte) (x >>> 48));
        out.write((byte) (x >>> 40));
        out.write((byte) (x >>> 32));
        out.write((byte) (x >>> 24));
        out.write((byte) (x >>> 16));
        out.write((byte) (x >>> 8));
        out.write((byte) (x >>> 0));
    }

    private int flags(boolean more) {
        return more ? FLAG_MORE : 0;
    }

    public void writeFinal(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException("Auto-generated method stub 30 Oct 2013");
    }

    public void writeFrame(byte[] b, int off, int len, boolean isFinal) throws IOException {
        throw new UnsupportedOperationException("Auto-generated method stub 30 Oct 2013");
    }

}
