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

/** An x86 512-bit general-purpose register. */
public enum RegisterZMM implements Register {

	/** The register ZMM0. */
	ZMM0("zmm0"),

	/** The register ZMM1. */
	ZMM1("zmm1"),

	/** The register ZMM2. */
	ZMM2("zmm2"),

	/** The register ZMM3. */
	ZMM3("zmm3"),

	/** The register ZMM4. */
	ZMM4("zmm4"),

	/** The register ZMM5. */
	ZMM5("zmm5"),

	/** The register ZMM6. */
	ZMM6("zmm6"),

	/** The register ZMM7. */
	ZMM7("zmm7"),

	/** The register ZMM8. */
	ZMM8("zmm8"),

	/** The register ZMM9. */
	ZMM9("zmm9"),

	/** The register ZMM10. */
	ZMM10("zmm10"),

	/** The register ZMM11. */
	ZMM11("zmm11"),

	/** The register ZMM12. */
	ZMM12("zmm12"),

	/** The register ZMM13. */
	ZMM13("zmm13"),

	/** The register ZMM14. */
	ZMM14("zmm14"),

	/** The register ZMM15. */
	ZMM15("zmm15");

	private final String mnemonic;

	RegisterZMM(final String mnemonic) {
		this.mnemonic = Objects.requireNonNull(mnemonic);
	}

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
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static byte toByte(final RegisterZMM r) {
		return switch (r) {
			case ZMM0, ZMM8 -> (byte) 0x00;
			case ZMM1, ZMM9 -> (byte) 0x01;
			case ZMM2, ZMM10 -> (byte) 0x02;
			case ZMM3, ZMM11 -> (byte) 0x03;
			case ZMM4, ZMM12 -> (byte) 0x04;
			case ZMM5, ZMM13 -> (byte) 0x05;
			case ZMM6, ZMM14 -> (byte) 0x06;
			case ZMM7, ZMM15 -> (byte) 0x07;
		};
	}

	// Check https://github.com/pmd/pmd/issues/5568
	@SuppressWarnings("PMD.NPathComplexity")
	public static boolean requiresExtension(final RegisterZMM r) {
		return switch (r) {
			case ZMM0, ZMM1, ZMM2, ZMM3, ZMM4, ZMM5, ZMM6, ZMM7 -> false;
			case ZMM8, ZMM9, ZMM10, ZMM11, ZMM12, ZMM13, ZMM14, ZMM15 -> true;
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
