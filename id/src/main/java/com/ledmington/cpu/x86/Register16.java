package com.ledmington.cpu.x86;

/**
 * An x86 16-bit general-purpose register.
 */
public final class Register16 extends Register {

    /**
     * The register AX.
     */
    public static final Register16 AX = new Register16("ax");

    /**
     * The register BX.
     */
    public static final Register16 BX = new Register16("bx");

    /**
     * The register CX.
     */
    public static final Register16 CX = new Register16("cx");

    /**
     * The register DX.
     */
    public static final Register16 DX = new Register16("dx");

    /**
     * The register SI.
     */
    public static final Register16 SI = new Register16("si");

    /**
     * The register DI.
     */
    public static final Register16 DI = new Register16("di");

    /**
     * The register SP.
     */
    public static final Register16 SP = new Register16("sp");

    /**
     * The register BP.
     */
    public static final Register16 BP = new Register16("bp");

    /**
     * The register R8W.
     */
    public static final Register16 R8W = new Register16("r8w");

    /**
     * The register R9W.
     */
    public static final Register16 R9W = new Register16("r9w");

    /**
     * The register R10W.
     */
    public static final Register16 R10W = new Register16("r10w");

    /**
     * The register R11W.
     */
    public static final Register16 R11W = new Register16("r11w");

    /**
     * The register R12W.
     */
    public static final Register16 R12W = new Register16("r12w");

    /**
     * The register R13W.
     */
    public static final Register16 R13W = new Register16("r13w");

    /**
     * The register R14W.
     */
    public static final Register16 R14W = new Register16("r14w");

    /**
     * The register R15W.
     */
    public static final Register16 R15W = new Register16("r15w");

    /**
     * The segment register CS.
     */
    public static final Register16 CS = new Register16("cs");

    /**
     * The segment register DS.
     */
    public static final Register16 DS = new Register16("ds");

    /**
     * The segment register SS.
     */
    public static final Register16 SS = new Register16("ss");

    /**
     * The segment register ES.
     */
    public static final Register16 ES = new Register16("es");

    /**
     * The segment register FS.
     */
    public static final Register16 FS = new Register16("fs");

    /**
     * The segment register GS.
     */
    public static final Register16 GS = new Register16("gs");

    private Register16(final String mnemonic) {
        super(mnemonic);
    }

    public static Register16 fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> AX;
            case 0x01 -> CX;
            case 0x02 -> DX;
            case 0x03 -> BX;
            case 0x04 -> SP;
            case 0x05 -> BP;
            case 0x06 -> SI;
            case 0x07 -> DI;
            case 0x08 -> R8W;
            case 0x09 -> R9W;
            case 0x0a -> R10W;
            case 0x0b -> R11W;
            case 0x0c -> R12W;
            case 0x0d -> R13W;
            case 0x0e -> R14W;
            case 0x0f -> R15W;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 16;
    }
}
