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

/** An x86 8-bit general-purpose register. */
@SuppressWarnings("PMD.CyclomaticComplexity")
public enum Register8 implements Register {

	/** The register AL. */
	AL,

	/** The register BL. */
	BL,

	/** The register CL. */
	CL,

	/** The register DL. */
	DL,

	/** The register AH. */
	AH,

	/** The register BH. */
	BH,

	/** The register CH. */
	CH,

	/** The register DH. */
	DH,

	/** The register DIL. */
	DIL,

	/** The register SIL. */
	SIL,

	/** The register BPL. */
	BPL,

	/** The register SPL. */
	SPL,

	/** The register R8B. */
	R8B,

	/** The register R9B. */
	R9B,

	/** The register R10B. */
	R10B,

	/** The register R11B. */
	R11B,

	/** The register R12B. */
	R12B,

	/** The register R13B. */
	R13B,

	/** The register R14B. */
	R14B,

	/** The register R15B. */
	R15B;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 8-bit register corresponding to the given byte.
	 *
	 * @param b The byte representing an 8-bit register.
	 * @param hasRexPrefix Allows to select different sets of registers. If true, values in the inclusive range
	 *     0x04-0x07 map to SPL, BPL, SIL and DIL respectively; otherwise, they map to AH, CH, DH, BH.
	 * @return An 8-bit register.
	 */
	public static Register8 fromByte(final byte b, final boolean hasRexPrefix) {
		return switch (b) {
			case 0x00 -> AL;
			case 0x01 -> CL;
			case 0x02 -> DL;
			case 0x03 -> BL;
			case 0x04 -> hasRexPrefix ? SPL : AH;
			case 0x05 -> hasRexPrefix ? BPL : CH;
			case 0x06 -> hasRexPrefix ? SIL : DH;
			case 0x07 -> hasRexPrefix ? DIL : BH;
			case 0x08 -> R8B;
			case 0x09 -> R9B;
			case 0x0a -> R10B;
			case 0x0b -> R11B;
			case 0x0c -> R12B;
			case 0x0d -> R13B;
			case 0x0e -> R14B;
			case 0x0f -> R15B;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	/**
	 * Returns the numeric encoding of the given {@link Register8}.
	 *
	 * @param r The 8-bit register to convert.
	 * @return The byte value representing the given 8-bit register.
	 */
	public static byte toByte(final Register8 r) {
		return switch (r) {
			case AL, R8B -> (byte) 0x00;
			case CL, R9B -> (byte) 0x01;
			case DL, R10B -> (byte) 0x02;
			case BL, R11B -> (byte) 0x03;
			case AH, SPL, R12B -> (byte) 0x04;
			case CH, BPL, R13B -> (byte) 0x05;
			case DH, SIL, R14B -> (byte) 0x06;
			case BH, DIL, R15B -> (byte) 0x07;
		};
	}

	public static boolean requiresExtension(final Register8 r) {
		return switch (r) {
			case AL, CL, DL, BL, CH, BPL, AH, SPL, DH, SIL, BH, DIL -> false;
			case R8B, R9B, R10B, R11B, R12B, R13B, R14B, R15B -> true;
		};
	}

	public static boolean requiresRexPrefix(final Register8 r) {
		return switch (r) {
			case AL, CL, DL, BL, CH, AH, DH, BH, R8B, R9B, R10B, R11B, R12B, R13B, R14B, R15B -> false;
			case BPL, SPL, SIL, DIL -> true;
		};
	}

	@Override
	public int bits() {
		return 8;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "Register8(mnemonic=" + mnemonic + ')';
	}
}
