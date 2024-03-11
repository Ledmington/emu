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
    public static final Register8 DIL = new Register8("dil");
    public static final Register8 SIL = new Register8("sil");
    public static final Register8 BPL = new Register8("bpl");
    public static final Register8 SPL = new Register8("spl");
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

    /**
     * From page 85, paragraph 3.7.2.1:
     *
     * <pre>
     * 8-bit general-purpose registers: AL, BL, CL, DL, SIL, DIL, SPL, BPL, and R8B-R15B are available using REX prefixes;
     * AL, BL, CL, DL, AH, BH, CH, DH are available without using REX prefixes.
     * </pre>
     */
    public static Register8 fromByte(final byte b, final boolean hasRexPrefix) {
        return switch (b) {
            case 0x00 -> AL;
            case 0x01 -> CL;
            case 0x02 -> DL;
            case 0x03 -> BL;
            case 0x04 -> hasRexPrefix ? SIL : AH;
            case 0x05 -> hasRexPrefix ? SPL : CH;
            case 0x06 -> hasRexPrefix ? BPL : DH;
            case 0x07 -> hasRexPrefix ? DIL : BH;
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
