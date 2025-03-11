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
			// case 3 -> encodeThreeOperandsInstruction(wb, inst);
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
		if (inst.firstOperand() instanceof IndirectOperand io && io.getIndex() instanceof Register32) {
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
			if (rex != DEFAULT_REX_PREFIX) {
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
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		{
			byte modrm = (byte) 0;
			if (inst.firstOperand() instanceof Register r) {
				modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b11, 6));
				modrm = BitUtils.or(modrm, BitUtils.shl(reg, 3));
				modrm = BitUtils.or(modrm, Registers.toByte(r));
				wb.write(modrm);
			} else if (inst.firstOperand() instanceof IndirectOperand io) {
				// Setting mod
				if (io.hasDisplacement() && io.getDisplacementBits() == 8) {
					modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b01, 6));
				} else if (io.hasDisplacement() && io.getDisplacementBits() == 32) {
					modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b10, 6));
				}

				// Setting reg
				modrm = BitUtils.or(modrm, BitUtils.shl(reg, 3));

				// Setting r/m
				if (isSimpleIndirectOperand(io)) {
					modrm = BitUtils.or(modrm, Registers.toByte(io.getIndex()));
				} else {
					modrm = BitUtils.or(modrm, (byte) 0b100);
				}

				wb.write(modrm);

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
		if (inst.firstOperand() instanceof IndirectOperand io && io.getIndex() instanceof Register32) {
			wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
		}
		if (inst.firstOperand().bits() == 16) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}

		// rex
		{
			byte rex = DEFAULT_REX_PREFIX;
			if (inst.firstOperand().bits() == 64) {
				rex = BitUtils.or(rex, (byte) 0b1000);
			}
			if ((inst.firstOperand() instanceof Register r && Registers.requiresExtension(r))
					|| (inst.firstOperand() instanceof IndirectOperand
							&& inst.secondOperand() instanceof Register r2
							&& Registers.requiresExtension(r2))) {
				rex = BitUtils.or(rex, (byte) 0b0100);
			}
			if (inst.opcode() == Opcode.CMP
					&& inst.secondOperand() instanceof Register r
					&& Registers.requiresExtension(r)) {
				rex = BitUtils.or(rex, (byte) 0b0010);
			}
			if ((inst.secondOperand() instanceof Register r && Registers.requiresExtension(r))
					|| (inst.secondOperand() instanceof IndirectOperand io
							&& io.hasBase()
							&& Registers.requiresExtension(io.getBase()))) {
				rex = BitUtils.or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX) {
				wb.write(rex);
			}
		}

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
				}
			}
			case MOV -> {
				wb.write((byte) 0x89);
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		// modrm
		{
			byte modrm = 0;
			if (inst.firstOperand() instanceof Register r1 && inst.secondOperand() instanceof Register r2) {
				modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b11, 6));
				modrm = BitUtils.or(modrm, BitUtils.shl(Registers.toByte(r1), 3));
				modrm = BitUtils.or(modrm, Registers.toByte(r2));
				wb.write(modrm);
			} else if (inst.firstOperand() instanceof Register r
					&& inst.secondOperand() instanceof IndirectOperand io) {
				modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b10, 6));
				modrm = BitUtils.or(modrm, BitUtils.shl(Registers.toByte(r), 3));
				modrm = BitUtils.or(modrm, (byte) 0b100);
				wb.write(modrm);
				encodeIndirectOperand(wb, io);
			} else if (inst.firstOperand() instanceof IndirectOperand io
					&& inst.secondOperand() instanceof Register r) {
				modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b00, 6));
				modrm = BitUtils.or(modrm, BitUtils.shl(Registers.toByte(r), 3));
				modrm = BitUtils.or(
						modrm, isSimpleIndirectOperand(io) ? Registers.toByte(io.getIndex()) : (byte) 0b100);
				wb.write(modrm);
				encodeIndirectOperand(wb, io);
			} else if (inst.firstOperand() instanceof IndirectOperand io
					&& inst.secondOperand() instanceof Immediate imm) {
				modrm = BitUtils.or(
						modrm,
						BitUtils.shl(
								isSimpleIndirectOperand(io)
										? (byte) 0b00
										: (io.getDisplacementBits() == 8 ? (byte) 0b01 : (byte) 0b10),
								6));
				modrm = BitUtils.or(modrm, BitUtils.shl((byte) 0b111, 3));
				modrm = BitUtils.or(
						modrm, isSimpleIndirectOperand(io) ? Registers.toByte(io.getIndex()) : (byte) 0b100);
				wb.write(modrm);
				encodeIndirectOperand(wb, io);
				encodeImmediate(wb, imm);
			}
		}
	}

	private static void encodeIndirectOperand(final WriteOnlyByteBuffer wb, final IndirectOperand io) {
		final boolean isWeirdIndirectOperand =
				isSimpleIndirectOperand(io) && (io.getIndex() == Register32.ESP || io.getIndex() == Register64.RSP);
		if (!isSimpleIndirectOperand(io) || isWeirdIndirectOperand) {
			final byte base =
					isWeirdIndirectOperand ? (byte) 0b100 : (io.hasBase() ? Registers.toByte(io.getBase()) : (byte) 0);
			wb.write(BitUtils.or(
					BitUtils.shl(
							switch (io.getScale()) {
								case 1 -> (byte) 0b00;
								case 2 -> (byte) 0b01;
								case 4 -> (byte) 0b10;
								case 8 -> (byte) 0b11;
								default ->
									throw new IllegalArgumentException(
											String.format("Invalid scale: %,d.", io.getScale()));
							},
							6),
					BitUtils.or(BitUtils.shl(Registers.toByte(io.getIndex()), 3), base)));
		}

		if (io.hasDisplacement()) {
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

	private static boolean isSimpleIndirectOperand(final IndirectOperand io) {
		return !io.hasBase() && io.hasIndex() && !io.hasScale() && !io.hasDisplacement();
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
