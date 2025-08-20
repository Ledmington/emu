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

/** An x86 128-bit general-purpose register. */
public enum RegisterXMM implements Register {

	/** The register XMM0. */
	XMM0,

	/** The register XMM1. */
	XMM1,

	/** The register XMM2. */
	XMM2,

	/** The register XMM3. */
	XMM3,

	/** The register XMM4. */
	XMM4,

	/** The register XMM5. */
	XMM5,

	/** The register XMM6. */
	XMM6,

	/** The register XMM7. */
	XMM7,

	/** The register XMM8. */
	XMM8,

	/** The register XMM9. */
	XMM9,

	/** The register XMM10. */
	XMM10,

	/** The register XMM11. */
	XMM11,

	/** The register XMM12. */
	XMM12,

	/** The register XMM13. */
	XMM13,

	/** The register XMM14. */
	XMM14,

	/** The register XMM15. */
	XMM15,

	/** The register XMM16. */
	XMM16,

	/** The register XMM17. */
	XMM17,

	/** The register XMM18. */
	XMM18,

	/** The register XMM19. */
	XMM19,

	/** The register XMM20. */
	XMM20,

	/** The register XMM21. */
	XMM21,

	/** The register XMM22. */
	XMM22,

	/** The register XMM23. */
	XMM23,

	/** The register XMM24. */
	XMM24,

	/** The register XMM25. */
	XMM25,

	/** The register XMM26. */
	XMM26,

	/** The register XMM27. */
	XMM27,

	/** The register XMM28. */
	XMM28,

	/** The register XMM29. */
	XMM29,

	/** The register XMM30. */
	XMM30,

	/** The register XMM31. */
	XMM31;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the 128-bit XMM register corresponding to the given byte.
	 *
	 * @param b The byte representing a 128-bit XMM register.
	 * @return A 128-bit XMM register.
	 */
	public static RegisterXMM fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> XMM0;
			case 0x01 -> XMM1;
			case 0x02 -> XMM2;
			case 0x03 -> XMM3;
			case 0x04 -> XMM4;
			case 0x05 -> XMM5;
			case 0x06 -> XMM6;
			case 0x07 -> XMM7;
			case 0x08 -> XMM8;
			case 0x09 -> XMM9;
			case 0x0a -> XMM10;
			case 0x0b -> XMM11;
			case 0x0c -> XMM12;
			case 0x0d -> XMM13;
			case 0x0e -> XMM14;
			case 0x0f -> XMM15;
			case 0x10 -> XMM16;
			case 0x11 -> XMM17;
			case 0x12 -> XMM18;
			case 0x13 -> XMM19;
			case 0x14 -> XMM20;
			case 0x15 -> XMM21;
			case 0x16 -> XMM22;
			case 0x17 -> XMM23;
			case 0x18 -> XMM24;
			case 0x19 -> XMM25;
			case 0x1a -> XMM26;
			case 0x1b -> XMM27;
			case 0x1c -> XMM28;
			case 0x1d -> XMM29;
			case 0x1e -> XMM30;
			case 0x1f -> XMM31;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	public static byte toByte(final RegisterXMM r) {
		return switch (r) {
			case XMM0, XMM8, XMM16, XMM24 -> (byte) 0x00;
			case XMM1, XMM9, XMM17, XMM25 -> (byte) 0x01;
			case XMM2, XMM10, XMM18, XMM26 -> (byte) 0x02;
			case XMM3, XMM11, XMM19, XMM27 -> (byte) 0x03;
			case XMM4, XMM12, XMM20, XMM28 -> (byte) 0x04;
			case XMM5, XMM13, XMM21, XMM29 -> (byte) 0x05;
			case XMM6, XMM14, XMM22, XMM30 -> (byte) 0x06;
			case XMM7, XMM15, XMM23, XMM31 -> (byte) 0x07;
		};
	}

	public static boolean requiresExtension(final RegisterXMM r) {
		return switch (r) {
			case XMM0,
					XMM1,
					XMM2,
					XMM3,
					XMM4,
					XMM5,
					XMM6,
					XMM7,
					XMM16,
					XMM17,
					XMM18,
					XMM19,
					XMM20,
					XMM21,
					XMM22,
					XMM23 -> false;
			case XMM8,
					XMM9,
					XMM10,
					XMM11,
					XMM12,
					XMM13,
					XMM14,
					XMM15,
					XMM24,
					XMM25,
					XMM26,
					XMM27,
					XMM28,
					XMM29,
					XMM30,
					XMM31 -> true;
		};
	}

	public static boolean requiresEvexExtension(final RegisterXMM r) {
		return switch (r) {
			case XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7, XMM8, XMM9, XMM10, XMM11, XMM12, XMM13, XMM14, XMM15 ->
				false;
			case XMM16,
					XMM17,
					XMM18,
					XMM19,
					XMM20,
					XMM21,
					XMM22,
					XMM23,
					XMM24,
					XMM25,
					XMM26,
					XMM27,
					XMM28,
					XMM29,
					XMM30,
					XMM31 -> true;
		};
	}

	@Override
	public int bits() {
		return 128;
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "RegisterXMM(mnemonic=" + mnemonic + ")";
	}
}
