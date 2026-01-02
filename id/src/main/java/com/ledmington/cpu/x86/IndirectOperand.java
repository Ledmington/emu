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

import java.util.Objects;
import java.util.Optional;

import com.ledmington.utils.BitUtils;

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
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class IndirectOperand implements Operand {

	private final PointerSize ptrSize;
	private final SegmentRegister segment;
	private final Register base;
	private final Register index;
	private final Integer scale;
	private final Integer displacement;
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
		return r instanceof Register32 || r instanceof Register64;
	}

	@SuppressWarnings("PMD.NPathComplexity")
	/* default */ IndirectOperand(
			final PointerSize ptrSize,
			final SegmentRegister segment,
			final Register base,
			final Register index,
			final Integer scale,
			final Integer displacement,
			final DisplacementType displacementType) {
		Objects.requireNonNull(ptrSize, "Cannot build an IndirectOperand without an explicit pointer size.");

		if (displacement != null && displacementType == null) {
			throw new IllegalArgumentException("Cannot have displacement with no type.");
		}

		final boolean hasBase = base != null;
		final boolean hasIndex = index != null;
		final boolean hasScale = scale != null;
		final boolean hasDisplacement = displacement != null;

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

		if (!(base == null || isValidRegister(base))) {
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
		this.segment = segment;
		this.base = base;
		this.index = index;
		this.scale = scale;
		this.displacement = displacement;
		this.displacementType = displacementType;
	}

	/**
	 * Returns the size of the pointer of this indirect operand.
	 *
	 * @return The size of the pointer.
	 */
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
		return base;
	}

	/**
	 * Returns true if this indirect operand has a base operand.
	 *
	 * @return True if this indirect operand has a base, false otherwise.
	 */
	public boolean hasBase() {
		return base != null;
	}

	/**
	 * Returns true if this indirect operand has a segment register.
	 *
	 * @return True if this indirect operand has a segment register, false otherwise.
	 */
	public boolean hasSegment() {
		return segment != null;
	}

	/**
	 * Returns the segment register of this indirect operand.
	 *
	 * @return The segment register.
	 */
	public SegmentRegister getSegment() {
		Objects.requireNonNull(this.segment, "No segment.");
		return segment;
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

	/**
	 * Returns true if this indirect operand has an index register.
	 *
	 * @return True if this indirect operand has an index register, false otherwise.
	 */
	public boolean hasIndex() {
		return index != null;
	}

	/**
	 * Returns true if this indirect operand has a scale.
	 *
	 * @return True if this indirect operand has a scale, false otherwise.
	 */
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

	/**
	 * Returns true if this indirect operand has a displacement.
	 *
	 * @return True if this indirect operand has a displacement, false otherwise.
	 */
	public boolean hasDisplacement() {
		return displacement != null;
	}

	/**
	 * Returns the displacement of this indirect operand. In case it doesn't appear in the asm, it is 0.
	 *
	 * @return The displacement of this indirect operand.
	 */
	public int getDisplacement() {
		return displacement;
	}

	/**
	 * Returns the type of displacement.
	 *
	 * @return The type of displacement.
	 */
	public DisplacementType getDisplacementType() {
		if (!hasDisplacement()) {
			throw new IllegalArgumentException("No displacement.");
		}
		return displacementType;
	}

	@Override
	public int bits() {
		return ptrSize.bits();
	}

	private boolean isDisplacementNegative() {
		return displacement < 0;
	}

	private void addDisplacementSign(final StringBuilder sb) {
		sb.append(isDisplacementNegative() ? '-' : '+');
	}

	private void addDisplacement(
			final StringBuilder sb, final Optional<Integer> compressedDisplacement, final boolean shortHex) {
		switch (displacementType) {
			case DisplacementType.SHORT -> {
				final String fmt = shortHex ? "0x%x" : "0x%02x";
				if (compressedDisplacement.isEmpty()) {
					final byte x = BitUtils.asByte(displacement);
					sb.append(String.format(fmt, isDisplacementNegative() ? -x : x));
				} else {
					final int x = displacement * compressedDisplacement.orElseThrow();
					sb.append(String.format(fmt, isDisplacementNegative() ? -x : x));
				}
			}
			case DisplacementType.LONG -> {
				final String fmt = shortHex ? "0x%x" : "0x%08x";
				final int x = BitUtils.asInt(displacement);
				sb.append(String.format(fmt, isDisplacementNegative() ? -x : x));
			}
		}
	}

	/**
	 * This is used to distinguish instructions like LEA which have an indirect operand but don't have the pointer size.
	 *
	 * @param addPointerSize If true, adds the pointer size.
	 * @param compressedDisplacement The optional implicit displacement.
	 * @param shortHex WHen enabled, does not add leading zeroes in the displacement.
	 * @return The assembly representation of this instruction in Intel syntax.
	 */
	public String toIntelSyntax(
			final boolean addPointerSize, final Optional<Integer> compressedDisplacement, final boolean shortHex) {
		final StringBuilder sb = new StringBuilder();
		if (addPointerSize) {
			sb.append(ptrSize.name().replace('_', ' ')).append(' ');
		}
		if (hasSegment()) {
			sb.append(segment.toIntelSyntax()).append(':');
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
			addDisplacement(sb, compressedDisplacement, shortHex);
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String toIntelSyntax() {
		return toIntelSyntax(true, Optional.empty(), false);
	}

	@Override
	public String toString() {
		return "IndirectOperand(ptrSize=" + ptrSize + ";segment="
				+ segment + ";base="
				+ (base == null ? "null" : base.toString())
				+ ";index="
				+ (index == null ? "null" : index.toString()) + ";scale="
				+ scale + ";displacement="
				+ (displacement == null ? "null" : String.format("0x%x", displacement)) + ";displacementType="
				+ displacementType
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + ptrSize.hashCode();
		h = 31 * h + (segment == null ? 0 : segment.hashCode());
		h = 31 * h + (base == null ? 0 : base.hashCode());
		h = 31 * h + (index == null ? 0 : index.hashCode());
		h = 31 * h + (scale == null ? 0 : scale.hashCode());
		h = 31 * h + (displacement == null ? 0 : displacement);
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
		return this.ptrSize == io.ptrSize
				&& Objects.equals(this.segment, io.segment)
				&& Objects.equals(this.base, io.base)
				&& Objects.equals(this.scale, io.scale)
				&& Objects.equals(this.index, io.index)
				&& Objects.equals(this.displacement, io.displacement)
				&& Objects.equals(this.displacementType, io.displacementType);
	}
}
