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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;

/** This class represents an x86 ModR/M byte, used to represent "direct" operands in x86 instructions. */
public final class ModRM {

	private final byte modByte;
	private final byte regByte;
	private final byte rmByte;

	private static byte extractMod(final byte m) {
		final byte MODRM_MOD_MASK = (byte) 0b11000000;
		return BitUtils.shr(BitUtils.and(m, MODRM_MOD_MASK), 6);
	}

	private static byte extractReg(final byte m) {
		final byte MODRM_REG_MASK = (byte) 0b00111000;
		return BitUtils.shr(BitUtils.and(m, MODRM_REG_MASK), 3);
	}

	private static byte extractRM(final byte m) {
		final byte MODRM_RM_MASK = (byte) 0b00000111;
		return BitUtils.and(m, MODRM_RM_MASK);
	}

	/**
	 * Creates a ModRM object by parsing the given byte.
	 *
	 * @param m The byte to be converted.
	 */
	public ModRM(final byte m) {
		this.modByte = extractMod(m);
		this.regByte = extractReg(m);
		this.rmByte = extractRM(m);
	}

	/**
	 * Returns the MOD portion of this ModR/M object.
	 *
	 * @return The MOD portion as a byte.
	 */
	public byte mod() {
		return modByte;
	}

	/**
	 * Returns the REG portion of this ModR/M object.
	 *
	 * @return The REG portion as a byte.
	 */
	public byte reg() {
		return regByte;
	}

	/**
	 * Returns the R/M portion of this ModR/M object.
	 *
	 * @return The R/M portion as a byte.
	 */
	public byte rm() {
		return rmByte;
	}

	@Override
	public String toString() {
		return "mod:" + BitUtils.toBinaryString(modByte).substring(6, 8) + " reg:"
				+ BitUtils.toBinaryString(regByte).substring(5, 8) + " r/m:"
				+ BitUtils.toBinaryString(rmByte).substring(5, 8);
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + HashUtils.hash(modByte);
		h = 31 * h + HashUtils.hash(regByte);
		h = 31 * h + HashUtils.hash(rmByte);
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
		final ModRM m = (ModRM) other;
		return this.modByte == m.modByte && this.regByte == m.regByte && this.rmByte == m.rmByte;
	}
}
