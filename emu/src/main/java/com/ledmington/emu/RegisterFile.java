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
package com.ledmington.emu;

import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;

/** Represents a mutable register file. */
public interface RegisterFile extends ImmutableRegisterFile {

	/**
	 * Sets the value of the given 8-bit register to given byte. This operation does not modify the other registers.
	 *
	 * @param r The Register to be overwritten.
	 * @param v The value to be written.
	 */
	void set(final Register8 r, final byte v);

	/**
	 * Sets the value of the given 16-bit register to given short. This operation does not modify the other registers.
	 *
	 * @param r The Register to be overwritten.
	 * @param v The value to be written.
	 */
	void set(final Register16 r, final short v);

	/**
	 * Sets the value of the given 32-bit register to given int. This operation does not modify the other registers.
	 *
	 * @param r The Register to be overwritten.
	 * @param v The value to be written.
	 */
	void set(final Register32 r, final int v);

	/**
	 * Sets the value of the given 64-bit register to given long. This operation does not modify the other registers.
	 *
	 * @param r The Register to be overwritten.
	 * @param v The value to be written.
	 */
	void set(final Register64 r, final long v);

	/**
	 * Sets the given flag to the given value.
	 *
	 * @param f The flag to be set.
	 * @param v The value to be written.
	 */
	void set(final RFlags f, final boolean v);

	/** Resets all RFLAGS. */
	void resetFlags();
}
