package com.ledmington.cpu.x86;

/**
 * An x86 64-bit general-purpose register.
 */
public final class Register64 extends Register {

    /**
     * The register RAX.
     */
    public static final Register64 RAX = new Register64("rax");

    /**
     * The register RBX.
     */
    public static final Register64 RBX = new Register64("rbx");

    /**
     * The register RCX.
     */
    public static final Register64 RCX = new Register64("rcx");

    /**
     * The register RDX.
     */
    public static final Register64 RDX = new Register64("rdx");

    /**
     * The register RSI.
     */
    public static final Register64 RSI = new Register64("rsi");

    /**
     * The register RDI.
     */
    public static final Register64 RDI = new Register64("rdi");

    /**
     * The register RSP.
     */
    public static final Register64 RSP = new Register64("rsp");

    /**
     * The register RBP.
     */
    public static final Register64 RBP = new Register64("rbp");

    /**
     * The register R8.
     */
    public static final Register64 R8 = new Register64("r8");

    /**
     * The register R9.
     */
    public static final Register64 R9 = new Register64("r9");

    /**
     * The register R10.
     */
    public static final Register64 R10 = new Register64("r10");

    /**
     * The register R11.
     */
    public static final Register64 R11 = new Register64("r11");

    /**
     * The register R12.
     */
    public static final Register64 R12 = new Register64("r12");

    /**
     * The register R13.
     */
    public static final Register64 R13 = new Register64("r13");

    /**
     * The register R14.
     */
    public static final Register64 R14 = new Register64("r14");

    /**
     * The register R15.
     */
    public static final Register64 R15 = new Register64("r15");

    /**
     * The instruction pointer register RIP.
     */
    public static final Register64 RIP = new Register64("rip");

    /**
     * The zero register RIZ.
     */
    public static final Register64 RIZ = new Register64("riz");

    private Register64(final String mnemonic) {
        super(mnemonic);
    }

    public static Register64 fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> RAX;
            case 0x01 -> RCX;
            case 0x02 -> RDX;
            case 0x03 -> RBX;
            case 0x04 -> RSP;
            case 0x05 -> RBP;
            case 0x06 -> RSI;
            case 0x07 -> RDI;
            case 0x08 -> R8;
            case 0x09 -> R9;
            case 0x0a -> R10;
            case 0x0b -> R11;
            case 0x0c -> R12;
            case 0x0d -> R13;
            case 0x0e -> R14;
            case 0x0f -> R15;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    public int bits() {
        return 64;
    }
}
