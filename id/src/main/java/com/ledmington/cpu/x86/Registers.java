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

public final class Registers {

    /**
     * Performs a bitwise OR with the given byte and a byte with the given value in the third bit. xxxxxxxx OR 0000b000
     */
    public static byte combine(final boolean b, final byte x) {
        return BitUtils.asByte(x | (b ? ((byte) 0b00001000) : 0));
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
