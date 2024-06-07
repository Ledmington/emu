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

import java.util.Objects;

/** An x86 8-bit general-purpose register. */
public enum Register8 implements Register {

    /** The register AL. */
    AL("al"),

    /** The register BL. */
    BL("bl"),

    /** The register CL. */
    CL("cl"),

    /** The register DL. */
    DL("dl"),

    /** The register AH. */
    AH("ah"),

    /** The register BH. */
    BH("bh"),

    /** The register CH. */
    CH("ch"),

    /** The register DH. */
    DH("dh"),

    /** The register DIL. */
    DIL("dil"),

    /** The register SIL. */
    SIL("sil"),

    /** The register BPL. */
    BPL("bpl"),

    /** The register SPL. */
    SPL("spl"),

    /** The register R8B. */
    R8B("r8b"),

    /** The register R9B. */
    R9B("r9b"),

    /** The register R10B. */
    R10B("r10b"),

    /** The register R11B. */
    R11B("r11b"),

    /** The register R12B. */
    R12B("r12b"),

    /** The register R13B. */
    R13B("r13b"),

    /** The register R14B. */
    R14B("r14b"),

    /** The register R15B. */
    R15B("r15b");

    private final String mnemonic;

    Register8(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    /**
     * Returns the 8-bit register corresponding to the given byte.
     *
     * @param b The byte representing a 8-bit register.
     * @param hasRexPrefix Allows to select different sets of registers. If true, values in the inclusive range
     *     0x04-0x07 map to SPL, BPL, SIL and DIL respectively; otherwise, they map to AH, CH, DH, BH.
     * @return A 8-bit register.
     */
    public static Register8 fromByte(final byte b, final boolean hasRexPrefix) {
        return switch (b) {
            case 0x00 -> AL;
            case 0x01 -> CL;
            case 0x02 -> DL;
            case 0x03 -> BL;
            case 0x04 -> hasRexPrefix ? SPL : AH;
            case 0x05 -> hasRexPrefix ? BPL : CH;
            case 0x06 -> hasRexPrefix ? SIL : DH;
            case 0x07 -> hasRexPrefix ? DIL : BH;
            case 0x08 -> R8B;
            case 0x09 -> R9B;
            case 0x0a -> R10B;
            case 0x0b -> R11B;
            case 0x0c -> R12B;
            case 0x0d -> R13B;
            case 0x0e -> R14B;
            case 0x0f -> R15B;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 8;
    }

    @Override
    public String toIntelSyntax() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return "Register8(mnemonic=" + mnemonic + ')';
    }
}
