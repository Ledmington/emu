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

/** An x86 32-bit general-purpose register. */
public enum Register32 implements Register {

	/** The register EAX. */
	EAX,

	/** The register EBX. */
	EBX,

	/** The register ECX. */
	ECX,

	/** The register EDX. */
	EDX,

	/** The register ESI. */
	ESI,

	/** The register EDI. */
	EDI,

	/** The register ESP. */
	ESP,

	/** The register EBP. */
	EBP,

	/** The register R8D. */
	R8D,

	/** The register R9D. */
	R9D,

	/** The register R10D. */
	R10D,

	/** The register R11D. */
	R11D,

	/** The register R12D. */
	R12D,

	/** The register R13D. */
	R13D,

	/** The register R14D. */
	R14D,

	/** The register R15D. */
	R15D,

	/** The instruction pointer register EIP. */
	EIP;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 32-bit register corresponding to the given byte.
	 *
	 * @param b The byte representing a 32-bit register.
	 * @return A 32-bit register.
	 */
	public static Register32 fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> EAX;
			case 0x01 -> ECX;
			case 0x02 -> EDX;
			case 0x03 -> EBX;
			case 0x04 -> ESP;
			case 0x05 -> EBP;
			case 0x06 -> ESI;
			case 0x07 -> EDI;
			case 0x08 -> R8D;
			case 0x09 -> R9D;
			case 0x0a -> R10D;
			case 0x0b -> R11D;
			case 0x0c -> R12D;
			case 0x0d -> R13D;
			case 0x0e -> R14D;
			case 0x0f -> R15D;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	/**
	 * Returns the numeric encoding of the given {@link Register32}.
	 *
	 * @param r The 32-bit register to convert.
	 * @return The byte value representing the given 32-bit register.
	 */
	public static byte toByte(final Register32 r) {
		return switch (r) {
			case EAX, R8D -> (byte) 0x00;
			case ECX, R9D -> (byte) 0x01;
			case EDX, R10D -> (byte) 0x02;
			case EBX, R11D -> (byte) 0x03;
			case ESP, R12D -> (byte) 0x04;
			case EBP, R13D -> (byte) 0x05;
			case ESI, R14D -> (byte) 0x06;
			case EDI, R15D -> (byte) 0x07;
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
		};
	}

	public static boolean requiresExtension(final Register32 r) {
		return switch (r) {
			case EAX, EBX, ECX, EDX, ESI, EDI, ESP, EBP -> false;
			case R8D, R9D, R10D, R11D, R12D, R13D, R14D, R15D -> true;
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
		};
	}

	@Override
	public int bits() {
		return 32;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "Register32(mnemonic=" + mnemonic + ")";
	}
}
