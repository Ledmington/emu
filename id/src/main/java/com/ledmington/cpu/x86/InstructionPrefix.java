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

public enum InstructionPrefix {
    LOCK((byte) 0xf0),

    /** REPNE / REPNZ */
    REPNZ((byte) 0xf2),

    /** REP / REPE / REPZ */
    REP((byte) 0xf3);

    public final byte code;

    InstructionPrefix(final byte code) {
        this.code = code;
    }

    public static InstructionPrefix fromByte(final byte x) {
        return switch (x) {
            case (byte) 0xf0 -> LOCK;
            case (byte) 0xf2 -> REPNZ;
            case (byte) 0xf3 -> REP;
            default -> throw new IllegalArgumentException();
        };
    }
}
