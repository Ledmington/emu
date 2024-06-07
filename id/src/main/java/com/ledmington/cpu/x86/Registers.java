/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

/** A collection of utility static methods for parsing register codes. */
public final class Registers {

    /**
     * Performs a bitwise OR with the given byte and a byte with the given value in the third bit.
     *
     * @param b The value of the third bit.
     * @param x The byte to be ORed.
     * @return xxxxxxxx OR 0000b000.
     */
    public static byte combine(final boolean b, final byte x) {
        return BitUtils.asByte(x | (b ? ((byte) 0b00001000) : 0));
    }

    /**
     * Returns the proper general-purpose register by choosing which type/size based on the given arguments.
     *
     * @param registerCode The "base" code of the register.
     * @param isOperand64Bit If not 16 bits, selects a 64-bit register if true, a 32-bit register otherwise.
     * @param extension A single bit extension for the "base" code of the register.
     * @param hasOperandSizeOverridePrefix If true, selects a 16-bit register, otherwise the result depends on
     *     isOperand64Bit.
     * @return The selected register corresponding to the arguments passed.
     */
    public static Register fromCode(
            final byte registerCode,
            final boolean isOperand64Bit,
            final boolean extension,
            final boolean hasOperandSizeOverridePrefix) {
        return hasOperandSizeOverridePrefix
                ? Register16.fromByte(combine(extension, registerCode))
                : isOperand64Bit
                        ? Register64.fromByte(combine(extension, registerCode))
                        : Register32.fromByte(combine(extension, registerCode));
    }

    private Registers() {}
}
