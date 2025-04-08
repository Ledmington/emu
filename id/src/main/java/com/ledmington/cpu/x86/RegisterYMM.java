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

/** An x86 256-bit general-purpose register. */
public enum RegisterYMM implements Register {

	/** The register YMM0. */
	YMM0("ymm0"),

	/** The register YMM1. */
	YMM1("ymm1"),

	/** The register YMM2. */
	YMM2("ymm2"),

	/** The register YMM3. */
	YMM3("ymm3"),

	/** The register YMM4. */
	YMM4("ymm4"),

	/** The register YMM5. */
	YMM5("ymm5"),

	/** The register YMM6. */
	YMM6("ymm6"),

	/** The register YMM7. */
	YMM7("ymm7"),

	/** The register YMM8. */
	YMM8("ymm8"),

	/** The register YMM9. */
	YMM9("ymm9"),

	/** The register YMM10. */
	YMM10("ymm10"),

	/** The register YMM11. */
	YMM11("ymm11"),

	/** The register YMM12. */
	YMM12("ymm12"),

	/** The register YMM13. */
	YMM13("ymm13"),

	/** The register YMM14. */
	YMM14("ymm14"),

	/** The register YMM15. */
	YMM15("ymm15");

	private final String mnemonic;

	RegisterYMM(final String mnemonic) {
		this.mnemonic = Objects.requireNonNull(mnemonic);
	}

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
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static byte toByte(final RegisterYMM r) {
		return switch (r) {
			case YMM0, YMM8 -> (byte) 0x00;
			case YMM1, YMM9 -> (byte) 0x01;
			case YMM2, YMM10 -> (byte) 0x02;
			case YMM3, YMM11 -> (byte) 0x03;
			case YMM4, YMM12 -> (byte) 0x04;
			case YMM5, YMM13 -> (byte) 0x05;
			case YMM6, YMM14 -> (byte) 0x06;
			case YMM7, YMM15 -> (byte) 0x07;
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static boolean requiresExtension(final RegisterYMM r) {
		return switch (r) {
			case YMM0, YMM1, YMM2, YMM3, YMM4, YMM5, YMM6, YMM7 -> false;
			case YMM8, YMM9, YMM10, YMM11, YMM12, YMM13, YMM14, YMM15 -> true;
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
