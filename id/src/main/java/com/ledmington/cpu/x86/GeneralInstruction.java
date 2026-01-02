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

/** Most general implementation of an x86 instruction. */
public final class GeneralInstruction implements Instruction {

	private final InstructionPrefix prefix;
	private final Opcode code;
	private final MaskRegister destinationMask;
	private final boolean destinationMaskZero;
	private final Operand op1;
	private final Operand op2;
	private final Operand op3;
	private final Operand op4;

	/* default */ GeneralInstruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final MaskRegister destinationMask,
			final boolean destinationMaskZero,
			final Operand firstOperand,
			final Operand secondOperand,
			final Operand thirdOperand,
			final Operand fourthOperand) {
		this.prefix = prefix;
		this.code = Objects.requireNonNull(opcode);
		this.destinationMask = destinationMask;
		this.destinationMaskZero = destinationMaskZero;
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
		if (fourthOperand != null && (firstOperand == null || secondOperand == null || thirdOperand == null)) {
			throw new IllegalArgumentException(String.format(
					"Cannot have an x86 instruction with a fourth operand (%s) but not a first or a second or a third.",
					fourthOperand));
		}
		this.op4 = fourthOperand;
	}

	/**
	 * Creates an instruction with a prefix and two operands (like REP MOVS BYTE PTR ES:[EDI],BYTE PTR DS:[ESI]).
	 *
	 * @param prefix The prefix of the Instruction.
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 */
	public GeneralInstruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand) {
		this(prefix, opcode, null, false, firstOperand, secondOperand, null, null);
	}

	/**
	 * Creates an Instruction with 3 operands (like PSHUFD XMM0, XMM1, 0x12).
	 *
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 * @param thirdOperand The third operand of the Instruction.
	 */
	public GeneralInstruction(
			final Opcode opcode, final Operand firstOperand, final Operand secondOperand, final Operand thirdOperand) {
		this(null, opcode, null, false, firstOperand, secondOperand, thirdOperand, null);
	}

	/**
	 * Creates an Instruction with a prefix and 3 operands.
	 *
	 * @param prefix The prefix of the Instruction.
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 * @param thirdOperand The third operand of the Instruction.
	 */
	public GeneralInstruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand,
			final Operand thirdOperand) {
		this(prefix, opcode, null, false, firstOperand, secondOperand, thirdOperand, null);
	}

	/**
	 * Creates an Instruction with 4 operands.
	 *
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 * @param thirdOperand The third operand of the Instruction.
	 * @param fourthOperand The fourth operand of the Instruction.
	 */
	public GeneralInstruction(
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand,
			final Operand thirdOperand,
			final Operand fourthOperand) {
		this(null, opcode, null, false, firstOperand, secondOperand, thirdOperand, fourthOperand);
	}

	/**
	 * Creates an Instruction with two operands (like XOR EAX, EAX).
	 *
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 * @param secondOperand The second operand of the Instruction.
	 */
	public GeneralInstruction(final Opcode opcode, final Operand firstOperand, final Operand secondOperand) {
		this(null, opcode, null, false, firstOperand, secondOperand, null, null);
	}

	/**
	 * Creates an Instruction with a prefix and one operands.
	 *
	 * @param prefix The prefix of the Instruction.
	 * @param opcode The opcode of the Instruction.
	 * @param firstOperand The first operand of the Instruction.
	 */
	public GeneralInstruction(final InstructionPrefix prefix, final Opcode opcode, final Operand firstOperand) {
		this(prefix, opcode, null, false, firstOperand, null, null, null);
	}

	/**
	 * Creates an Instruction with a prefix and no operands.
	 *
	 * @param prefix The prefix of the Instruction.
	 * @param opcode The opcode of the Instruction.
	 */
	public GeneralInstruction(final InstructionPrefix prefix, final Opcode opcode) {
		this(prefix, opcode, null, false, null, null, null, null);
	}

	/**
	 * Creates an Instruction with one operand (like PUSH R9).
	 *
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The only operand of this instruction.
	 */
	public GeneralInstruction(final Opcode opcode, final Operand firstOperand) {
		this(null, opcode, null, false, firstOperand, null, null, null);
	}

	/**
	 * Creates an instruction with just the opcode (like NOP or ENDBR64).
	 *
	 * @param opcode The opcode of the instruction.
	 */
	public GeneralInstruction(final Opcode opcode) {
		this(null, opcode, null, false, null, null, null, null);
	}

	@Override
	public boolean hasPrefix() {
		return prefix != null;
	}

	@Override
	public InstructionPrefix getPrefix() {
		if (!hasPrefix()) {
			throw new IllegalArgumentException("No prefix.");
		}
		return prefix;
	}

	@Override
	public Opcode opcode() {
		return code;
	}

	@Override
	public boolean hasDestinationMask() {
		return destinationMask != null;
	}

	@Override
	public MaskRegister getDestinationMask() {
		if (!hasDestinationMask()) {
			throw new IllegalArgumentException("No destination mask.");
		}
		return destinationMask;
	}

	@Override
	public boolean hasZeroDestinationMask() {
		return hasDestinationMask() && destinationMaskZero;
	}

	@Override
	public boolean hasFirstOperand() {
		return op1 != null;
	}

	@Override
	public Operand firstOperand() {
		if (!hasFirstOperand()) {
			throw new IllegalArgumentException("No first operand.");
		}
		return op1;
	}

	@Override
	public boolean hasSecondOperand() {
		return op2 != null;
	}

	@Override
	public Operand secondOperand() {
		if (!hasSecondOperand()) {
			throw new IllegalArgumentException("No second operand.");
		}
		return op2;
	}

	@Override
	public boolean hasThirdOperand() {
		return op3 != null;
	}

	@Override
	public Operand thirdOperand() {
		if (!hasThirdOperand()) {
			throw new IllegalArgumentException("No third operand.");
		}
		return op3;
	}

	@Override
	public boolean hasFourthOperand() {
		return op4 != null;
	}

	@Override
	public Operand fourthOperand() {
		if (!hasFourthOperand()) {
			throw new IllegalArgumentException("No fourth operand.");
		}
		return op4;
	}

	@Override
	public String toString() {
		return "GeneralInstruction(prefix=" + prefix + ";opcode=" + code.toString()
				+ ";mask=" + destinationMask + ";destinationMaskZero=" + destinationMaskZero
				+ ";operands=[" + op1 + "," + op2 + "," + op3 + "," + op4 + "]"
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Objects.hashCode(prefix);
		h = 31 * h + code.hashCode();
		h = 31 * h + Objects.hashCode(destinationMask);
		h = 31 * h + Boolean.hashCode(destinationMaskZero);
		h = 31 * h + Objects.hashCode(op1);
		h = 31 * h + Objects.hashCode(op2);
		h = 31 * h + Objects.hashCode(op3);
		h = 31 * h + Objects.hashCode(op4);
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
		if (!(other instanceof final GeneralInstruction inst)) {
			return false;
		}
		return Objects.equals(this.prefix, inst.prefix)
				&& this.code == inst.code
				&& Objects.equals(this.destinationMask, inst.destinationMask)
				&& this.destinationMaskZero == inst.destinationMaskZero
				&& Objects.equals(this.op1, inst.op1)
				&& Objects.equals(this.op2, inst.op2)
				&& Objects.equals(this.op3, inst.op3)
				&& Objects.equals(this.op4, inst.op4);
	}
}
