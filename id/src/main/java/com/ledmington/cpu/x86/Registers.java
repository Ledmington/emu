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

import com.ledmington.utils.BitUtils;

/** A collection of utility static methods for parsing register codes. */
public final class Registers {

	/**
	 * Performs a bitwise OR with the given byte and a byte with the given value in the third bit.
	 *
	 * @param b The value of the third bit.
	 * @param x The byte to be ORed.
	 * @return xxxxxxxx OR 0000b000.
	 */
	public static byte combine(final boolean b, final byte x) {
		return b ? BitUtils.or(x, (byte) 0b00001000) : x;
	}

	/**
	 * Returns the proper general-purpose register by choosing which type/size based on the given arguments.
	 *
	 * @param registerCode The "base" code of the register.
	 * @param isOperand64Bit If not 16 bits, selects a 64-bit register if true, a 32-bit register otherwise.
	 * @param extension A single bit extension for the "base" code of the register.
	 * @param hasOperandSizeOverridePrefix If true, selects a 16-bit register, otherwise the result depends on
	 *     isOperand64Bit.
	 * @return The selected register corresponding to the arguments passed.
	 */
	public static Register fromCode(
			final byte registerCode,
			final boolean isOperand64Bit,
			final boolean extension,
			final boolean hasOperandSizeOverridePrefix) {
		return hasOperandSizeOverridePrefix
				? Register16.fromByte(combine(extension, registerCode))
				: isOperand64Bit
						? Register64.fromByte(combine(extension, registerCode))
						: Register32.fromByte(combine(extension, registerCode));
	}

	public static byte toByte(final Register r) {
		return switch (r) {
			case Register8 r8 -> Register8.toByte(r8);
			case Register16 r16 -> Register16.toByte(r16);
			case Register32 r32 -> Register32.toByte(r32);
			case Register64 r64 -> Register64.toByte(r64);
			case RegisterMMX rmm -> RegisterMMX.toByte(rmm);
			case RegisterXMM rxmm -> RegisterXMM.toByte(rxmm);
			case RegisterYMM rymm -> RegisterYMM.toByte(rymm);
			case RegisterZMM rzmm -> RegisterZMM.toByte(rzmm);
			case MaskRegister rk -> MaskRegister.toByte(rk);
			case SegmentRegister sr -> SegmentRegister.toByte(sr);
			default -> throw new IllegalArgumentException(String.format("Unknown register '%s'.", r));
		};
	}

	public static boolean requiresExtension(final Register r) {
		return switch (r) {
			case Register8 r8 -> Register8.requiresExtension(r8);
			case Register16 r16 -> Register16.requiresExtension(r16);
			case Register32 r32 -> Register32.requiresExtension(r32);
			case Register64 r64 -> Register64.requiresExtension(r64);
			case RegisterMMX ignored -> false;
			case RegisterXMM rxmm -> RegisterXMM.requiresExtension(rxmm);
			case RegisterYMM rymm -> RegisterYMM.requiresExtension(rymm);
			case RegisterZMM rzmm -> RegisterZMM.requiresExtension(rzmm);
			case MaskRegister ignored -> false;
			case SegmentRegister ignored -> false;
			default -> throw new IllegalArgumentException(String.format("Unknown register '%s'.", r));
		};
	}

	public static boolean requiresEvexExtension(final Register r) {
		return switch (r) {
			case Register8 ignored -> false;
			case Register16 ignored -> false;
			case Register32 ignored -> false;
			case Register64 ignored -> false;
			case RegisterMMX ignored -> false;
			case MaskRegister ignored -> false;
			case RegisterXMM rxmm -> RegisterXMM.requiresEvexExtension(rxmm);
			case RegisterYMM rymm -> RegisterYMM.requiresEvexExtension(rymm);
			case RegisterZMM rzmm -> RegisterZMM.requiresEvexExtension(rzmm);
			default -> throw new IllegalArgumentException(String.format("Unknown register '%s'.", r));
		};
	}

	private Registers() {}
}
