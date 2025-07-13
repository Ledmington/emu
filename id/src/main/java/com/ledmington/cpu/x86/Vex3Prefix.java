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

import com.ledmington.utils.BitUtils;

/** The VEX3 prefix (0xc4 + 2 bytes). */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.AvoidFieldNameMatchingMethodName"})
public final class Vex3Prefix {

	private final boolean r;
	private final boolean x;
	private final boolean b;
	private final byte m;
	private final boolean w;
	private final byte v;
	private final boolean l;
	private final byte p;

	public static boolean isVEX3Prefix(final byte vexByte) {
		return vexByte == (byte) 0xc4;
	}

	public static Vex3Prefix of(final byte firstByte, final byte secondByte) {
		final boolean r = BitUtils.and(firstByte, (byte) 0b10000000) != (byte) 0;
		final boolean x = BitUtils.and(firstByte, (byte) 0b01000000) != (byte) 0;
		final boolean b = BitUtils.and(firstByte, (byte) 0b00100000) != (byte) 0;
		final byte m = BitUtils.and(firstByte, (byte) 0b00011111);
		final boolean w = BitUtils.and(secondByte, (byte) 0b10000000) != (byte) 0;
		final byte v = BitUtils.shr(BitUtils.and(secondByte, (byte) 0b01111000), 3);
		final boolean l = BitUtils.and(secondByte, (byte) 0b00000100) != (byte) 0;
		final byte p = BitUtils.and(secondByte, (byte) 0b00000011);
		return new Vex3Prefix(r, x, b, m, w, v, l, p);
	}

	private Vex3Prefix(
			final boolean r,
			final boolean x,
			final boolean b,
			final byte m,
			final boolean w,
			final byte v,
			final boolean l,
			final byte p) {
		if (BitUtils.and(m, (byte) 0b11100000) != 0) {
			throw new IllegalArgumentException(String.format("Invalid m field in VEX3 prefix: 0x%02x.", m));
		}
		if (BitUtils.and(v, (byte) 0b11110000) != 0) {
			throw new IllegalArgumentException(String.format("Invalid v field in VEX3 prefix: 0x%02x.", v));
		}
		if (BitUtils.and(p, (byte) 0b11111100) != 0) {
			throw new IllegalArgumentException(String.format("Invalid p field in VEX3 prefix: 0x%02x.", p));
		}
		this.r = r;
		this.x = x;
		this.b = b;
		this.m = m;
		this.w = w;
		this.v = v;
		this.l = l;
		this.p = p;
	}

	public boolean r() {
		return r;
	}

	public boolean x() {
		return x;
	}

	public boolean b() {
		return b;
	}

	public boolean w() {
		return w;
	}

	public byte v() {
		return v;
	}
}
