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

import java.util.Objects;

/** An x86 16-bit general-purpose register. */
public enum Register16 implements Register {

	/** The register AX. */
	AX("ax"),

	/** The register BX. */
	BX("bx"),

	/** The register CX. */
	CX("cx"),

	/** The register DX. */
	DX("dx"),

	/** The register SI. */
	SI("si"),

	/** The register DI. */
	DI("di"),

	/** The register SP. */
	SP("sp"),

	/** The register BP. */
	BP("bp"),

	/** The register R8W. */
	R8W("r8w"),

	/** The register R9W. */
	R9W("r9w"),

	/** The register R10W. */
	R10W("r10w"),

	/** The register R11W. */
	R11W("r11w"),

	/** The register R12W. */
	R12W("r12w"),

	/** The register R13W. */
	R13W("r13w"),

	/** The register R14W. */
	R14W("r14w"),

	/** The register R15W. */
	R15W("r15w"),

	/** The segment register CS (Code Segment). */
	CS("cs"),

	/** The segment register DS (Data Segment). */
	DS("ds"),

	/** The segment register SS (Stack Segment). */
	SS("ss"),

	/** The segment register ES (Extra Segment). */
	ES("es"),

	/** The segment register FS. */
	FS("fs"),

	/** The segment register GS. */
	GS("gs");

	private final String mnemonic;

	Register16(final String mnemonic) {
		this.mnemonic = Objects.requireNonNull(mnemonic);
	}

	/**
	 * Returns the 16-bit register corresponding to the given byte.
	 *
	 * @param b The byte representing a 16-bit register.
	 * @return A 16-bit register.
	 */
	public static Register16 fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> AX;
			case 0x01 -> CX;
			case 0x02 -> DX;
			case 0x03 -> BX;
			case 0x04 -> SP;
			case 0x05 -> BP;
			case 0x06 -> SI;
			case 0x07 -> DI;
			case 0x08 -> R8W;
			case 0x09 -> R9W;
			case 0x0a -> R10W;
			case 0x0b -> R11W;
			case 0x0c -> R12W;
			case 0x0d -> R13W;
			case 0x0e -> R14W;
			case 0x0f -> R15W;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
		};
	}

	@Override
	public int bits() {
		return 16;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "Register16(mnemonic=" + mnemonic + ")";
	}
}
