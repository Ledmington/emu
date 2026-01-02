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
package com.ledmington.cpu;

/** Different categories of operands. */
public enum OperandType {

	// TODO: since some instruction can only target AL, AX, EAX or RAX, maybe make a specific category only for those?

	/** An 8-bit register. */
	R8,

	/** A 16-bit register. */
	R16,

	/** A 32-bit register (without EIP). */
	R32,

	/** A general-purpose 64-bit register (without RIP). */
	R64,

	/** An MMX 64-bit register. */
	RMM,

	/** An XMM 128-bit register. */
	RX,

	/** A YMM 256-bit register. */
	RY,

	/** A ZMM 512-bit register. */
	RZ,

	/** A mask register. */
	RK,

	/** A segment register. */
	RS,

	// TODO: encode also indirect operands with and without a segment register
	/** An indirect operand with BYTE PTR size. */
	M8,

	/** An indirect operand with WORD PTR size. */
	M16,

	/** An indirect operand with DWORD PTR size. */
	M32,

	/** An indirect operand with QWORD PTR size. */
	M64,

	/** An indirect operand with XMMWORD PTR size. */
	M128,

	/** An indirect operand with YMMWORD PTR size. */
	M256,

	/** An indirect operand with ZMMWORD PTR size. */
	M512,

	/** An immediate value of 8 bits. */
	I8,

	/** An immediate value of 16 bits. */
	I16,

	/** An immediate value of 32 bits. */
	I32,

	/** An immediate value of 64 bits. */
	I64,

	/** A segmented address with a 64-bit immediate. */
	S64
}
