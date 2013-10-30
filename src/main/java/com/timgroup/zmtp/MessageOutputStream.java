package com.timgroup.zmtp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class MessageOutputStream extends FilterOutputStream {

    public static final byte[] EMPTY_FRAME = {};

    public MessageOutputStream(FrameOutputStream out) throws IOException {
        super(out);
        out().write(EMPTY_FRAME);
    }

    public MessageOutputStream(FrameOutputStream out, String identity) throws IOException {
        super(out);
        out().write(identity.getBytes(Charsets.US_ASCII));
    }

    private FrameOutputStream out() {
        return (FrameOutputStream) this.out;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte) b});
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out().write(b, off, len);
    }

    public void write(Iterable<byte[]> frames) throws IOException {
        Iterator<byte[]> it = frames.iterator();
        while (it.hasNext()) {
            byte[] frame = it.next();
            out().writeFrame(frame, 0, frame.length, it.hasNext());
        }
    }

}
