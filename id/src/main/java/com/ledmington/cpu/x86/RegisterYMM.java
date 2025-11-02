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

/** An x86 256-bit general-purpose register. */
@SuppressWarnings("PMD.CyclomaticComplexity")
public enum RegisterYMM implements Register {

	/** The register YMM0. */
	YMM0,

	/** The register YMM1. */
	YMM1,

	/** The register YMM2. */
	YMM2,

	/** The register YMM3. */
	YMM3,

	/** The register YMM4. */
	YMM4,

	/** The register YMM5. */
	YMM5,

	/** The register YMM6. */
	YMM6,

	/** The register YMM7. */
	YMM7,

	/** The register YMM8. */
	YMM8,

	/** The register YMM9. */
	YMM9,

	/** The register YMM10. */
	YMM10,

	/** The register YMM11. */
	YMM11,

	/** The register YMM12. */
	YMM12,

	/** The register YMM13. */
	YMM13,

	/** The register YMM14. */
	YMM14,

	/** The register YMM15. */
	YMM15,

	/** The register YMM16. */
	YMM16,

	/** The register YMM17. */
	YMM17,

	/** The register YMM18. */
	YMM18,

	/** The register YMM19. */
	YMM19,

	/** The register YMM20. */
	YMM20,

	/** The register YMM21. */
	YMM21,

	/** The register YMM22. */
	YMM22,

	/** The register YMM23. */
	YMM23,

	/** The register YMM24. */
	YMM24,

	/** The register YMM25. */
	YMM25,

	/** The register YMM26. */
	YMM26,

	/** The register YMM27. */
	YMM27,

	/** The register YMM28. */
	YMM28,

	/** The register YMM29. */
	YMM29,

	/** The register YMM30. */
	YMM30,

	/** The register YMM31. */
	YMM31;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 256-bit YMM register corresponding to the given byte.
	 *
	 * @param b The byte representing a 256-bit YMM register.
	 * @return A 256-bit YMM register.
	 */
	public static RegisterYMM fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> YMM0;
			case 0x01 -> YMM1;
			case 0x02 -> YMM2;
			case 0x03 -> YMM3;
			case 0x04 -> YMM4;
			case 0x05 -> YMM5;
			case 0x06 -> YMM6;
			case 0x07 -> YMM7;
			case 0x08 -> YMM8;
			case 0x09 -> YMM9;
			case 0x0a -> YMM10;
			case 0x0b -> YMM11;
			case 0x0c -> YMM12;
			case 0x0d -> YMM13;
			case 0x0e -> YMM14;
			case 0x0f -> YMM15;
			case 0x10 -> YMM16;
			case 0x11 -> YMM17;
			case 0x12 -> YMM18;
			case 0x13 -> YMM19;
			case 0x14 -> YMM20;
			case 0x15 -> YMM21;
			case 0x16 -> YMM22;
			case 0x17 -> YMM23;
			case 0x18 -> YMM24;
			case 0x19 -> YMM25;
			case 0x1a -> YMM26;
			case 0x1b -> YMM27;
			case 0x1c -> YMM28;
			case 0x1d -> YMM29;
			case 0x1e -> YMM30;
			case 0x1f -> YMM31;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	/**
	 * Returns the numeric encoding of the given {@link RegisterYMM}.
	 *
	 * @param r The YMM register to convert.
	 * @return The byte value representing the given YMM register.
	 */
	public static byte toByte(final RegisterYMM r) {
		return switch (r) {
			case YMM0, YMM8, YMM16, YMM24 -> (byte) 0x00;
			case YMM1, YMM9, YMM17, YMM25 -> (byte) 0x01;
			case YMM2, YMM10, YMM18, YMM26 -> (byte) 0x02;
			case YMM3, YMM11, YMM19, YMM27 -> (byte) 0x03;
			case YMM4, YMM12, YMM20, YMM28 -> (byte) 0x04;
			case YMM5, YMM13, YMM21, YMM29 -> (byte) 0x05;
			case YMM6, YMM14, YMM22, YMM30 -> (byte) 0x06;
			case YMM7, YMM15, YMM23, YMM31 -> (byte) 0x07;
		};
	}

	/**
	 * Returns true if the given RegisterYMM instance requires an extension to be properly encoded.
	 *
	 * @param r The 256-bit register.
	 * @return True if the 256-bit register requires an extension.
	 */
	public static boolean requiresExtension(final RegisterYMM r) {
		return switch (r) {
			case YMM0,
					YMM1,
					YMM2,
					YMM3,
					YMM4,
					YMM5,
					YMM6,
					YMM7,
					YMM16,
					YMM17,
					YMM18,
					YMM19,
					YMM20,
					YMM21,
					YMM22,
					YMM23 -> false;
			case YMM8,
					YMM9,
					YMM10,
					YMM11,
					YMM12,
					YMM13,
					YMM14,
					YMM15,
					YMM24,
					YMM25,
					YMM26,
					YMM27,
					YMM28,
					YMM29,
					YMM30,
					YMM31 -> true;
		};
	}

	/**
	 * Returns true if the given RegisterYMM instance requires an extension to be properly encoded in the EVEX prefix.
	 *
	 * @param r The 256-bit register.
	 * @return True if the 256-bit register requires an EVEX extension.
	 */
	public static boolean requiresEvexExtension(final RegisterYMM r) {
		return switch (r) {
			case YMM0, YMM1, YMM2, YMM3, YMM4, YMM5, YMM6, YMM7, YMM8, YMM9, YMM10, YMM11, YMM12, YMM13, YMM14, YMM15 ->
				false;
			case YMM16,
					YMM17,
					YMM18,
					YMM19,
					YMM20,
					YMM21,
					YMM22,
					YMM23,
					YMM24,
					YMM25,
					YMM26,
					YMM27,
					YMM28,
					YMM29,
					YMM30,
					YMM31 -> true;
		};
	}

	@Override
	public int bits() {
		return 256;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "RegisterYMM(mnemonic=" + mnemonic + ")";
	}
}
