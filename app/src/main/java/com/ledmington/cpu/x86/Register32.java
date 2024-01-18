package com.ledmington.cpu.x86;

import java.util.Objects;

public final class Register32 implements Register {
    public static final Register32 EAX = new Register32("eax");
    public static final Register32 EBX = new Register32("ebx");
    public static final Register32 ECX = new Register32("ecx");
    public static final Register32 EDX = new Register32("edx");
    public static final Register32 ESI = new Register32("esi");
    public static final Register32 EDI = new Register32("edi");
    public static final Register32 ESP = new Register32("esp");
    public static final Register32 EBP = new Register32("ebp");
    public static final Register32 R8D = new Register32("r8d");
    public static final Register32 R9D = new Register32("r9d");
    public static final Register32 R10D = new Register32("r10d");
    public static final Register32 R11D = new Register32("r11d");
    public static final Register32 R12D = new Register32("r12d");
    public static final Register32 R13D = new Register32("r13d");
    public static final Register32 R14D = new Register32("r14d");
    public static final Register32 R15D = new Register32("r15d");

    private final String mnemonic;

    private Register32(final String mnemonic) {
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
    public String toString() {
        return mnemonic;
    }
}
