/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

/** A register of the x87 FPU register stack. */
public enum RegisterFPU implements Register {

	/**
	 * The implicit top-of-stack operand, displayed as "st" (without an index) in the first operand position of
	 * two-operand x87 instructions.
	 */
	ST("st"),

	/** The FPU stack register st(0). */
	ST0("st(0)"),

	/** The FPU stack register st(1). */
	ST1("st(1)"),

	/** The FPU stack register st(2). */
	ST2("st(2)"),

	/** The FPU stack register st(3). */
	ST3("st(3)"),

	/** The FPU stack register st(4). */
	ST4("st(4)"),

	/** The FPU stack register st(5). */
	ST5("st(5)"),

	/** The FPU stack register st(6). */
	ST6("st(6)"),

	/** The FPU stack register st(7). */
	ST7("st(7)");

	private final String mnemonic;

	RegisterFPU(final String mnemonic) {
		this.mnemonic = mnemonic;
	}

	@Override
	public int bits() {
		throw new UnsupportedOperationException("This is an FPU stack register.");
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "RegisterFPU(mnemonic=" + mnemonic + ")";
	}

	/**
	 * Returns the FPU stack register st(i) corresponding to the given index.
	 *
	 * @param b The 3-bit index of the FPU stack register.
	 * @return The corresponding RegisterFPU.
	 */
	public static RegisterFPU fromByte(final byte b) {
		return switch (b) {
			case 0x00 -> ST0;
			case 0x01 -> ST1;
			case 0x02 -> ST2;
			case 0x03 -> ST3;
			case 0x04 -> ST4;
			case 0x05 -> ST5;
			case 0x06 -> ST6;
			case 0x07 -> ST7;
			default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x.", b));
		};
	}

	/**
	 * Returns the 3-bit index of the given FPU stack register st(i).
	 *
	 * @param r The FPU stack register to convert. Must not be {@link #ST}.
	 * @return The byte value representing the index of the given FPU stack register.
	 */
	public static byte toByte(final RegisterFPU r) {
		return switch (r) {
			case ST0 -> (byte) 0x00;
			case ST1 -> (byte) 0x01;
			case ST2 -> (byte) 0x02;
			case ST3 -> (byte) 0x03;
			case ST4 -> (byte) 0x04;
			case ST5 -> (byte) 0x05;
			case ST6 -> (byte) 0x06;
			case ST7 -> (byte) 0x07;
			case ST -> throw new IllegalArgumentException("The implicit top-of-stack register has no index.");
		};
	}
}
