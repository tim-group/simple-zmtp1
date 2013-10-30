package com.timgroup.zmtp;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class FrameOutputStreamTest {

    private static final int FINAL = 0;

    @Test
    public void writesBytesAsAFinalFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).write(new byte[] {10, 20, 30, 40});

        assertArrayEquals(shortFrame(5, FINAL, new byte[] {10, 20, 30, 40}), buf.toByteArray());
    }

    @Test
    public void writesASingleByteAsAFinalFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).write(99);

        assertArrayEquals(shortFrame(2, FINAL, new byte[] {99}), buf.toByteArray());
    }

    @Test
    public void writesBytesAsAFinalLongFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] body = new byte[254];
        Arrays.fill(body, (byte) 99);

        new FrameOutputStream(buf).write(body);

        assertArrayEquals(longFrame((byte) 0xff, 0, 0, 0, 0, 0, 0, 0, (byte) 255, FINAL, body), buf.toByteArray());
    }

    @Test
    public void writesLotsOfBytesAsAFinalLongFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] body = new byte[9000];
        Arrays.fill(body, (byte) 99);

        new FrameOutputStream(buf).write(body);

        assertArrayEquals(longFrame((byte) 0xff, 0, 0, 0, 0, 0, 0, (byte) (9001 / 256), (byte) (9001 % 256), FINAL, body), buf.toByteArray());
    }

    private byte[] shortFrame(int length, int flags, byte[] body) {
        byte[] header = new byte[] {(byte) length, (byte) flags};
        return concatenate(header, body);
    }

    private byte[] longFrame(int lengthFlag, int l0, int l1, int l2, int l3, int l4, int l5, int l6, int l7, int flags, byte[] body) {
        byte[] header = new byte[] {(byte) lengthFlag, (byte) l0, (byte) l1, (byte) l2, (byte) l3, (byte) l4, (byte) l5, (byte) l6, (byte) l7, (byte) flags};
        return concatenate(header, body);
    }

    private byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

}
