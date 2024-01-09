package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Register32 implements Register {
    EAX("eax"),
    EBX("ebx"),
    ECX("ecx"),
    EDX("edx"),
    ESI("esi"),
    EDI("edi"),
    ESP("esp"),
    EBP("ebp"),
    R8D("r8d"),
    R9D("r9d"),
    R10D("r10d"),
    R11D("r11d"),
    R12D("r12d"),
    R13D("r13d"),
    R14D("r14d"),
    R15D("r15d");

    private final String mnemonic;

    Register32(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public static Register32 fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> EAX;
            case 0x01 -> ECX;
            case 0x02 -> EDX;
            case 0x03 -> EBX;
            case 0x04 -> ESP;
            case 0x05 -> EBP;
            case 0x06 -> ESI;
            case 0x07 -> EDI;
            case 0x08 -> R8D;
            case 0x09 -> R9D;
            case 0x0a -> R10D;
            case 0x0b -> R11D;
            case 0x0c -> R12D;
            case 0x0d -> R13D;
            case 0x0e -> R14D;
            case 0x0f -> R15D;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 32;
    }

    @Override
    public String getName() {
        return mnemonic;
    }
}
