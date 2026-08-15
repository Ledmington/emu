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
package com.ledmington.emu;

import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RegisterXMM;
import com.ledmington.cpu.x86.SegmentRegister;

/** Represents an immutable register file. */
public interface ImmutableRegisterFile {

	/**
	 * Returns the value of the given 8-bit register as a byte.
	 *
	 * @param r The register to be read.
	 * @return The value in the register.
	 */
	byte get(Register8 r);

	/**
	 * Returns the value of the given 16-bit register as a short.
	 *
	 * @param r The register to be read.
	 * @return The value in the register.
	 */
	short get(Register16 r);

	/**
	 * Returns the value of the given 16-bit segment register as a short.
	 *
	 * @param s The segment register to be read.
	 * @return The value in the register.
	 */
	short get(SegmentRegister s);

	/**
	 * Returns the value of the given 32-bit register as an int.
	 *
	 * @param r The Register to be read.
	 * @return The value of the register.
	 */
	int get(Register32 r);

	/**
	 * Returns the value of the given 64-bit register as a long.
	 *
	 * @param r The Register to be read.
	 * @return The value of the register.
	 */
	long get(Register64 r);

	/**
	 * Returns the low 32 bits of the given 128-bit XMM register, as an int.
	 *
	 * @param r The register to be read.
	 * @return The value of the low 32 bits of the register.
	 */
	int getXMM32(RegisterXMM r);

	/**
	 * Returns the full 128 bits of the given XMM register, as an array containing the low 64 bits at index 0 and the
	 * high 64 bits at index 1.
	 *
	 * @param r The register to be read.
	 * @return A 2-element array {low, high} with the two 64-bit halves of the register.
	 */
	long[] getXMM(RegisterXMM r);

	/**
	 * Checks whether the given flag is set.
	 *
	 * @param f The flag to be checked.
	 * @return True if it is set, false otherwise.
	 */
	boolean isSet(RFlags f);
}
