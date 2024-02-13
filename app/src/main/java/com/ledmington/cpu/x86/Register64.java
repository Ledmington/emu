package com.ledmington.cpu.x86;

import java.util.Objects;

public final class Register64 implements Register {

    public static final Register64 RAX = new Register64("rax");
    public static final Register64 RBX = new Register64("rbx");
    public static final Register64 RCX = new Register64("rcx");
    public static final Register64 RDX = new Register64("rdx");
    public static final Register64 RSI = new Register64("rsi");
    public static final Register64 RDI = new Register64("rdi");
    public static final Register64 RSP = new Register64("rsp");
    public static final Register64 RBP = new Register64("rbp");
    public static final Register64 R8 = new Register64("r8");
    public static final Register64 R9 = new Register64("r9");
    public static final Register64 R10 = new Register64("r10");
    public static final Register64 R11 = new Register64("r11");
    public static final Register64 R12 = new Register64("r12");
    public static final Register64 R13 = new Register64("r13");
    public static final Register64 R14 = new Register64("r14");
    public static final Register64 R15 = new Register64("r15");

    private final String mnemonic;

    private Register64(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
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

    @Override
    public int bits() {
        return 64;
    }

    @Override
    public String toIntelSyntax() {
        return mnemonic;
    }

    public String toString() {
        return mnemonic;
    }
}
