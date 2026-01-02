/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

import com.ledmington.utils.BitUtils;

/** The VEX2 prefix (0xc5 + 1 byte). */
@SuppressWarnings("PMD.UnusedPrivateField")
public final class Vex2Prefix {

	private final boolean r;
	private final byte v;
	private final boolean l;
	private final byte p;

	/**
	 * Checks whether the given byte represents a VEX2 prefix.
	 *
	 * @param vexByte The byte to check.
	 * @return True if the byte is a VEX2 prefix, false otherwise.
	 */
	public static boolean isVEX2Prefix(final byte vexByte) {
		return vexByte == (byte) 0xc5;
	}

	/**
	 * Creates a {@link Vex2Prefix} instance from the given byte.
	 *
	 * @param b The byte containing the VEX2 prefix fields.
	 * @return A new {@link Vex2Prefix} instance.
	 */
	public static Vex2Prefix of(final byte b) {
		final boolean r = BitUtils.and(b, (byte) 0b10000000) != 0;
		final byte v = BitUtils.shr(BitUtils.and(b, (byte) 0b01111000), 3);
		final boolean l = BitUtils.and(b, (byte) 0b00000100) != 0;
		final byte p = BitUtils.and(b, (byte) 0b00000011);
		return new Vex2Prefix(r, v, l, p);
	}

	private Vex2Prefix(final boolean r, final byte v, final boolean l, final byte p) {
		if (BitUtils.and(v, (byte) 0b11110000) != 0) {
			throw new IllegalArgumentException(String.format("Invalid v field in VEX2 prefix: 0x%02x.", v));
		}
		if (BitUtils.and(p, (byte) 0b11111100) != 0) {
			throw new IllegalArgumentException(String.format("Invalid p field in VEX2 prefix: 0x%02x.", p));
		}

		this.r = r;
		this.v = v;
		this.l = l;
		this.p = p;
	}

	/**
	 * Returns the value of the R bit in this VEX2 prefix.
	 *
	 * @return True if the R bit is set, false otherwise.
	 */
	public boolean r() {
		return r;
	}

	/**
	 * Returns the value of the V field in this VEX2 prefix.
	 *
	 * @return The 4-bit V field.
	 */
	public byte v() {
		return v;
	}
}
