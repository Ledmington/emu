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
 * <p>[base]
 *
 * <p>[base + displacement]
 *
 * <p>[displacement]
 *
 * <p>[base + index * scale]
 *
 * <p>[base + index * scale + displacement]
 *
 * <p>[index * scale + displacement]
 */
public final class IndirectOperand implements Operand {

	private final PointerSize ptrSize;
	private final Register base;
	private final Register index;
	private final Integer scale;
	private final Long displacement;
	private final DisplacementType displacementType;

	/**
	 * Returns a fresh instance of IndirectOperandBuilder.
	 *
	 * @return A new IndirectOperandBuilder.
	 */
	public static IndirectOperandBuilder builder() {
		return new IndirectOperandBuilder();
	}

	private static boolean isValidRegister(final Register r) {
		return switch (r) {
			case Register32.EAX,
					Register32.EBX,
					Register32.ECX,
					Register32.EDX,
					Register32.ESI,
					Register32.EDI,
					Register32.EBP,
					Register32.ESP,
					Register32.R8D,
					Register32.R9D,
					Register32.R10D,
					Register32.R11D,
					Register32.R12D,
					Register32.R13D,
					Register32.R14D,
					Register32.R15D,
					Register32.EIP,
					Register64.RAX,
					Register64.RBX,
					Register64.RCX,
					Register64.RDX,
					Register64.RSI,
					Register64.RDI,
					Register64.RBP,
					Register64.RSP,
					Register64.R8,
					Register64.R9,
					Register64.R10,
					Register64.R11,
					Register64.R12,
					Register64.R13,
					Register64.R14,
					Register64.R15,
					Register64.RIP -> true;
			default -> false;
		};
	}

	/* default */ IndirectOperand(
			final PointerSize ptrSize,
			final Register base,
			final Register index,
			final Integer scale,
			final Long displacement,
			final DisplacementType displacementType) {
		Objects.requireNonNull(ptrSize, "Cannot build an IndirectOperand without an explicit pointer size.");

		final boolean hasBase = base != null;
		final boolean hasIndex = index != null;
		final boolean hasScale = scale != null;
		final boolean hasDisplacement = displacement != null && displacementType != null;

		if (displacement != null && displacementType == null) {
			throw new IllegalArgumentException("Cannot have displacement with no type.");
		}

		// [base]
		final boolean isB = hasBase && !hasIndex && !hasScale && !hasDisplacement;
		// [base+displacement]
		final boolean isBD = hasBase && !hasIndex && !hasScale && hasDisplacement;
		// [displacement]
		final boolean isD = !hasBase && !hasIndex && !hasScale && hasDisplacement;
		// [base+index*scale]
		final boolean isBIS = hasBase && hasIndex && hasScale && !hasDisplacement;
		// [base+index*scale+displacement]
		final boolean isBISD = hasBase && hasIndex && hasScale && hasDisplacement;
		// [index*scale+displacement]
		final boolean isISD = !hasBase && hasIndex && hasScale && hasDisplacement;

		if (!isB && !isBD && !isD && !isBIS && !isBISD && !isISD) {
			throw new IllegalArgumentException(String.format(
					"Invalid argument combination: cannot build an IndirectOperand with %s, %s, %s and %s.",
					(base == null) ? "no base" : "base=" + base,
					(index == null) ? "no index" : "index=" + index,
					(scale == null) ? "no scale" : "scale=" + scale,
					(displacement == null) ? "no displacement" : "displacement=" + displacement));
		}

		if (!(base == null
				|| isValidRegister(base)
				|| (base instanceof final SegmentRegister sr && isValidRegister(sr.register())))) {
			throw new IllegalArgumentException(String.format("%s is not a valid base register.", base));
		}
		if (index != null && (!isValidRegister(index) || index == Register32.ESP || index == Register64.RSP)) {
			throw new IllegalArgumentException(String.format("%s is not a valid index register.", index));
		}
		if (hasScale && scale != 1 && scale != 2 && scale != 4 && scale != 8) {
			throw new IllegalArgumentException(String.format("%d is not a valid scale value.", scale));
		}
		if (!isB && !isBD && base == Register64.RIP) {
			throw new IllegalArgumentException("rip can only be used as base register.");
		}

		this.ptrSize = ptrSize;
		this.base = base;
		this.index = index;
		this.scale = scale;
		this.displacement = displacement;
		this.displacementType = displacementType;
	}

	public PointerSize getPointerSize() {
		return ptrSize;
	}

	/**
	 * Returns the base register of this indirect operand.
	 *
	 * @return The base register.
	 */
	public Register getBase() {
		Objects.requireNonNull(this.base, "No base register.");
		return (base instanceof final SegmentRegister sr) ? sr.register() : base;
	}

	public boolean hasBase() {
		return base != null;
	}

	public boolean hasSegment() {
		return hasBase() && base instanceof SegmentRegister;
	}

	public Register16 getSegment() {
		Objects.requireNonNull(this.base, "No base register.");
		if (!hasSegment()) {
			throw new IllegalStateException("No segment register.");
		}
		return ((SegmentRegister) base).segment();
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

	public boolean hasScale() {
		return scale != null;
	}

	/**
	 * Returns the scale to multiply the index register.
	 *
	 * @return The scale of this indirect operand.
	 */
	public int getScale() {
		return scale;
	}

	public boolean hasDisplacement() {
		return displacement != null;
	}

	/**
	 * Returns the displacement of this indirect operand. In case it doesn't appear in the asm, it is 0.
	 *
	 * @return The displacement of this indirect operand.
	 */
	public long getDisplacement() {
		return displacement;
	}

	public DisplacementType getDisplacementType() {
		Objects.requireNonNull(displacementType, "No displacement.");
		return displacementType;
	}

	@Override
	public int bits() {
		return ptrSize.bits();
	}

	private boolean isDisplacementNegative() {
		return switch (displacementType) {
			case DisplacementType.SHORT -> BitUtils.asByte(displacement) < (byte) 0;
			case DisplacementType.LONG -> BitUtils.asInt(displacement) < 0;
		};
	}

	private void addDisplacementSign(final StringBuilder sb) {
		sb.append(isDisplacementNegative() ? '-' : '+');
	}

	private void addDisplacement(final StringBuilder sb) {
		switch (displacementType) {
			case DisplacementType.SHORT -> {
				final byte x = BitUtils.asByte(displacement);
				sb.append(String.format("0x%x", isDisplacementNegative() ? -x : x));
			}
			case DisplacementType.LONG -> {
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
		if (base instanceof final SegmentRegister sr) {
			sb.append(sr.segment().toIntelSyntax()).append(':');
		}
		sb.append('[');
		if (hasBase()) {
			sb.append(base.toIntelSyntax());
		}
		if (hasIndex()) {
			if (hasBase()) {
				sb.append('+');
			}
			sb.append(index.toIntelSyntax());
			if (hasScale()) {
				sb.append('*').append(scale);
			}
		}
		if (hasDisplacement()) {
			addDisplacementSign(sb);
			addDisplacement(sb);
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
		return "IndirectOperand(ptrSize=" + ptrSize + ";base="
				+ (base == null ? "null" : base.toString())
				+ ";index="
				+ (index == null ? "null" : index.toString()) + ";scale="
				+ scale + ";displacement="
				+ displacement + ";displacementType=" + displacementType
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + ptrSize.hashCode();
		h = 31 * h + (base == null ? 0 : base.hashCode());
		h = 31 * h + (index == null ? 0 : index.hashCode());
		h = 31 * h + (scale == null ? 0 : scale.hashCode());
		h = 31 * h + (displacement == null ? 0 : HashUtils.hash(displacement));
		h = 31 * h + (displacementType == null ? 0 : displacementType.hashCode());
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
		if (!(other instanceof final IndirectOperand io)) {
			return false;
		}
		return this.ptrSize.equals(io.ptrSize)
				&& Objects.equals(this.base, io.base)
				&& Objects.equals(this.scale, io.scale)
				&& Objects.equals(this.index, io.index)
				&& Objects.equals(this.displacement, io.displacement)
				&& Objects.equals(this.displacementType, io.displacementType);
	}
}
