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

public final class InstructionBuilder {

	private InstructionPrefix prefix = null;
	private Opcode opcode = null;
	private MaskRegister destinationMask = null;
	private Operand op1 = null;
	private Operand op2 = null;
	private Operand op3 = null;
	private Operand op4 = null;
	private boolean alreadyBuilt = false;

	public InstructionBuilder() {}

	private void assertNotBuilt() {
		if (alreadyBuilt) {
			throw new IllegalArgumentException("Cannot build twice.");
		}
	}

	public InstructionBuilder prefix(final InstructionPrefix prefix) {
		assertNotBuilt();
		if (this.prefix != null) {
			throw new IllegalArgumentException("Cannot set prefix twice.");
		}
		this.prefix = Objects.requireNonNull(prefix, "Null prefix.");
		return this;
	}

	public InstructionBuilder opcode(final Opcode opcode) {
		assertNotBuilt();
		if (this.opcode != null) {
			throw new IllegalArgumentException("Cannot set opcode twice.");
		}
		this.opcode = Objects.requireNonNull(opcode, "Null opcode.");
		return this;
	}

	public InstructionBuilder mask(final MaskRegister mask) {
		assertNotBuilt();
		if (this.destinationMask != null) {
			throw new IllegalArgumentException("Cannot set destination mask twice.");
		}
		this.destinationMask = Objects.requireNonNull(mask, "Null mask.");
		return this;
	}

	public InstructionBuilder op(final Operand op) {
		assertNotBuilt();
		Objects.requireNonNull(op, "Null operand.");
		if (this.op1 == null) {
			this.op1 = op;
		} else if (this.op2 == null) {
			this.op2 = op;
		} else if (this.op3 == null) {
			this.op3 = op;
		} else if (this.op4 == null) {
			this.op4 = op;
		} else {
			throw new IllegalArgumentException("Too many operands.");
		}
		return this;
	}

	public Instruction build() {
		assertNotBuilt();

		if (opcode == null) {
			throw new IllegalArgumentException("Cannot build an Instruction without an opcode.");
		}

		alreadyBuilt = true;
		return new Instruction(prefix, opcode, destinationMask, op1, op2, op3, op4);
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(final Object other) {
		throw new UnsupportedOperationException();
	}
}
