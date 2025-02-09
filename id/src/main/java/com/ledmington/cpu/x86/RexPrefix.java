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
import com.ledmington.utils.HashUtils;

/** This class represents an x86 REX prefix byte, used for extending the operands of an x86 instructions. */
public final class RexPrefix {

	private static final byte REX_PREFIX_MASK = (byte) 0b11110000;
	private static final byte REX_PREFIX = (byte) 0b01000000;

	private final boolean wBit;
	private final boolean rBit;
	private final boolean xBit;
	private final boolean bBit;

	/**
	 * Checks whether the given byte is a valid REX prefix byte.
	 *
	 * @param b The byte to be checked
	 * @return True if it is a valid REX prefix, false otherwise.
	 */
	public static boolean isREXPrefix(final byte b) {
		return BitUtils.and(b, REX_PREFIX_MASK) == REX_PREFIX;
	}

	/**
	 * Creates a REX prefix object by parsing the given byte.
	 *
	 * @param b The byte to be parsed.
	 */
	public RexPrefix(final byte b) {
		if (!isREXPrefix(b)) {
			throw new IllegalArgumentException(String.format("Input byte 0x%02x is not a valid REX prefix", b));
		}

		final byte REX_w_mask = (byte) 0b00001000;
		final byte REX_r_mask = (byte) 0b00000100;
		final byte REX_x_mask = (byte) 0b00000010;
		final byte REX_b_mask = (byte) 0b00000001;

		this.wBit = BitUtils.and(b, REX_w_mask) != 0;
		this.rBit = BitUtils.and(b, REX_r_mask) != 0;
		this.xBit = BitUtils.and(b, REX_x_mask) != 0;
		this.bBit = BitUtils.and(b, REX_b_mask) != 0;
	}

	/**
	 * Returns the 'W' bit of this REX prefix.
	 *
	 * @return True if the 'R' bit is set, false otherwise.
	 */
	public boolean w() {
		return wBit;
	}

	/**
	 * Returns the 'W' bit of this REX prefix.
	 *
	 * @return True if the 'R' bit is set, false otherwise.
	 */
	public boolean isOperand64Bit() {
		return wBit;
	}

	/**
	 * Returns the 'R' bit of this REX prefix.
	 *
	 * @return True if the 'R' bit is set, false otherwise.
	 */
	public boolean r() {
		return rBit;
	}

	/**
	 * Returns the 'R' bit of this REX prefix.
	 *
	 * @return True if the 'R' bit is set, false otherwise.
	 */
	public boolean getModRMRegExtension() {
		return rBit;
	}

	/**
	 * Returns the 'X' bit of this REX prefix.
	 *
	 * @return True if the 'X' bit is set, false otherwise.
	 */
	public boolean x() {
		return xBit;
	}

	/**
	 * Returns the 'X' bit of this REX prefix.
	 *
	 * @return True if the 'X' bit is set, false otherwise.
	 */
	public boolean SIBIndexExtension() {
		return xBit;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean b() {
		return bBit;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean SIBBaseExtension() {
		return bBit;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean getModRMRMExtension() {
		return bBit;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean opcodeRegExtension() {
		return bBit;
	}

	@Override
	public String toString() {
		return (wBit ? ".W" : "") + (rBit ? ".R" : "") + (xBit ? ".X" : "") + (bBit ? ".B" : "");
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 + h + HashUtils.hash(wBit);
		h = 31 + h + HashUtils.hash(rBit);
		h = 31 + h + HashUtils.hash(xBit);
		h = 31 + h + HashUtils.hash(bBit);
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final RexPrefix rex = (RexPrefix) other;
		return this.wBit == rex.wBit && this.rBit == rex.rBit && this.xBit == rex.xBit && this.bBit == rex.bBit;
	}
}
