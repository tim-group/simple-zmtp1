package com.timgroup.zmtp;

import com.timgroup.io.ByteArrayUtils;

public class FrameTestUtils {

    public static final int FINAL = 0;
    public static final int MORE = 1;

    public static byte[] longFrame(int lengthFlag, int l0, int l1, int l2, int l3, int l4, int l5, int l6, int l7, int flags, byte[] body) {
        byte[] header = new byte[] {(byte) lengthFlag, (byte) l0, (byte) l1, (byte) l2, (byte) l3, (byte) l4, (byte) l5, (byte) l6, (byte) l7, (byte) flags};
        return ByteArrayUtils.concatenate(header, body);
    }

    public static byte[] shortFrame(int length, int flags, byte[] body) {
        byte[] header = new byte[] {(byte) length, (byte) flags};
        return ByteArrayUtils.concatenate(header, body);
    }

}
