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

/** An x86 512-bit general-purpose register. */
@SuppressWarnings("PMD.CyclomaticComplexity")
public enum RegisterZMM implements Register {

	/** The register ZMM0. */
	ZMM0,

	/** The register ZMM1. */
	ZMM1,

	/** The register ZMM2. */
	ZMM2,

	/** The register ZMM3. */
	ZMM3,

	/** The register ZMM4. */
	ZMM4,

	/** The register ZMM5. */
	ZMM5,

	/** The register ZMM6. */
	ZMM6,

	/** The register ZMM7. */
	ZMM7,

	/** The register ZMM8. */
	ZMM8,

	/** The register ZMM9. */
	ZMM9,

	/** The register ZMM10. */
	ZMM10,

	/** The register ZMM11. */
	ZMM11,

	/** The register ZMM12. */
	ZMM12,

	/** The register ZMM13. */
	ZMM13,

	/** The register ZMM14. */
	ZMM14,

	/** The register ZMM15. */
	ZMM15,

	/** The register ZMM16. */
	ZMM16,

	/** The register ZMM17. */
	ZMM17,

	/** The register ZMM18. */
	ZMM18,

	/** The register ZMM19. */
	ZMM19,

	/** The register ZMM20. */
	ZMM20,

	/** The register ZMM21. */
	ZMM21,

	/** The register ZMM22. */
	ZMM22,

	/** The register ZMM23. */
	ZMM23,

	/** The register ZMM24. */
	ZMM24,

	/** The register ZMM25. */
	ZMM25,

	/** The register ZMM26. */
	ZMM26,

	/** The register ZMM27. */
	ZMM27,

	/** The register ZMM28. */
	ZMM28,

	/** The register ZMM29. */
	ZMM29,

	/** The register ZMM30. */
	ZMM30,

	/** The register ZMM31. */
	ZMM31;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 512-bit ZMM register corresponding to the given byte.
	 *
	 * @param b The byte representing a 512-bit ZMM register.
	 * @return A 512-bit ZMM register.
	 */
	public static RegisterZMM fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> ZMM0;
			case 0x01 -> ZMM1;
			case 0x02 -> ZMM2;
			case 0x03 -> ZMM3;
			case 0x04 -> ZMM4;
			case 0x05 -> ZMM5;
			case 0x06 -> ZMM6;
			case 0x07 -> ZMM7;
			case 0x08 -> ZMM8;
			case 0x09 -> ZMM9;
			case 0x0a -> ZMM10;
			case 0x0b -> ZMM11;
			case 0x0c -> ZMM12;
			case 0x0d -> ZMM13;
			case 0x0e -> ZMM14;
			case 0x0f -> ZMM15;
			case 0x10 -> ZMM16;
			case 0x11 -> ZMM17;
			case 0x12 -> ZMM18;
			case 0x13 -> ZMM19;
			case 0x14 -> ZMM20;
			case 0x15 -> ZMM21;
			case 0x16 -> ZMM22;
			case 0x17 -> ZMM23;
			case 0x18 -> ZMM24;
			case 0x19 -> ZMM25;
			case 0x1a -> ZMM26;
			case 0x1b -> ZMM27;
			case 0x1c -> ZMM28;
			case 0x1d -> ZMM29;
			case 0x1e -> ZMM30;
			case 0x1f -> ZMM31;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	public static byte toByte(final RegisterZMM r) {
		return switch (r) {
			case ZMM0, ZMM8, ZMM16, ZMM24 -> (byte) 0x00;
			case ZMM1, ZMM9, ZMM17, ZMM25 -> (byte) 0x01;
			case ZMM2, ZMM10, ZMM18, ZMM26 -> (byte) 0x02;
			case ZMM3, ZMM11, ZMM19, ZMM27 -> (byte) 0x03;
			case ZMM4, ZMM12, ZMM20, ZMM28 -> (byte) 0x04;
			case ZMM5, ZMM13, ZMM21, ZMM29 -> (byte) 0x05;
			case ZMM6, ZMM14, ZMM22, ZMM30 -> (byte) 0x06;
			case ZMM7, ZMM15, ZMM23, ZMM31 -> (byte) 0x07;
		};
	}

	public static boolean requiresExtension(final RegisterZMM r) {
		return switch (r) {
			case ZMM0,
					ZMM1,
					ZMM2,
					ZMM3,
					ZMM4,
					ZMM5,
					ZMM6,
					ZMM7,
					ZMM16,
					ZMM17,
					ZMM18,
					ZMM19,
					ZMM20,
					ZMM21,
					ZMM22,
					ZMM23 -> false;
			case ZMM8,
					ZMM9,
					ZMM10,
					ZMM11,
					ZMM12,
					ZMM13,
					ZMM14,
					ZMM15,
					ZMM24,
					ZMM25,
					ZMM26,
					ZMM27,
					ZMM28,
					ZMM29,
					ZMM30,
					ZMM31 -> true;
		};
	}

	public static boolean requiresEvexExtension(final RegisterZMM r) {
		return switch (r) {
			case ZMM0, ZMM1, ZMM2, ZMM3, ZMM4, ZMM5, ZMM6, ZMM7, ZMM8, ZMM9, ZMM10, ZMM11, ZMM12, ZMM13, ZMM14, ZMM15 ->
				false;
			case ZMM16,
					ZMM17,
					ZMM18,
					ZMM19,
					ZMM20,
					ZMM21,
					ZMM22,
					ZMM23,
					ZMM24,
					ZMM25,
					ZMM26,
					ZMM27,
					ZMM28,
					ZMM29,
					ZMM30,
					ZMM31 -> true;
		};
	}

	@Override
	public int bits() {
		return 512;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "RegisterZMM(mnemonic=" + mnemonic + ")";
	}
}
