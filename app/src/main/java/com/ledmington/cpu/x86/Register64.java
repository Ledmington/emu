package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Register64 implements Register {
    RAX("rax"),
    RBX("rbx"),
    RCX("rcx"),
    RDX("rdx"),
    RSI("rsi"),
    RDI("rdi"),
    RSP("rsp"),
    RBP("rbp"),
    R8("r8"),
    R9("r9"),
    R10("r10"),
    R11("r11"),
    R12("r12"),
    R13("r13"),
    R14("r14"),
    R15("r15");

    private final String mnemonic;

    Register64(final String mnemonic) {
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
    public String getName() {
        return mnemonic;
    }
}
