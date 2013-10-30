package com.timgroup.zmtp;

public class FrameTestUtils {

    public static final int FINAL = 0;
    public static final int MORE = 1;

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] longFrame(int lengthFlag, int l0, int l1, int l2, int l3, int l4, int l5, int l6, int l7, int flags, byte[] body) {
        byte[] header = new byte[] {(byte) lengthFlag, (byte) l0, (byte) l1, (byte) l2, (byte) l3, (byte) l4, (byte) l5, (byte) l6, (byte) l7, (byte) flags};
        return concatenate(header, body);
    }

    public static byte[] shortFrame(int length, int flags, byte[] body) {
        byte[] header = new byte[] {(byte) length, (byte) flags};
        return concatenate(header, body);
    }

}
