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

/** An x86 8-bit general-purpose register. */
public final class Register8 extends Register {

    /** The register AL. */
    public static final Register8 AL = new Register8("al");

    /** The register BL. */
    public static final Register8 BL = new Register8("bl");

    /** The register CL. */
    public static final Register8 CL = new Register8("cl");

    /** The register DL. */
    public static final Register8 DL = new Register8("dl");

    /** The register AH. */
    public static final Register8 AH = new Register8("ah");

    /** The register BH. */
    public static final Register8 BH = new Register8("bh");

    /** The register CH. */
    public static final Register8 CH = new Register8("ch");

    /** The register DH. */
    public static final Register8 DH = new Register8("dh");

    /** The register DIL. */
    public static final Register8 DIL = new Register8("dil");

    /** The register SIL. */
    public static final Register8 SIL = new Register8("sil");

    /** The register BPL. */
    public static final Register8 BPL = new Register8("bpl");

    /** The register SPL. */
    public static final Register8 SPL = new Register8("spl");

    /** The register R8B. */
    public static final Register8 R8B = new Register8("r8b");

    /** The register R9B. */
    public static final Register8 R9B = new Register8("r9b");

    /** The register R10B. */
    public static final Register8 R10B = new Register8("r10b");

    /** The register R11B. */
    public static final Register8 R11B = new Register8("r11b");

    /** The register R12B. */
    public static final Register8 R12B = new Register8("r12b");

    /** The register R13B. */
    public static final Register8 R13B = new Register8("r13b");

    /** The register R14B. */
    public static final Register8 R14B = new Register8("r14b");

    /** The register R15B. */
    public static final Register8 R15B = new Register8("r15b");

    private Register8(final String mnemonic) {
        super(mnemonic);
    }

    /**
     * From page 85, paragraph 3.7.2.1:
     *
     * <pre>
     * 8-bit general-purpose registers: AL, BL, CL, DL, SIL, DIL, SPL, BPL, and R8B-R15B are available using REX prefixes;
     * AL, BL, CL, DL, AH, BH, CH, DH are available without using REX prefixes.
     * </pre>
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
}
