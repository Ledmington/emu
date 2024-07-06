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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestRegisters {

    private static Stream<Arguments> registers() {
        return Stream.of(
                // 16 bits
                Arguments.of((byte) 0x00, false, false, true, Register16.AX),
                Arguments.of((byte) 0x01, false, false, true, Register16.CX),
                Arguments.of((byte) 0x02, false, false, true, Register16.DX),
                Arguments.of((byte) 0x03, false, false, true, Register16.BX),
                Arguments.of((byte) 0x04, false, false, true, Register16.SP),
                Arguments.of((byte) 0x05, false, false, true, Register16.BP),
                Arguments.of((byte) 0x06, false, false, true, Register16.SI),
                Arguments.of((byte) 0x07, false, false, true, Register16.DI),
                Arguments.of((byte) 0x00, false, true, true, Register16.R8W),
                Arguments.of((byte) 0x01, false, true, true, Register16.R9W),
                Arguments.of((byte) 0x02, false, true, true, Register16.R10W),
                Arguments.of((byte) 0x03, false, true, true, Register16.R11W),
                Arguments.of((byte) 0x04, false, true, true, Register16.R12W),
                Arguments.of((byte) 0x05, false, true, true, Register16.R13W),
                Arguments.of((byte) 0x06, false, true, true, Register16.R14W),
                Arguments.of((byte) 0x07, false, true, true, Register16.R15W),
                // 32 bits
                Arguments.of((byte) 0x00, false, false, false, Register32.EAX),
                Arguments.of((byte) 0x01, false, false, false, Register32.ECX),
                Arguments.of((byte) 0x02, false, false, false, Register32.EDX),
                Arguments.of((byte) 0x03, false, false, false, Register32.EBX),
                Arguments.of((byte) 0x04, false, false, false, Register32.ESP),
                Arguments.of((byte) 0x05, false, false, false, Register32.EBP),
                Arguments.of((byte) 0x06, false, false, false, Register32.ESI),
                Arguments.of((byte) 0x07, false, false, false, Register32.EDI),
                Arguments.of((byte) 0x00, false, true, false, Register32.R8D),
                Arguments.of((byte) 0x01, false, true, false, Register32.R9D),
                Arguments.of((byte) 0x02, false, true, false, Register32.R10D),
                Arguments.of((byte) 0x03, false, true, false, Register32.R11D),
                Arguments.of((byte) 0x04, false, true, false, Register32.R12D),
                Arguments.of((byte) 0x05, false, true, false, Register32.R13D),
                Arguments.of((byte) 0x06, false, true, false, Register32.R14D),
                Arguments.of((byte) 0x07, false, true, false, Register32.R15D),
                // 64 bits
                Arguments.of((byte) 0x00, true, false, false, Register64.RAX),
                Arguments.of((byte) 0x01, true, false, false, Register64.RCX),
                Arguments.of((byte) 0x02, true, false, false, Register64.RDX),
                Arguments.of((byte) 0x03, true, false, false, Register64.RBX),
                Arguments.of((byte) 0x04, true, false, false, Register64.RSP),
                Arguments.of((byte) 0x05, true, false, false, Register64.RBP),
                Arguments.of((byte) 0x06, true, false, false, Register64.RSI),
                Arguments.of((byte) 0x07, true, false, false, Register64.RDI),
                Arguments.of((byte) 0x00, true, true, false, Register64.R8),
                Arguments.of((byte) 0x01, true, true, false, Register64.R9),
                Arguments.of((byte) 0x02, true, true, false, Register64.R10),
                Arguments.of((byte) 0x03, true, true, false, Register64.R11),
                Arguments.of((byte) 0x04, true, true, false, Register64.R12),
                Arguments.of((byte) 0x05, true, true, false, Register64.R13),
                Arguments.of((byte) 0x06, true, true, false, Register64.R14),
                Arguments.of((byte) 0x07, true, true, false, Register64.R15));
    }

    @ParameterizedTest
    @MethodSource("registers")
    void decodeRegisters(
            final byte registerCode,
            final boolean is64Bit,
            final boolean extension,
            final boolean hasOperandSizeOverridePrefix,
            final Register expected) {
        final Register actual = Registers.fromCode(registerCode, is64Bit, extension, hasOperandSizeOverridePrefix);
        assertEquals(
                expected,
                actual,
                () -> String.format(
                        "Decoding 0x%02x, is64Bit=%s, extension=%s, hasOperandSizeOverridePrefix=%s: expected %s but was %s",
                        registerCode, is64Bit, extension, hasOperandSizeOverridePrefix, expected, actual));
    }
}
