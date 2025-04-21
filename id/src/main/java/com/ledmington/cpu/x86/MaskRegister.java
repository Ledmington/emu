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

import java.util.Objects;

public enum MaskRegister implements Register {

	/** The mask register k0. */
	K0("k0"),

	/** The mask register k1. */
	K1("k1"),

	/** The mask register k2. */
	K2("k2"),

	/** The mask register k3. */
	K3("k3"),

	/** The mask register k4. */
	K4("k4"),

	/** The mask register k5. */
	K5("k5"),

	/** The mask register k6. */
	K6("k6"),

	/** The mask register k7. */
	K7("k7");

	private final String mnemonic;

	MaskRegister(final String mnemonic) {
		this.mnemonic = Objects.requireNonNull(mnemonic);
	}

	@Override
	public int bits() {
		throw new UnsupportedOperationException("This is a mask register.");
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "MaskRegister(mnemonic=" + mnemonic + ")";
	}

	public static MaskRegister fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> K0;
			case 0x01 -> K1;
			case 0x02 -> K2;
			case 0x03 -> K3;
			case 0x04 -> K4;
			case 0x05 -> K5;
			case 0x06 -> K6;
			case 0x07 -> K7;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	public static byte toByte(final MaskRegister r) {
		return switch (r) {
			case K0 -> (byte) 0x00;
			case K1 -> (byte) 0x01;
			case K2 -> (byte) 0x02;
			case K3 -> (byte) 0x03;
			case K4 -> (byte) 0x04;
			case K5 -> (byte) 0x05;
			case K6 -> (byte) 0x06;
			case K7 -> (byte) 0x07;
		};
	}
}
