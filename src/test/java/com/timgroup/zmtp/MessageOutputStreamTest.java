package com.timgroup.zmtp;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

import static com.timgroup.zmtp.FrameTestUtils.FINAL;
import static com.timgroup.zmtp.FrameTestUtils.MORE;
import static com.timgroup.zmtp.FrameTestUtils.concatenate;
import static com.timgroup.zmtp.FrameTestUtils.shortFrame;

public class MessageOutputStreamTest {

    @Test
    public void writesAnonymousGreeting() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        FrameOutputStream fout = new FrameOutputStream(buf);

        new MessageOutputStream(fout);

        assertArrayEquals(anonymousGreeting(), buf.toByteArray());
    }

    @Test
    public void writesIdentifiedGreeting() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        FrameOutputStream fout = new FrameOutputStream(buf);

        new MessageOutputStream(fout, "me");

        assertArrayEquals(shortFrame(3, FINAL, new byte[] {'m', 'e'}), buf.toByteArray());
    }

    @Test
    public void writesASliceOfBytesAsASingleFrameMessage() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        FrameOutputStream fout = new FrameOutputStream(buf);

        new MessageOutputStream(fout).write(new byte[] {99, 10, 20, 30, 40, 99}, 1, 4);

        assertArrayEquals(concatenate(anonymousGreeting(), shortFrame(5, FINAL, new byte[] {10, 20, 30, 40})), buf.toByteArray());
    }

    @Test
    public void writesASingleByteAsASingleFrameMessage() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        FrameOutputStream fout = new FrameOutputStream(buf);

        new MessageOutputStream(fout).write(99);

        assertArrayEquals(concatenate(anonymousGreeting(), shortFrame(2, FINAL, new byte[] {99})), buf.toByteArray());
    }

    @Test
    public void writesAMessageMadeOfSeveralFrames() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        FrameOutputStream fout = new FrameOutputStream(buf);

        new MessageOutputStream(fout).write(Arrays.asList(new byte[] {10}, new byte[] {20}, new byte[] {30}));

        assertArrayEquals(concatenate(anonymousGreeting(),
                                      shortFrame(2, MORE, new byte[] {10}),
                                      shortFrame(2, MORE, new byte[] {20}),
                                      shortFrame(2, FINAL, new byte[] {30})),
                          buf.toByteArray());
    }

    private byte[] anonymousGreeting() {
        return shortFrame(1, FINAL, new byte[] {});
    }

}
