package com.ledmington.cpu.x86;

/**
 * XMM registers.
 */
public final class RegisterXMM extends Register {

    public static final RegisterXMM XMM0 = new RegisterXMM("xmm0");
    public static final RegisterXMM XMM1 = new RegisterXMM("xmm1");
    public static final RegisterXMM XMM2 = new RegisterXMM("xmm2");
    public static final RegisterXMM XMM3 = new RegisterXMM("xmm3");
    public static final RegisterXMM XMM4 = new RegisterXMM("xmm4");
    public static final RegisterXMM XMM5 = new RegisterXMM("xmm5");
    public static final RegisterXMM XMM6 = new RegisterXMM("xmm6");
    public static final RegisterXMM XMM7 = new RegisterXMM("xmm7");

    private RegisterXMM(final String mnemonic) {
        super(mnemonic);
    }

    public static RegisterXMM fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> XMM0;
            case 0x01 -> XMM1;
            case 0x02 -> XMM2;
            case 0x03 -> XMM3;
            case 0x04 -> XMM4;
            case 0x05 -> XMM5;
            case 0x06 -> XMM6;
            case 0x07 -> XMM7;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    public int bits() {
        return 128;
    }
}
