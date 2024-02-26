package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public interface Register extends Operand {

    int bits();

    /**
     * Performs a bitwise OR with the given byte and a byte
     * with the given value in the third bit.
     * xxxxxxxx OR
     * 0000b000
     */
    public static byte combine(final boolean b, final byte x) {
        return BitUtils.asByte(x | (b ? ((byte) 0x08) : 0));
    }

    public static Register fromCode(final byte operandCode, final boolean isOperand64Bit, final boolean extension) {
        return isOperand64Bit
                ? Register64.fromByte(combine(extension, operandCode))
                : Register32.fromByte(combine(extension, operandCode));
    }
}
