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

import static com.ledmington.utils.BitUtils.and;
import static com.ledmington.utils.BitUtils.asByte;
import static com.ledmington.utils.BitUtils.asInt;
import static com.ledmington.utils.BitUtils.not;
import static com.ledmington.utils.BitUtils.or;
import static com.ledmington.utils.BitUtils.shl;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * Encodes an x86 instruction to either binary or intel syntax. NOTE: prefix are encoded in a specific order. First
 * there are the LOCK/REP/REPNE, then the CS segment override, then the address size override, then the operand size
 * override.
 */
public final class InstructionEncoder {

	private static final byte DEFAULT_REX_PREFIX = (byte) 0x40;
	private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
	private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
	private static final byte DOUBLE_BYTE_OPCODE_PREFIX = (byte) 0x0f;
	private static final byte TABLE_A4_PREFIX = (byte) 0x38;
	private static final byte TABLE_A5_PREFIX = (byte) 0x3a;
	private static final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
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
	private static final Map<Opcode, Byte> SET_OPCODES = Map.ofEntries(
			Map.entry(Opcode.SETO, (byte) 0x00),
			Map.entry(Opcode.SETNO, (byte) 0x01),
			Map.entry(Opcode.SETB, (byte) 0x02),
			Map.entry(Opcode.SETAE, (byte) 0x03),
			Map.entry(Opcode.SETE, (byte) 0x04),
			Map.entry(Opcode.SETNE, (byte) 0x05),
			Map.entry(Opcode.SETBE, (byte) 0x06),
			Map.entry(Opcode.SETA, (byte) 0x07),
			Map.entry(Opcode.SETL, (byte) 0x0c),
			Map.entry(Opcode.SETGE, (byte) 0x0d),
			Map.entry(Opcode.SETLE, (byte) 0x0e),
			Map.entry(Opcode.SETG, (byte) 0x0f));

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
		if (op instanceof final IndirectOperand io) {
			final boolean requiresExplicitPointerSize = code != Opcode.LEA && code != Opcode.LDDQU;
			return io.toIntelSyntax(requiresExplicitPointerSize);
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
		InstructionChecker.check(inst);
		final StringBuilder sb = new StringBuilder();
		if (inst.hasPrefix()) {
			sb.append(inst.getPrefix().name().toLowerCase(Locale.US)).append(' ');
		}

		sb.append(inst.opcode().mnemonic());

		if (inst.hasFirstOperand()) {
			sb.append(' ').append(operandString(inst.opcode(), inst.firstOperand()));
			if (inst.hasDestinationMask()) {
				sb.append('{').append(inst.getDestinationMask().toIntelSyntax()).append('}');
			}
			if (inst.hasSecondOperand()) {
				sb.append(',').append(operandString(inst.opcode(), inst.secondOperand()));
				if (inst.hasThirdOperand()) {
					sb.append(',').append(operandString(inst.opcode(), inst.thirdOperand()));
					if (inst.hasFourthOperand()) {
						sb.append(',').append(operandString(inst.opcode(), inst.fourthOperand()));
					}
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

	private static void toHex(final WriteOnlyByteBuffer wb, final Instruction inst) {
		InstructionChecker.check(inst);
		switch (inst.getNumOperands()) {
			case 0 -> encodeZeroOperandsInstruction(wb, inst);
			case 1 -> encodeSingleOperandInstruction(wb, inst);
			case 2 -> encodeTwoOperandsInstruction(wb, inst);
			case 3 -> encodeThreeOperandsInstruction(wb, inst);
			case 4 -> encodeFourOperandsInstruction(wb, inst);
			default ->
				throw new IllegalArgumentException(String.format(
						"Unknown instruction with %,d operands: '%s'.", inst.getNumOperands(), toIntelSyntax(inst)));
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
			case CPUID -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xa2);
			case XGETBV -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x01, (byte) 0xd0);
			case UD2 -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x0b);
			case SYSCALL -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x05);
			case CDQE -> wb.write((byte) 0x48, (byte) 0x98);
			case SETG -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x9f);
			case BSWAP -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xcc);
			case ENDBR64 -> wb.write((byte) 0xf3, DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1e, (byte) 0xfa);
			case VZEROALL -> wb.write((byte) 0xc5, (byte) 0xfc, (byte) 0x77);
			case SFENCE -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xae, (byte) 0xf8);
			case CLD -> wb.write((byte) 0xfc);
			case STD -> wb.write((byte) 0xfd);
			case XTEST -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x01, (byte) 0xd6);
			default -> throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", inst.opcode()));
		}
	}

	private static boolean requiresAddressSizeOverride(final Operand op) {
		return op instanceof final IndirectOperand io && io.hasBase() && io.getBase() instanceof Register32;
	}

	private static boolean hasExtendedIndex(final Operand op) {
		return op instanceof final IndirectOperand io && io.hasIndex() && Registers.requiresExtension(io.getIndex());
	}

	private static boolean hasExtendedBase(final Operand op) {
		return op instanceof final IndirectOperand io && io.hasBase() && Registers.requiresExtension(io.getBase());
	}

	private static boolean isSP(final Register r) {
		return r == Register32.ESP || r == Register32.R12D || r == Register64.RSP || r == Register64.R12;
	}

	private static boolean hasOnlyBase(final IndirectOperand io) {
		return io.hasBase() && !io.hasIndex() && !io.hasScale();
	}

	private static void encodeSingleOperandInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if (inst.firstOperand() instanceof final IndirectOperand io
				&& io.hasSegment()
				&& io.getSegment() == Register16.CS) {
			wb.write(CS_SEGMENT_OVERRIDE_PREFIX);
		}
		if (requiresAddressSizeOverride(inst.firstOperand())) {
			wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
		}
		if (inst.firstOperand().bits() == 16
				|| ((inst.opcode() == Opcode.CALL || inst.opcode() == Opcode.JMP)
						&& inst.firstOperand() instanceof IndirectOperand
						&& inst.firstOperand().bits() == 32)) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}

		if (inst.opcode() == Opcode.RDSSPQ || inst.opcode() == Opcode.INCSSPQ) {
			wb.write((byte) 0xf3);
		}

		{
			byte rex = DEFAULT_REX_PREFIX;
			if (inst.firstOperand().bits() == 64
					&& !(inst.opcode() == Opcode.CALL
							|| inst.opcode() == Opcode.JMP
							|| inst.opcode() == Opcode.PUSH
							|| inst.opcode() == Opcode.POP)) {
				rex = or(rex, (byte) 0b1000);
			}
			if (hasExtendedIndex(inst.firstOperand()) && inst.opcode() != Opcode.JMP) {
				rex = or(rex, (byte) 0b0010);
			}
			if ((inst.firstOperand() instanceof final Register r && Registers.requiresExtension(r))
					|| (hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.JMP && hasExtendedIndex(inst.firstOperand()))) {
				rex = or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX
					|| (inst.firstOperand() instanceof final Register8 r && Register8.requiresRexPrefix(r))) {
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
					reg = (inst.firstOperand() instanceof final IndirectOperand io
									&& io.getPointerSize() == PointerSize.DWORD_PTR)
							? (byte) 0b011
							: (byte) 0b010;

					if (inst.firstOperand() instanceof final IndirectOperand io && isIpAndOffset(io)) {
						wb.write((byte) 0x15);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case JA, JAE, JB, JBE, JG, JE, JL, JLE, JGE, JNE, JNS, JS, JP -> {
				final byte c = CONDITIONAL_JUMPS_OPCODES.get(inst.opcode());
				if (inst.firstOperand().bits() == 8) {
					wb.write(asByte((byte) 0x70 + c));
				} else if (inst.firstOperand().bits() == 32) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, asByte((byte) 0x80 + c));
				}
			}
			case JMP -> {
				if (inst.firstOperand() instanceof final Immediate imm) {
					if (imm.bits() == 8) {
						wb.write((byte) 0xeb);
					} else if (imm.bits() == 32) {
						wb.write((byte) 0xe9);
					}
				} else {
					wb.write((byte) 0xff);
					reg = (inst.firstOperand() instanceof final IndirectOperand io
									&& io.getPointerSize() == PointerSize.DWORD_PTR)
							? (byte) 0b101
							: (byte) 0b100;
				}
			}
			case IDIV, DIV, MUL -> {
				wb.write(inst.firstOperand().bits() == 8 ? (byte) 0xf6 : (byte) 0xf7);
				reg = DIV_MUL_REG_BYTES.get(inst.opcode());
			}
			case PUSH -> {
				if (inst.firstOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x6a : (byte) 0x68);
				} else if (inst.firstOperand() instanceof final Register r) {
					wb.write(asByte((byte) 0x50 + Registers.toByte(r)));
					return;
				} else if (inst.firstOperand() instanceof IndirectOperand) {
					wb.write((byte) 0xff);
					reg = (byte) 0b110;
				}
			}
			case POP -> {
				if (inst.firstOperand() instanceof final Register r) {
					wb.write(asByte((byte) 0x58 + Registers.toByte(r)));
					return;
				}
			}
			case NOT -> {
				wb.write((byte) 0xf7);
				reg = (byte) 0b010;
			}
			case NEG -> {
				wb.write((byte) 0xf7);
				reg = (byte) 0b011;
			}
			case SETB, SETO, SETNO, SETAE, SETE, SETNE, SETBE, SETA, SETL, SETGE, SETLE, SETG ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, or((byte) 0x90, SET_OPCODES.get(inst.opcode())));
			case INC, DEC -> {
				if (inst.firstOperand().bits() == 8) {
					wb.write((byte) 0xfe);
				} else {
					wb.write((byte) 0xff);
				}
				reg = (inst.opcode() == Opcode.INC) ? (byte) 0b000 : (byte) 0b001;
			}
			case BSWAP -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, or((byte) 0xc8, Registers.toByte((Register) inst.firstOperand())));
				return;
			}
			case PREFETCHNTA, PREFETCHT0, PREFETCHT1, PREFETCHT2 -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x18);
				reg = PREFETCH_OPCODES.get(inst.opcode());
			}
			case RDRAND, RDSEED -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc7);
				reg = (inst.opcode() == Opcode.RDRAND) ? (byte) 0b110 : (byte) 0b111;
			}
			case RDSSPQ -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1e);
				reg = (byte) 0b001;
			}
			case INCSSPQ -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xae);
				reg = (byte) 0b101;
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof final Register r) {
			encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
		} else if (inst.firstOperand() instanceof final IndirectOperand io) {
			encodeModRM(
					wb, getMod(io), reg, isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if ((inst.opcode() == Opcode.CALL || inst.opcode() == Opcode.INC || inst.opcode() == Opcode.DEC)
					&& io.hasBase()
					&& isSP(io.getBase())) {
				// FIXME: this can be replaced with wb.write((byte) 0x24);
				encodeSIB(wb, (byte) 0b00, (byte) 0b100, (byte) 0b100);
			}
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof final Immediate imm) {
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

	private static boolean isDefaultMovsSource(final Operand op) {
		return op instanceof final IndirectOperand io
				&& io.hasBase()
				&& (io.getBase() == Register64.RSI || io.getBase() == Register32.ESI)
				&& io.hasSegment()
				&& io.getSegment() == Register16.DS
				&& !io.hasIndex()
				&& !io.hasScale()
				&& !io.hasDisplacement();
	}

	private static boolean isDefaultMovsDestination(final Operand op) {
		return op instanceof final IndirectOperand io
				&& io.hasBase()
				&& (io.getBase() == Register64.RDI || io.getBase() == Register32.EDI)
				&& io.hasSegment()
				&& io.getSegment() == Register16.ES
				&& !io.hasIndex()
				&& !io.hasScale()
				&& !io.hasDisplacement();
	}

	// Checks that the given IndirectOperand is [eip + offset] or [rip + offset]
	private static boolean isIpAndOffset(final IndirectOperand io) {
		return io.hasBase()
				&& (io.getBase() == Register32.EIP || io.getBase() == Register64.RIP)
				&& !io.hasIndex()
				&& !io.hasScale()
				&& io.hasDisplacement()
				&& io.getDisplacementType() == DisplacementType.LONG;
	}

	private static boolean requiresOperandSizeOverride(final Instruction inst) {
		return (inst.firstOperand() instanceof Register16
						|| inst.firstOperand() instanceof final IndirectOperand io
								&& io.getPointerSize() == PointerSize.WORD_PTR)
				|| (inst.opcode() == Opcode.MOVDQA && inst.firstOperand().bits() == 128)
				|| (inst.opcode() == Opcode.MOVAPD)
				|| (inst.opcode() == Opcode.MOVQ
						&& inst.firstOperand() instanceof RegisterXMM
						&& !(inst.secondOperand() instanceof IndirectOperand))
				|| (inst.opcode() == Opcode.MOVQ && inst.secondOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PUNPCKLQDQ)
				|| (inst.opcode() == Opcode.PUNPCKLDQ)
				|| (inst.opcode() == Opcode.PUNPCKLBW)
				|| (inst.opcode() == Opcode.PUNPCKHQDQ)
				|| (inst.opcode() == Opcode.PUNPCKLWD)
				|| (inst.opcode() == Opcode.MOVHPD)
				|| (inst.opcode() == Opcode.PXOR && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.POR && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PAND && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PADDQ && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PSUBB && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PSUBW && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PSUBD && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PSUBQ && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PMINUB && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PMAXUB && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PSHUFB && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.UCOMISD)
				|| (inst.opcode() == Opcode.PCMPEQB && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PCMPEQW && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PCMPEQD && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.MOVD && inst.firstOperand() instanceof RegisterXMM)
				|| (inst.opcode() == Opcode.PMOVMSKB)
				|| (inst.opcode() == Opcode.PSLLDQ)
				|| (inst.opcode() == Opcode.PSRLDQ)
				|| (inst.opcode() == Opcode.MOVNTDQ)
				|| (inst.opcode() == Opcode.PCMPGTB);
	}

	private static void encodeTwoOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if (inst.firstOperand() instanceof final IndirectOperand io
				&& io.hasSegment()
				&& io.getSegment() == Register16.CS) {
			wb.write(CS_SEGMENT_OVERRIDE_PREFIX);
		}
		if (requiresAddressSizeOverride(inst.firstOperand()) || requiresAddressSizeOverride(inst.secondOperand())) {
			wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
		}
		if (requiresOperandSizeOverride(inst)) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}
		if (inst.hasLockPrefix()) {
			wb.write(InstructionPrefix.LOCK.getCode());
		}
		if (inst.hasRepPrefix()) {
			wb.write(InstructionPrefix.REP.getCode());
		}
		if (inst.hasRepnzPrefix()) {
			wb.write(InstructionPrefix.REPNZ.getCode());
		}

		if ((inst.opcode() == Opcode.MOVQ || inst.opcode() == Opcode.MOVDQU)
				&& inst.firstOperand() instanceof RegisterXMM
				&& inst.secondOperand() instanceof IndirectOperand) {
			wb.write((byte) 0xf3);
		}
		if ((inst.opcode() == Opcode.MOVSD
						&& inst.firstOperand() instanceof RegisterXMM
						&& inst.secondOperand() instanceof IndirectOperand)
				|| (inst.opcode() == Opcode.CVTSI2SD)
				|| (inst.opcode() == Opcode.DIVSD)
				|| (inst.opcode() == Opcode.ADDSD)
				|| (inst.opcode() == Opcode.LDDQU)) {
			wb.write((byte) 0xf2);
		}

		final boolean hasExtendedFirstRegister =
				inst.firstOperand() instanceof final Register r && Registers.requiresExtension(r);
		final boolean hasExtendedSecondRegister =
				inst.secondOperand() instanceof final Register r && Registers.requiresExtension(r);
		final boolean isShift =
				inst.opcode() == Opcode.SHR || inst.opcode() == Opcode.SAR || inst.opcode() == Opcode.SHL;
		final boolean isConditionalMove = inst.opcode() == Opcode.CMOVE
				|| inst.opcode() == Opcode.CMOVNE
				|| inst.opcode() == Opcode.CMOVA
				|| inst.opcode() == Opcode.CMOVAE
				|| inst.opcode() == Opcode.CMOVB
				|| inst.opcode() == Opcode.CMOVBE
				|| inst.opcode() == Opcode.CMOVG
				|| inst.opcode() == Opcode.CMOVGE
				|| inst.opcode() == Opcode.CMOVL
				|| inst.opcode() == Opcode.CMOVLE
				|| inst.opcode() == Opcode.CMOVS
				|| inst.opcode() == Opcode.CMOVNS;

		// rex
		{
			byte rex = DEFAULT_REX_PREFIX;
			if ((inst.firstOperand() instanceof Register64
							|| inst.firstOperand() instanceof RegisterMMX
							|| (inst.opcode() == Opcode.MOVQ && inst.firstOperand() instanceof RegisterXMM)
							|| (inst.firstOperand() instanceof final IndirectOperand io
									&& io.getPointerSize() == PointerSize.QWORD_PTR)
							|| (inst.opcode() == Opcode.CVTSI2SD && inst.secondOperand() instanceof Register64))
					&& !(inst.opcode() == Opcode.MOVQ && inst.firstOperand() instanceof IndirectOperand)
					&& !(inst.opcode() == Opcode.MOVQ && inst.secondOperand() instanceof IndirectOperand)
					&& !(inst.opcode() == Opcode.PXOR && inst.firstOperand() instanceof RegisterMMX)
					&& !(inst.opcode() == Opcode.PCMPEQB && inst.firstOperand() instanceof RegisterMMX)
					&& !(inst.opcode() == Opcode.PCMPEQW && inst.firstOperand() instanceof RegisterMMX)
					&& !(inst.opcode() == Opcode.PCMPEQD && inst.firstOperand() instanceof RegisterMMX)
					&& !(inst.opcode() == Opcode.MOVD)
					&& !(inst.opcode() == Opcode.MOVHPS)
					&& !(inst.opcode() == Opcode.MOVHPD)
					&& !(inst.opcode() == Opcode.VMOVQ)) {
				rex = or(rex, (byte) 0b1000);
			}
			if ((inst.opcode() == Opcode.CMP && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOV && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVSXD && hasExtendedFirstRegister)
					|| (isConditionalMove && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.LEA && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.MOVZX && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.ADD
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof IndirectOperand)
					|| (inst.opcode() == Opcode.ADD
							&& inst.firstOperand() instanceof IndirectOperand
							&& hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.ADD
							&& inst.firstOperand() instanceof Register
							&& hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.AND && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.SUB
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof IndirectOperand)
					|| (inst.opcode() == Opcode.SUB && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.SBB && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.OR && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.TEST && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVUPS && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVSD && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.XCHG && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.CMPXCHG && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.BTC && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.BTR && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.BTS && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.CVTSI2SD && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.DIVSD && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.ADDSD && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.XORPS && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.UCOMISD && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.UCOMISS && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.XADD && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVDQA && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.MOVDQU && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.PCMPGTB && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.XOR
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof IndirectOperand)) {
				rex = or(rex, (byte) 0b0100);
			}
			if ((inst.opcode() == Opcode.IMUL && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOV && hasExtendedIndex(inst.firstOperand()))
					|| (inst.opcode() == Opcode.MOV && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOVSXD && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.CMP && hasExtendedIndex(inst.firstOperand()))
					|| (inst.opcode() == Opcode.CMP && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.OR
							&& hasExtendedIndex(inst.firstOperand())
							&& !(inst.secondOperand() instanceof Register64))
					|| (inst.opcode() == Opcode.LEA && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOVDQA && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOVAPS && hasExtendedIndex(inst.firstOperand()))
					|| (inst.opcode() == Opcode.MOVAPS && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOVAPD && hasExtendedIndex(inst.firstOperand()))
					|| (inst.opcode() == Opcode.MOVQ && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOVD && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.PXOR && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.POR && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.PAND && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.PADDQ && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.PSUBQ && hasExtendedIndex(inst.secondOperand()))
					|| (inst.opcode() == Opcode.SUB && hasExtendedIndex(inst.firstOperand()))) {
				rex = or(rex, (byte) 0b0010);
			}
			if ((isShift && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.MOV && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.MOV && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.MOVSXD && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVSXD && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.CMP && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.CMP && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.OR && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.OR && hasExtendedBase(inst.secondOperand()))
					|| (isConditionalMove && hasExtendedSecondRegister)
					|| (isConditionalMove && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.ADD && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.ADD
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Immediate)
					|| (inst.opcode() == Opcode.ADD
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Register)
					|| (inst.opcode() == Opcode.AND && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.AND && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.AND && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.SUB
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Register)
					|| (inst.opcode() == Opcode.SUB && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.SUB
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Immediate)
					|| (inst.opcode() == Opcode.SBB
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Register)
					|| (inst.opcode() == Opcode.SBB
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Immediate)
					|| (inst.opcode() == Opcode.XOR
							&& hasExtendedFirstRegister
							&& inst.secondOperand() instanceof Immediate)
					|| (inst.opcode() == Opcode.TEST && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.TEST && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.MOVQ && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVQ && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.MOVDQA && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PUNPCKLQDQ && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PUNPCKLDQ && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PUNPCKHQDQ && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVUPS && hasExtendedBase(inst.firstOperand()))
					|| (inst.opcode() == Opcode.MOVSD && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.PXOR && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.POR && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PAND && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PADDQ && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PSUBB && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PSUBW && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PSUBD && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PSUBQ && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.XCHG && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.DIVSD && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.ADDSD && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.XORPS && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PCMPEQB && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PCMPEQW && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.PCMPEQD && hasExtendedSecondRegister)
					|| (inst.opcode() == Opcode.MOVD && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.ROR && hasExtendedFirstRegister)
					|| (inst.opcode() == Opcode.MOVZX && hasExtendedSecondRegister)) {
				rex = or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX
					|| (inst.firstOperand() instanceof final Register8 r && Register8.requiresRexPrefix(r))
					|| (inst.secondOperand() instanceof final Register8 r && Register8.requiresRexPrefix(r))) {
				wb.write(rex);
			}
		}

		byte reg = 0;
		switch (inst.opcode()) {
			case CMOVE, CMOVNS, CMOVAE, CMOVB, CMOVBE, CMOVNE, CMOVG, CMOVGE, CMOVS, CMOVA, CMOVL, CMOVLE ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, asByte((byte) 0x40 + CONDITIONAL_MOVE_OPCODES.get(inst.opcode())));
			case CMP -> {
				if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof Register) {
					wb.write(io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x38 : (byte) 0x39);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x1d);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							imm.bits() == 8
									? (io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b111;
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write(r instanceof Register8 ? (byte) 0x3a : (byte) 0x3b);
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof final Immediate imm) {
					if (r instanceof Register8) {
						if (r == Register8.AL) {
							wb.write((byte) 0x3c);
							encodeImmediate(wb, imm);
							return;
						} else {
							wb.write((byte) 0x80);
							reg = (byte) 0b111;
						}
					} else {
						if ((r == Register32.EAX || r == Register64.RAX) && imm.bits() == 32) {
							wb.write((byte) 0x3d);
							encodeImmediate(wb, imm);
							return;
						}
						wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
						reg = (byte) 0b111;
					}
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof Register) {
					wb.write(r instanceof Register8 ? (byte) 0x38 : (byte) 0x39);
				}
			}
			case MOV -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x89);
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0xc6 : (byte) 0xc7);
					reg = (byte) 0b000;

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						encodeImmediate(wb, imm);
						return;
					}
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register r2) {
					wb.write(r2 instanceof Register8 ? (byte) 0x88 : (byte) 0x89);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof IndirectOperand io) {
					wb.write((r instanceof Register8) ? (byte) 0x8a : (byte) 0x8b);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof Immediate) {
					if (r instanceof Register64) {
						wb.write((byte) 0xc7);
					} else {
						wb.write(asByte((r instanceof Register8 ? (byte) 0xb0 : (byte) 0xb8) + Registers.toByte(r)));
					}
				}
			}
			case MOVSXD -> wb.write((byte) 0x63);
			case SUB -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x29);
				} else if (inst.firstOperand() instanceof IndirectOperand && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x29);
				} else if ((inst.firstOperand() instanceof Register || inst.firstOperand() instanceof IndirectOperand)
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							(imm.bits() == 8)
									? ((inst.firstOperand().bits() == 8) ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b101;
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x2b);
				}
			}
			case SBB -> {
				if (inst.firstOperand().equals(Register8.AL) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x1c);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand().equals(Register16.AX)
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x1d);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write((imm.bits() == 8) ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b011;
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x19);
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x1a);
					reg = (byte) 0b011;
				}
			}
			case SHR, SAR, SHL -> {
				reg = SHIFT_REG_BYTES.get(inst.opcode());
				if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof final Immediate imm) {
					final boolean isImmediateOne = imm.bits() == 8 && imm.asByte() == (byte) 1;
					wb.write(or(
							isImmediateOne ? (byte) 0xd0 : (byte) 0xc0,
							(r instanceof Register8) ? (byte) 0 : (byte) 1));
					if (isImmediateOne) {
						encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
						return;
					}
				} else if (inst.firstOperand() instanceof final Register r1
						&& inst.secondOperand() instanceof final Register r2) {
					wb.write(r1 instanceof Register8 ? (byte) 0xd2 : (byte) 0xd3);
					if (r2.equals(Register8.CL)) {
						encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r1));
					}
					return;
				}
			}
			case IMUL -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xaf);
			case OR -> {
				if (inst.firstOperand().equals(Register8.AL) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x0c);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand().equals(Register16.AX) && imm.bits() == 16)
								|| (inst.firstOperand().equals(Register32.EAX) && imm.bits() == 32)
								|| (inst.firstOperand().equals(Register64.RAX) && imm.bits() == 32))) {
					wb.write((byte) 0x0d);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register8 && inst.secondOperand() instanceof Immediate) {
					wb.write((byte) 0x80);
					reg = (byte) 0b001;
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof Register) {
					wb.write((r.bits() == 8) ? (byte) 0x08 : (byte) 0x09);
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write((r instanceof Register8) ? (byte) 0x0a : (byte) 0x0b);
				} else if (inst.firstOperand() instanceof Register
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b001;
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							imm.bits() == 8
									? (io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b001;
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register r) {
					wb.write((r instanceof Register8) ? (byte) 0x08 : (byte) 0x09);
					reg = (byte) 0b001;

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case LEA -> wb.write((byte) 0x8d);
			case MOVZX ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, inst.secondOperand().bits() == 8 ? (byte) 0xb6 : (byte) 0xb7);
			case MOVSX ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, inst.secondOperand().bits() == 8 ? (byte) 0xbe : (byte) 0xbf);
			case ADD -> {
				if (inst.firstOperand().equals(Register8.AL) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x04);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand().equals(Register16.AX) && imm.bits() == 16)
								|| (inst.firstOperand().equals(Register32.EAX) && imm.bits() == 32)
								|| (inst.firstOperand().equals(Register64.RAX) && imm.bits() == 32))) {
					wb.write((byte) 0x05);
					encodeImmediate(wb, imm);
					return;
				} else if ((inst.firstOperand() instanceof IndirectOperand || inst.firstOperand() instanceof Register)
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b000;
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x03);
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x01);
				} else if (inst.firstOperand() instanceof IndirectOperand
						&& inst.secondOperand() instanceof final Register r) {
					wb.write((r instanceof Register8) ? (byte) 0x00 : (byte) 0x01);
				}
			}
			case ADC -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b010;
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof Register) {
					wb.write((io.bits() == 8) ? (byte) 0x10 : (byte) 0x11);
				}
			}
			case DEC -> wb.write((byte) 0xfe);
			case AND -> {
				if (inst.firstOperand().equals(Register8.AL) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x24);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand().equals(Register16.AX) && imm.bits() == 16)
								|| (inst.firstOperand().equals(Register32.EAX) && imm.bits() == 32)
								|| (inst.firstOperand().equals(Register64.RAX) && imm.bits() == 32))) {
					wb.write((byte) 0x25);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b100;
				} else if (inst.firstOperand() instanceof Register8
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x22);
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x23);
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x21);
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							imm.bits() == 8
									? (io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b100;

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x25);
						wb.write(asInt(io.getDisplacement()));
						encodeImmediate(wb, imm);
						return;
					}
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register r) {
					wb.write(
							(r instanceof Register8)
									? (io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x20 : (byte) 0x23)
									: (byte) 0x21);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case XOR -> {
				if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand().equals(Register16.AX) && imm.bits() == 16)
								|| (inst.firstOperand().equals(Register32.EAX) && imm.bits() == 32)
								|| (inst.firstOperand().equals(Register64.RAX) && imm.bits() == 32))) {
					wb.write((byte) 0x35);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write((imm.bits() == 8) ? ((r instanceof Register8) ? (byte) 0x80 : (byte) 0x83) : (byte) 0x81);
					reg = (byte) 0b110;
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof Register) {
					wb.write((io.bits() == 8) ? (byte) 0x30 : (byte) 0x31);
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write((r instanceof Register8) ? (byte) 0x32 : (byte) 0x33);
				}
			}
			case TEST -> {
				if (inst.firstOperand().equals(Register8.AL) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0xa8);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand().equals(Register16.AX) && imm.bits() == 16)
								|| (inst.firstOperand().equals(Register32.EAX) && imm.bits() == 32)
								|| (inst.firstOperand().equals(Register64.RAX) && imm.bits() == 32))) {
					wb.write((byte) 0xa9);
					encodeImmediate(wb, imm);
					return;
				} else if ((inst.firstOperand() instanceof IndirectOperand || inst.firstOperand() instanceof Register)
						&& inst.secondOperand() instanceof final Register r) {
					wb.write(r.bits() == 8 ? (byte) 0x84 : (byte) 0x85);
				} else if ((inst.firstOperand() instanceof IndirectOperand || inst.firstOperand() instanceof Register)
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0xf6 : (byte) 0xf7);
				}
			}
			case MOVS -> {
				if (isDefaultMovsDestination(inst.firstOperand()) && isDefaultMovsSource(inst.secondOperand())) {
					wb.write(inst.firstOperand().bits() == 8 ? (byte) 0xa4 : (byte) 0xa5);
					return;
				}
			}
			case STOS -> {
				if (isDefaultMovsDestination(inst.firstOperand()) && inst.secondOperand() instanceof Register) {
					wb.write(inst.firstOperand().bits() == 8 ? (byte) 0xaa : (byte) 0xab);
					return;
				}
			}
			case MOVDQA, MOVDQU -> {
				if (inst.firstOperand() instanceof IndirectOperand) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x7f);
				} else {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6f);
				}
				if (inst.secondOperand() instanceof final IndirectOperand io && isIpAndOffset(io)) {
					wb.write((byte) 0x15);
					encodeIndirectOperand(wb, io);
					return;
				}
			}
			case MOVAPS, MOVAPD -> {
				if (inst.firstOperand() instanceof RegisterXMM && inst.secondOperand() instanceof RegisterXMM) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x28);
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof RegisterXMM) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x29);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x35);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (inst.firstOperand() instanceof RegisterXMM
						&& inst.secondOperand() instanceof final IndirectOperand io) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x28);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x35);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case MOVQ -> {
				if ((inst.firstOperand() instanceof RegisterXMM || inst.firstOperand() instanceof RegisterMMX)
						&& inst.secondOperand() instanceof Register64) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6e);
				} else if (inst.firstOperand() instanceof IndirectOperand
						&& inst.secondOperand() instanceof RegisterXMM) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xd6);
				} else if (inst.firstOperand() instanceof RegisterXMM
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x7e);
				} else if (inst.firstOperand() instanceof RegisterMMX
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6f);
				}
			}
			case MOVD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6e);
			case MOVHPS -> {
				if (inst.firstOperand() instanceof IndirectOperand && inst.secondOperand() instanceof RegisterXMM) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x17);
				} else if (inst.firstOperand() instanceof RegisterXMM
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x16);
				}
			}
			case MOVHPD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x17);
			case MOVHLPS -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x12);
			case PUNPCKLBW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x60);
			case PUNPCKLWD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x61);
			case PUNPCKLDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x62);
			case PUNPCKLQDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6c);
			case PUNPCKHQDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6d);
			case MOVABS -> {
				if (inst.firstOperand() instanceof final Register64 r
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(or((byte) 0xb8, Registers.toByte(r)));
					encodeImmediate(wb, imm);
					return;
				}
			}
			case MOVUPS -> {
				if (inst.firstOperand() instanceof IndirectOperand && inst.secondOperand() instanceof RegisterXMM) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x11);
				} else if (inst.firstOperand() instanceof RegisterXMM
						&& inst.secondOperand() instanceof IndirectOperand) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x10);
				}
			}
			case MOVSD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x10);
			case PXOR -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xef);
			case POR -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xeb);
			case PMINUB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xda);
			case PMAXUB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xde);
			case PAND -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xdb);
			case PADDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xd4);
			case PSUBB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xf8);
			case PSUBW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xf9);
			case PSUBD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xfa);
			case PSUBQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xfb);
			case XCHG -> {
				if (inst.firstOperand() instanceof final Register r
						&& (inst.secondOperand() == Register16.AX
								|| inst.secondOperand() == Register32.EAX
								|| inst.secondOperand() == Register64.RAX)) {
					wb.write(asByte((byte) 0x90 + Registers.toByte(r)));
					return;
				} else {
					wb.write((inst.firstOperand().bits() == 8) ? (byte) 0x86 : (byte) 0x87);
				}
			}
			case CMPXCHG ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (inst.firstOperand().bits() == 8) ? (byte) 0xb0 : (byte) 0xb1);
			case BT, BTC, BTR, BTS -> {
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Immediate) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xba);
					reg = BIT_TEST_OPCODES.get(inst.opcode());
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof Register) {
					wb.write(
							DOUBLE_BYTE_OPCODE_PREFIX,
							or((byte) 0b10000011, shl(BIT_TEST_OPCODES.get(inst.opcode()), 3)));
				}
			}
			case CVTSI2SD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x2a);
			case DIVSD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x5e);
			case ADDSD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x58);
			case XORPS -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x57);
			case UCOMISD, UCOMISS -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x2e);
				if (inst.secondOperand() instanceof final IndirectOperand io) {
					if (isIpAndOffset(io)) {
						wb.write((byte) 0x2d);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case XADD ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (inst.firstOperand().bits() == 8) ? (byte) 0xc0 : (byte) 0xc1);
			case PCMPEQB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x74);
			case PCMPEQW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x75);
			case PCMPEQD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x76);
			case BSF -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xbc);
			case BSR -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xbd);
			case ROL -> {
				wb.write((byte) 0xc1);
				reg = (byte) 0b000;
			}
			case ROR -> {
				wb.write((byte) 0xc1);
				reg = (byte) 0b001;
			}
			case RCL -> {
				wb.write((byte) 0xc1);
				reg = (byte) 0b010;
			}
			case RCR -> {
				wb.write((byte) 0xc1);
				reg = (byte) 0b011;
			}
			case PMOVMSKB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xd7);
			case MOVNTDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xe7);
			case PSLLDQ -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x73);
				reg = (byte) 0b111;
			}
			case PSRLDQ -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x73);
				reg = (byte) 0b011;
			}
			case VMOVDQU -> {
				if ((inst.firstOperand() instanceof final IndirectOperand io
								&& ((io.hasBase() && Registers.requiresExtension(io.getBase()))
										|| (io.hasIndex() && Registers.requiresExtension(io.getIndex()))))
						|| (inst.secondOperand() instanceof final IndirectOperand io2
								&& ((io2.hasBase() && Registers.requiresExtension(io2.getBase()))
										|| (io2.hasIndex() && Registers.requiresExtension(io2.getIndex()))))) {
					encodeVex3Prefix(wb, inst);
				} else {
					encodeVex2Prefix(wb, inst);
				}
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x6f);
				} else if (inst.firstOperand() instanceof IndirectOperand && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x7f);
				}
			}
			case VPMOVMSKB -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0xd7);
			}
			case VMOVQ -> {
				if (inst.firstOperand() instanceof Register64 && inst.secondOperand() instanceof final RegisterXMM r2) {
					if (RegisterXMM.requiresEvexR1(r2)) {
						encodeEvexPrefix(wb, inst);
					} else {
						encodeVex3Prefix(wb, inst);
					}
					wb.write((byte) 0x7e);
				} else {
					encodeVex2Prefix(wb, inst);

					if (inst.firstOperand() instanceof RegisterXMM && inst.secondOperand() instanceof IndirectOperand) {
						wb.write((byte) 0x7e);
					} else if (inst.firstOperand() instanceof IndirectOperand
							&& inst.secondOperand() instanceof RegisterXMM) {
						wb.write((byte) 0xd6);
					}
				}
			}
			case VMOVD -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0x6e);
			}
			case VPBROADCASTB -> {
				if (inst.firstOperand() instanceof RegisterZMM && inst.secondOperand() instanceof Register32) {
					encodeEvexPrefix(wb, inst);
					wb.write((byte) 0x7a);
				} else {
					encodeVex3Prefix(wb, inst);
					wb.write((byte) 0x78);
				}
			}
			case VPBROADCASTD -> {
				if (inst.firstOperand() instanceof RegisterZMM && inst.secondOperand() instanceof Register32) {
					encodeEvexPrefix(wb, inst);
					wb.write((byte) 0x7c);
				} else {
					encodeVex3Prefix(wb, inst);
					wb.write((byte) 0x58);
				}
			}
			case MOVBE -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A4_PREFIX, (byte) 0xf0);
			case LDDQU -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xf0);
			case VMOVUPS -> {
				encodeEvexPrefix(wb, inst);
				if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x10);
				} else if (inst.firstOperand() instanceof IndirectOperand && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x11);
				}
			}
			case VMOVDQU8, VMOVDQU64 -> {
				encodeEvexPrefix(wb, inst);
				if (inst.firstOperand() instanceof IndirectOperand && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x7f);
				} else if (inst.firstOperand() instanceof Register && inst.secondOperand() instanceof IndirectOperand) {
					wb.write((byte) 0x6f);
				}
			}
			case VMOVNTDQ -> {
				if (inst.firstOperand().bits() == 512) {
					encodeEvexPrefix(wb, inst);
				} else if (inst.firstOperand().bits() == 256) {
					encodeVex2Prefix(wb, inst);
				}
				wb.write((byte) 0xe7);
			}
			case PCMPGTB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x64);
			case PSHUFB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A4_PREFIX, (byte) 0x00);
			case VBROADCASTSS -> {
				encodeEvexPrefix(wb, inst);
				wb.write((byte) 0x18);
			}
			case VMOVAPS -> {
				encodeEvexPrefix(wb, inst);
				wb.write((byte) 0x29);
			}
			case KMOVQ -> {
				encodeVex3Prefix(wb, inst);
				wb.write((byte) 0x92);
			}
			case KMOVD -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0x92);
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		// FIXME: refactor all the isIpAndOffset calls to one case down here
		if (inst.firstOperand() instanceof final Register r1 && inst.secondOperand() instanceof final Register r2) {
			// Most ALU operations encode the destination operand (the first one) in the r/m portion, while some
			// instructions like MOV encode the destination as Reg
			// FIXME: actually, it seems to be the opposite... why?
			if (inst.opcode() == Opcode.MOV
					|| inst.opcode() == Opcode.SUB
					|| inst.opcode() == Opcode.CMP
					|| inst.opcode() == Opcode.ADD
					|| inst.opcode() == Opcode.AND
					|| inst.opcode() == Opcode.OR
					|| inst.opcode() == Opcode.XCHG
					|| inst.opcode() == Opcode.BT
					|| inst.opcode() == Opcode.BTR
					|| inst.opcode() == Opcode.BTC
					|| inst.opcode() == Opcode.BTS
					|| inst.opcode() == Opcode.VMOVQ) {
				encodeModRM(wb, (byte) 0b11, Registers.toByte(r2), Registers.toByte(r1));
			} else {
				encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r2));
			}
		} else if (inst.firstOperand() instanceof final Register r
				&& inst.secondOperand() instanceof final IndirectOperand io) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if ((inst.opcode() == Opcode.MOV
							|| inst.opcode() == Opcode.MOVSXD
							|| inst.opcode() == Opcode.CMOVE
							|| isConditionalMove
							|| inst.opcode() == Opcode.XOR
							|| inst.opcode() == Opcode.OR)
					&& io.hasBase()
					&& isSP(io.getBase())) {
				// FIXME: this could be replaced with just wb.write((byte) 0x24);
				encodeSIB(wb, (byte) 0b00, (byte) 0b100, (byte) 0b100);
			}
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof final IndirectOperand io
				&& inst.secondOperand() instanceof final Register r) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if (inst.opcode() == Opcode.OR && io.hasBase() && isSP(io.getBase())) {
				// FIXME: this could be replaced with just wb.write((byte) 0x24);
				encodeSIB(wb, (byte) 0b00, (byte) 0b100, (byte) 0b100);
			}
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof final IndirectOperand io
				&& inst.secondOperand() instanceof final Immediate imm) {
			encodeModRM(
					wb, getMod(io), reg, (isSimpleIndirectOperand(io)) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if ((inst.opcode() == Opcode.OR || inst.opcode() == Opcode.SUB) && hasOnlyBase(io) && isSP(io.getBase())) {
				// FIXME: this could be replaced with just wb.write((byte) 0x24);
				encodeSIB(wb, (byte) 0b00, (byte) 0b100, (byte) 0b100);
			}
			encodeIndirectOperand(wb, io);
			encodeImmediate(wb, imm);
		} else if (inst.firstOperand() instanceof final Register r
				&& inst.secondOperand() instanceof final Immediate imm) {
			if (inst.opcode() != Opcode.MOV || r instanceof Register64) {
				encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
			}
			encodeImmediate(wb, imm);
		}
	}

	private static void encodeThreeOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if ((inst.opcode() == Opcode.PSHUFD)
				|| (inst.opcode() == Opcode.SHUFPD)
				|| (inst.opcode() == Opcode.PALIGNR)
				|| (inst.opcode() == Opcode.PCMPISTRI)) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}

		final boolean hasExtendedFirstRegister =
				inst.firstOperand() instanceof final Register r && Registers.requiresExtension(r);
		final boolean hasExtendedSecondRegister =
				inst.secondOperand() instanceof final Register r && Registers.requiresExtension(r);

		// rex
		{
			byte rex = DEFAULT_REX_PREFIX;
			if (inst.firstOperand() instanceof Register64 && !(inst.opcode() == Opcode.BZHI)) {
				rex = or(rex, (byte) 0b1000);
			}
			if ((inst.opcode() == Opcode.IMUL && hasExtendedFirstRegister)) {
				rex = or(rex, (byte) 0b0100);
			}
			if ((inst.opcode() == Opcode.IMUL && hasExtendedIndex(inst.secondOperand()))) {
				rex = or(rex, (byte) 0b0010);
			}
			if ((inst.opcode() == Opcode.IMUL && hasExtendedBase(inst.secondOperand()))
					|| (inst.opcode() == Opcode.IMUL && hasExtendedSecondRegister)) {
				rex = or(rex, (byte) 0b0001);
			}
			if (rex != DEFAULT_REX_PREFIX) {
				wb.write(rex);
			}
		}

		byte reg = -1;
		switch (inst.opcode()) {
			case IMUL -> {
				if (inst.firstOperand() instanceof Register
						&& (inst.secondOperand() instanceof Register || inst.secondOperand() instanceof IndirectOperand)
						&& inst.thirdOperand() instanceof final Immediate imm) {
					wb.write((imm.bits() == 8) ? (byte) 0x6b : (byte) 0x69);
				}
			}
			case PSHUFD, PSHUFW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x70);
			case SHUFPS, SHUFPD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc6);
			case PALIGNR -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A5_PREFIX, (byte) 0x0f);
			case PCMPISTRI -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A5_PREFIX, (byte) 0x63);
			case VPXOR -> {
				if (requiresVex3Prefix(inst)) {
					encodeVex3Prefix(wb, inst);
				} else {
					encodeVex2Prefix(wb, inst);
				}
				wb.write((byte) 0xef);
			}
			case VPOR -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0xeb);
			}
			case VPAND -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0xdb);
			}
			case VPANDN -> {
				if (requiresVex3Prefix(inst)) {
					encodeVex3Prefix(wb, inst);
				} else {
					encodeVex2Prefix(wb, inst);
				}
				wb.write((byte) 0xdf);
			}
			case VPMINUB -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0xda);
			}
			case VPCMPGTB -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0x64);
			}
			case VPCMPEQB -> {
				if (inst.firstOperand() instanceof Register
						&& inst.thirdOperand() instanceof final IndirectOperand io
						&& !isSimpleIndirectOperand(io)) {
					encodeVex3Prefix(wb, inst);
				} else {
					encodeVex2Prefix(wb, inst);
				}
				wb.write((byte) 0x74);
			}
			case PEXTRW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc5);
			case SARX -> {
				encodeVex3Prefix(wb, inst);
				wb.write((byte) 0xf7);
			}
			case BZHI -> {
				encodeVex3Prefix(wb, inst);
				wb.write((byte) 0xf5);
			}
			case VPSUBB -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0xf8);
			}
			case VPCMPISTRI -> {
				encodeVex3Prefix(wb, inst);
				wb.write((byte) 0x63);
			}
			case VPSLLDQ -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0x73);
				reg = (byte) 0b111;
			}
			case VPSRLDQ -> {
				encodeVex2Prefix(wb, inst);
				wb.write((byte) 0x73);
				reg = (byte) 0b010;
			}
			case VPSHUFB -> {
				encodeVex3Prefix(wb, inst);
				wb.write((byte) 0x00);
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof final Register r2
				&& inst.thirdOperand() instanceof final Immediate imm) {
			if (reg != -1) {
				encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r2));
			} else {
				encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r2));
			}
			encodeImmediate(wb, imm);
		} else if (inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof final IndirectOperand io
				&& inst.thirdOperand() instanceof final Immediate imm) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r1),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
			encodeImmediate(wb, imm);
		} else if ((inst.opcode() == Opcode.SARX || inst.opcode() == Opcode.BZHI)
				&& inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof final Register r2
				&& inst.thirdOperand() instanceof Register) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r2));
		} else if ((requiresVex2Prefix(inst) || requiresVex3Prefix(inst))
				&& inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof Register
				&& inst.thirdOperand() instanceof final Register r3) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r3));
		} else if (requiresVex2Prefix(inst)
				&& inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof Register
				&& inst.thirdOperand() instanceof final IndirectOperand io) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r1),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
		}
	}

	private static void encodeFourOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case VPALIGNR -> {
				encodeVex3Prefix(wb, inst);
				wb.write((byte) 0x0f);
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof Register
				&& inst.thirdOperand() instanceof final IndirectOperand io
				&& inst.fourthOperand() instanceof final Immediate imm) {
			encodeModRM(wb, getMod(io), Registers.toByte(r1), (byte) 0b100);
			encodeIndirectOperand(wb, io);
			encodeImmediate(wb, imm);
		}
	}

	private static boolean requiresVex2Prefix(final Instruction inst) {
		return switch (inst.opcode()) {
			case VPXOR, VPMINUB, VPCMPEQB, VPCMPGTB, VPOR, VPAND, VPANDN, VPSUBB -> true;
			default -> false;
		};
	}

	private static boolean requiresVex3Prefix(final Instruction inst) {
		if (inst.firstOperand() instanceof final Register r1
				&& Registers.requiresExtension(r1)
				&& inst.secondOperand() instanceof final Register r2
				&& Registers.requiresExtension(r2)
				&& inst.thirdOperand() instanceof final Register r3
				&& Registers.requiresExtension(r3)) {
			return true;
		}
		return switch (inst.opcode()) {
			case SARX, BZHI, VPSHUFB -> true;
			default -> false;
		};
	}

	private static boolean hasImpliedOperandSizeOverridePrefix(final Instruction inst) {
		return switch (inst.opcode()) {
			case VPXOR,
					VPOR,
					VPAND,
					VPANDN,
					VPSUBB,
					VPMINUB,
					VPMOVMSKB,
					VPCMPEQB,
					VPCMPGTB,
					VMOVD,
					VPBROADCASTB,
					VPBROADCASTD,
					VMOVNTDQ,
					VPCMPISTRI,
					VPSLLDQ,
					VPSRLDQ,
					VPALIGNR,
					VPSHUFB,
					VBROADCASTSS -> true;
			case VMOVQ ->
				(inst.firstOperand() instanceof IndirectOperand || inst.firstOperand() instanceof Register64)
						&& inst.secondOperand() instanceof RegisterXMM;
			default -> false;
		};
	}

	private static boolean hasImpliedRepPrefix(final Instruction inst) {
		return switch (inst.opcode()) {
			case VMOVDQU, VMOVDQU64, SARX -> true;
			case VMOVQ -> inst.firstOperand() instanceof RegisterXMM && inst.secondOperand() instanceof IndirectOperand;
			default -> false;
		};
	}

	private static boolean hasImpliedRepnzPrefix(final Instruction inst) {
		return switch (inst.opcode()) {
			case VMOVDQU8, KMOVQ, KMOVD -> true;
			default -> false;
		};
	}

	private static byte getVex3OpcodeMap(final Opcode opcode) {
		return switch (opcode) {
			case VPXOR, VMOVDQU, VPCMPEQB, VPANDN, VMOVQ, KMOVQ -> (byte) 0b01;
			case VPBROADCASTB, VPBROADCASTD, SARX, BZHI, VPSHUFB -> (byte) 0b10;
			case VPCMPISTRI, VPALIGNR -> (byte) 0b11;
			default -> throw new IllegalArgumentException(String.format("Unknown VEX3 opcode map for %s.", opcode));
		};
	}

	private static void encodeVex2Prefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		wb.write((byte) 0xc5);

		encodeVex2Byte(
				wb,
				!((inst.firstOperand() instanceof final Register r && Registers.requiresExtension(r))
						|| (hasExtendedBase(inst.firstOperand()))),
				(inst.getNumOperands() == 3 && inst.secondOperand() instanceof final Register r)
						? Registers.combine(Registers.requiresExtension(r), Registers.toByte(r))
						: 0,
				inst.firstOperand() instanceof RegisterYMM || inst.secondOperand() instanceof RegisterYMM,
				hasImpliedOperandSizeOverridePrefix(inst)
						? (byte) 0b01
						: (hasImpliedRepPrefix(inst) ? (byte) 0b10 : (hasImpliedRepnzPrefix(inst) ? (byte) 0b11 : 0)));
	}

	private static void encodeVex2Byte(
			final WriteOnlyByteBuffer wb, final boolean r, final byte v, final boolean l, final byte p) {
		wb.write(or(r ? (byte) 0b10000000 : 0, shl(and(not(v), (byte) 0b00001111), 3), l ? (byte) 0b00000100 : 0, p));
	}

	private static void encodeVex3Prefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		wb.write((byte) 0xc4);

		final boolean hasExtendedFirstRegister =
				inst.firstOperand() instanceof final Register r && Registers.requiresExtension(r);
		final boolean hasExtendedSecondRegister =
				inst.secondOperand() instanceof final Register r && Registers.requiresExtension(r);
		final boolean hasExtendedThirdRegister = inst.hasThirdOperand()
				&& inst.thirdOperand() instanceof final Register r
				&& Registers.requiresExtension(r);

		encodeVex3FirstByte(
				wb,
				!((inst.getNumOperands() == 2
								&& ((hasExtendedFirstRegister && inst.secondOperand() instanceof Register)
										|| (inst.firstOperand() instanceof IndirectOperand
												&& hasExtendedSecondRegister)))
						|| (inst.getNumOperands() == 3
								&& (inst.firstOperand() instanceof final Register r
										&& Registers.requiresExtension(r)))),
				!((inst.getNumOperands() == 2
								&& ((hasExtendedIndex(inst.firstOperand()) && inst.secondOperand() instanceof Register)
										|| (inst.firstOperand() instanceof Register
												&& hasExtendedIndex(inst.secondOperand()))))
						|| (inst.getNumOperands() == 3 && hasExtendedIndex(inst.thirdOperand()))),
				!((inst.getNumOperands() == 2
								&& ((inst.firstOperand() instanceof Register && hasExtendedBase(inst.secondOperand()))
										|| (hasExtendedBase(inst.firstOperand())
												&& inst.secondOperand() instanceof Register)
										|| (inst.firstOperand() instanceof Register && hasExtendedSecondRegister)))
						|| (inst.getNumOperands() == 3 && (hasExtendedThirdRegister))),
				getVex3OpcodeMap(inst.opcode()));

		encodeVex3SecondByte(
				wb,
				inst.firstOperand() instanceof Register64 || inst.secondOperand() instanceof Register64,
				(inst.getNumOperands() == 3)
						? ((inst.opcode() == Opcode.SARX || inst.opcode() == Opcode.BZHI)
								? ((inst.thirdOperand() instanceof final Register r
										? Registers.combine(Registers.requiresExtension(r), Registers.toByte(r))
										: 0))
								: ((inst.secondOperand() instanceof final Register r
												&& !(inst.thirdOperand() instanceof Immediate))
										? Registers.combine(Registers.requiresExtension(r), Registers.toByte(r))
										: 0))
						: 0,
				inst.firstOperand() instanceof RegisterYMM || inst.secondOperand() instanceof RegisterYMM,
				hasImpliedOperandSizeOverridePrefix(inst)
						? (byte) 0b01
						: (hasImpliedRepPrefix(inst) ? (byte) 0b10 : (hasImpliedRepnzPrefix(inst) ? (byte) 0b11 : 0)));
	}

	private static void encodeVex3FirstByte(
			final WriteOnlyByteBuffer wb, final boolean r, final boolean x, final boolean b, final byte m) {
		wb.write(or(r ? (byte) 0b10000000 : 0, x ? (byte) 0b01000000 : 0, b ? (byte) 0b00100000 : 0, m));
	}

	private static void encodeVex3SecondByte(
			final WriteOnlyByteBuffer wb, final boolean w, final byte v, final boolean l, final byte p) {
		wb.write(or(w ? (byte) 0b10000000 : 0, shl(and(not(v), (byte) 0b00001111), 3), l ? (byte) 0b00000100 : 0, p));
	}

	private static boolean is64Bits(final Instruction inst) {
		return switch (inst.opcode()) {
			case VMOVDQU64, VMOVQ -> true;
			default -> false;
		};
	}

	private static byte getEvexOpcodeMap(final Opcode opcode) {
		return switch (opcode) {
			case VMOVUPS, VMOVAPS, VMOVDQU8, VMOVDQU64, VMOVNTDQ, VMOVQ -> (byte) 0b001;
			case VBROADCASTSS, VPBROADCASTB, VPBROADCASTD -> (byte) 0b010;
			default -> (byte) 0b000;
		};
	}

	private static void encodeEvexPrefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		wb.write((byte) 0x62);

		encodeEvexFirstByte(
				wb,
				inst.secondOperand() instanceof Register r && Registers.requiresExtension(r),
				false,
				false,
				(inst.firstOperand() instanceof final Register r && Registers.requiresEvexR1(r))
						|| (inst.secondOperand() instanceof final Register r2 && Registers.requiresEvexR1(r2)),
				getEvexOpcodeMap(inst.opcode()));

		encodeEvexSecondByte(
				wb,
				is64Bits(inst),
				(inst.getNumOperands() == 3 && inst.firstOperand() instanceof final Register r)
						? Registers.toByte(r)
						: (byte) 0,
				hasImpliedOperandSizeOverridePrefix(inst)
						? (byte) 0b01
						: (hasImpliedRepPrefix(inst) ? (byte) 0b10 : (hasImpliedRepnzPrefix(inst) ? (byte) 0b11 : 0)));

		wb.write(or(
				!(inst.firstOperand() instanceof RegisterZMM || inst.secondOperand() instanceof RegisterZMM)
						? (byte) 0
						: (byte) 0b01000000,
				(byte) 0b00001000,
				inst.hasDestinationMask() ? MaskRegister.toByte(inst.getDestinationMask()) : (byte) 0));
	}

	private static void encodeEvexFirstByte(
			final WriteOnlyByteBuffer wb,
			final boolean r,
			final boolean x,
			final boolean b,
			final boolean r1,
			final byte m) {
		wb.write(or(
				r ? 0 : (byte) 0b10000000,
				x ? 0 : (byte) 0b01000000,
				b ? 0 : (byte) 0b00100000,
				r1 ? 0 : (byte) 0b00010000,
				m));
	}

	private static void encodeEvexSecondByte(
			final WriteOnlyByteBuffer wb, final boolean w, final byte v, final byte p) {
		wb.write(or(w ? (byte) 0b10000000 : 0, shl(and(not(v), (byte) 0b00001111), 3), (byte) 0b00000100, p));
	}

	private static byte getMod(final IndirectOperand io) {
		if (!io.hasDisplacement() || !io.hasBase()) {
			return (byte) 0b00;
		}

		return switch (io.getDisplacementType()) {
			case DisplacementType.SHORT -> (byte) 0b01;
			case DisplacementType.LONG -> (byte) 0b10;
		};
	}

	private static void encodeModRM(final WriteOnlyByteBuffer wb, final byte mod, final byte reg, final byte rm) {
		wb.write(or(shl(mod, 6), shl(reg, 3), rm));
	}

	private static byte getScale(final IndirectOperand io) {
		return switch (io.getScale()) {
			case 1 -> (byte) 0b00;
			case 2 -> (byte) 0b01;
			case 4 -> (byte) 0b10;
			case 8 -> (byte) 0b11;
			default -> throw new IllegalArgumentException(String.format("Invalid scale: %,d.", io.getScale()));
		};
	}

	private static void encodeIndirectOperand(final WriteOnlyByteBuffer wb, final IndirectOperand io) {
		if (!isSimpleIndirectOperand(io)) {
			// TODO: check this
			final byte base = io.hasBase() ? Registers.toByte(io.getBase()) : (byte) 0b101;
			encodeSIB(wb, getScale(io), Registers.toByte(io.getIndex()), base);
		}

		if (io.hasDisplacement()) {
			switch (io.getDisplacementType()) {
				case DisplacementType.SHORT -> wb.write(asByte(io.getDisplacement()));
				case DisplacementType.LONG -> wb.write(asInt(io.getDisplacement()));
			}
		}
	}

	private static void encodeSIB(final WriteOnlyByteBuffer wb, final byte scale, final byte index, final byte base) {
		wb.write(or(shl(scale, 6), shl(index, 3), base));
	}

	private static boolean isSimpleIndirectOperand(final IndirectOperand io) {
		return io.hasBase() && !io.hasIndex() && !io.hasScale();
	}

	private static void encodeImmediate(final WriteOnlyByteBuffer wb, final Immediate imm) {
		switch (imm.bits()) {
			case 8 -> wb.write(imm.asByte());
			case 16 -> wb.write(imm.asShort());
			case 32 -> wb.write(imm.asInt());
			case 64 -> wb.write(imm.asLong());
			default -> throw new IllegalArgumentException(String.format("Unknown immediate: '%s'.", imm));
		}
	}
}
