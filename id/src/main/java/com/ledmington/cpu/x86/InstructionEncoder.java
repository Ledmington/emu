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
	private static final byte DOUBLE_BYTE_OPCODE_PREFIX = (byte) 0x0f;
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
			case Opcode.XGETBV -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x01, (byte) 0xd0);
			case Opcode.UD2 -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x0b);
			case Opcode.CDQE -> wb.write((byte) 0x48, (byte) 0x98);
			case Opcode.SETG -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x9f);
			case Opcode.BSWAP -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xcc);
			case Opcode.ENDBR64 -> wb.write((byte) 0xf3, DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1e, (byte) 0xfa);
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
				}
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1f);
				if (inst.firstOperand() instanceof Register) {
					wb.write(BitUtils.asByte((byte) 0xc0 + Registers.toByte((Register) inst.firstOperand())));
				} else if (inst.firstOperand() instanceof IndirectOperand) {
					encodeModRM(wb, inst);
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
					if (io.bits() == 32) {
						wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
					}
					if (io.index().bits() == 32) {
						wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
					}
					wb.write((byte) 0xff);
					encodeModRM(wb, inst);
				}
			}
			case Opcode.INC, Opcode.DEC -> {
				encodeRexPrefix(wb, inst);
				if (inst.firstOperand() instanceof Register8) {
					wb.write((byte) 0xfe);
				} else {
					wb.write((byte) 0xff);
				}
				encodeSingleRegister(
						wb, (Register) inst.firstOperand(), inst.opcode() == Opcode.DEC ? (byte) 0xc8 : (byte) 0xc0);
			}
			case Opcode.NOT -> {}
			case Opcode.MUL -> {}

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
					wb.write(
							DOUBLE_BYTE_OPCODE_PREFIX,
							BitUtils.asByte(0x80 + CONDITIONAL_JUMPS_OPCODES.get(inst.opcode())));

					wb.write(BitUtils.asInt(ro.getValue()));
				}
			}

			case Opcode.INCSSPQ -> {
				wb.write((byte) 0xf3);
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xae);
				encodeSingleRegister(wb, (Register) inst.firstOperand(), (byte) 0xe8);
			}
			case Opcode.RDSSPQ -> {
				wb.write((byte) 0xf3);
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1e);
				encodeSingleRegister(wb, (Register) inst.firstOperand(), (byte) 0xc8);
			}
			case Opcode.RDSEED -> {
				if (inst.firstOperand().bits() == 16) {
					wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
				}
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc7);
				encodeSingleRegister(wb, (Register) inst.firstOperand(), (byte) 0xf8);
			}
			case Opcode.RDRAND -> {
				if (inst.firstOperand().bits() == 16) {
					wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
				}
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc7);
				encodeSingleRegister(wb, (Register) inst.firstOperand(), (byte) 0xf0);
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static void encodeTwoOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case Opcode.MOV -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					encodeRexPrefix(wb, inst);
					wb.write((byte) 0x89);
					encodeModRM(wb, inst);
				}
			}
			case Opcode.MOVSXD -> {
				if (inst.firstOperand() instanceof Register64 r1 && inst.secondOperand() instanceof Register32 r2) {
					byte rex = (byte) 0x48;
					if (Register64.requiresExtension(r1)) {
						rex = BitUtils.or(rex, (byte) 0x04);
					}
					if (Register32.requiresExtension(r2)) {
						rex = BitUtils.or(rex, (byte) 0x01);
					}
					wb.write(rex);
					wb.write((byte) 0x63);
					wb.write(BitUtils.or(
							(byte) 0b11000000,
							BitUtils.or(BitUtils.shl(Register64.toByte(r1), 3), Register32.toByte(r2))));
				}
			}
			case Opcode.PCMPEQD -> {
				wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x76);
				encodeModRM(wb, inst);
			}
			case Opcode.XADD -> {
				if (inst.firstOperand().bits() == 16) {
					wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
				}
				encodeRexPrefix(wb, inst);
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc1);
				encodeModRM(wb, inst);
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static void encodeRexPrefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		encodeRexPrefix(wb, inst, true);
	}

	private static void encodeRexPrefix(
			final WriteOnlyByteBuffer wb, final Instruction inst, final boolean encode64Bits) {
		final byte baseRexPrefix = (byte) 0x40;
		byte rex = baseRexPrefix;
		if (encode64Bits && inst.firstOperand().bits() == 64) {
			rex = BitUtils.or(rex, (byte) 0b1000);
		}
		if (inst.firstOperand() instanceof Register r1 && Registers.requiresExtension(r1)) {
			rex = BitUtils.or(rex, (byte) 0b0001);
		} else if (inst.firstOperand() instanceof IndirectOperand io && Registers.requiresExtension(io.index())) {
			rex = BitUtils.or(rex, (byte) 0b0010);
		}
		if (inst.hasSecondOperand()) {
			if (inst.secondOperand() instanceof Register r2 && Registers.requiresExtension(r2)) {
				rex = BitUtils.or(rex, (byte) 0b0100);
			}
		}
		if (rex != baseRexPrefix
				|| (inst.hasFirstOperand()
						&& inst.firstOperand() instanceof Register8 r8
						&& Register8.requiresRexPrefix(r8))) {
			wb.write(rex);
		}
	}

	private static void encodeModRM(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if (inst.firstOperand() instanceof Register r1 && inst.secondOperand() instanceof Register r2) {
			encodePairOfRegisters(wb, (byte) 0b11000000, r1, r2);
		} else if (inst.firstOperand() instanceof IndirectOperand io) {
			// 0x 16
			// 0b 00|010|110
			if (io.base() == null && io.index() != null && io.scale() == 1L && io.getDisplacement() == 0L) {
				// simple indirect operand
				if (inst.hasSecondOperand()) {
					encodePairOfRegisters(wb, (byte) 0, io.index(), (Register) inst.secondOperand());
				} else {
					encodeSingleRegister(wb, io.index(), (byte) 0);
				}
			} else {
				// generic indirect operand
				if (inst.hasSecondOperand()) {
					wb.write(
							// mod=0b10, R/M=0b100
							BitUtils.or(
									(byte) 0b10000100,
									BitUtils.shl(Registers.toByte((Register) inst.secondOperand()), 3)));
				} else {
					wb.write(
							// mod=0b10, R/M=0b100
							(byte) 0b10000100);
				}
				encodeSIB(wb, io);
				wb.write(BitUtils.asInt(io.getDisplacement()));
			}
		}
	}

	private static void encodeSIB(final WriteOnlyByteBuffer wb, final IndirectOperand io) {
		final byte scale =
				switch (BitUtils.asInt(io.scale())) {
					case 1 -> (byte) 0b00;
					case 2 -> (byte) 0b01;
					case 4 -> (byte) 0b10;
					case 8 -> (byte) 0b11;
					default -> throw new IllegalArgumentException(
							String.format("Invalid scale value: %,d.", io.scale()));
				};
		byte x = BitUtils.shl(scale, 6);
		x = BitUtils.or(x, BitUtils.shl(Registers.toByte(io.index()), 3));
		x = BitUtils.or(x, Registers.toByte(io.base()));
		wb.write(x);
	}

	private static void encodePairOfRegisters(
			final WriteOnlyByteBuffer wb, final byte mod, final Register r1, final Register r2) {
		if (BitUtils.and(mod, (byte) 0b00111111) != (byte) 0) {
			throw new AssertionError(String.format("Invalid Mod: 0x%02x.", mod));
		}
		byte x = mod;
		x = BitUtils.or(x, BitUtils.shl(Registers.toByte(r2), 3));
		x = BitUtils.or(x, Registers.toByte(r1));
		wb.write(x);
	}

	// TODO: remove baseValue and implement proper ModRM encoding
	private static void encodeSingleRegister(final WriteOnlyByteBuffer wb, final Register r, final byte baseValue) {
		switch (r) {
			case Register8 r8 -> wb.write(BitUtils.asByte(baseValue + Register8.toByte(r8)));
			case Register16 r16 -> wb.write(BitUtils.asByte(baseValue + Register16.toByte(r16)));
			case Register32 r32 -> wb.write(BitUtils.asByte(baseValue + Register32.toByte(r32)));
			case Register64 r64 -> wb.write(BitUtils.asByte(baseValue + Register64.toByte(r64)));
			default -> throw new IllegalArgumentException(String.format("Unknown register %s.", r));
		}
	}
}
