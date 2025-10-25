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

/** An x86 64-bit general-purpose register. */
public enum Register64 implements Register {

	/** The register RAX. Usually contains the return value of a function call. */
	RAX,

	/** The register RBX. */
	RBX,

	/** The register RCX. */
	RCX,

	/** The register RDX. */
	RDX,

	/** The register RSI. */
	RSI,

	/** The register RDI. */
	RDI,

	/** The register RBP. Usually points to the base (the start) of the current stack frame. */
	RBP,

	/** The register RSP. Usually points to the top (the end) of the current stack frame. */
	RSP,

	/** The register R8. */
	R8,

	/** The register R9. */
	R9,

	/** The register R10. */
	R10,

	/** The register R11. */
	R11,

	/** The register R12. */
	R12,

	/** The register R13. */
	R13,

	/** The register R14. */
	R14,

	/** The register R15. */
	R15,

	/** The instruction pointer register RIP. */
	RIP;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 64-bit register corresponding to the given byte.
	 *
	 * @param b The byte representing a 64-bit register.
	 * @return A 64-bit register.
	 */
	public static Register64 fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> RAX;
			case 0x01 -> RCX;
			case 0x02 -> RDX;
			case 0x03 -> RBX;
			case 0x04 -> RSP;
			case 0x05 -> RBP;
			case 0x06 -> RSI;
			case 0x07 -> RDI;
			case 0x08 -> R8;
			case 0x09 -> R9;
			case 0x0a -> R10;
			case 0x0b -> R11;
			case 0x0c -> R12;
			case 0x0d -> R13;
			case 0x0e -> R14;
			case 0x0f -> R15;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	public static byte toByte(final Register64 r) {
		return switch (r) {
			case RAX, R8 -> (byte) 0x00;
			case RCX, R9 -> (byte) 0x01;
			case RDX, R10 -> (byte) 0x02;
			case RBX, R11 -> (byte) 0x03;
			case RSP, R12 -> (byte) 0x04;
			case RBP, R13 -> (byte) 0x05;
			case RSI, R14 -> (byte) 0x06;
			case RDI, R15 -> (byte) 0x07;
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
		};
	}

	public static boolean requiresExtension(final Register64 r) {
		return switch (r) {
			case RAX, RBX, RCX, RDX, RSI, RDI, RSP, RBP, RIP -> false;
			case R8, R9, R10, R11, R12, R13, R14, R15 -> true;
		};
	}

	@Override
	public int bits() {
		return 64;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "Register64(mnemonic=" + mnemonic + ")";
	}
}
