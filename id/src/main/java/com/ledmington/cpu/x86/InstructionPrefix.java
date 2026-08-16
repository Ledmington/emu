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

/**
 * Common interface for x86_64 instruction prefixes that extend register encoding: REX (0x4X), VEX2 (0xc5), VEX3 (0xc4),
 * and EVEX (0x62).
 *
 * <p>All four prefix types share a common set of extension bits that expand the register namespace beyond the 3-bit
 * limit of the base x86 encoding. The methods here expose those bits under consistent semantic names, regardless of
 * which prefix type is in use.
 *
 * <p>Bits that do not exist in a given prefix type (e.g. {@link #x()} in VEX2) must be implemented to return
 * {@code false} — their logical absence is equivalent to the extension bit being clear.
 */
public interface InstructionPrefix {

	/**
	 * Returns the R extension bit, which extends the REG field of the ModR/M byte from 3 bits to 4 bits (REX, VEX2,
	 * VEX3) or to 5 bits combined with R' in EVEX ({@link #r1()}).
	 *
	 * <p>Equivalent to REX.R, VEX.R, EVEX.R.
	 *
	 * @return {@code true} if the R extension bit is set.
	 */
	boolean r();

	/**
	 * Returns the X extension bit, which extends the Index field of the SIB byte from 3 bits to 4 bits.
	 *
	 * <p>Equivalent to REX.X, VEX.X, EVEX.X. Returns {@code false} for prefix types that do not encode this bit (VEX2).
	 *
	 * @return {@code true} if the X extension bit is set.
	 */
	boolean x();

	/**
	 * Returns the B extension bit, which extends one of the following fields depending on instruction encoding context:
	 *
	 * <ul>
	 *   <li>the RM field of the ModR/M byte,
	 *   <li>the Base field of the SIB byte,
	 *   <li>the register field embedded in the opcode byte.
	 * </ul>
	 *
	 * <p>Equivalent to REX.B, VEX.B, EVEX.B. Returns {@code false} for prefix types that do not encode this bit (VEX2).
	 *
	 * @return {@code true} if the B extension bit is set.
	 */
	boolean b();

	/**
	 * Returns the W bit, which typically selects between 32-bit and 64-bit operand size, though its exact meaning is
	 * opcode-dependent for VEX/EVEX.
	 *
	 * <p>Equivalent to REX.W, VEX.W, EVEX.W. Returns {@code false} for prefix types that do not encode this bit (VEX2).
	 *
	 * @return {@code true} if the W bit is set.
	 */
	boolean w();

	/**
	 * Returns the V field, a 4-bit value encoding an additional source register operand (in one's complement). Used by
	 * VEX and EVEX instructions with three or four operands.
	 *
	 * <p>Equivalent to VEX.vvvv, EVEX.vvvv. Returns {@code 0} for prefix types that do not encode this field (REX).
	 *
	 * @return The 4-bit V field, in the range [0, 15].
	 */
	byte v();

	/**
	 * Returns the R' (R1) extension bit, which combines with {@link #r()} to extend the REG field of the ModR/M byte to
	 * 5 bits, allowing access to all 32 EVEX registers.
	 *
	 * <p>Only meaningful in EVEX. All other prefix types must return {@code false}.
	 *
	 * @return {@code true} if the R' extension bit is set.
	 */
	default boolean r1() {
		return false;
	}
}
