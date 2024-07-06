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

/** A prefix for an x86 instruction which changes its behavior. */
public enum InstructionPrefix {

    /** Executes this instruction atomically. */
    LOCK((byte) 0xf0),

    /** Equivalent to REPNE. Executes the instruction until the ECX (to be checked) register is not zero. */
    REPNZ((byte) 0xf2),

    /** Equivalent to REPE and REPZ. Executes the instruction until the ECX (to be checked) register is zero. */
    REP((byte) 0xf3);

    private final byte code;

    InstructionPrefix(final byte code) {
        this.code = code;
    }

    /**
     * Returns the proper instruction prefix object corresponding to the given byte.
     *
     * @param x The byte with the prefix.
     * @return The InstructionPrefix object.
     */
    public static InstructionPrefix fromByte(final byte x) {
        return switch (x) {
            case (byte) 0xf0 -> LOCK;
            case (byte) 0xf2 -> REPNZ;
            case (byte) 0xf3 -> REP;
            default -> throw new IllegalArgumentException();
        };
    }

    /**
     * Returns the 1-byte code with this prefix.
     *
     * @return The 1-byte code with this prefix.
     */
    public byte getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "InstructionPrefix(code=" + code + ')';
    }
}
