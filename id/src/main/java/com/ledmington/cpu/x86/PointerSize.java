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

/** The size of a pointer (indirect operand) in an x86 instruction. */
public enum PointerSize {

	/** Pointer to byte (8 bits). */
	BYTE_PTR(8),

	/** Pointer to word (16 bits). */
	WORD_PTR(16),

	/** Pointer to double word (32 bits). */
	DWORD_PTR(32),

	/** Pointer to quadword (64 bits). */
	QWORD_PTR(64),

	/** Pointer to word for XMM registers (2x64 bits). */
	XMMWORD_PTR(128),

	/** Pointer to word for YMM registers (4x64 bits). */
	YMMWORD_PTR(256),

	/** Pointer to word for ZMM registers (8x64 bits). */
	ZMMWORD_PTR(512);

	private final int size;

	PointerSize(final int size) {
		this.size = size;
	}

	/**
	 * Returns the number of bits represented by this pointer size.
	 *
	 * @return The number of bits represented by this pointer size.
	 */
	public int bits() {
		return size;
	}

	/**
	 * Returns the proper PointerSize object from corresponding to the given size in bits.
	 *
	 * @param size The size in bits of the pointer.
	 * @return The proper PointerSize object.
	 */
	public static PointerSize fromSize(final int size) {
		return switch (size) {
			case 8 -> BYTE_PTR;
			case 16 -> WORD_PTR;
			case 32 -> DWORD_PTR;
			case 64 -> QWORD_PTR;
			case 128 -> XMMWORD_PTR;
			case 256 -> YMMWORD_PTR;
			default -> throw new IllegalStateException(String.format("Unexpected value: %,d.", size));
		};
	}

	@Override
	public String toString() {
		return "PointerSize(bits=" + size + ")";
	}
}
