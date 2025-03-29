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

import com.ledmington.cpu.x86.exc.InvalidInstruction;

/** High-level representation of an x86 instruction. */
public final class Instruction {

	private final InstructionPrefix prefix;
	private final Opcode code;
	private final Operand op1;
	private final Operand op2;
	private final Operand op3;

	Instruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand,
			final Operand thirdOperand) {
		this.prefix = prefix;
		this.code = Objects.requireNonNull(opcode);
		this.op1 = firstOperand;
		if (firstOperand == null && secondOperand != null) {
			throw new IllegalArgumentException(String.format(
					"Cannot have an x86 instruction with a second operand (%s) but not a first.", secondOperand));
		}
		this.op2 = secondOperand;
		if (thirdOperand != null && (firstOperand == null || secondOperand == null)) {
			throw new IllegalArgumentException(String.format(
					"Cannot have an x86 instruction with a third operand (%s) but not a first or a second.",
					thirdOperand));
		}
		this.op3 = thirdOperand;

		switch (code) {
			case Opcode.MOV -> {
				if (op3 != null) {
					throw new InvalidInstruction(String.format("%s cannot have 3 operands.", code.name()));
				}
				if (op1 == null || op2 == null) {
					throw new InvalidInstruction(String.format("%s requires 2 operands.", code.name()));
				}
				if (op1 instanceof Immediate) {
					throw new InvalidInstruction(
							String.format("Destination operand of %s cannot be an immediate value.", code.name()));
				}
				if (op1 instanceof IndirectOperand && op2 instanceof IndirectOperand) {
					throw new InvalidInstruction(String.format("%s cannot have two indirect operands.", code.name()));
				}
				if (op1.bits() != op2.bits()
						&&
						// Some instructions allow an implicit sign-extension when the first operand is 64 bits and the
						// second operand is a 32-bit immediate
						!(op2 instanceof Immediate imm && imm.bits() == 32 && op1.bits() == 64)) {
					throw new InvalidInstruction(String.format(
							"%s cannot have two operands of different sizes: were %,d (%s) and %,d (%s) bits, respectively.",
							code.name(), op1.bits(), op1, op2.bits(), op2));
				}
			}
			case Opcode.MOVSXD -> {
				if (op3 != null) {
					throw new InvalidInstruction(String.format("%s cannot have 3 operands.", code.name()));
				}
				if (op1 == null || op2 == null) {
					throw new InvalidInstruction(String.format("%s requires 2 operands.", code.name()));
				}
				if (!(op1 instanceof Register64)) {
					throw new InvalidInstruction(
							String.format("%s requires destination operand to be a 64-bit register.", code.name()));
				}
				if (!(op2 instanceof Register32) && (!(op2 instanceof IndirectOperand io) || io.bits() != 32)) {
					throw new InvalidInstruction(String.format(
							"%s requires source operand to be a 32-bit register or a 32-bit indirect operand.",
							code.name()));
				}
			}
			case Opcode.NOP -> {
				if (op2 != null || op3 != null) {
					throw new InvalidInstruction(String.format("%s cannot have 2 or 3 operands.", code.name()));
				}
				if (op1 != null && (op1.bits() == 8 || op1 instanceof Immediate)) {
					throw new InvalidInstruction(
							String.format("%s cannot have an 8-bit operand (%s).", code.name(), op1));
				}
			}
			case Opcode.LEA -> {
				if (op1 == null || op2 == null || op3 != null) {
					throw new InvalidInstruction(String.format("%s requires 2 operands.", code.name()));
				}
				if (!(op1 instanceof Register) || op1.bits() == 8) {
					throw new InvalidInstruction(
							String.format("%s requires a not 8-bit destination register (%s).", code.name(), op1));
				}
				if (!(op2 instanceof IndirectOperand)) {
					throw new InvalidInstruction(
							String.format("%s requires an indirect operand (%s).", code.name(), op2));
				}
			}
			default -> {}
		}
	}

	/**
	 * Creates an instruction with a prefix and two operands (like REP MOVS BYTE PTR ES:[EDI],BYTE PTR DS:[ESI]).
	 *
	 * @param prefix The prefix of the Instruction.
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 */
	public Instruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand) {
		this(prefix, opcode, firstOperand, secondOperand, null);
	}

	/**
	 * Creates an Instruction with 3 operands (like PSHUFD XMM0, XMM1, 0x12).
	 *
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 * @param thirdOperand The third of the Instruction.
	 */
	public Instruction(
			final Opcode opcode, final Operand firstOperand, final Operand secondOperand, final Operand thirdOperand) {
		this(null, opcode, firstOperand, secondOperand, thirdOperand);
	}

	/**
	 * Creates an Instruction with two operands (like XOR EAX, EAX).
	 *
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 */
	public Instruction(final Opcode opcode, final Operand firstOperand, final Operand secondOperand) {
		this(null, opcode, firstOperand, secondOperand, null);
	}

	public Instruction(final InstructionPrefix prefix, final Opcode opcode, final Operand firstOperand) {
		this(prefix, opcode, firstOperand, null, null);
	}

	/**
	 * Creates an Instruction with one operand (like PUSH R9).
	 *
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The only operand of this instruction.
	 */
	public Instruction(final Opcode opcode, final Operand firstOperand) {
		this(null, opcode, firstOperand, null, null);
	}

	/**
	 * Creates an instruction with just the opcode (like NOP or ENDBR64).
	 *
	 * @param opcode The opcode of the instruction.
	 */
	public Instruction(final Opcode opcode) {
		this(null, opcode, null, null, null);
	}

	public boolean hasPrefix() {
		return prefix != null;
	}

	public boolean hasLockPrefix() {
		return prefix == InstructionPrefix.LOCK;
	}

	public boolean hasRepPrefix() {
		return prefix == InstructionPrefix.REP;
	}

	public boolean hasRepnzPrefix() {
		return prefix == InstructionPrefix.REPNZ;
	}

	public InstructionPrefix getPrefix() {
		if (!hasPrefix()) {
			throw new IllegalArgumentException("No prefix.");
		}
		return prefix;
	}

	/**
	 * Returns the opcode of this Instruction.
	 *
	 * @return The opcode of this Instruction.
	 */
	public Opcode opcode() {
		return code;
	}

	public boolean hasFirstOperand() {
		return op1 != null;
	}

	/**
	 * Returns the first operand of this Instruction.
	 *
	 * @return The first operand.
	 * @throws IllegalArgumentException If this instruction has no operands.
	 */
	public Operand firstOperand() {
		if (!hasFirstOperand()) {
			throw new IllegalArgumentException("No first operand.");
		}
		return op1;
	}

	public boolean hasSecondOperand() {
		return op2 != null;
	}

	/**
	 * Returns the second operand of this Instruction.
	 *
	 * @return The second operand.
	 * @throws IllegalArgumentException If this instruction has less than two operands.
	 */
	public Operand secondOperand() {
		if (!hasSecondOperand()) {
			throw new IllegalArgumentException("No second operand.");
		}
		return op2;
	}

	public boolean hasThirdOperand() {
		return op3 != null;
	}

	public Operand thirdOperand() {
		if (!hasThirdOperand()) {
			throw new IllegalArgumentException("No third operand.");
		}
		return op3;
	}

	/**
	 * Checks whether this instruction is part of the legacy/compatibility x86 set.
	 *
	 * @return True if it is legacy, false otherwise.
	 */
	public boolean isLegacy() {
		return code == Opcode.ENDBR32;
	}

	@Override
	public String toString() {
		return "Instruction(prefix=" + prefix + ";opcode=" + code.toString()
				+ ";operands=[" + op1 + "," + op2 + "," + op3 + "]"
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + (prefix == null ? 0 : prefix.hashCode());
		h = 31 * h + code.hashCode();
		h = 31 * h + (op1 == null ? 0 : op1.hashCode());
		h = 31 * h + (op2 == null ? 0 : op2.hashCode());
		h = 31 * h + (op3 == null ? 0 : op3.hashCode());
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
		if (!(other instanceof Instruction inst)) {
			return false;
		}
		return Objects.equals(this.prefix, inst.prefix)
				&& this.code.equals(inst.code)
				&& Objects.equals(this.op1, inst.op1)
				&& Objects.equals(this.op2, inst.op2)
				&& Objects.equals(this.op3, inst.op3);
	}
}
