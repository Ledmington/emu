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

/** An x86 128-bit general-purpose register. */
public final class RegisterXMM extends Register {

    /** The register XMM0. */
    public static final RegisterXMM XMM0 = new RegisterXMM("xmm0");

    /** The register XMM1. */
    public static final RegisterXMM XMM1 = new RegisterXMM("xmm1");

    /** The register XMM2. */
    public static final RegisterXMM XMM2 = new RegisterXMM("xmm2");

    /** The register XMM3. */
    public static final RegisterXMM XMM3 = new RegisterXMM("xmm3");

    /** The register XMM4. */
    public static final RegisterXMM XMM4 = new RegisterXMM("xmm4");

    /** The register XMM5. */
    public static final RegisterXMM XMM5 = new RegisterXMM("xmm5");

    /** The register XMM6. */
    public static final RegisterXMM XMM6 = new RegisterXMM("xmm6");

    /** The register XMM7. */
    public static final RegisterXMM XMM7 = new RegisterXMM("xmm7");

    /** The register XMM8. */
    public static final RegisterXMM XMM8 = new RegisterXMM("xmm8");

    /** The register XMM9. */
    public static final RegisterXMM XMM9 = new RegisterXMM("xmm9");

    /** The register XMM10. */
    public static final RegisterXMM XMM10 = new RegisterXMM("xmm10");

    /** The register XMM11. */
    public static final RegisterXMM XMM11 = new RegisterXMM("xmm11");

    /** The register XMM12. */
    public static final RegisterXMM XMM12 = new RegisterXMM("xmm12");

    /** The register XMM13. */
    public static final RegisterXMM XMM13 = new RegisterXMM("xmm13");

    /** The register XMM14. */
    public static final RegisterXMM XMM14 = new RegisterXMM("xmm14");

    /** The register XMM15. */
    public static final RegisterXMM XMM15 = new RegisterXMM("xmm15");

    private RegisterXMM(final String mnemonic) {
        super(mnemonic);
    }

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
}
