/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.util.Locale;

/** SSE registers. */
public enum RegisterMMX implements Register {

	/** The register MM0. */
	MM0,

	/** The register MM1. */
	MM1,

	/** The register MM2. */
	MM2,

	/** The register MM3. */
	MM3,

	/** The register MM4. */
	MM4,

	/** The register MM5. */
	MM5,

	/** The register MM6. */
	MM6,

	/** The register MM7. */
	MM7;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 64-bit MMX register corresponding to the given byte.
	 *
	 * @param b The byte representing a 64-bit MMX register.
	 * @return A 64-bit MMX register.
	 */
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

	public static byte toByte(final RegisterMMX r) {
		return switch (r) {
			case MM0 -> (byte) 0x00;
			case MM1 -> (byte) 0x01;
			case MM2 -> (byte) 0x02;
			case MM3 -> (byte) 0x03;
			case MM4 -> (byte) 0x04;
			case MM5 -> (byte) 0x05;
			case MM6 -> (byte) 0x06;
			case MM7 -> (byte) 0x07;
		};
	}

	@Override
	public int bits() {
		return 64;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "RegisterMMX(mnemonic=" + mnemonic + ")";
	}
}
