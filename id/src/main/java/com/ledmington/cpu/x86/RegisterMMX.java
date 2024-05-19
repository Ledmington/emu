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

/** SSE registers. */
public final class RegisterMMX extends Register {

    /** The register MM0. */
    public static final RegisterMMX MM0 = new RegisterMMX("mm0");

    /** The register MM1. */
    public static final RegisterMMX MM1 = new RegisterMMX("mm1");

    /** The register MM2. */
    public static final RegisterMMX MM2 = new RegisterMMX("mm2");

    /** The register MM3. */
    public static final RegisterMMX MM3 = new RegisterMMX("mm3");

    /** The register MM4. */
    public static final RegisterMMX MM4 = new RegisterMMX("mm4");

    /** The register MM5. */
    public static final RegisterMMX MM5 = new RegisterMMX("mm5");

    /** The register MM6. */
    public static final RegisterMMX MM6 = new RegisterMMX("mm6");

    /** The register MM7. */
    public static final RegisterMMX MM7 = new RegisterMMX("mm7");

    private RegisterMMX(final String mnemonic) {
        super(mnemonic);
    }

    public static RegisterMMX fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> MM0;
            case 0x01 -> MM1;
            case 0x02 -> MM2;
            case 0x03 -> MM3;
            case 0x04 -> MM4;
            case 0x05 -> MM5;
            case 0x06 -> MM6;
            case 0x07 -> MM7;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 64;
    }
}
