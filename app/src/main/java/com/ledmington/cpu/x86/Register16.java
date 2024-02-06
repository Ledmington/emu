package com.ledmington.cpu.x86;

import java.util.Objects;

public final class Register16 implements Register {

    public static final Register16 AX = new Register16("ax");
    public static final Register16 BX = new Register16("bx");
    public static final Register16 CX = new Register16("cx");
    public static final Register16 DX = new Register16("dx");
    public static final Register16 SI = new Register16("si");
    public static final Register16 DI = new Register16("di");
    public static final Register16 SP = new Register16("sp");
    public static final Register16 BP = new Register16("bp");

    private final String mnemonic;

    private Register16(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
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
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 16;
    }

    @Override
    public String toIntelSyntax() {
        return mnemonic;
    }
}
