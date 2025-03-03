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
import com.ledmington.utils.HashUtils;

import java.util.Objects;

/**
 * This class maps the following cases:
 *
 * <p>[index]
 *
 * <p>[index + displacement]
 *
 * <p>[index * scale]
 *
 * <p>[index * scale + displacement]
 *
 * <p>[displacement]
 *
 * <p>[base + index * scale]
 *
 * <p>[base + index * scale + displacement]
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
		this.ptrSize = Objects.requireNonNull(ptrSize);
	}

	/**
	 * Returns the base register of this indirect operand.
	 *
	 * @return The base register.
	 */
	public Register getBase() {
		Objects.requireNonNull(this.base, "No base register.");
		return base;
	}

	public boolean hasBase() {
		return base != null;
	}

	/**
	 * Returns the index register of this indirect operand.
	 *
	 * @return The index register.
	 */
	public Register getIndex() {
		Objects.requireNonNull(this.index, "No index register.");
		return index;
	}

	public boolean hasIndex() {
		return index != null;
	}

	/**
	 * Returns the scale to multiply the index register.
	 *
	 * @return The scale of this indirect operand.
	 */
	public int getScale() {
		return scale;
	}

	public boolean hasScale() {
		return scale != 1;
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
			case INT -> 32;
		};
	}

	public boolean hasDisplacement() {
		return displacement != 0L;
	}

	@Override
	public int bits() {
		return ptrSize.bits();
	}

	private boolean isDisplacementNegative() {
		return switch (displacementType) {
			case DisplacementType.BYTE -> BitUtils.asByte(displacement) < (byte) 0;
			case DisplacementType.INT -> BitUtils.asInt(displacement) < 0;
		};
	}

	private void addDisplacementSign(final StringBuilder sb) {
		sb.append(isDisplacementNegative() ? '-' : '+');
	}

	private void addDisplacement(final StringBuilder sb) {
		switch (displacementType) {
			case DisplacementType.BYTE -> {
				final byte x = BitUtils.asByte(displacement);
				sb.append(String.format("0x%x", isDisplacementNegative() ? -x : x));
			}
			case DisplacementType.INT -> {
				final int x = BitUtils.asInt(displacement);
				sb.append(String.format("0x%x", isDisplacementNegative() ? -x : x));
			}
		}
	}

	/**
	 * This is used to distinguish instructions like LEA which have an indirect operand but don't have the pointer size.
	 *
	 * @param addPointerSize If true, adds the pointer size.
	 * @return The assembly representation of this instruction in Intel syntax.
	 */
	public String toIntelSyntax(final boolean addPointerSize) {
		final StringBuilder sb = new StringBuilder();
		if (addPointerSize) {
			sb.append(ptrSize.name().replace('_', ' ')).append(' ');
		}
		if (index instanceof SegmentRegister sr) {
			sb.append(sr.segment().toIntelSyntax()).append(':');
		}
		sb.append('[');
		if (!hasBase() && !hasIndex()) {
			addDisplacement(sb);
		} else if (!hasBase()) {
			sb.append(index.toIntelSyntax());
			if (hasScale()) {
				sb.append('*').append(scale);
			}
			if (hasDisplacement() || hasScale()) {
				addDisplacementSign(sb);
				addDisplacement(sb);
			}
		} else if (hasIndex()) {
			sb.append(base.toIntelSyntax())
					.append('+')
					.append(index.toIntelSyntax())
					.append('*')
					.append(scale);
			addDisplacementSign(sb);
			addDisplacement(sb);
		} else {
			throw new IllegalStateException("Unreachable.");
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
		return "IndirectOperand(base="
				+ (base == null ? "null" : base.toString())
				+ ";index="
				+ index.toString() + ";scale="
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
