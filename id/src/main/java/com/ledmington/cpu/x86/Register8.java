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
    public static final Register8 R8B = new Register8("r8b");
    public static final Register8 R9B = new Register8("r9b");
    public static final Register8 R10B = new Register8("r10b");
    public static final Register8 R11B = new Register8("r11b");
    public static final Register8 R12B = new Register8("r12b");
    public static final Register8 R13B = new Register8("r13b");
    public static final Register8 R14B = new Register8("r14b");
    public static final Register8 R15B = new Register8("r15b");

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
            case 0x08 -> R8B;
            case 0x09 -> R9B;
            case 0x0a -> R10B;
            case 0x0b -> R11B;
            case 0x0c -> R12B;
            case 0x0d -> R13B;
            case 0x0e -> R14B;
            case 0x0f -> R15B;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    public int bits() {
        return 8;
    }
}
