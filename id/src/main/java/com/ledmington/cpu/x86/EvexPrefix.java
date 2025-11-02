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

import com.ledmington.cpu.x86.exc.DecodingException;
import com.ledmington.utils.BitUtils;

/** The extended VEX prefix (EVEX): 0x62 + 3 bytes. */
@SuppressWarnings("PMD.UnusedPrivateField")
public final class EvexPrefix {

	private final boolean r;
	private final boolean x;
	private final boolean b;
	private final boolean r1;
	private final byte m;
	private final boolean w;
	private final byte v;
	private final byte p;
	private final boolean z;
	private final boolean l1;
	private final boolean l;
	private final boolean b1;
	private final boolean v1;
	private final byte a;

	/**
	 * Determines whether the given byte represents an EVEX prefix.
	 *
	 * @param evexByte The byte to check.
	 * @return True if the byte equals 0x62, false otherwise.
	 */
	public static boolean isEvexPrefix(final byte evexByte) {
		return evexByte == (byte) 0x62;
	}

	/**
	 * Creates an {@link EvexPrefix} instance from the provided EVEX prefix bytes.
	 *
	 * @param firstByte The first byte following 0x62.
	 * @param secondByte The second byte of the EVEX prefix.
	 * @param thirdByte The third byte of the EVEX prefix.
	 * @return A new {@link EvexPrefix} instance.
	 */
	public static EvexPrefix of(final byte firstByte, final byte secondByte, final byte thirdByte) {
		final boolean r = BitUtils.and(firstByte, (byte) 0b10000000) != (byte) 0;
		final boolean x = BitUtils.and(firstByte, (byte) 0b01000000) != (byte) 0;
		final boolean b = BitUtils.and(firstByte, (byte) 0b00100000) != (byte) 0;
		final boolean r1 = BitUtils.and(firstByte, (byte) 0b00010000) != (byte) 0;
		if (BitUtils.and(firstByte, (byte) 0b00001000) != 0) {
			throw new DecodingException("Invalid first byte of EVEX prefix: bit n.3 should be 0.");
		}
		final byte m = BitUtils.and(firstByte, (byte) 0b00000111);

		final boolean w = BitUtils.and(secondByte, (byte) 0b10000000) != (byte) 0;
		final byte v = BitUtils.shr(BitUtils.and(secondByte, (byte) 0b01111000), 3);
		if (BitUtils.and(secondByte, (byte) 0b00000100) == 0) {
			throw new DecodingException("Invalid second byte of EVEX prefix: bit n.2 should be 1.");
		}
		final byte p = BitUtils.and(secondByte, (byte) 0b00000011);

		final boolean z = BitUtils.and(thirdByte, (byte) 0b10000000) != (byte) 0;
		final boolean l1 = BitUtils.and(thirdByte, (byte) 0b01000000) != (byte) 0;
		final boolean l = BitUtils.and(thirdByte, (byte) 0b00100000) != (byte) 0;
		final boolean b1 = BitUtils.and(thirdByte, (byte) 0b00010000) != (byte) 0;
		final boolean v1 = BitUtils.and(thirdByte, (byte) 0b00001000) != (byte) 0;
		final byte a = BitUtils.and(thirdByte, (byte) 0b00000111);

		return new EvexPrefix(r, x, b, r1, m, w, v, p, z, l1, l, b1, v1, a);
	}

	private EvexPrefix(
			final boolean r,
			final boolean x,
			final boolean b,
			final boolean r1,
			final byte m,
			final boolean w,
			final byte v,
			final byte p,
			final boolean z,
			final boolean l1,
			final boolean l,
			final boolean b1,
			final boolean v1,
			final byte a) {
		if (BitUtils.and(m, (byte) 0b11111000) != 0) {
			throw new IllegalArgumentException(String.format("Invalid m field in EVEX prefix: 0x%02x.", m));
		}
		if (BitUtils.and(v, (byte) 0b11110000) != 0) {
			throw new IllegalArgumentException(String.format("Invalid v field in EVEX prefix: 0x%02x.", v));
		}
		if (BitUtils.and(p, (byte) 0b11111100) != 0) {
			throw new IllegalArgumentException(String.format("Invalid p field in EVEX prefix: 0x%02x.", p));
		}
		if (BitUtils.and(a, (byte) 0b11111000) != 0) {
			throw new IllegalArgumentException(String.format("Invalid a field in EVEX prefix: 0x%02x.", a));
		}
		this.r = r;
		this.x = x;
		this.b = b;
		this.r1 = r1;
		this.m = m;
		this.w = w;
		this.v = v;
		this.p = p;
		this.z = z;
		this.l1 = l1;
		this.l = l;
		this.b1 = b1;
		this.v1 = v1;
		this.a = a;
	}

	/**
	 * Returns the R bit value.
	 *
	 * @return The R bit.
	 */
	public boolean r() {
		return r;
	}

	/**
	 * Returns the X bit value.
	 *
	 * @return The X bit.
	 */
	public boolean x() {
		return x;
	}

	/**
	 * Returns the B bit value.
	 *
	 * @return The B bit.
	 */
	public boolean b() {
		return b;
	}

	/**
	 * Returns the R' (R1) bit value.
	 *
	 * @return The R1 bit.
	 */
	public boolean r1() {
		return r1;
	}

	/**
	 * Returns the M field value.
	 *
	 * @return The M field.
	 */
	public byte m() {
		return m;
	}

	/**
	 * Returns the W bit value.
	 *
	 * @return The W bit.
	 */
	public boolean w() {
		return w;
	}

	/**
	 * Returns the V field value.
	 *
	 * @return The V field.
	 */
	public byte v() {
		return v;
	}

	/**
	 * Returns the Z bit value.
	 *
	 * @return The Z bit.
	 */
	public boolean z() {
		return z;
	}

	/**
	 * Returns the L bit value.
	 *
	 * @return The L bit.
	 */
	public boolean l() {
		return l;
	}

	/**
	 * Returns the V' (V1) bit value.
	 *
	 * @return The V1 bit.
	 */
	public boolean v1() {
		return v1;
	}

	/**
	 * Returns the A field value.
	 *
	 * @return The A field.
	 */
	public byte a() {
		return a;
	}
}
