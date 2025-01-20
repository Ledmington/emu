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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;

/**
 * This class maps the following cases:
 *
 * <p>[reg2]
 *
 * <p>[reg2 + displacement]
 *
 * <p>[reg2 * constant]
 *
 * <p>[reg2 * constant + displacement]
 *
 * <p>[displacement]
 *
 * <p>[reg1 + reg2 * constant]
 *
 * <p>[reg1 + reg2 * constant + displacement]
 */
public final class IndirectOperand implements Operand {

	private final Register reg1;
	private final int constant;
	private final Register reg2;
	private final long displacement;
	private final DisplacementType displacementType;
	private final PointerSize ptrSize;

	/**
	 * Returns a fresh instance of IndirectOperandBuilder.
	 *
	 * @return A new IndirectOperandBuilder.
	 */
	public static IndirectOperandBuilder builder() {
		return new IndirectOperandBuilder();
	}

	/* default */ IndirectOperand(
			final Register reg1,
			final Register reg2,
			final int constant,
			final long displacement,
			final DisplacementType displacementType,
			final PointerSize ptrSize) {
		this.reg1 = reg1;
		this.constant = constant;
		this.reg2 = reg2;
		this.displacement = displacement;
		this.displacementType = Objects.requireNonNull(displacementType);
		this.ptrSize = ptrSize;
	}

	/**
	 * Returns the base register of this indirect operand.
	 *
	 * @return The base register.
	 */
	public Register base() {
		return reg1;
	}

	/**
	 * Returns the index register of this indirect operand.
	 *
	 * @return The index register.
	 */
	public Register index() {
		return reg2;
	}

	/**
	 * Returns the constant to multiply the index register.
	 *
	 * @return The constant of this indirect operand.
	 */
	public long scale() {
		return constant;
	}

	/**
	 * Returns the displacement of this indirect operand. In case it doesn't appear in the asm, it is 0.
	 *
	 * @return The displacement of this indirect operand.
	 */
	public long getDisplacement() {
		return displacement;
	}

	@Override
	public int bits() {
		if (hasExplicitPtrSize()) {
			return ptrSize.getSize();
		}
		return reg2.bits();
	}

	/**
	 * Checks whether this indirect operand has been built with an explicit pointer size or it has been inferred from
	 * its arguments.
	 *
	 * @return True if the pointer size was made explicit, false otherwise.
	 */
	public boolean hasExplicitPtrSize() {
		return this.ptrSize != null;
	}

	/**
	 * Returns the explicit pointer size of this indirect operand, in case it has been specified.
	 *
	 * @return The pointer size, if it was specified, 0 otherwise.
	 */
	public int explicitPtrSize() {
		return hasExplicitPtrSize() ? ptrSize.getSize() : 0;
	}

	@Override
	public String toIntelSyntax() {
		final StringBuilder sb = new StringBuilder();
		boolean shouldAddSign = false;
		if (reg2 instanceof SegmentRegister sr) {
			sb.append(sr.segment().toIntelSyntax()).append(':');
		}
		sb.append('[');
		if (reg1 != null) {
			sb.append(reg1.toIntelSyntax());
			if (reg2 != null) {
				sb.append('+');
			}
			shouldAddSign = true;
		}
		if (reg2 != null) {
			sb.append(reg2.toIntelSyntax());
			shouldAddSign = true;
		}
		final int uselessConstant = 1;
		if (constant != uselessConstant) {
			sb.append('*').append(constant);
			shouldAddSign = true;
		}
		final long defaultDisplacement = 0L;
		if (displacement != defaultDisplacement) {
			long d = displacement;
			if (displacement < 0) {
				d = switch (displacementType) {
					case BYTE -> (~BitUtils.asByte(d)) + 1;
					case SHORT -> (~BitUtils.asShort(d)) + 1;
					case INT -> (~BitUtils.asInt(d)) + 1;
					case LONG -> (~d) + 1;
				};
			}
			if (shouldAddSign) {
				sb.append((displacement < 0) ? '-' : '+');
			}
			sb.append(String.format("0x%x", d));
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String toString() {
		return "IndirectOperand(reg1="
				+ (reg1 == null ? "null" : reg1.toString())
				+ ";reg2="
				+ reg2.toString() + ";constant="
				+ constant + ";displacement="
				+ displacement + ";displacementType=" + displacementType
				+ ";ptrSize=" + ptrSize
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + reg1.hashCode();
		h = 31 * h + constant;
		h = 31 * h + reg2.hashCode();
		h = 31 * h + HashUtils.hash(displacement);
		h = 31 * h + displacementType.hashCode();
		h = 31 * h + ptrSize.hashCode();
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final IndirectOperand io = (IndirectOperand) other;
		return this.reg1.equals(io.reg1)
				&& this.constant == io.constant
				&& this.reg2.equals(io.reg2)
				&& this.displacement == io.displacement
				&& this.displacementType.equals(io.displacementType)
				&& this.ptrSize.equals(io.ptrSize);
	}
}
