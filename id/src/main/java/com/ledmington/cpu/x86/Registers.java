package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class Registers {

    /**
     * Performs a bitwise OR with the given byte and a byte
     * with the given value in the third bit.
     * xxxxxxxx OR
     * 0000b000
     */
    public static byte combine(final boolean b, final byte x) {
        return BitUtils.asByte(x | (b ? ((byte) 0x08) : 0));
    }

    public static Register fromCode(
            final byte operandCode,
            final boolean isOperand64Bit,
            final boolean extension,
            final boolean hasOperandSizeOverridePrefix) {
        return hasOperandSizeOverridePrefix
                ? Register16.fromByte(combine(extension, operandCode))
                : isOperand64Bit
                        ? Register64.fromByte(combine(extension, operandCode))
                        : Register32.fromByte(combine(extension, operandCode));
    }

    private Registers() {}
}
