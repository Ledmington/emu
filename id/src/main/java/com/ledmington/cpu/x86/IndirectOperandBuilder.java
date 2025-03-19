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

/** This class allows an easier construction of an IndirectOperand object. */
public final class IndirectOperandBuilder {

	private PointerSize ptrSize = null;
	private Register baseRegister = null;
	private Register indexRegister = null;
	private Integer scale = null;
	private Long displacement = null;
	private DisplacementType displacementType = null;
	private boolean alreadyBuilt = false;

	/**
	 * Creates an IndirectOperandBuilder with the default values. It is important to note that the default set of values
	 * does not lead to a valid IndirectOperand instance.
	 */
	public IndirectOperandBuilder() {}

	/**
	 * Sets the base register for the IndirectOperand.
	 *
	 * @param r The new base register.
	 * @return This instance of IndirectOperandBuilder.
	 */
	public IndirectOperandBuilder base(final Register r) {
		if (this.baseRegister != null) {
			throw new IllegalArgumentException("Cannot define base register twice.");
		}
		Objects.requireNonNull(r);
		if (!(r instanceof Register32) && !(r instanceof Register64) && !(r instanceof SegmentRegister)) {
			throw new IllegalArgumentException(
					String.format("'%s' is not a valid base register: must be 32-bit, 64-bit or a segment."));
		}
		if (indexRegister != null && r.bits() != indexRegister.bits()) {
			throw new IllegalArgumentException(String.format(
					"Cannot mix %,d-bit and %,d-bit registers (%s, %s).",
					r.bits(), indexRegister.bits(), r, indexRegister));
		}
		this.baseRegister = r;
		return this;
	}

	/**
	 * Sets the scale for the IndirectOperand.
	 *
	 * @param c The new scale.
	 * @return This instance of IndirectOperandBuilder.
	 */
	public IndirectOperandBuilder scale(final int c) {
		if (this.scale != null) {
			throw new IllegalArgumentException("Cannot define scale twice.");
		}
		if (c != 1 && c != 2 && c != 4 && c != 8) {
			throw new IllegalArgumentException(String.format("Invalid indirect operand scale %,d.", c));
		}
		this.scale = c;
		return this;
	}

	/**
	 * Sets the index register for the IndirectOperand.
	 *
	 * @param r The new index register.
	 * @return This instance of IndirectOperandBuilder.
	 */
	public IndirectOperandBuilder index(final Register r) {
		if (this.indexRegister != null) {
			throw new IllegalArgumentException("Cannot define index register twice.");
		}
		Objects.requireNonNull(r);
		if (!(r instanceof Register32) && !(r instanceof Register64)) {
			throw new IllegalArgumentException(
					String.format("'%s' is not a valid index register: must be 32-bit or 64-bit."));
		}
		if (baseRegister != null && r.bits() != baseRegister.bits()) {
			throw new IllegalArgumentException(String.format(
					"Cannot mix %,d-bit and %,d-bit registers (%s, %s).",
					baseRegister.bits(), r.bits(), baseRegister, r));
		}
		this.indexRegister = r;
		return this;
	}

	/**
	 * Sets an 8-bit displacement for the IndirectOperand.
	 *
	 * @param disp The new displacement.
	 * @return This instance of IndirectOperandBuilder.
	 */
	public IndirectOperandBuilder displacement(final byte disp) {
		return displacement(disp, DisplacementType.BYTE);
	}

	/**
	 * Sets a 32-bit displacement for the IndirectOperand.
	 *
	 * @param disp The new displacement.
	 * @return This instance of IndirectOperandBuilder.
	 */
	public IndirectOperandBuilder displacement(final int disp) {
		return displacement(disp, DisplacementType.INT);
	}

	private IndirectOperandBuilder displacement(final long disp, final DisplacementType displacementType) {
		if (this.displacement != null) {
			throw new IllegalArgumentException("Cannot define displacement twice.");
		}
		this.displacement = disp;
		this.displacementType = Objects.requireNonNull(displacementType);
		return this;
	}

	/**
	 * Sets an explicit pointer size for the IndirectOperand. This method is currently required in the current
	 * implementation since it does not allow, otherwise, to create indirect operands such as "WORD PTR ...".
	 *
	 * @param ptrSize The explicit pointer size.
	 * @return This instance of IndirectOperandBuilder.
	 */
	public IndirectOperandBuilder pointer(final PointerSize ptrSize) {
		if (this.ptrSize != null) {
			throw new IllegalArgumentException("Cannot define pointer size twice.");
		}

		this.ptrSize = Objects.requireNonNull(ptrSize);
		return this;
	}

	/**
	 * Builds the IndirectOperand object with the parameters that have been set.
	 *
	 * @return A new instance of IndirectOperand.
	 */
	public IndirectOperand build() {
		if (alreadyBuilt) {
			throw new IllegalArgumentException("Cannot build the same IndirectOperandBuilder twice.");
		}

		alreadyBuilt = true;

		return new IndirectOperand(ptrSize, baseRegister, indexRegister, scale, displacement, displacementType);
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(final Object other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "IndirectOperandBuilder(base=" + baseRegister + ";index=" + indexRegister + ";scale=" + scale
				+ ";displacement="
				+ displacement + ";displacementType="
				+ displacementType + ";ptrSize=" + ptrSize + ";alreadyBuilt=" + alreadyBuilt + ")";
	}
}
