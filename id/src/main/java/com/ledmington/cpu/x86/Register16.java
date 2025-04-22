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

import java.util.Locale;

/** An x86 16-bit general-purpose register. */
public enum Register16 implements Register {

	/** The register AX. */
	AX,

	/** The register BX. */
	BX,

	/** The register CX. */
	CX,

	/** The register DX. */
	DX,

	/** The register SI. */
	SI,

	/** The register DI. */
	DI,

	/** The register SP. */
	SP,

	/** The register BP. */
	BP,

	/** The register R8W. */
	R8W,

	/** The register R9W. */
	R9W,

	/** The register R10W. */
	R10W,

	/** The register R11W. */
	R11W,

	/** The register R12W. */
	R12W,

	/** The register R13W. */
	R13W,

	/** The register R14W. */
	R14W,

	/** The register R15W. */
	R15W,

	// TODO: should the proper segment registers be separated from the general-purpose ones?
	/** The segment register CS (Code Segment). */
	CS,

	/** The segment register DS (Data Segment). */
	DS,

	/** The segment register SS (Stack Segment). */
	SS,

	/** The segment register ES (Extra Segment). */
	ES,

	/** The segment register FS. */
	FS,

	/** The segment register GS. */
	GS;

	private final String mnemonic = name().toLowerCase(Locale.US);

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
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static byte toByte(final Register16 r) {
		return switch (r) {
			case AX, R8W -> (byte) 0x00;
			case CX, R9W -> (byte) 0x01;
			case DX, R10W -> (byte) 0x02;
			case BX, R11W -> (byte) 0x03;
			case SP, R12W -> (byte) 0x04;
			case BP, R13W -> (byte) 0x05;
			case SI, R14W -> (byte) 0x06;
			case DI, R15W -> (byte) 0x07;
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static boolean requiresExtension(final Register16 r) {
		return switch (r) {
			case AX, BX, CX, DX, SI, DI, SP, BP -> false;
			case R8W, R9W, R10W, R11W, R12W, R13W, R14W, R15W -> true;
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
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
