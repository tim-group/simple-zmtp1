package com.timgroup.zmtp;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

import static com.timgroup.zmtp.FrameTestUtils.FINAL;
import static com.timgroup.zmtp.FrameTestUtils.MORE;
import static com.timgroup.zmtp.FrameTestUtils.longFrame;
import static com.timgroup.zmtp.FrameTestUtils.shortFrame;

public class FrameOutputStreamTest {

    @Test
    public void writesBytesAsAFinalFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).write(new byte[] {10, 20, 30, 40});

        assertArrayEquals(shortFrame(5, FINAL, new byte[] {10, 20, 30, 40}), buf.toByteArray());
    }

    @Test
    public void writesASliceOfBytesAsAFinalFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).write(new byte[] {99, 10, 20, 30, 40, 99}, 1, 4);

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

    @Test
    public void writesBytesAsAMoreFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).writeMore(new byte[] {10, 20, 30, 40});

        assertArrayEquals(shortFrame(5, MORE, new byte[] {10, 20, 30, 40}), buf.toByteArray());
    }

    @Test
    public void writesASliceOfBytesAsAMoreFrame() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).writeMore(new byte[] {99, 10, 20, 30, 40, 99}, 1, 4);

        assertArrayEquals(shortFrame(5, MORE, new byte[] {10, 20, 30, 40}), buf.toByteArray());
    }

    @Test
    public void writesASliceOfBytesAsAFrameWithSpecifiedFinality() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).writeFrame(new byte[] {99, 10, 20, 30, 40, 99}, 1, 4, false);

        assertArrayEquals(shortFrame(5, FINAL, new byte[] {10, 20, 30, 40}), buf.toByteArray());
    }

    @Test
    public void writesASliceOfBytesAsAFrameWithSpecifiedMoreness() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        new FrameOutputStream(buf).writeFrame(new byte[] {99, 10, 20, 30, 40, 99}, 1, 4, true);

        assertArrayEquals(shortFrame(5, MORE, new byte[] {10, 20, 30, 40}), buf.toByteArray());
    }

}
