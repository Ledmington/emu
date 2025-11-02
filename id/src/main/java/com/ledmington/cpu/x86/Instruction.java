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

/** High-level representation of an x86 instruction. */
public final class Instruction {

	private final InstructionPrefix prefix;
	private final Opcode code;
	private final MaskRegister destinationMask;
	private final boolean destinationMaskZero;
	private final Operand op1;
	private final Operand op2;
	private final Operand op3;
	private final Operand op4;

	/**
	 * Creates a new {@code InstructionBuilder} instance.
	 *
	 * @return A new {@code InstructionBuilder}.
	 */
	public static InstructionBuilder builder() {
		return new InstructionBuilder();
	}

	/* default */ Instruction(
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
	 * Creates an instruction with a prefix and two operands (like REP MOVS BYTE PTR ES:[EDI], BYTE PTR DS:[ESI]).
	 *
	 * @param prefix The prefix of the instruction.
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The first operand of the instruction.
	 * @param secondOperand The second operand of the instruction.
	 */
	public Instruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand) {
		this(prefix, opcode, null, false, firstOperand, secondOperand, null, null);
	}

	/**
	 * Creates an instruction with three operands (like PSHUFD XMM0, XMM1, 0x12).
	 *
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The first operand of the instruction.
	 * @param secondOperand The second operand of the instruction.
	 * @param thirdOperand The third operand of the instruction.
	 */
	public Instruction(
			final Opcode opcode, final Operand firstOperand, final Operand secondOperand, final Operand thirdOperand) {
		this(null, opcode, null, false, firstOperand, secondOperand, thirdOperand, null);
	}

	/**
	 * Creates an instruction with a prefix and three operands.
	 *
	 * @param prefix The prefix of the instruction.
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The first operand of the instruction.
	 * @param secondOperand The second operand of the instruction.
	 * @param thirdOperand The third operand of the instruction.
	 */
	public Instruction(
			final InstructionPrefix prefix,
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand,
			final Operand thirdOperand) {
		this(prefix, opcode, null, false, firstOperand, secondOperand, thirdOperand, null);
	}

	/**
	 * Creates an instruction with four operands.
	 *
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The first operand of the instruction.
	 * @param secondOperand The second operand of the instruction.
	 * @param thirdOperand The third operand of the instruction.
	 * @param fourthOperand The fourth operand of the instruction.
	 */
	public Instruction(
			final Opcode opcode,
			final Operand firstOperand,
			final Operand secondOperand,
			final Operand thirdOperand,
			final Operand fourthOperand) {
		this(null, opcode, null, false, firstOperand, secondOperand, thirdOperand, fourthOperand);
	}

	/**
	 * Creates an instruction with two operands (like XOR EAX, EAX).
	 *
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The first operand of the instruction.
	 * @param secondOperand The second operand of the instruction.
	 */
	public Instruction(final Opcode opcode, final Operand firstOperand, final Operand secondOperand) {
		this(null, opcode, null, false, firstOperand, secondOperand, null, null);
	}

	/**
	 * Creates an instruction with a prefix and a single operand.
	 *
	 * @param prefix The prefix of the instruction.
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The only operand of the instruction.
	 */
	public Instruction(final InstructionPrefix prefix, final Opcode opcode, final Operand firstOperand) {
		this(prefix, opcode, null, false, firstOperand, null, null, null);
	}

	/**
	 * Creates an instruction with only a prefix and an opcode.
	 *
	 * @param prefix The prefix of the instruction.
	 * @param opcode The opcode of the instruction.
	 */
	public Instruction(final InstructionPrefix prefix, final Opcode opcode) {
		this(prefix, opcode, null, false, null, null, null, null);
	}

	/**
	 * Creates an instruction with one operand (like PUSH R9).
	 *
	 * @param opcode The opcode of the instruction.
	 * @param firstOperand The only operand of the instruction.
	 */
	public Instruction(final Opcode opcode, final Operand firstOperand) {
		this(null, opcode, null, false, firstOperand, null, null, null);
	}

	/**
	 * Creates an instruction with just the opcode (like NOP or ENDBR64).
	 *
	 * @param opcode The opcode of the instruction.
	 */
	public Instruction(final Opcode opcode) {
		this(null, opcode, null, false, null, null, null, null);
	}

	/**
	 * Checks whether this instruction has a prefix.
	 *
	 * @return True if this instruction has a prefix, false otherwise.
	 */
	public boolean hasPrefix() {
		return prefix != null;
	}

	/**
	 * Checks whether this instruction has a LOCK prefix.
	 *
	 * @return True if this instruction has a LOCK prefix, false otherwise.
	 */
	public boolean hasLockPrefix() {
		return prefix == InstructionPrefix.LOCK;
	}

	/**
	 * Checks whether this instruction has a REP prefix.
	 *
	 * @return True if this instruction has a REP prefix, false otherwise.
	 */
	public boolean hasRepPrefix() {
		return prefix == InstructionPrefix.REP;
	}

	/**
	 * Checks whether this instruction has a REPNZ prefix.
	 *
	 * @return True if this instruction has a REPNZ prefix, false otherwise.
	 */
	public boolean hasRepnzPrefix() {
		return prefix == InstructionPrefix.REPNZ;
	}

	/**
	 * Returns the prefix of this instruction.
	 *
	 * @return The prefix of this instruction.
	 */
	public InstructionPrefix getPrefix() {
		if (!hasPrefix()) {
			throw new IllegalArgumentException("No prefix.");
		}
		return prefix;
	}

	/**
	 * Returns the opcode of this instruction.
	 *
	 * @return The opcode of this instruction.
	 */
	public Opcode opcode() {
		return code;
	}

	/**
	 * Checks whether this instruction has a destination mask.
	 *
	 * @return True if this instruction has a destination mask, false otherwise.
	 */
	public boolean hasDestinationMask() {
		return destinationMask != null;
	}

	/**
	 * Returns the destination mask of this instruction.
	 *
	 * @return The destination mask of this instruction.
	 */
	public MaskRegister getDestinationMask() {
		if (!hasDestinationMask()) {
			throw new IllegalArgumentException("No destination mask.");
		}
		return destinationMask;
	}

	/**
	 * Checks whether this instruction has a zero destination mask.
	 *
	 * @return True if the instruction has a zero destination mask, false otherwise.
	 */
	public boolean hasZeroDestinationMask() {
		return hasDestinationMask() && destinationMaskZero;
	}

	/**
	 * Checks whether this instruction has an operand at the given index.
	 *
	 * @param index The operand index (0–3).
	 * @return True if the operand exists, false otherwise.
	 */
	public boolean hasOperand(final int index) {
		return switch (index) {
			case 0 -> hasFirstOperand();
			case 1 -> hasSecondOperand();
			case 2 -> hasThirdOperand();
			case 3 -> hasFourthOperand();
			default -> throw new IllegalArgumentException(String.format("No operand at index %,d.", index));
		};
	}

	/**
	 * Returns the operand at the specified index.
	 *
	 * @param index The operand index (0–3).
	 * @return The operand at the specified index.
	 */
	public Operand operand(final int index) {
		return switch (index) {
			case 0 -> firstOperand();
			case 1 -> secondOperand();
			case 2 -> thirdOperand();
			case 3 -> fourthOperand();
			default -> throw new IllegalArgumentException(String.format("No operand at index %,d.", index));
		};
	}

	/**
	 * Checks whether this instruction has a first operand.
	 *
	 * @return True if the first operand exists, false otherwise.
	 */
	public boolean hasFirstOperand() {
		return op1 != null;
	}

	/**
	 * Returns the first operand of this instruction.
	 *
	 * @return The first operand.
	 */
	public Operand firstOperand() {
		if (!hasFirstOperand()) {
			throw new IllegalArgumentException("No first operand.");
		}
		return op1;
	}

	/**
	 * Checks whether this instruction has a second operand.
	 *
	 * @return True if the second operand exists, false otherwise.
	 */
	public boolean hasSecondOperand() {
		return op2 != null;
	}

	/**
	 * Returns the second operand of this instruction.
	 *
	 * @return The second operand.
	 */
	public Operand secondOperand() {
		if (!hasSecondOperand()) {
			throw new IllegalArgumentException("No second operand.");
		}
		return op2;
	}

	/**
	 * Checks whether this instruction has a third operand.
	 *
	 * @return True if the third operand exists, false otherwise.
	 */
	public boolean hasThirdOperand() {
		return op3 != null;
	}

	/**
	 * Returns the third operand of this instruction.
	 *
	 * @return The third operand.
	 */
	public Operand thirdOperand() {
		if (!hasThirdOperand()) {
			throw new IllegalArgumentException("No third operand.");
		}
		return op3;
	}

	/**
	 * Checks whether this instruction has a fourth operand.
	 *
	 * @return True if the fourth operand exists, false otherwise.
	 */
	public boolean hasFourthOperand() {
		return op4 != null;
	}

	/**
	 * Returns the fourth operand of this instruction.
	 *
	 * @return The fourth operand.
	 */
	public Operand fourthOperand() {
		if (!hasFourthOperand()) {
			throw new IllegalArgumentException("No fourth operand.");
		}
		return op4;
	}

	/**
	 * Returns the number of operands in this instruction.
	 *
	 * @return The number of operands.
	 */
	public int getNumOperands() {
		if (op4 != null) {
			return 4;
		}
		if (op3 != null) {
			return 3;
		}
		if (op2 != null) {
			return 2;
		}
		if (op1 != null) {
			return 1;
		}
		return 0;
	}

	/**
	 * Checks whether this instruction is part of the legacy x86 set.
	 *
	 * @return True if the instruction is legacy, false otherwise.
	 */
	public boolean isLegacy() {
		return code == Opcode.ENDBR32;
	}

	/**
	 * Returns a string representation of this instruction.
	 *
	 * @return A string representing this instruction.
	 */
	@Override
	public String toString() {
		return "Instruction(prefix=" + prefix + ";opcode=" + code.toString()
				+ ";mask=" + destinationMask + ";destinationMaskZero=" + destinationMaskZero
				+ ";operands=[" + op1 + "," + op2 + "," + op3 + "," + op4 + "]"
				+ ")";
	}

	/**
	 * Computes the hash code for this instruction.
	 *
	 * @return The hash code value.
	 */
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

	/**
	 * Checks equality between this instruction and another object.
	 *
	 * @param other The object to compare with.
	 * @return True if the objects are equal, false otherwise.
	 */
	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final Instruction inst)) {
			return false;
		}
		return Objects.equals(this.prefix, inst.prefix)
				&& this.code.equals(inst.code)
				&& Objects.equals(this.destinationMask, inst.destinationMask)
				&& this.destinationMaskZero == inst.destinationMaskZero
				&& Objects.equals(this.op1, inst.op1)
				&& Objects.equals(this.op2, inst.op2)
				&& Objects.equals(this.op3, inst.op3)
				&& Objects.equals(this.op4, inst.op4);
	}
}
