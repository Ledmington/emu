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

/** An x86 128-bit general-purpose register. */
public enum RegisterXMM implements Register {

    /** The register XMM0. */
    XMM0("xmm0"),

    /** The register XMM1. */
    XMM1("xmm1"),

    /** The register XMM2. */
    XMM2("xmm2"),

    /** The register XMM3. */
    XMM3("xmm3"),

    /** The register XMM4. */
    XMM4("xmm4"),

    /** The register XMM5. */
    XMM5("xmm5"),

    /** The register XMM6. */
    XMM6("xmm6"),

    /** The register XMM7. */
    XMM7("xmm7"),

    /** The register XMM8. */
    XMM8("xmm8"),

    /** The register XMM9. */
    XMM9("xmm9"),

    /** The register XMM10. */
    XMM10("xmm10"),

    /** The register XMM11. */
    XMM11("xmm11"),

    /** The register XMM12. */
    XMM12("xmm12"),

    /** The register XMM13. */
    XMM13("xmm13"),

    /** The register XMM14. */
    XMM14("xmm14"),

    /** The register XMM15. */
    XMM15("xmm15");

    private final String mnemonic;

    RegisterXMM(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    /**
     * Returns the 128-bit XMM register corresponding to the given byte.
     *
     * @param b The byte representing a 128-bit XMM register.
     * @return A 128-bit XMM register.
     */
    public static RegisterXMM fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> XMM0;
            case 0x01 -> XMM1;
            case 0x02 -> XMM2;
            case 0x03 -> XMM3;
            case 0x04 -> XMM4;
            case 0x05 -> XMM5;
            case 0x06 -> XMM6;
            case 0x07 -> XMM7;
            case 0x08 -> XMM8;
            case 0x09 -> XMM9;
            case 0x0a -> XMM10;
            case 0x0b -> XMM11;
            case 0x0c -> XMM12;
            case 0x0d -> XMM13;
            case 0x0e -> XMM14;
            case 0x0f -> XMM15;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 128;
    }

    @Override
    public String toIntelSyntax() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return "RegisterXMM(mnemonic=" + mnemonic + ")";
    }
}
