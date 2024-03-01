package com.ledmington.cpu.x86;

public final class Register8 extends Register {

    public static final Register8 AL = new Register8("al");
    public static final Register8 BL = new Register8("bl");
    public static final Register8 CL = new Register8("cl");
    public static final Register8 DL = new Register8("dl");
    public static final Register8 AH = new Register8("ah");
    public static final Register8 BH = new Register8("bh");
    public static final Register8 CH = new Register8("ch");
    public static final Register8 DH = new Register8("dh");

    public Register8(final String mnemonic) {
        super(mnemonic);
    }

    public static Register8 fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> AL;
            case 0x01 -> CL;
            case 0x02 -> DL;
            case 0x03 -> BL;
            case 0x04 -> AH;
            case 0x05 -> CH;
            case 0x06 -> DH;
            case 0x07 -> BH;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    public int bits() {
        return 8;
    }
}
