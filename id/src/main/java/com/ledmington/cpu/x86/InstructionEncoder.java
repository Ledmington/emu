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

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * Encodes an x86 instruction to either binary or intel syntax. NOTE: prefix are encoded in a specific order. First here
 * are the LOCK/REP/REPNE, then the CS segment override, then the address size override, the the operand size override.
 */
public final class InstructionEncoder {

	private static final byte DEFAULT_REX_PREFIX = (byte) 0x40;
	private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
	private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
	private static final byte DOUBLE_BYTE_OPCODE_PREFIX = (byte) 0x0f;
	private static final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
	private static final byte LOCK_PREFIX = (byte) 0xf0;
	private static final Map<Opcode, Byte> CONDITIONAL_JUMPS_OPCODES = Map.ofEntries(
			Map.entry(Opcode.JB, (byte) 0x02),
			Map.entry(Opcode.JAE, (byte) 0x03),
			Map.entry(Opcode.JE, (byte) 0x04),
			Map.entry(Opcode.JNE, (byte) 0x05),
			Map.entry(Opcode.JBE, (byte) 0x06),
			Map.entry(Opcode.JA, (byte) 0x07),
			Map.entry(Opcode.JS, (byte) 0x08),
			Map.entry(Opcode.JNS, (byte) 0x09),
			Map.entry(Opcode.JP, (byte) 0x0a),
			Map.entry(Opcode.JL, (byte) 0x0c),
			Map.entry(Opcode.JGE, (byte) 0x0d),
			Map.entry(Opcode.JLE, (byte) 0x0e),
			Map.entry(Opcode.JG, (byte) 0x0f));
	private static final Map<Opcode, Byte> PREFETCH_OPCODES = Map.ofEntries(
			Map.entry(Opcode.PREFETCHNTA, (byte) 0x00),
			Map.entry(Opcode.PREFETCHT0, (byte) 0x01),
			Map.entry(Opcode.PREFETCHT1, (byte) 0x02),
			Map.entry(Opcode.PREFETCHT2, (byte) 0x03));
	private static final Map<Opcode, Byte> BIT_TEST_OPCODES = Map.ofEntries(
			Map.entry(Opcode.BT, (byte) 0b100),
			Map.entry(Opcode.BTS, (byte) 0b101),
			Map.entry(Opcode.BTR, (byte) 0b110),
			Map.entry(Opcode.BTC, (byte) 0b111));
	private static final Map<Opcode, Byte> CONDITIONAL_MOVE_OPCODES = Map.ofEntries(
			Map.entry(Opcode.CMOVB, (byte) 0x02),
			Map.entry(Opcode.CMOVAE, (byte) 0x03),
			Map.entry(Opcode.CMOVE, (byte) 0x04),
			Map.entry(Opcode.CMOVNE, (byte) 0x05),
			Map.entry(Opcode.CMOVBE, (byte) 0x06),
			Map.entry(Opcode.CMOVA, (byte) 0x07),
			Map.entry(Opcode.CMOVS, (byte) 0x08),
			Map.entry(Opcode.CMOVNS, (byte) 0x09),
			Map.entry(Opcode.CMOVL, (byte) 0x0c),
			Map.entry(Opcode.CMOVGE, (byte) 0x0d),
			Map.entry(Opcode.CMOVLE, (byte) 0x0e),
			Map.entry(Opcode.CMOVG, (byte) 0x0f));

	// The value for the Reg field for the shift instructions
	private static final Map<Opcode, Byte> SHIFT_REG_BYTES = Map.ofEntries(
			Map.entry(Opcode.SHR, (byte) 0b101), Map.entry(Opcode.SAR, (byte) 0b111), Map.entry(Opcode.SHL, (byte)
					0b100));
	// The value for the Reg field for the IDIV/DIV/MUL instructions
	private static final Map<Opcode, Byte> DIV_MUL_REG_BYTES = Map.ofEntries(
			Map.entry(Opcode.IDIV, (byte) 0b111), Map.entry(Opcode.DIV, (byte) 0b110), Map.entry(Opcode.MUL, (byte)
					0b100));

	private InstructionEncoder() {}

	private static String operandString(final Opcode code, final Operand op) {
		if (op instanceof IndirectOperand io) {
			return io.toIntelSyntax(code != Opcode.LEA);
		}
		return op.toIntelSyntax();
	}

	/**
	 * Reference obtainable through <code>objdump -Mintel-mnemonic ...</code>
	 *
	 * @return A String representation of this instruction with Intel syntax.
	 */
	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	public static String toIntelSyntax(final Instruction inst) {
		Objects.requireNonNull(inst);
		final StringBuilder sb = new StringBuilder();
		if (inst.hasPrefix()) {
			sb.append(inst.getPrefix().name().toLowerCase(Locale.US)).append(' ');
		}

		sb.append(inst.opcode().mnemonic());

		if (inst.hasFirstOperand()) {
			sb.append(' ').append(operandString(inst.opcode(), inst.firstOperand()));
			if (inst.hasSecondOperand()) {
				sb.append(',').append(operandString(inst.opcode(), inst.secondOperand()));
				if (inst.hasThirdOperand()) {
					sb.append(',').append(operandString(inst.opcode(), inst.thirdOperand()));
				}
			}
		}

		return sb.toString();
	}

	public static byte[] toHex(final Instruction... code) {
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		for (final Instruction inst : code) {
			toHex(wb, inst);
		}
		return wb.array();
	}

	public static byte[] toHex(final Instruction inst) {
		Objects.requireNonNull(inst);
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		toHex(wb, inst);
		return wb.array();
	}

	private static int countOperands(final Instruction inst) {
		if (!inst.hasFirstOperand()) {
			return 0;
		} else if (!inst.hasSecondOperand()) {
			return 1;
		} else if (!inst.hasThirdOperand()) {
			return 2;
		} else {
			return 3;
		}
	}

	private static void toHex(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (countOperands(inst)) {
			case 0 -> encodeZeroOperandsInstruction(wb, inst);
			case 1 -> encodeSingleOperandInstruction(wb, inst);
			case 2 -> encodeTwoOperandsInstruction(wb, inst);
			case 3 -> encodeThreeOperandsInstruction(wb, inst);
			default ->
				throw new IllegalArgumentException(String.format(
						"Unknown instruction with %,d operands: '%s'.", countOperands(inst), toIntelSyntax(inst)));
		}
	}

	private static void encodeZeroOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case NOP -> wb.write((byte) 0x90);
			case CWDE -> wb.write((byte) 0x98);
			case CDQ -> wb.write((byte) 0x99);
			case SAHF -> wb.write((byte) 0x9e);
			case LAHF -> wb.write((byte) 0x9f);
			case RET -> wb.write((byte) 0xc3);
			case LEAVE -> wb.write((byte) 0xc9);
			case INT3 -> wb.write((byte) 0xcc);
			case HLT -> wb.write((byte) 0xf4);
			case DEC -> wb.write((byte) 0xff);
			case XGETBV -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x01, (byte) 0xd0);
			case UD2 -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x0b);
			case CDQE -> wb.write((byte) 0x48, (byte) 0x98);
			case SETG -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x9f);
			case BSWAP -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xcc);
			case ENDBR64 -> wb.write((byte) 0xf3, DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1e, (byte) 0xfa);
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static void encodeSingleOperandInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if (inst.firstOperand() instanceof IndirectOperand io && io.hasSegment()) {
			wb.write(CS_SEGMENT_OVERRIDE_PREFIX);
		}
		if (inst.firstOperand() instanceof IndirectOperand io && io.hasIndex() && io.getIndex() instanceof Register32) {
			wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
		}
		if (inst.firstOperand().bits() == 16
				|| ((inst.opcode() == Opcode.CALL || inst.opcode() == Opcode.JMP)
						&& inst.firstOperand() instanceof IndirectOperand
						&& inst.firstOperand().bits() == 32)) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}

		{
			byte rex = DEFAULT_REX_PREFIX;
			if (inst.firstOperand().bits() == 64 && !(inst.opcode() == Opcode.CALL || inst.opcode() == Opcode.JMP)) {
				rex = BitUtils.or(rex, (byte) 0b1000);
			}
			if (inst.firstOperand() instanceof IndirectOperand io
					&& io.hasIndex()
					&& Registers.requiresExtension(io.getIndex())
					&& inst.opcode() != Opcode.JMP) {
				rex = BitUtils.or(rex, (byte) 0b0010);
			}
			if ((inst.firstOperand() instanceof Register r && Registers.requiresExtension(r))
					|| (inst.firstOperand() instanceof IndirectOperand io
							&& io.hasBase()
							&& Registers.requiresExtension(io.getBase()))
					|| (inst.opcode() == Opcode.JMP
							&& inst.firstOperand() instanceof IndirectOperand io
							&& Registers.requiresExtension(io.getIndex()))) {
				rex = BitUtils.or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX
					|| (inst.firstOperand() instanceof Register8 r && Register8.requiresRexPrefix(r))) {
				wb.write(rex);
			}
		}

		byte reg = (byte) 0;

		switch (inst.opcode()) {
			case NOP -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1f);
			case CALL -> {
				if (inst.firstOperand() instanceof Immediate) {
					wb.write((byte) 0xe8);
				} else {
					wb.write((byte) 0xff);
					reg = (inst.firstOperand() instanceof IndirectOperand io && io.bits() == 32)
							? (byte) 0b011
							: (byte) 0b010;
				}
			}
			case JA, JAE, JB, JBE, JG, JE, JL, JLE, JGE, JNE, JNS, JS, JP -> {
				if (inst.firstOperand().bits() == 8) {
					wb.write(BitUtils.asByte((byte) 0x70 + CONDITIONAL_JUMPS_OPCODES.get(inst.opcode())));
				} else if (inst.firstOperand().bits() == 32) {
					wb.write(
							DOUBLE_BYTE_OPCODE_PREFIX,
							BitUtils.asByte((byte) 0x80 + CONDITIONAL_JUMPS_OPCODES.get(inst.opcode())));
				}
			}
			case JMP -> {
				if (inst.firstOperand() instanceof Immediate imm) {
					if (imm.bits() == 8) {
						wb.write((byte) 0xeb);
					} else if (imm.bits() == 32) {
						wb.write((byte) 0xe9);
					}
				} else {
					wb.write((byte) 0xff);
					reg = (inst.firstOperand() instanceof IndirectOperand io && io.bits() == 32)
							? (byte) 0b101
							: (byte) 0b100;
				}
			}
			case IDIV, DIV, MUL -> {
				wb.write(inst.firstOperand().bits() == 8 ? (byte) 0xf6 : (byte) 0xf7);
				reg = DIV_MUL_REG_BYTES.get(inst.opcode());
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		{
			if (inst.firstOperand() instanceof Register r) {
				encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
			} else if (inst.firstOperand() instanceof IndirectOperand io) {
				encodeModRM(
						wb,
						getMod(io),
						reg,
						isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
				encodeIndirectOperand(wb, io);
			} else if (inst.firstOperand() instanceof Immediate imm) {
				if (imm.bits() == 8) {
					wb.write(imm.asByte());
				} else if (imm.bits() == 32) {
					wb.write(imm.asInt());
				} else {
					throw new IllegalArgumentException(String.format("Invalid immediate: '%s'.", imm));
				}
			} else {
				throw new IllegalArgumentException(String.format("Unknown operand: '%s'.", inst.firstOperand()));
			}
		}
	}

	private static void encodeTwoOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if (inst.firstOperand() instanceof IndirectOperand io && io.hasIndex() && io.getIndex() instanceof Register32) {
			wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
		}
		if (inst.firstOperand().bits() == 16) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}

		final boolean hasExtendedFirstRegister =
				inst.firstOperand() instanceof Register r && Registers.requiresExtension(r);
		final boolean hasExtendedSecondRegister =
				inst.secondOperand() instanceof Register r && Registers.requiresExtension(r);
		final boolean isShift =
				inst.opcode() == Opcode.SHR || inst.opcode() == Opcode.SAR || inst.opcode() == Opcode.SHL;

		// rex
		{
			byte rex = DEFAULT_REX_PREFIX;
			if (inst.firstOperand().bits() == 64) {
				rex = BitUtils.or(rex, (byte) 0b1000);
			}
			if ((hasExtendedFirstRegister
							|| (inst.firstOperand() instanceof IndirectOperand && hasExtendedSecondRegister))
					&& !(isShift && hasExtendedFirstRegister)) {
				rex = BitUtils.or(rex, (byte) 0b0100);
			}
			if ((inst.opcode() == Opcode.CMP && hasExtendedSecondRegister)
					|| ((inst.opcode() == Opcode.IMUL)
							&& inst.secondOperand() instanceof IndirectOperand io
							&& io.hasIndex()
							&& Registers.requiresExtension(io.getIndex()))
					|| (inst.opcode() == Opcode.MOV
							&& inst.firstOperand() instanceof IndirectOperand io
							&& io.hasIndex()
							&& Registers.requiresExtension(io.getIndex()))
					|| (inst.opcode() == Opcode.OR
							&& inst.firstOperand() instanceof IndirectOperand io
							&& io.hasIndex()
							&& Registers.requiresExtension(io.getIndex())
							&& !(inst.secondOperand() instanceof Register64))) {
				rex = BitUtils.or(rex, (byte) 0b0010);
			}
			if ((hasExtendedSecondRegister && !(inst.secondOperand() instanceof Register8))
					|| (isShift && hasExtendedFirstRegister)
					|| (inst.secondOperand() instanceof IndirectOperand io
							&& io.hasBase()
							&& Registers.requiresExtension(io.getBase()))
					|| (inst.opcode() == Opcode.MOV
							&& inst.firstOperand() instanceof IndirectOperand io
							&& io.hasBase()
							&& Registers.requiresExtension(io.getBase()))
					|| (inst.opcode() == Opcode.OR
							&& inst.firstOperand() instanceof IndirectOperand io
							&& io.hasBase()
							&& Registers.requiresExtension(io.getBase())
							&& !(inst.secondOperand() instanceof Register8))) {
				rex = BitUtils.or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX
					|| (inst.firstOperand() instanceof Register8 r && Register8.requiresRexPrefix(r))
					|| (inst.secondOperand() instanceof Register8 r && Register8.requiresRexPrefix(r))) {
				wb.write(rex);
			}
		}

		byte reg = 0;
		switch (inst.opcode()) {
			case CMOVE, CMOVNS, CMOVAE, CMOVB, CMOVBE, CMOVNE, CMOVG, CMOVGE, CMOVS, CMOVA, CMOVL, CMOVLE ->
				wb.write(
						DOUBLE_BYTE_OPCODE_PREFIX,
						BitUtils.asByte((byte) 0x40 + CONDITIONAL_MOVE_OPCODES.get(inst.opcode())));
			case CMP -> {
				if (inst.firstOperand() instanceof IndirectOperand io && inst.secondOperand() instanceof Register) {
					wb.write(io.bits() == 8 ? (byte) 0x38 : (byte) 0x39);
				} else if (inst.firstOperand() instanceof IndirectOperand io
						&& inst.secondOperand() instanceof Immediate imm) {
					wb.write(io.bits() == 16 ? (imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81) : (byte) 0x80);
					reg = (byte) 0b111;
				}
			}
			case MOV -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x89);
				} else if (inst.firstOperand() instanceof IndirectOperand io
						&& inst.secondOperand() instanceof Immediate) {
					wb.write(io.bits() == 32 ? (byte) 0xc7 : (byte) 0xc6);
					reg = (byte) 0b000;
				} else if (inst.firstOperand() instanceof IndirectOperand
						&& inst.secondOperand() instanceof Register8) {
					wb.write((byte) 0x88);
				}
			}
			case MOVSXD -> wb.write((byte) 0x63);
			case SUB -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x29);
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b101;
				} else {
					wb.write((byte) 0x2b);
				}
			}
			case SBB -> {
				if (inst.firstOperand().equals(Register8.AL) && inst.secondOperand() instanceof Immediate imm) {
					wb.write((byte) 0x1c);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand().equals(Register16.AX) && inst.secondOperand() instanceof Immediate imm) {
					wb.write((byte) 0x1d);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Immediate) {
					wb.write((byte) 0x81);
					reg = (byte) 0b011;
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x19);
				}
			}
			case SHR, SAR, SHL -> {
				reg = SHIFT_REG_BYTES.get(inst.opcode());
				if (inst.firstOperand() instanceof Register r && inst.secondOperand() instanceof Immediate imm) {
					final boolean isImmediateOne = imm.bits() == 8 && imm.asByte() == (byte) 1;
					wb.write(BitUtils.or(
							isImmediateOne ? (byte) 0xd0 : (byte) 0xc0,
							(r instanceof Register8) ? (byte) 0 : (byte) 1));
					if (isImmediateOne) {
						encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
						return;
					}
				} else if (inst.firstOperand() instanceof Register r1 && inst.secondOperand() instanceof Register r2) {
					wb.write(r1 instanceof Register8 ? (byte) 0xd2 : (byte) 0xd3);
					if (r2.equals(Register8.CL)) {
						encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r1));
					}
					return;
				}
			}
			case IMUL -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xaf);
			case OR -> {
				if (inst.firstOperand() instanceof IndirectOperand) {
					wb.write(BitUtils.asByte(((inst.secondOperand() instanceof Immediate) ? (byte) 0x80 : (byte) 0x08)
							+ ((inst.secondOperand().bits() == 8) ? (byte) 0 : (byte) 1)));
				}
				reg = (byte) 001;
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof Register r1 && inst.secondOperand() instanceof Register r2) {
			// Most ALU operations encode the destination operand (the first one) in the r/m portion, while some
			// instructions like MOV encode the destination as Reg
			// FIXME: actually, it seems to be the opposite... why?
			if (inst.opcode() == Opcode.MOV || inst.opcode() == Opcode.SUB) {
				encodeModRM(wb, (byte) 0b11, Registers.toByte(r2), Registers.toByte(r1));
			} else {
				encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r2));
			}
		} else if (inst.firstOperand() instanceof Register r && inst.secondOperand() instanceof IndirectOperand io) {
			encodeModRM(wb, (byte) 0b10, Registers.toByte(r), (byte) 0b100);
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof IndirectOperand io && inst.secondOperand() instanceof Register r) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof IndirectOperand io && inst.secondOperand() instanceof Immediate imm) {
			final boolean hasRBP =
					io.hasIndex() && (io.getIndex() == Register32.EBP || io.getIndex() == Register64.RBP);
			encodeModRM(
					wb,
					getMod(io),
					reg,
					(isSimpleIndirectOperand(io) && !hasRBP) ? Registers.toByte(io.getIndex()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
			encodeImmediate(wb, imm);
		} else if (inst.firstOperand() instanceof Register r && inst.secondOperand() instanceof Immediate imm) {
			encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
			encodeImmediate(wb, imm);
		}
	}

	private static void encodeThreeOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		// rex
		{
			byte rex = DEFAULT_REX_PREFIX;
			if (inst.firstOperand().bits() == 64) {
				rex = BitUtils.or(rex, (byte) 0b1000);
			}
			if (inst.firstOperand() instanceof Register r && Registers.requiresExtension(r)) {
				rex = BitUtils.or(rex, (byte) 0b0100);
			}
			if (inst.secondOperand() instanceof IndirectOperand io
					&& io.hasBase()
					&& Registers.requiresExtension(io.getBase())) {
				rex = BitUtils.or(rex, (byte) 0b0010);
			}
			if ((inst.secondOperand() instanceof IndirectOperand io
							&& io.hasIndex()
							&& Registers.requiresExtension(io.getIndex()))
					|| (inst.secondOperand() instanceof Register r && Registers.requiresExtension(r))) {
				rex = BitUtils.or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX) {
				wb.write(rex);
			}
		}

		switch (inst.opcode()) {
			case IMUL -> wb.write((inst.secondOperand() instanceof IndirectOperand) ? (byte) 0x69 : (byte) 0x6b);
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof Register r1
				&& inst.secondOperand() instanceof Register r2
				&& inst.thirdOperand() instanceof Immediate imm) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r2));
			encodeImmediate(wb, imm);
		} else if (inst.firstOperand() instanceof Register r1
				&& inst.secondOperand() instanceof IndirectOperand io
				&& inst.thirdOperand() instanceof Immediate imm) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r1),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
			encodeImmediate(wb, imm);
		}
	}

	private static byte getMod(final IndirectOperand io) {
		final boolean hasShortDisplacement = io.hasDisplacement() && io.getDisplacementBits() == 8;
		final boolean hasLongDisplacement = io.hasDisplacement() && io.getDisplacementBits() == 32;
		return hasShortDisplacement ? (byte) 0b01 : (hasLongDisplacement ? (byte) 0b10 : 0b00);
	}

	private static void encodeModRM(final WriteOnlyByteBuffer wb, final byte mod, final byte reg, final byte rm) {
		wb.write(BitUtils.or(BitUtils.shl(mod, 6), BitUtils.shl(reg, 3), rm));
	}

	private static byte getScale(final IndirectOperand io) {
		return io.hasScale()
				? switch (io.getScale()) {
					case 1 -> (byte) 0b00;
					case 2 -> (byte) 0b01;
					case 4 -> (byte) 0b10;
					case 8 -> (byte) 0b11;
					default -> throw new IllegalArgumentException(String.format("Invalid scale: %,d.", io.getScale()));
				}
				: (byte) 0b00;
	}

	private static void encodeIndirectOperand(final WriteOnlyByteBuffer wb, final IndirectOperand io) {
		final boolean hasRBP = io.hasBase() && (io.getBase() == Register32.EBP || io.getBase() == Register64.RBP);
		if (!isSimpleIndirectOperand(io) || hasRBP) {
			final byte base = hasRBP ? (byte) 0b100 : (io.hasBase() ? Registers.toByte(io.getBase()) : (byte) 0);
			encodeSIB(wb, getScale(io), Registers.toByte(io.getIndex()), base);
		}

		if (io.hasDisplacement() || hasRBP) {
			if (io.getDisplacementBits() == 8) {
				wb.write(BitUtils.asByte(io.getDisplacement()));
			} else if (io.getDisplacementBits() == 32) {
				wb.write(BitUtils.asInt(io.getDisplacement()));
			} else {
				throw new IllegalArgumentException(
						String.format("Unknown displacement in indirect operand: '%s'.", io));
			}
		}
	}

	private static void encodeSIB(final WriteOnlyByteBuffer wb, final byte scale, final byte index, final byte base) {
		wb.write(BitUtils.or(BitUtils.shl(scale, 6), BitUtils.shl(index, 3), base));
	}

	private static boolean isSimpleIndirectOperand(final IndirectOperand io) {
		return io.hasBase() && !io.hasIndex() && !io.hasScale() && !io.hasDisplacement();
	}

	private static void encodeImmediate(final WriteOnlyByteBuffer wb, final Immediate imm) {
		if (imm.bits() == 8) {
			wb.write(imm.asByte());
		} else if (imm.bits() == 16) {
			wb.write(imm.asShort());
		} else if (imm.bits() == 32) {
			wb.write(imm.asInt());
		} else if (imm.bits() == 64) {
			wb.write(imm.asLong());
		} else {
			throw new IllegalArgumentException(String.format("Unknown immediate: '%s'.", imm));
		}
	}
}
