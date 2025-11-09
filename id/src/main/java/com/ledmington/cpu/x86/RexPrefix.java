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

/**
 * This class represents an x86 REX prefix byte, used for extending the operands of an x86 instructions.
 *
 * @param w The W bit, for specifying that an operand is 64-bit.
 * @param r The R bit, an extension for the REG field of the ModR/M byte.
 * @param x The X bit, an extension for the Index field of the SIB byte.
 * @param b The B bit, an extension for the Base field of the SIB byte, the RM field of the ModR/M byte or the opcode.
 */
public record RexPrefix(boolean w, boolean r, boolean x, boolean b) {

	private static final byte REX_PREFIX_MASK = (byte) 0b11110000;
	private static final byte REX_PREFIX = (byte) 0b01000000;

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
	 * @return A proper RexPrefix instance corresponding to the given byte.
	 */
	public static RexPrefix of(final byte b) {
		if (!isREXPrefix(b)) {
			throw new IllegalArgumentException(String.format("Input byte 0x%02x is not a valid REX prefix", b));
		}

		final byte REX_w_mask = (byte) 0b00001000;
		final byte REX_r_mask = (byte) 0b00000100;
		final byte REX_x_mask = (byte) 0b00000010;
		final byte REX_b_mask = (byte) 0b00000001;

		return new RexPrefix(
				BitUtils.and(b, REX_w_mask) != 0,
				BitUtils.and(b, REX_r_mask) != 0,
				BitUtils.and(b, REX_x_mask) != 0,
				BitUtils.and(b, REX_b_mask) != 0);
	}

	/**
	 * Returns the 'W' bit of this REX prefix.
	 *
	 * @return True if the 'W' bit is set, false otherwise.
	 */
	public boolean isOperand64Bit() {
		return w;
	}

	/**
	 * Returns the 'R' bit of this REX prefix.
	 *
	 * @return True if the 'R' bit is set, false otherwise.
	 */
	public boolean hasModRMRegExtension() {
		return r;
	}

	/**
	 * Returns the 'X' bit of this REX prefix.
	 *
	 * @return True if the 'X' bit is set, false otherwise.
	 */
	public boolean hasSIBIndexExtension() {
		return x;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean hasSIBBaseExtension() {
		return b;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean hasModRMRMExtension() {
		return b;
	}

	/**
	 * Returns the 'B' bit of this REX prefix.
	 *
	 * @return True if the 'B' bit is set, false otherwise.
	 */
	public boolean hasOpcodeRegExtension() {
		return b;
	}
}
