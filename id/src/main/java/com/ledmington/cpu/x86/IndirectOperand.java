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

	private final Register base;
	private final int scale;
	private final Register index;
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
			final Register base,
			final Register index,
			final int scale,
			final long displacement,
			final DisplacementType displacementType,
			final PointerSize ptrSize) {
		this.base = base;
		this.scale = scale;
		this.index = index;
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
		return base;
	}

	/**
	 * Returns the index register of this indirect operand.
	 *
	 * @return The index register.
	 */
	public Register index() {
		return index;
	}

	/**
	 * Returns the constant to multiply the index register.
	 *
	 * @return The constant of this indirect operand.
	 */
	public long scale() {
		return scale;
	}

	/**
	 * Returns the displacement of this indirect operand. In case it doesn't appear in the asm, it is 0.
	 *
	 * @return The displacement of this indirect operand.
	 */
	public long getDisplacement() {
		return displacement;
	}

	public int getDisplacementBits() {
		return switch (displacementType) {
			case BYTE -> 8;
			case SHORT -> 16;
			case INT -> 32;
			case LONG -> 64;
		};
	}

	@Override
	public int bits() {
		return ptrSize.bits();
	}

	/**
	 * This is used to distinguish instructions like LEA which have an indirect operand but don't have the pointer size.
	 *
	 * @param addPointerSize If true, adds the pointer size.
	 * @return The assembly representation of this instruction in Intel syntax.
	 */
	public String toIntelSyntax(final boolean addPointerSize) {
		final StringBuilder sb = new StringBuilder();
		boolean shouldAddSign = false;
		if (addPointerSize) {
			sb.append(ptrSize.name().replace('_', ' ')).append(' ');
		}
		if (index instanceof SegmentRegister sr) {
			sb.append(sr.segment().toIntelSyntax()).append(':');
		}
		sb.append('[');
		if (base != null) {
			sb.append(base.toIntelSyntax());
			if (index != null) {
				sb.append('+');
			}
			shouldAddSign = true;
		}
		if (index != null) {
			sb.append(index.toIntelSyntax());
			shouldAddSign = true;
		}
		final int uselessConstant = 1;
		if (scale != uselessConstant) {
			sb.append('*').append(scale);
			shouldAddSign = true;
		}
		final long defaultDisplacement = 0L;
		if (displacement != defaultDisplacement || (base != null || scale != uselessConstant)) {
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
	public String toIntelSyntax() {
		return toIntelSyntax(true);
	}

	@Override
	public String toString() {
		return "IndirectOperand(reg1="
				+ (base == null ? "null" : base.toString())
				+ ";reg2="
				+ index.toString() + ";constant="
				+ scale + ";displacement="
				+ displacement + ";displacementType=" + displacementType
				+ ";ptrSize=" + ptrSize
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + base.hashCode();
		h = 31 * h + scale;
		h = 31 * h + index.hashCode();
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
		return this.base.equals(io.base)
				&& this.scale == io.scale
				&& this.index.equals(io.index)
				&& this.displacement == io.displacement
				&& this.displacementType.equals(io.displacementType)
				&& this.ptrSize.equals(io.ptrSize);
	}
}
