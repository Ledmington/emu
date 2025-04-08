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

/** An x86 128-bit general-purpose register. */
public enum RegisterXMM implements Register {

	/** The register XMM0. */
	XMM0("xmm0"),

	/** The register XMM1. */
	XMM1("xmm1"),

	/** The register XMM2. */
	XMM2("xmm2"),

	/** The register XMM3. */
	XMM3("xmm3"),

	/** The register XMM4. */
	XMM4("xmm4"),

	/** The register XMM5. */
	XMM5("xmm5"),

	/** The register XMM6. */
	XMM6("xmm6"),

	/** The register XMM7. */
	XMM7("xmm7"),

	/** The register XMM8. */
	XMM8("xmm8"),

	/** The register XMM9. */
	XMM9("xmm9"),

	/** The register XMM10. */
	XMM10("xmm10"),

	/** The register XMM11. */
	XMM11("xmm11"),

	/** The register XMM12. */
	XMM12("xmm12"),

	/** The register XMM13. */
	XMM13("xmm13"),

	/** The register XMM14. */
	XMM14("xmm14"),

	/** The register XMM15. */
	XMM15("xmm15");

	private final String mnemonic;

	RegisterXMM(final String mnemonic) {
		this.mnemonic = Objects.requireNonNull(mnemonic);
	}

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
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static byte toByte(final RegisterXMM r) {
		return switch (r) {
			case XMM0, XMM8 -> (byte) 0x00;
			case XMM1, XMM9 -> (byte) 0x01;
			case XMM2, XMM10 -> (byte) 0x02;
			case XMM3, XMM11 -> (byte) 0x03;
			case XMM4, XMM12 -> (byte) 0x04;
			case XMM5, XMM13 -> (byte) 0x05;
			case XMM6, XMM14 -> (byte) 0x06;
			case XMM7, XMM15 -> (byte) 0x07;
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static boolean requiresExtension(final RegisterXMM r) {
		return switch (r) {
			case XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7 -> false;
			case XMM8, XMM9, XMM10, XMM11, XMM12, XMM13, XMM14, XMM15 -> true;
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
