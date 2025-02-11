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

import java.util.Map;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

public final class InstructionEncoder {

	private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
	private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
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

	private InstructionEncoder() {}

	public static byte[] encode(final Instruction... code) {
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		for (final Instruction inst : code) {
			encode(wb, inst);
		}
		return wb.array();
	}

	public static byte[] encode(final Instruction inst) {
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		encode(wb, inst);
		return wb.array();
	}

	private static int countOperands(final Instruction inst) {
		if (!inst.hasFirstOperand()) {
			return 0;
		} else if (!inst.hasSecondOperand()) {
			return 1;
		} else {
			return 2;
		}
	}

	private static void encode(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (countOperands(inst)) {
			case 0 -> encodeZeroOperandsInstruction(wb, inst);
			case 1 -> encodeSingleOperandInstruction(wb, inst);
			case 2 -> encodeTwoOperandsInstruction(wb, inst);
			default -> throw new IllegalArgumentException(String.format(
					"Unknown instruction with %,d operands ('%s').", countOperands(inst), inst.toIntelSyntax()));
		}
	}

	private static void encodeZeroOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case Opcode.NOP -> wb.write((byte) 0x90);
			case Opcode.CWDE -> wb.write((byte) 0x98);
			case Opcode.CDQ -> wb.write((byte) 0x99);
			case Opcode.SAHF -> wb.write((byte) 0x9e);
			case Opcode.LAHF -> wb.write((byte) 0x9f);
			case Opcode.DEC -> wb.write((byte) 0xff);
			case Opcode.XGETBV -> wb.write((byte) 0x0f, (byte) 0x01, (byte) 0xd0);
			case Opcode.UD2 -> wb.write((byte) 0x0f, (byte) 0x0b);
			case Opcode.CDQE -> wb.write((byte) 0x48, (byte) 0x98);
			case Opcode.SETG -> wb.write((byte) 0x0f, (byte) 0x9f);
			case Opcode.BSWAP -> wb.write((byte) 0x0f, (byte) 0xcc);
			case Opcode.ENDBR64 -> wb.write((byte) 0xf3, (byte) 0x0f, (byte) 0x1e, (byte) 0xfa);
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static void encodeSingleOperandInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case Opcode.NOP -> {
				if (inst.firstOperand() instanceof IndirectOperand io
						&& io.index().bits() == 32) {
					wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
				}
				if (inst.firstOperand().bits() == 16) {
					wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
				} else if (inst.firstOperand().bits() == 64) {
					wb.write((byte) 0x48);
				}
				wb.write((byte) 0x0f, (byte) 0x1f);
				if (inst.firstOperand() instanceof Register) {
					wb.write(BitUtils.asByte((byte) 0xc0 + Registers.toByte((Register) inst.firstOperand())));
				} else {
					encodeIndirectOperand(wb, (IndirectOperand) inst.firstOperand());
				}
			}
			case Opcode.CALL -> {
				if (inst.firstOperand() instanceof RelativeOffset ro) {
					wb.write((byte) 0xe8);
					wb.write(BitUtils.asInt(ro.getValue()));
				} else if (inst.firstOperand() instanceof Register r) {
					encodeRexPrefix(wb, inst, false);
					wb.write((byte) 0xff);
					encodeSingleRegister(wb, r, (byte) 0xd0);
				} else if (inst.firstOperand() instanceof IndirectOperand io) {
					wb.write((byte) 0xff);
					encodeIndirectOperand(wb, io);
				}
			}

				// Conditional jumps
			case Opcode.JA,
					Opcode.JAE,
					Opcode.JB,
					Opcode.JBE,
					Opcode.JG,
					Opcode.JGE,
					Opcode.JE,
					Opcode.JNE,
					Opcode.JL,
					Opcode.JLE,
					Opcode.JS,
					Opcode.JNS,
					Opcode.JP -> {
				final RelativeOffset ro = (RelativeOffset) inst.firstOperand();
				if (ro.bits() == 8) {
					wb.write(
							BitUtils.asByte(0x70 + CONDITIONAL_JUMPS_OPCODES.get(inst.opcode())),
							BitUtils.asByte(ro.getValue()));
				} else {
					wb.write((byte) 0x0f, BitUtils.asByte(0x80 + CONDITIONAL_JUMPS_OPCODES.get(inst.opcode())));

					wb.write(BitUtils.asInt(ro.getValue()));
				}
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static void encodeTwoOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case Opcode.MOV -> {
				if (inst.firstOperand() instanceof Register r1 && inst.secondOperand() instanceof Register r2) {
					encodeRexPrefix(wb, inst, true);
					wb.write((byte) 0x89);
					byte x = (byte) 0b11000000;
					x = BitUtils.or(x, BitUtils.shl(Registers.toByte(r2), 3));
					x = BitUtils.or(x, Registers.toByte(r1));
					wb.write(x);
				}
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static void encodeIndirectOperand(final WriteOnlyByteBuffer wb, final IndirectOperand io) {
		wb.write((byte) 0x00);
	}

	private static void encodeRexPrefix(
			final WriteOnlyByteBuffer wb, final Instruction inst, final boolean encode64Bits) {
		byte rex = (byte) 0x40;
		if (encode64Bits && inst.firstOperand() instanceof Register64) {
			rex = BitUtils.or(rex, (byte) 0x08);
		}
		if (Registers.requiresExtension((Register) inst.firstOperand())) {
			rex = BitUtils.or(rex, (byte) 0x01);
		}
		if (inst.hasSecondOperand()
				&& inst.secondOperand() instanceof Register32 r2
				&& Register32.requiresExtension(r2)) {
			rex = BitUtils.or(rex, (byte) 0x04);
		}
		if (rex != (byte) 0x40) {
			wb.write(rex);
		}
	}

	// TODO: remove baseValue and implement proper ModRM encoding
	private static void encodeSingleRegister(final WriteOnlyByteBuffer wb, final Register r, final byte baseValue) {
		switch (r) {
			case Register8 r8 -> {}
			case Register16 r16 -> {}
			case Register32 r32 -> wb.write(BitUtils.asByte(baseValue + Register32.toByte(r32)));
			case Register64 r64 -> wb.write(BitUtils.asByte(baseValue + Register64.toByte(r64)));
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
		}
	}
}
