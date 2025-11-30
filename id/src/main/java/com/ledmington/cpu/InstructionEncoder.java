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
package com.ledmington.cpu;

import static com.ledmington.utils.BitUtils.and;
import static com.ledmington.utils.BitUtils.asByte;
import static com.ledmington.utils.BitUtils.asInt;
import static com.ledmington.utils.BitUtils.not;
import static com.ledmington.utils.BitUtils.or;
import static com.ledmington.utils.BitUtils.shl;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.ledmington.cpu.x86.DisplacementType;
import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionPrefix;
import com.ledmington.cpu.x86.MaskRegister;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Operand;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RegisterMMX;
import com.ledmington.cpu.x86.RegisterXMM;
import com.ledmington.cpu.x86.RegisterYMM;
import com.ledmington.cpu.x86.RegisterZMM;
import com.ledmington.cpu.x86.Registers;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.cpu.x86.SegmentedAddress;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * Encodes an x86 instruction to either binary or intel syntax. NOTE: prefix are encoded in a specific order. First
 * there are the LOCK/REP/REPNE, then the CS segment override, then the address size override, then the operand size
 * override.
 */
@SuppressWarnings({
	"PMD.AvoidLiteralsInIfCondition",
	"PMD.NPathComplexity",
	"PMD.CyclomaticComplexity",
	"PMD.UselessParentheses",
	"PMD.NcssCount",
	"PMD.CognitiveComplexity",
	"PMD.TooManyStaticImports",
	"PMD.TooManyMethods",
	"PMD.LinguisticNaming",
	"PMD.CommentDefaultAccessModifier",
	"PMD.AvoidDuplicateLiterals",
	"PMD.CollapsibleIfStatements",
	"PMD.ConfusingTernary"
})
public final class InstructionEncoder {

	private static final byte DEFAULT_REX_PREFIX = (byte) 0x40;
	private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
	private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
	private static final byte DOUBLE_BYTE_OPCODE_PREFIX = (byte) 0x0f;
	private static final byte TABLE_A4_PREFIX = (byte) 0x38;
	private static final byte TABLE_A5_PREFIX = (byte) 0x3a;
	private static final byte OPCODE_GROUP_7_PREFIX = (byte) 0x01;
	private static final byte OPCODE_GROUP_9_PREFIX = (byte) 0xc7;
	private static final byte OPCODE_GROUP_15_PREFIX = (byte) 0xae;
	private static final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
	private static final Map<Opcode, Byte> CONDITIONAL_JUMPS_OPCODES = Map.ofEntries(
			Map.entry(Opcode.JO, (byte) 0x00),
			Map.entry(Opcode.JNO, (byte) 0x01),
			Map.entry(Opcode.JB, (byte) 0x02),
			Map.entry(Opcode.JAE, (byte) 0x03),
			Map.entry(Opcode.JE, (byte) 0x04),
			Map.entry(Opcode.JNE, (byte) 0x05),
			Map.entry(Opcode.JBE, (byte) 0x06),
			Map.entry(Opcode.JA, (byte) 0x07),
			Map.entry(Opcode.JS, (byte) 0x08),
			Map.entry(Opcode.JNS, (byte) 0x09),
			Map.entry(Opcode.JP, (byte) 0x0a),
			Map.entry(Opcode.JNP, (byte) 0x0b),
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
			Map.entry(Opcode.SETS, (byte) 0x08),
			Map.entry(Opcode.SETNS, (byte) 0x09),
			Map.entry(Opcode.SETL, (byte) 0x0c),
			Map.entry(Opcode.SETGE, (byte) 0x0d),
			Map.entry(Opcode.SETLE, (byte) 0x0e),
			Map.entry(Opcode.SETG, (byte) 0x0f));

	// The value for the Reg field for the instructions of the extended opcode group 2
	private static final Map<Opcode, Byte> GROUP2_REG_BYTES = Map.ofEntries(
			Map.entry(Opcode.ROL, (byte) 0b000),
			Map.entry(Opcode.ROR, (byte) 0b001),
			Map.entry(Opcode.RCL, (byte) 0b010),
			Map.entry(Opcode.RCR, (byte) 0b011),
			Map.entry(Opcode.SHL, (byte) 0b100),
			Map.entry(Opcode.SHR, (byte) 0b101),
			// 0b110 is reserved
			Map.entry(Opcode.SAR, (byte) 0b111));
	// The value for the Reg field for the IDIV/DIV/MUL instructions
	private static final Map<Opcode, Byte> DIV_MUL_REG_BYTES = Map.ofEntries(
			Map.entry(Opcode.IDIV, (byte) 0b111), Map.entry(Opcode.DIV, (byte) 0b110), Map.entry(Opcode.MUL, (byte)
					0b100));

	private InstructionEncoder() {}

	private static String operandString(final Instruction inst, final Operand op, final boolean shortHex) {
		return switch (op) {
			case IndirectOperand io -> {
				final Opcode code = inst.opcode();
				final boolean requiresExplicitPointerSize = code != Opcode.LEA
						&& code != Opcode.LDDQU
						&& code != Opcode.FXSAVE
						&& code != Opcode.FXRSTOR
						&& code != Opcode.XSAVE
						&& code != Opcode.XRSTOR
						&& code != Opcode.XSAVEC;
				final Optional<Integer> compressedDisplacement =
						((code == Opcode.VPTERNLOGD || code == Opcode.VPMINUB || code == Opcode.VPMINUD)
										&& inst.firstOperand() instanceof final Register r1
										&& Registers.requiresEvexExtension(r1)
										&& inst.secondOperand() instanceof final Register r2
										&& Registers.requiresEvexExtension(r2))
								? Optional.of(32)
								: Optional.empty();
				yield io.toIntelSyntax(requiresExplicitPointerSize, compressedDisplacement, shortHex);
			}
			case Immediate imm -> imm.toIntelSyntax(shortHex);
			case Register r -> r.toIntelSyntax();
			case SegmentedAddress sa -> sa.toIntelSyntax();
			default -> throw new IllegalArgumentException(String.format("Unknown operand type: '%s'.", op));
		};
	}

	/**
	 * Encodes a single instruction in intel syntax. Reference obtainable through <code>objdump -Mintel-mnemonic ...
	 * </code>
	 *
	 * @param inst The instruction to be encoded.
	 * @return An unambiguous String representation of this instruction with Intel syntax.
	 */
	public static String toIntelSyntax(final Instruction inst) {
		return toIntelSyntax(inst, true, 0, false);
	}

	/**
	 * Encodes a single instruction in intel syntax.
	 *
	 * @param inst The instruction to be encoded.
	 * @param checkInstructions When enabled, checks the instruction before encoding it.
	 * @return An unambiguous String representation of this instruction with Intel syntax.
	 */
	public static String toIntelSyntax(final Instruction inst, final boolean checkInstructions) {
		return toIntelSyntax(inst, checkInstructions, 0, false);
	}

	/**
	 * Encodes the given instruction into intel syntax representation.
	 *
	 * @param inst The instruction to be encoded.
	 * @param checkInstructions When enabled, checks the instruction before encoding it.
	 * @param opcodePad Number of spaces to pad the opcode with in the string.
	 * @param shortHex When enabled, does not add leading zeroes to immediates and/or indirect operand displacements.
	 * @return The input instruction encoded in intel syntax.
	 */
	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	public static String toIntelSyntax(
			final Instruction inst, final boolean checkInstructions, final int opcodePad, final boolean shortHex) {
		Objects.requireNonNull(inst);
		if (opcodePad < 0) {
			throw new IllegalArgumentException(String.format("Invalid opcode pad value: %,d.", opcodePad));
		}
		if (checkInstructions) {
			InstructionChecker.check(inst);
		}
		final StringBuilder sb = new StringBuilder();
		if (inst.hasPrefix()) {
			sb.append(inst.getPrefix().name().toLowerCase(Locale.US)).append(' ');
		}

		final String opcode = inst.opcode().mnemonic();
		sb.append(opcode);

		if (inst.hasFirstOperand()) {
			if (opcode.length() < opcodePad) {
				sb.append(" ".repeat(opcodePad - opcode.length()));
			}
			sb.append(' ').append(operandString(inst, inst.firstOperand(), shortHex));
			if (inst.hasDestinationMask()) {
				sb.append('{').append(inst.getDestinationMask().toIntelSyntax()).append('}');
			}
			if (inst.hasZeroDestinationMask()) {
				sb.append("{z}");
			}
			if (inst.hasSecondOperand()) {
				sb.append(',').append(operandString(inst, inst.secondOperand(), shortHex));
				if (inst.hasThirdOperand()) {
					sb.append(',').append(operandString(inst, inst.thirdOperand(), shortHex));
					if (inst.hasFourthOperand()) {
						sb.append(',').append(operandString(inst, inst.fourthOperand(), shortHex));
					}
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Encodes the given instructions into raw bytes.
	 *
	 * @param check When enabled, each instruction is checked for validity before being encoded.
	 * @param code The instructions to be encoded.
	 * @return The raw bytes containing the encoded instructions.
	 */
	public static byte[] toHex(final boolean check, final Instruction... code) {
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		for (final Instruction inst : code) {
			toHex(wb, inst, check);
		}
		return wb.array();
	}

	/**
	 * Encodes the given instruction into raw bytes.
	 *
	 * @param inst The instruction to be encoded.
	 * @param check When enabled, checks the given instruction for validity before encoding it.
	 * @return The raw bytes containing the encoded instruction.
	 */
	public static byte[] toHex(final Instruction inst, final boolean check) {
		Objects.requireNonNull(inst);
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(0, true);
		toHex(wb, inst, check);
		return wb.array();
	}

	private static void toHex(final WriteOnlyByteBuffer wb, final Instruction inst, final boolean check) {
		if (check) {
			InstructionChecker.check(inst);
		}

		encodePrefixes(wb, inst);

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

	enum Prefix {
		VEX2,
		VEX3,
		EVEX,
		NONE
	}

	private static void encodePrefixes(final WriteOnlyByteBuffer wb, final Instruction inst) {
		if (inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final IndirectOperand io
				&& io.hasSegment()
				&& io.getSegment() == SegmentRegister.CS) {
			wb.write(CS_SEGMENT_OVERRIDE_PREFIX);
		}
		if ((inst.hasFirstOperand() && requiresAddressSizeOverride(inst.firstOperand()))
				|| (inst.hasSecondOperand() && requiresAddressSizeOverride(inst.secondOperand()))) {
			wb.write(ADDRESS_SIZE_OVERRIDE_PREFIX);
		}
		if (requiresOperandSizeOverride(inst)) {
			wb.write(OPERAND_SIZE_OVERRIDE_PREFIX);
		}
		if (inst.hasLockPrefix()) {
			wb.write(InstructionPrefix.LOCK.getCode());
		}
		if (inst.hasRepPrefix()
				|| (inst.opcode() == Opcode.RDSSPQ
						|| inst.opcode() == Opcode.INCSSPQ
						|| inst.opcode() == Opcode.ENDBR64
						|| inst.opcode() == Opcode.TZCNT
						|| inst.opcode() == Opcode.DIVSS)
				|| (((inst.opcode() == Opcode.MOVQ || inst.opcode() == Opcode.MOVDQU)
						&& isFirstXMM(inst)
						&& isSecondM(inst)))) {
			wb.write(InstructionPrefix.REP.getCode());
		}
		if (inst.hasRepnzPrefix()
				|| ((inst.opcode() == Opcode.MOVSD && isFirstXMM(inst) && isSecondM(inst))
						|| (inst.opcode() == Opcode.CVTSI2SD)
						|| (inst.opcode() == Opcode.DIVSD)
						|| (inst.opcode() == Opcode.ADDSD)
						|| (inst.opcode() == Opcode.LDDQU)
						|| (inst.opcode() == Opcode.BND_JMP))) {
			wb.write(InstructionPrefix.REPNZ.getCode());
		}

		// TODO: maybe refactor this into 'checkRequiredPrefix'?
		encodeRexPrefix(wb, inst);

		switch (checkRequiredPrefix(inst)) {
			case VEX2 -> encodeVex2Prefix(wb, inst);
			case VEX3 -> encodeVex3Prefix(wb, inst);
			case EVEX -> encodeEvexPrefix(wb, inst);
			case NONE -> {}
		}
	}

	private static Prefix checkRequiredPrefix(final Instruction inst) {
		if (inst.getNumOperands() >= 3 && countEvexExtensions(inst) > 1) {
			return Prefix.EVEX;
		}
		return switch (inst.opcode()) {
			case VPOR, VPAND, VZEROALL, VMOVD, VPCMPGTB, VPSUBB, VPSLLDQ, VPSRLDQ, KMOVD, KUNPCKBW -> Prefix.VEX2;
			case VPMINUD, SARX, BZHI, VPCMPISTRI, VPSHUFB, KMOVQ, VPALIGNR, KORTESTD, KORD, KUNPCKDQ, VPCMPEQQ ->
				Prefix.VEX3;
			case VPXORQ, VMOVUPS, VMOVDQU8, VMOVDQU64, VBROADCASTSS, VMOVAPS, VPCMPNEQUB -> Prefix.EVEX;
			case VPMOVMSKB -> isSecondER(inst) ? Prefix.VEX3 : Prefix.VEX2;
			case VPXOR -> countExtensions(inst) >= 2 ? Prefix.VEX3 : Prefix.VEX2;
			case VPANDN -> countExtensions(inst) >= 3 ? Prefix.VEX3 : Prefix.VEX2;
			case VMOVNTDQ -> isSecondZ(inst) ? Prefix.EVEX : Prefix.VEX2;
			case VMOVQ ->
				(isFirstEER(inst) || isSecondEER(inst))
						? Prefix.EVEX
						: ((isFirstMS(inst) || isSecondM(inst)) ? Prefix.VEX2 : Prefix.VEX3);
			case VPMINUB -> countEvexExtensions(inst) > 0 ? Prefix.EVEX : Prefix.VEX2;
			case VPCMPEQB ->
				(isFirstMask(inst) && isSecondEER(inst) && isThirdM(inst))
						? Prefix.EVEX
						: (isThirdMS(inst) ? Prefix.VEX2 : Prefix.VEX3);
			case VPBROADCASTB, VPBROADCASTD -> isFirstEER(inst) ? Prefix.EVEX : Prefix.VEX3;
			default -> Prefix.NONE;
		};
	}

	private static boolean isSecondZ(final Instruction inst) {
		return inst.hasSecondOperand() && inst.secondOperand() instanceof RegisterZMM;
	}

	private static boolean isFirstMS(final Instruction inst) {
		return inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final IndirectOperand io
				&& isSimpleIndirectOperand(io);
	}

	private static boolean isThirdMS(final Instruction inst) {
		return inst.hasThirdOperand()
				&& inst.thirdOperand() instanceof final IndirectOperand io
				&& isSimpleIndirectOperand(io);
	}

	private static boolean isSecondEER(final Instruction inst) {
		return inst.hasSecondOperand()
				&& inst.secondOperand() instanceof final Register r
				&& Registers.requiresEvexExtension(r);
	}

	private static boolean isThirdEER(final Instruction inst) {
		return inst.hasThirdOperand()
				&& inst.thirdOperand() instanceof final Register r
				&& Registers.requiresEvexExtension(r);
	}

	private static boolean isFourthEER(final Instruction inst) {
		return inst.hasFourthOperand()
				&& inst.fourthOperand() instanceof final Register r
				&& Registers.requiresEvexExtension(r);
	}

	private static void encodeZeroOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		// TODO: refactor this into a map
		switch (inst.opcode()) {
			case VZEROALL -> wb.write((byte) 0x77);
			case NOP -> wb.write((byte) 0x90);
			case CWDE, CDQE -> wb.write((byte) 0x98);
			case CDQ -> wb.write((byte) 0x99);
			case FWAIT -> wb.write((byte) 0x9b);
			case PUSHF -> wb.write((byte) 0x9c);
			case POPF -> wb.write((byte) 0x9d);
			case SAHF -> wb.write((byte) 0x9e);
			case LAHF -> wb.write((byte) 0x9f);
			case RET -> wb.write((byte) 0xc3);
			case LEAVE -> wb.write((byte) 0xc9);
			case RETF -> wb.write((byte) 0xcb);
			case INT3 -> wb.write((byte) 0xcc);
			case IRET -> wb.write((byte) 0xcf);
			case HLT -> wb.write((byte) 0xf4);
			case CMC -> wb.write((byte) 0xf5);
			case CLC -> wb.write((byte) 0xf8);
			case STC -> wb.write((byte) 0xf9);
			case CLI -> wb.write((byte) 0xfa);
			case STI -> wb.write((byte) 0xfb);
			case CLD -> wb.write((byte) 0xfc);
			case STD -> wb.write((byte) 0xfd);
			case SYSCALL -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x05);
			case UD2 -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x0b);
			case CPUID -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xa2);
			case XGETBV -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_7_PREFIX, (byte) 0xd0);
			case XEND -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_7_PREFIX, (byte) 0xd5);
			case XTEST -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_7_PREFIX, (byte) 0xd6);
			case ENDBR64 -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1e, (byte) 0xfa);
			case SFENCE -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xae, (byte) 0xf8);
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
		byte reg = (byte) 0;
		switch (inst.opcode()) {
			case NOP -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x1f);
			case RET -> wb.write((byte) 0xc2);
			case RETF -> wb.write((byte) 0xca);
			case INT -> wb.write((byte) 0xcd);
			case LOOPNE -> wb.write((byte) 0xe0);
			case LOOPE -> wb.write((byte) 0xe1);
			case LOOP -> wb.write((byte) 0xe2);
			case JRCXZ -> wb.write((byte) 0xe3);
			case XLAT -> {
				if (inst.firstOperand() instanceof final IndirectOperand io
						&& io.getPointerSize() == PointerSize.BYTE_PTR
						&& io.hasSegment()
						&& io.getSegment() == SegmentRegister.DS
						&& io.hasBase()
						&& io.getBase() == Register64.RBX) {
					wb.write((byte) 0xd7);
					return;
				}
			}
			case FADD -> {
				if (inst.firstOperand() instanceof final IndirectOperand io) {
					if (io.getPointerSize() == PointerSize.DWORD_PTR) {
						wb.write((byte) 0xd8);
					} else if (io.getPointerSize() == PointerSize.QWORD_PTR) {
						wb.write((byte) 0xdc);
					}
				}
			}
			case FIADD -> {
				if (inst.firstOperand() instanceof final IndirectOperand io) {
					if (io.getPointerSize() == PointerSize.DWORD_PTR) {
						wb.write((byte) 0xda);
					} else if (io.getPointerSize() == PointerSize.WORD_PTR) {
						wb.write((byte) 0xde);
					}
				}
			}
			case FILD -> {
				if (inst.firstOperand() instanceof final IndirectOperand io) {
					if (io.getPointerSize() == PointerSize.DWORD_PTR) {
						wb.write((byte) 0xdb);
					} else if (io.getPointerSize() == PointerSize.WORD_PTR) {
						wb.write((byte) 0xdf);
					}
				}
			}
			case FLD -> {
				if (inst.firstOperand() instanceof final IndirectOperand io) {
					if (io.getPointerSize() == PointerSize.DWORD_PTR) {
						wb.write((byte) 0xd9);
					} else if (io.getPointerSize() == PointerSize.QWORD_PTR) {
						wb.write((byte) 0xdd);
					}
				}
			}
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
			case JA, JAE, JB, JBE, JG, JE, JL, JLE, JGE, JNE, JNS, JS, JP, JNP, JO, JNO -> {
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
			case BND_JMP -> wb.write((byte) 0xff);
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
				} else if (isFirstM(inst)) {
					wb.write((byte) 0xff);
					reg = (byte) 0b110;
				}
			}
			case POP -> {
				if (inst.firstOperand() instanceof final Register r) {
					wb.write(asByte((byte) 0x58 + Registers.toByte(r)));
					return;
				} else if (isFirstM(inst)) {
					wb.write((byte) 0x8f);
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
			case IMUL -> {
				wb.write((byte) 0xf7);
				reg = (byte) 0b101;
			}
			case SETB, SETO, SETNO, SETAE, SETE, SETNE, SETBE, SETA, SETS, SETNS, SETL, SETGE, SETLE, SETG ->
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
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_9_PREFIX);
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
			case SLDT -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x00);
			case FXSAVE -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_15_PREFIX);
				reg = (byte) 0b000;
			}
			case FXRSTOR -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_15_PREFIX);
				reg = (byte) 0b001;
			}
			case XSAVE -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_15_PREFIX);
				reg = (byte) 0b100;
			}
			case XRSTOR -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_15_PREFIX);
				reg = (byte) 0b101;
			}
			case STMXCSR -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_15_PREFIX);
				reg = (byte) 0b011;
			}
			case XSAVEC -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, OPCODE_GROUP_9_PREFIX);
				reg = (byte) 0b100;
			}
			case XBEGIN -> wb.write((byte) 0xc7, (byte) 0xf8);
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof final Register r) {
			encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r));
		} else if (inst.firstOperand() instanceof final IndirectOperand io) {
			if (isIpAndOffset(io)) {
				wb.write((byte) 0x25);
				encodeDisplacement(wb, io);
				return;
			}
			encodeModRM(
					wb, getMod(io), reg, isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if (io.hasBase() && isSP(io.getBase())) {
				// FIXME: this can be replaced with wb.write((byte) 0x24);
				encodeSIB(wb, (byte) 0b00, (byte) 0b100, (byte) 0b100);
			}
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof final Immediate imm) {
			if (imm.bits() == 8) {
				wb.write(imm.asByte());
			} else if (imm.bits() == 16) {
				wb.write(imm.asShort());
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
				&& io.getSegment() == SegmentRegister.DS
				&& !io.hasIndex()
				&& !io.hasScale()
				&& !io.hasDisplacement();
	}

	private static boolean isDefaultMovsDestination(final Operand op) {
		return op instanceof final IndirectOperand io
				&& io.hasBase()
				&& (io.getBase() == Register64.RDI || io.getBase() == Register32.EDI)
				&& io.hasSegment()
				&& io.getSegment() == SegmentRegister.ES
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
		// TODO: refactor
		return ((isFirstR16(inst)
								|| (inst.hasFirstOperand()
										&& inst.firstOperand() instanceof final IndirectOperand io
										&& io.getPointerSize() == PointerSize.WORD_PTR
										&& !(inst.hasSecondOperand()
												&& inst.secondOperand() instanceof SegmentRegister)))
						|| (inst.opcode() == Opcode.MOVDQA
								&& inst.firstOperand().bits() == 128)
						|| (inst.opcode() == Opcode.MOVAPD)
						|| (inst.opcode() == Opcode.MOVQ && isFirstXMM(inst) && !(isSecondM(inst)))
						|| (inst.opcode() == Opcode.MOVQ && isSecondXMM(inst))
						|| (inst.opcode() == Opcode.PUNPCKLQDQ)
						|| (inst.opcode() == Opcode.PUNPCKLDQ)
						|| (inst.opcode() == Opcode.PUNPCKLBW)
						|| (inst.opcode() == Opcode.PUNPCKHQDQ)
						|| (inst.opcode() == Opcode.PUNPCKLWD)
						|| (inst.opcode() == Opcode.PUNPCKHDQ)
						|| (inst.opcode() == Opcode.MOVHPD)
						|| (inst.opcode() == Opcode.PXOR && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.POR && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PAND && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PADDQ && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PSUBB && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PSUBW && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PSUBD && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PSUBQ && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PADDD)
						|| (inst.opcode() == Opcode.PMINUB && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PMINUD)
						|| (inst.opcode() == Opcode.PMAXUB && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PSHUFB && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.UCOMISD)
						|| (inst.opcode() == Opcode.PCMPEQB && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PCMPEQW && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PCMPEQD && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.MOVD && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.PMOVMSKB)
						|| (inst.opcode() == Opcode.PSLLDQ)
						|| (inst.opcode() == Opcode.PSRLDQ)
						|| (inst.opcode() == Opcode.MOVNTDQ)
						|| (inst.opcode() == Opcode.PCMPGTB)
						|| (inst.opcode() == Opcode.PSHUFD)
						|| (inst.opcode() == Opcode.SHUFPD)
						|| (inst.opcode() == Opcode.ANDPD)
						|| (inst.opcode() == Opcode.DIVPD)
						|| (inst.opcode() == Opcode.PALIGNR)
						|| (inst.opcode() == Opcode.PCMPISTRI)
						|| (inst.opcode() == Opcode.CALL || inst.opcode() == Opcode.JMP)
								&& isFirstM(inst)
								&& inst.firstOperand().bits() == 32)
				&& inst.opcode() != Opcode.OUTS
				&& inst.opcode() != Opcode.OUT
				&& inst.opcode() != Opcode.SLDT
				&& inst.opcode() != Opcode.FIADD
				&& inst.opcode() != Opcode.FILD;
	}

	private static boolean isSecondXMM(final Instruction inst) {
		return inst.hasSecondOperand() && inst.secondOperand() instanceof RegisterXMM;
	}

	private static boolean isFirstR16(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof Register16;
	}

	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	private static void encodeTwoOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		byte reg = 0;
		switch (inst.opcode()) {
			case ENTER -> {
				wb.write((byte) 0xc8);
				encodeImmediate(wb, (Immediate) inst.firstOperand());
				encodeImmediate(wb, (Immediate) inst.secondOperand());
				return;
			}
			case IN -> {
				if (inst.secondOperand() instanceof final Immediate imm && imm.bits() == 8) {
					if (inst.firstOperand() == Register8.AL) {
						wb.write((byte) 0xe4);
					} else if (inst.firstOperand() == Register32.EAX) {
						wb.write((byte) 0xe5);
					}
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() == Register16.DX) {
					if (inst.firstOperand() == Register8.AL) {
						wb.write((byte) 0xec);
					} else if (inst.firstOperand() == Register32.EAX) {
						wb.write((byte) 0xed);
					}
					return;
				}
			}
			case OUT -> {
				if (inst.firstOperand() instanceof final Immediate imm && imm.bits() == 8) {
					if (inst.secondOperand() == Register8.AL) {
						wb.write((byte) 0xe6);
					} else if (inst.secondOperand() == Register32.EAX) {
						wb.write((byte) 0xe7);
					}
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() == Register16.DX) {
					if (inst.secondOperand() == Register8.AL) {
						wb.write((byte) 0xee);
					} else if (inst.secondOperand() == Register32.EAX) {
						wb.write((byte) 0xef);
					}
					return;
				}
			}
			case CMOVE, CMOVNS, CMOVAE, CMOVB, CMOVBE, CMOVNE, CMOVG, CMOVGE, CMOVS, CMOVA, CMOVL, CMOVLE ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, asByte((byte) 0x40 + CONDITIONAL_MOVE_OPCODES.get(inst.opcode())));
			case CMP -> {
				if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof Register) {
					wb.write(io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x38 : (byte) 0x39);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x1d);
						encodeDisplacement(wb, io);
						return;
					}
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							imm.bits() == 8
									? (io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b111;

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x3d);
						encodeDisplacement(wb, io);
						encodeImmediate(wb, imm);
						return;
					}
				} else if (inst.firstOperand() instanceof final Register r && isSecondM(inst)) {
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
				if (isFirstR(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x89);
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(io.getPointerSize() == PointerSize.BYTE_PTR ? (byte) 0xc6 : (byte) 0xc7);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						encodeImmediate(wb, imm);
						return;
					}
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register r2) {
					if (r2 instanceof SegmentRegister) {
						wb.write((byte) 0x8c);
					} else {
						wb.write(r2 instanceof Register8 ? (byte) 0x88 : (byte) 0x89);
					}

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof final IndirectOperand io) {
					if (r instanceof SegmentRegister) {
						wb.write((byte) 0x8e);
					} else {
						wb.write((r instanceof Register8) ? (byte) 0x8a : (byte) 0x8b);
					}

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
				if (isFirstR(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x29);
				} else if (isFirstM(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x29);
				} else if ((isFirstR(inst) || isFirstM(inst)) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							(imm.bits() == 8)
									? ((inst.firstOperand().bits() == 8) ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b101;
				} else if (isFirstR(inst) && isSecondM(inst)) {
					wb.write((byte) 0x2b);
				}
			}
			case SBB -> {
				if (inst.firstOperand() == Register8.AL && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x1c);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() == Register16.AX
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x1d);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write((imm.bits() == 8) ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b011;
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof Register) {
					wb.write((r instanceof Register8) ? (byte) 0x18 : (byte) 0x19);
				} else if (isFirstR(inst) && isSecondM(inst)) {
					wb.write((byte) 0x1a);
					reg = (byte) 0b011;
				} else if (isFirstM(inst) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							(imm.bits() == 8)
									? ((inst.firstOperand().bits() == 8) ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b011;
				}
			}
			case ROL, ROR, RCL, RCR, SHR, SAR, SHL -> {
				reg = GROUP2_REG_BYTES.get(inst.opcode());
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
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					final boolean isImmediateOne = imm.bits() == 8 && imm.asByte() == (byte) 1;
					wb.write(or(isImmediateOne ? (byte) 0xd0 : (byte) 0xc0, (io.bits() == 8) ? (byte) 0 : (byte) 1));
					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
					if (isImmediateOne) {
						encodeModRM(
								wb,
								getMod(io),
								reg,
								isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
						encodeIndirectOperand(wb, io);
						return;
					}
				} else if (inst.firstOperand() instanceof final Register r1
						&& inst.secondOperand() instanceof final Register r2) {
					wb.write(r1 instanceof Register8 ? (byte) 0xd2 : (byte) 0xd3);
					if (r2 == Register8.CL) {
						encodeModRM(wb, (byte) 0b11, reg, Registers.toByte(r1));
					}
					return;
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register r2) {
					wb.write(io.bits() == 8 ? (byte) 0xd2 : (byte) 0xd3);
					if (r2 == Register8.CL) {
						encodeModRM(
								wb,
								getMod(io),
								reg,
								isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
						encodeIndirectOperand(wb, io);
					}
					return;
				}
			}
			case IMUL -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xaf);
			case OR -> {
				if (inst.firstOperand() == Register8.AL && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x0c);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand() == Register16.AX && imm.bits() == 16)
								|| (inst.firstOperand() == Register32.EAX && imm.bits() == 32)
								|| (inst.firstOperand() == Register64.RAX && imm.bits() == 32))) {
					wb.write((byte) 0x0d);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register8 && inst.secondOperand() instanceof Immediate) {
					wb.write((byte) 0x80);
					reg = (byte) 0b001;
				} else if (inst.firstOperand() instanceof final Register r
						&& inst.secondOperand() instanceof Register) {
					wb.write((r.bits() == 8) ? (byte) 0x08 : (byte) 0x09);
				} else if (inst.firstOperand() instanceof final Register r && isSecondM(inst)) {
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

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x0d);
						encodeDisplacement(wb, io);
						encodeImmediate(wb, imm);
						return;
					}
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
				if (inst.firstOperand() == Register8.AL && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x04);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand() == Register16.AX && imm.bits() == 16)
								|| (inst.firstOperand() == Register32.EAX && imm.bits() == 32)
								|| (inst.firstOperand() == Register64.RAX && imm.bits() == 32))) {
					wb.write((byte) 0x05);
					encodeImmediate(wb, imm);
					return;
				} else if (isFirstR(inst) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
				} else if (inst.firstOperand() instanceof final Register r && isSecondM(inst)) {
					wb.write((r instanceof Register8) ? (byte) 0x02 : (byte) 0x03);
				} else if (isFirstR(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x01);
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Register r) {
					wb.write((r instanceof Register8) ? (byte) 0x00 : (byte) 0x01);
					if (isIpAndOffset(io)) {
						wb.write((byte) 0x0d);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						encodeImmediate(wb, imm);
						return;
					}
				}
			}
			case ADC -> {
				if ((isFirstR(inst) || isFirstM(inst)) && inst.secondOperand() instanceof final Immediate imm) {
					if ((inst.firstOperand() == Register32.EAX || inst.firstOperand() == Register64.RAX)
							&& imm.bits() == 32) {
						wb.write((byte) 0x15);
						encodeImmediate(wb, imm);
						return;
					}
					wb.write(
							imm.bits() == 8
									? ((inst.firstOperand().bits() == 8) ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b010;
				} else if ((isFirstR(inst) || isFirstM(inst)) && inst.secondOperand() instanceof Register) {
					wb.write((inst.firstOperand().bits() == 8) ? (byte) 0x10 : (byte) 0x11);
				}
			}
			case AND -> {
				if (inst.firstOperand() == Register8.AL && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0x24);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand() == Register16.AX && imm.bits() == 16)
								|| (inst.firstOperand() == Register32.EAX && imm.bits() == 32)
								|| (inst.firstOperand() == Register64.RAX && imm.bits() == 32))) {
					wb.write((byte) 0x25);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.firstOperand() instanceof Register
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(imm.bits() == 8 ? (byte) 0x83 : (byte) 0x81);
					reg = (byte) 0b100;
				} else if (inst.firstOperand() instanceof Register8 && isSecondM(inst)) {
					wb.write((byte) 0x22);
				} else if (isFirstR(inst) && isSecondM(inst)) {
					wb.write((byte) 0x23);
				} else if (isFirstR(inst) && inst.secondOperand() instanceof Register) {
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
						&& inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x21);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x05);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case XOR -> {
				if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand() == Register16.AX && imm.bits() == 16)
								|| (inst.firstOperand() == Register32.EAX && imm.bits() == 32)
								|| (inst.firstOperand() == Register64.RAX && imm.bits() == 32))) {
					wb.write((byte) 0x35);
					encodeImmediate(wb, imm);
					return;
				} else if ((isFirstR(inst) || isFirstM(inst)) && inst.secondOperand() instanceof final Immediate imm) {
					wb.write(
							(imm.bits() == 8)
									? ((inst.firstOperand().bits() == 8) ? (byte) 0x80 : (byte) 0x83)
									: (byte) 0x81);
					reg = (byte) 0b110;
				} else if (inst.firstOperand() instanceof final IndirectOperand io
						&& inst.secondOperand() instanceof Register) {
					wb.write((io.bits() == 8) ? (byte) 0x30 : (byte) 0x31);
				} else if (inst.firstOperand() instanceof final Register r && isSecondM(inst)) {
					wb.write((r instanceof Register8) ? (byte) 0x32 : (byte) 0x33);
				}
			}
			case TEST -> {
				if (inst.firstOperand() == Register8.AL && inst.secondOperand() instanceof final Immediate imm) {
					wb.write((byte) 0xa8);
					encodeImmediate(wb, imm);
					return;
				} else if (inst.secondOperand() instanceof final Immediate imm
						&& ((inst.firstOperand() == Register16.AX && imm.bits() == 16)
								|| (inst.firstOperand() == Register32.EAX && imm.bits() == 32)
								|| (inst.firstOperand() == Register64.RAX && imm.bits() == 32))) {
					wb.write((byte) 0xa9);
					encodeImmediate(wb, imm);
					return;
				} else if ((isFirstM(inst) || inst.firstOperand() instanceof Register)
						&& inst.secondOperand() instanceof final Register r) {
					wb.write(r.bits() == 8 ? (byte) 0x84 : (byte) 0x85);
				} else if ((isFirstM(inst) || inst.firstOperand() instanceof Register)
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
			case CMPS -> {
				if (isDefaultMovsSource(inst.firstOperand()) && isDefaultMovsDestination(inst.secondOperand())) {
					wb.write(inst.firstOperand().bits() == 8 ? (byte) 0xa6 : (byte) 0xa7);
					return;
				}
			}
			case STOS -> {
				if (isDefaultMovsDestination(inst.firstOperand())) {
					if (inst.secondOperand() == Register8.AL) {
						wb.write((byte) 0xaa);
					} else if (inst.secondOperand() == Register32.EAX || inst.secondOperand() == Register64.RAX) {
						wb.write((byte) 0xab);
					}
					return;
				}
			}
			case LODS -> {
				if (isDefaultMovsSource(inst.secondOperand())) {
					if (inst.firstOperand() == Register8.AL) {
						wb.write((byte) 0xac);
					} else if (inst.firstOperand() == Register32.EAX) {
						wb.write((byte) 0xad);
					}
					return;
				}
			}
			case SCAS -> {
				if (isDefaultMovsDestination(inst.secondOperand())) {
					if (inst.firstOperand() == Register8.AL) {
						wb.write((byte) 0xae);
					} else if (inst.firstOperand() == Register32.EAX) {
						wb.write((byte) 0xaf);
					}
					return;
				}
			}
			case INS -> {
				if (inst.firstOperand() instanceof final IndirectOperand io
						&& io.hasBase()
						&& io.getBase() == Register64.RDI
						&& io.hasSegment()
						&& io.getSegment() == SegmentRegister.ES
						&& !io.hasIndex()
						&& !io.hasScale()
						&& !io.hasDisplacement()
						&& inst.secondOperand() == Register16.DX) {
					if (io.getPointerSize() == PointerSize.BYTE_PTR) {
						wb.write((byte) 0x6c);
					} else if (io.getPointerSize() == PointerSize.DWORD_PTR) {
						wb.write((byte) 0x6d);
					}
				}
				return;
			}
			case OUTS -> {
				if (inst.firstOperand() == Register16.DX
						&& inst.secondOperand() instanceof final IndirectOperand io
						&& io.hasBase()
						&& io.getBase() == Register64.RSI
						&& io.hasSegment()
						&& io.getSegment() == SegmentRegister.DS
						&& !io.hasIndex()
						&& !io.hasScale()
						&& !io.hasDisplacement()) {
					if (io.getPointerSize() == PointerSize.BYTE_PTR) {
						wb.write((byte) 0x6e);
					} else if (io.getPointerSize() == PointerSize.DWORD_PTR) {
						wb.write((byte) 0x6f);
					}
				}
				return;
			}
			case MOVDQA, MOVDQU -> {
				if (isFirstM(inst)) {
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
				if (isFirstXMM(inst) && isSecondXMM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x28);
				} else if (inst.firstOperand() instanceof final IndirectOperand io && isSecondXMM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x29);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x35);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else if (isFirstXMM(inst) && inst.secondOperand() instanceof final IndirectOperand io) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x28);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x35);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				}
			}
			case MOVQ -> {
				if ((isFirstXMM(inst) || isFirstMMX(inst)) && isSecondR64(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6e);
				} else if (isFirstM(inst) && isSecondXMM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xd6);
				} else if (isFirstXMM(inst) && isSecondM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x7e);
				} else if (isFirstMMX(inst) && isSecondM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6f);
				}
			}
			case MOVD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6e);
			case MOVHPS -> {
				if (isFirstM(inst) && isSecondXMM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x17);
				} else if (isFirstXMM(inst) && isSecondM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x16);
				}
			}
			case MOVHPD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x17);
			case MOVHLPS -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x12);
			case PUNPCKLBW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x60);
			case PUNPCKLWD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x61);
			case PUNPCKLDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x62);
			case PUNPCKHDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6a);
			case PUNPCKLQDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6c);
			case PUNPCKHQDQ -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x6d);
			case MOVABS -> {
				if (inst.firstOperand() == Register8.AL
						&& inst.secondOperand()
								instanceof SegmentedAddress(final SegmentRegister segment, final Immediate imm)
						&& segment == SegmentRegister.DS) {
					wb.write((byte) 0xa0);
					encodeImmediate(wb, imm);
					return;
				}
				if (inst.firstOperand() == Register32.EAX
						&& inst.secondOperand()
								instanceof SegmentedAddress(final SegmentRegister segment, final Immediate imm)
						&& segment == SegmentRegister.DS) {
					wb.write((byte) 0xa1);
					encodeImmediate(wb, imm);
					return;
				}
				if (inst.firstOperand() instanceof SegmentedAddress(final SegmentRegister segment, final Immediate imm)
						&& segment == SegmentRegister.DS
						&& inst.secondOperand() == Register8.AL) {
					wb.write((byte) 0xa2);
					encodeImmediate(wb, imm);
					return;
				}
				if (inst.firstOperand() instanceof SegmentedAddress(final SegmentRegister segment, final Immediate imm)
						&& segment == SegmentRegister.DS
						&& inst.secondOperand() == Register32.EAX) {
					wb.write((byte) 0xa3);
					encodeImmediate(wb, imm);
					return;
				}
				if (inst.firstOperand() instanceof final Register64 r
						&& inst.secondOperand() instanceof final Immediate imm) {
					wb.write(or((byte) 0xb8, Registers.toByte(r)));
					encodeImmediate(wb, imm);
					return;
				}
			}
			case MOVUPS -> {
				if (isFirstM(inst) && isSecondXMM(inst)) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x11);
				} else if (isFirstXMM(inst) && isSecondM(inst)) {
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
			case PADDD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xfe);
			case PMINUD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A4_PREFIX, (byte) 0x3b);
			case XCHG -> {
				if (inst.firstOperand() instanceof final Register r
						&& (inst.secondOperand() == Register16.AX
								|| inst.secondOperand() == Register32.EAX
								|| inst.secondOperand() == Register64.RAX)) {
					wb.write(asByte((byte) 0x90 + Registers.toByte(r)));
					return;
				} else if (inst.firstOperand() instanceof final IndirectOperand io && isSecondR(inst)) {
					wb.write((inst.firstOperand().bits() == 8) ? (byte) 0x86 : (byte) 0x87);

					if (isIpAndOffset(io)) {
						wb.write((byte) 0x25);
						wb.write(asInt(io.getDisplacement()));
						return;
					}
				} else {
					wb.write((inst.firstOperand().bits() == 8) ? (byte) 0x86 : (byte) 0x87);
				}
			}
			case CMPXCHG ->
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (inst.firstOperand().bits() == 8) ? (byte) 0xb0 : (byte) 0xb1);
			case BT, BTC, BTR, BTS -> {
				if (isFirstR(inst) && inst.secondOperand() instanceof Immediate) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xba);
					reg = BIT_TEST_OPCODES.get(inst.opcode());
				} else if (isFirstR(inst) && inst.secondOperand() instanceof Register) {
					wb.write(
							DOUBLE_BYTE_OPCODE_PREFIX,
							or((byte) 0b10000011, shl(BIT_TEST_OPCODES.get(inst.opcode()), 3)));
				}
			}
			case CVTSI2SD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x2a);
			case MOVNTPS -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x2b);
			case DIVSD, DIVSS, DIVPS, DIVPD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x5e);
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
				if (isFirstR(inst) && isSecondM(inst)) {
					wb.write((byte) 0x6f);
				} else if (isFirstM(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x7f);
				}
			}
			case VPMOVMSKB -> wb.write((byte) 0xd7);
			case VMOVQ -> {
				if ((isFirstXMM(inst) && isFirstEER(inst)) && isSecondM(inst)) {
					wb.write((byte) 0x6e);
				} else if (isFirstR64(inst) && isSecondXMM(inst)) {
					wb.write((byte) 0x7e);
				} else {
					if (isFirstXMM(inst) && isSecondM(inst)) {
						wb.write((byte) 0x7e);
					} else if (isFirstM(inst) && isSecondXMM(inst)) {
						wb.write((byte) 0xd6);
					}
				}
			}
			case VMOVD -> wb.write((byte) 0x6e);
			case VPBROADCASTB -> {
				if (inst.firstOperand() instanceof RegisterZMM && inst.secondOperand() instanceof Register32) {
					wb.write((byte) 0x7a);
				} else {
					wb.write((byte) 0x78);
				}
			}
			case VPBROADCASTD -> {
				if (inst.firstOperand() instanceof RegisterZMM && inst.secondOperand() instanceof Register32) {
					wb.write((byte) 0x7c);
				} else {
					wb.write((byte) 0x58);
				}
			}
			case MOVBE -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A4_PREFIX, (byte) 0xf0);
			case LDDQU -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xf0);
			case VMOVUPS -> {
				if (isFirstR(inst) && isSecondM(inst)) {
					wb.write((byte) 0x10);
				} else if (isFirstM(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x11);
				}
			}
			case VMOVDQU8, VMOVDQU64 -> {
				if (isFirstM(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x7f);
				} else if (isFirstR(inst) && isSecondM(inst)) {
					wb.write((byte) 0x6f);
				}
			}
			case VMOVNTDQ -> wb.write((byte) 0xe7);
			case PCMPGTB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x64);
			case PSHUFB -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A4_PREFIX, (byte) 0x00);
			case VBROADCASTSS -> wb.write((byte) 0x18);
			case VMOVAPS -> wb.write((byte) 0x29);
			case KMOVQ -> {
				if (isFirstR64(inst) && inst.secondOperand() instanceof MaskRegister) {
					wb.write((byte) 0x93);
				} else if (isFirstMask(inst) && isSecondR64(inst)) {
					wb.write((byte) 0x92);
				}
			}
			case KMOVD -> {
				if (isFirstMask(inst) && inst.secondOperand() instanceof Register) {
					wb.write((byte) 0x92);
				} else if (isFirstR(inst) && inst.secondOperand() instanceof MaskRegister) {
					wb.write((byte) 0x93);
				}
			}
			case KORTESTD -> wb.write((byte) 0x98);
			case TZCNT -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xbc);
			case MOVMSKPS -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x50);
			case ANDPD -> {
				wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x54);
				reg = (byte) 0b001;
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		// FIXME: refactor all the isIpAndOffset calls to one case down here
		if (inst.firstOperand() instanceof final Register r1 && inst.secondOperand() instanceof final Register r2) {
			// Most ALU operations encode the destination operand (the first one) in the r/m
			// portion, while some
			// instructions like MOV encode the destination as Reg
			// FIXME: actually, it seems to be the opposite... why?
			if (inst.opcode() == Opcode.MOV
					|| inst.opcode() == Opcode.SUB
					|| inst.opcode() == Opcode.SBB
					|| inst.opcode() == Opcode.CMP
					|| inst.opcode() == Opcode.ADD
					|| inst.opcode() == Opcode.ADC
					|| inst.opcode() == Opcode.AND
					|| inst.opcode() == Opcode.OR
					|| inst.opcode() == Opcode.ROL
					|| inst.opcode() == Opcode.ROR
					|| inst.opcode() == Opcode.RCR
					|| inst.opcode() == Opcode.RCL
					|| inst.opcode() == Opcode.SHL
					|| inst.opcode() == Opcode.SHR
					|| inst.opcode() == Opcode.SAR
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
			if (isIpAndOffset(io)) {
				encodeModRM(wb, (byte) 0b00, reg, (byte) 0b101);
				wb.write(asInt(io.getDisplacement()));
				return;
			}
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if ((inst.opcode() == Opcode.MOV
							|| inst.opcode() == Opcode.MOVSXD
							|| inst.opcode() == Opcode.CMOVE
							|| isConditionalMove(inst.opcode())
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

	private static boolean isFirstR(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof Register;
	}

	private static boolean isConditionalMove(final Opcode opcode) {
		return opcode == Opcode.CMOVE
				|| opcode == Opcode.CMOVNE
				|| opcode == Opcode.CMOVA
				|| opcode == Opcode.CMOVAE
				|| opcode == Opcode.CMOVB
				|| opcode == Opcode.CMOVBE
				|| opcode == Opcode.CMOVG
				|| opcode == Opcode.CMOVGE
				|| opcode == Opcode.CMOVL
				|| opcode == Opcode.CMOVLE
				|| opcode == Opcode.CMOVS
				|| opcode == Opcode.CMOVNS;
	}

	private static void encodeRexPrefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		final boolean isShift =
				inst.opcode() == Opcode.SHR || inst.opcode() == Opcode.SAR || inst.opcode() == Opcode.SHL;

		byte rex = DEFAULT_REX_PREFIX;
		if ((isFirstR64(inst)
						|| isFirstMMX(inst)
						|| isFirstM64(inst)
						|| (inst.opcode() == Opcode.MOVQ && isFirstMMX(inst))
						|| (inst.opcode() == Opcode.MOVQ && isFirstXMM(inst))
						|| (inst.opcode() == Opcode.CVTSI2SD && isSecondR64(inst))
						|| (inst.opcode() == Opcode.CDQE))
				&& !(inst.opcode() == Opcode.MOVQ && isFirstM(inst))
				&& !(inst.opcode() == Opcode.MOVQ && isSecondM(inst))
				&& !(inst.opcode() == Opcode.PXOR && isFirstMMX(inst))
				&& !(inst.opcode() == Opcode.PCMPEQB && isFirstMMX(inst))
				&& !(inst.opcode() == Opcode.PCMPEQW && isFirstMMX(inst))
				&& !(inst.opcode() == Opcode.PCMPEQD && isFirstMMX(inst))
				&& inst.opcode() != Opcode.MOVD
				&& inst.opcode() != Opcode.MOVDQA
				&& inst.opcode() != Opcode.MOVDQU
				&& inst.opcode() != Opcode.MOVAPS
				&& inst.opcode() != Opcode.MOVAPD
				&& inst.opcode() != Opcode.MOVHPS
				&& inst.opcode() != Opcode.MOVHPD
				&& inst.opcode() != Opcode.VMOVQ
				&& inst.opcode() != Opcode.KMOVQ
				&& inst.opcode() != Opcode.BZHI
				&& inst.opcode() != Opcode.PSHUFW
				&& inst.opcode() != Opcode.CALL
				&& inst.opcode() != Opcode.JMP
				&& inst.opcode() != Opcode.BND_JMP
				&& inst.opcode() != Opcode.PUSH
				&& inst.opcode() != Opcode.POP
				&& inst.opcode() != Opcode.FADD
				&& inst.opcode() != Opcode.FLD
				&& inst.opcode() != Opcode.FXSAVE
				&& inst.opcode() != Opcode.FXRSTOR
				&& inst.opcode() != Opcode.XSAVE
				&& inst.opcode() != Opcode.XRSTOR
				&& inst.opcode() != Opcode.XSAVEC) {
			rex = or(rex, (byte) 0b1000);
		}
		if ((inst.opcode() == Opcode.CMP && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOV && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVSXD && isFirstER(inst))
				|| (isConditionalMove(inst.opcode()) && isFirstER(inst))
				|| (inst.opcode() == Opcode.LEA && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOVZX && isFirstER(inst))
				|| (inst.opcode() == Opcode.ADD && isFirstER(inst) && isSecondM(inst))
				|| (inst.opcode() == Opcode.ADD && isFirstM(inst) && isSecondER(inst))
				|| (inst.opcode() == Opcode.ADD && isFirstR(inst) && isSecondER(inst))
				|| (inst.opcode() == Opcode.ADC && isFirstM(inst) && isSecondER(inst))
				|| (inst.opcode() == Opcode.AND && isSecondER(inst))
				|| (inst.opcode() == Opcode.SUB && isFirstER(inst) && isSecondM(inst))
				|| (inst.opcode() == Opcode.SUB && isSecondER(inst))
				|| (inst.opcode() == Opcode.SBB && isSecondER(inst))
				|| (inst.opcode() == Opcode.OR && isSecondER(inst))
				|| (inst.opcode() == Opcode.TEST && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVUPS && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVSD && isFirstER(inst))
				|| (inst.opcode() == Opcode.XCHG && isSecondER(inst))
				|| (inst.opcode() == Opcode.CMPXCHG && isSecondER(inst))
				|| (inst.opcode() == Opcode.BTC && isSecondER(inst))
				|| (inst.opcode() == Opcode.BTR && isSecondER(inst))
				|| (inst.opcode() == Opcode.BTS && isSecondER(inst))
				|| (inst.opcode() == Opcode.CVTSI2SD && isFirstER(inst))
				|| (inst.opcode() == Opcode.DIVSD && isFirstER(inst))
				|| (inst.opcode() == Opcode.ADDSD && isFirstER(inst))
				|| (inst.opcode() == Opcode.XORPS && isFirstER(inst))
				|| (inst.opcode() == Opcode.UCOMISD && isFirstER(inst))
				|| (inst.opcode() == Opcode.UCOMISS && isFirstER(inst))
				|| (inst.opcode() == Opcode.XADD && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVDQA && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOVDQU && isFirstER(inst))
				|| (inst.opcode() == Opcode.PCMPGTB && isFirstER(inst))
				|| (inst.opcode() == Opcode.XOR && isFirstER(inst) && isSecondM(inst))
				|| (inst.opcode() == Opcode.IMUL && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOVMSKPS && isFirstER(inst))
				|| (inst.opcode() == Opcode.SHLD && isSecondER(inst))
				|| (inst.opcode() == Opcode.SHRD && isSecondER(inst))) {
			rex = or(rex, (byte) 0b0100);
		}
		if ((inst.opcode() == Opcode.IMUL && inst.hasSecondOperand() && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.MOV && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.MOV && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.NOP && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.MOVSXD && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.CMP && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.CMP && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.OR && hasExtendedFirstIndex(inst) && !(isSecondR64(inst)))
				|| (inst.opcode() == Opcode.LEA && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.MOVDQA && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.MOVAPS && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.MOVAPS && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.MOVAPD && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.MOVQ && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.MOVD && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.PXOR && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.POR && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.PAND && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.PADDQ && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.PSUBQ && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.SUB && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.IMUL && inst.hasSecondOperand() && hasExtendedIndex(inst.secondOperand()))
				|| (inst.opcode() == Opcode.CALL && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.DIV && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.NEG && hasExtendedFirstIndex(inst))
				|| (isSetOpcode(inst.opcode()) && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.PREFETCHNTA && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.PREFETCHT0 && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.PREFETCHT1 && hasExtendedFirstIndex(inst))
				|| (inst.opcode() == Opcode.PREFETCHT2 && hasExtendedFirstIndex(inst))) {
			rex = or(rex, (byte) 0b0010);
		}
		// TODO: inline 'isShift'
		if ((isShift && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOV && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.MOV && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOVSXD && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVSXD && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.CMP && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.CMP && isFirstER(inst))
				|| (inst.opcode() == Opcode.OR && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.OR && hasExtendedBase(inst.secondOperand()))
				|| (isConditionalMove(inst.opcode()) && isSecondER(inst))
				|| (isConditionalMove(inst.opcode()) && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.ADD && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.ADD && isFirstER(inst) && inst.secondOperand() instanceof Immediate)
				|| (inst.opcode() == Opcode.ADD && isFirstER(inst) && isSecondR(inst))
				|| (inst.opcode() == Opcode.ADC && isFirstER(inst))
				|| (inst.opcode() == Opcode.ADC && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.AND && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.AND && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.AND && isFirstER(inst))
				|| (inst.opcode() == Opcode.SUB && isFirstER(inst) && isSecondR(inst))
				|| (inst.opcode() == Opcode.SUB && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.SUB && isFirstER(inst) && inst.secondOperand() instanceof Immediate)
				|| (inst.opcode() == Opcode.SBB && isFirstER(inst) && isSecondR(inst))
				|| (inst.opcode() == Opcode.SBB && isFirstER(inst) && inst.secondOperand() instanceof Immediate)
				|| (inst.opcode() == Opcode.XOR && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.XOR && isFirstER(inst) && inst.secondOperand() instanceof Immediate)
				|| (inst.opcode() == Opcode.TEST && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.TEST && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOVQ && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVQ && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.MOVDQA && isSecondER(inst))
				|| (inst.opcode() == Opcode.PUNPCKLQDQ && isSecondER(inst))
				|| (inst.opcode() == Opcode.PUNPCKLDQ && isSecondER(inst))
				|| (inst.opcode() == Opcode.PUNPCKHQDQ && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVUPS && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.MOVSD && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.PXOR && isSecondER(inst))
				|| (inst.opcode() == Opcode.POR && isSecondER(inst))
				|| (inst.opcode() == Opcode.PAND && isSecondER(inst))
				|| (inst.opcode() == Opcode.PADDQ && isSecondER(inst))
				|| (inst.opcode() == Opcode.PSUBB && isSecondER(inst))
				|| (inst.opcode() == Opcode.PSUBW && isSecondER(inst))
				|| (inst.opcode() == Opcode.PSUBD && isSecondER(inst))
				|| (inst.opcode() == Opcode.PSUBQ && isSecondER(inst))
				|| (inst.opcode() == Opcode.XCHG && isFirstER(inst))
				|| (inst.opcode() == Opcode.DIVSD && isSecondER(inst))
				|| (inst.opcode() == Opcode.ADDSD && isSecondER(inst))
				|| (inst.opcode() == Opcode.XORPS && isSecondER(inst))
				|| (inst.opcode() == Opcode.PCMPEQB && isSecondER(inst))
				|| (inst.opcode() == Opcode.PCMPEQW && isSecondER(inst))
				|| (inst.opcode() == Opcode.PCMPEQD && isSecondER(inst))
				|| (inst.opcode() == Opcode.MOVD && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.ROR && isFirstER(inst))
				|| (inst.opcode() == Opcode.MOVZX && isSecondER(inst))
				|| (inst.opcode() == Opcode.IMUL && inst.hasSecondOperand() && hasExtendedBase(inst.secondOperand()))
				|| (inst.opcode() == Opcode.IMUL && isSecondER(inst))
				|| (inst.opcode() == Opcode.CALL && isFirstER(inst))
				|| (inst.opcode() == Opcode.CALL && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.JMP && isFirstER(inst))
				|| (inst.opcode() == Opcode.JMP && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.PUSH && isFirstER(inst))
				|| (inst.opcode() == Opcode.PUSH && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.POP && isFirstER(inst))
				|| (inst.opcode() == Opcode.IDIV && isFirstER(inst))
				|| (inst.opcode() == Opcode.DIV && isFirstER(inst))
				|| (inst.opcode() == Opcode.DIV && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.MUL && isFirstER(inst))
				|| (inst.opcode() == Opcode.NOT && isFirstER(inst))
				|| (inst.opcode() == Opcode.NEG && hasExtendedBase(inst.firstOperand()))
				|| (isSetOpcode(inst.opcode()) && isFirstER(inst))
				|| (inst.opcode() == Opcode.INC && isFirstER(inst))
				|| (inst.opcode() == Opcode.INC && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.DEC && isFirstER(inst))
				|| (inst.opcode() == Opcode.DEC && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.BSWAP && isFirstER(inst))
				|| (inst.opcode() == Opcode.PREFETCHNTA && isFirstER(inst))
				|| (inst.opcode() == Opcode.PREFETCHNTA && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.PREFETCHT0 && isFirstER(inst))
				|| (inst.opcode() == Opcode.PREFETCHT0 && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.PREFETCHT1 && isFirstER(inst))
				|| (inst.opcode() == Opcode.PREFETCHT1 && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.PREFETCHT2 && isFirstER(inst))
				|| (inst.opcode() == Opcode.PREFETCHT2 && hasExtendedBase(inst.firstOperand()))
				|| (inst.opcode() == Opcode.RDRAND && isFirstER(inst))
				|| (inst.opcode() == Opcode.RDSEED && isFirstER(inst))
				|| (inst.opcode() == Opcode.RDSSPQ && isFirstER(inst))
				|| (inst.opcode() == Opcode.INCSSPQ && isFirstER(inst))
				|| (inst.opcode() == Opcode.RCR && isFirstER(inst))
				|| (inst.opcode() == Opcode.RCR && hasExtendedBase(inst.firstOperand()))) {
			rex = or(rex, (byte) 0b0001);
		}
		if (rex != DEFAULT_REX_PREFIX || isFirstRexR8(inst) || isSecondRexR8(inst)) {
			wb.write(rex);
		}
	}

	private static boolean isSecondRexR8(final Instruction inst) {
		return inst.hasSecondOperand()
				&& inst.secondOperand() instanceof final Register8 r
				&& Register8.requiresRexPrefix(r);
	}

	private static boolean isFirstRexR8(final Instruction inst) {
		return inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final Register8 r
				&& Register8.requiresRexPrefix(r);
	}

	private static boolean isSetOpcode(final Opcode opcode) {
		return SET_OPCODES.containsKey(opcode);
	}

	private static boolean isSecondER(final Instruction inst) {
		return inst.hasSecondOperand()
				&& inst.secondOperand() instanceof final Register r
				&& Registers.requiresExtension(r);
	}

	private static boolean isThirdER(final Instruction inst) {
		return inst.hasThirdOperand()
				&& inst.thirdOperand() instanceof final Register r
				&& Registers.requiresExtension(r);
	}

	private static boolean isFourthER(final Instruction inst) {
		return inst.hasFourthOperand()
				&& inst.fourthOperand() instanceof final Register r
				&& Registers.requiresExtension(r);
	}

	private static boolean isSecondM(final Instruction inst) {
		return inst.hasSecondOperand() && inst.secondOperand() instanceof IndirectOperand;
	}

	private static boolean isFirstM(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof IndirectOperand;
	}

	private static boolean isFirstMMX(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof RegisterMMX;
	}

	private static boolean hasExtendedFirstIndex(final Instruction inst) {
		return inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final IndirectOperand io
				&& io.hasIndex()
				&& Registers.requiresExtension(io.getIndex());
	}

	private static boolean isFirstM64(final Instruction inst) {
		return inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final IndirectOperand io
				&& io.getPointerSize() == PointerSize.QWORD_PTR;
	}

	private static boolean isFirstER(final Instruction inst) {
		return inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final Register r
				&& Registers.requiresExtension(r);
	}

	private static boolean isFirstR64(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof Register64;
	}

	private static boolean isFirstXMM(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof RegisterXMM;
	}

	private static void encodeThreeOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		byte reg = -1;
		byte lastByte = -1;
		switch (inst.opcode()) {
			case IMUL -> {
				if (inst.firstOperand() instanceof Register
						&& (inst.secondOperand() instanceof Register || isSecondM(inst))
						&& inst.thirdOperand() instanceof final Immediate imm) {
					wb.write((imm.bits() == 8) ? (byte) 0x6b : (byte) 0x69);
				}
			}
			case PSHUFD, PSHUFW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0x70);
			case SHUFPS, SHUFPD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc6);
			case PALIGNR -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A5_PREFIX, (byte) 0x0f);
			case PCMPISTRI -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, TABLE_A5_PREFIX, (byte) 0x63);
			case VPXOR, VPXORQ -> wb.write((byte) 0xef);
			case VPOR, VPORQ -> wb.write((byte) 0xeb);
			case VPAND -> wb.write((byte) 0xdb);
			case VPANDN -> wb.write((byte) 0xdf);
			case VPMINUB -> wb.write((byte) 0xda);
			case VPMINUD -> wb.write((byte) 0x3b);
			case VPCMPGTB -> wb.write((byte) 0x64);
			case VPCMPEQB -> {
				if (isFirstMask(inst) && isSecondR(inst) && (isThirdM(inst) || isThirdR(inst))) {
					wb.write((byte) 0x3f);
					lastByte = (byte) 0x00;
				} else {
					wb.write((byte) 0x74);
				}
			}
			case VPCMPEQD -> {
				if (isFirstMask(inst) && isSecondR(inst) && isThirdM(inst)) {
					encodeEvexPrefix(wb, inst);
					wb.write((byte) 0x1f);
					lastByte = (byte) 0x00;
				} else {
					encodeVex2Prefix(wb, inst);
					wb.write((byte) 0x76);
				}
			}
			case VPCMPEQQ -> wb.write((byte) 0x29);
			case VPCMPNEQB -> {
				if (isFirstMask(inst) && isSecondR(inst) && inst.thirdOperand() instanceof IndirectOperand) {
					encodeEvexPrefix(wb, inst);
					wb.write((byte) 0x3f);
					lastByte = (byte) 0x04;
				}
			}
			case PEXTRW -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xc5);
			case SARX -> wb.write((byte) 0xf7);
			case BZHI -> wb.write((byte) 0xf5);
			case VPSUBB -> wb.write((byte) 0xf8);
			case VPCMPISTRI -> wb.write((byte) 0x63);
			case VPSLLDQ -> {
				wb.write((byte) 0x73);
				reg = (byte) 0b111;
			}
			case VPSRLDQ -> {
				wb.write((byte) 0x73);
				reg = (byte) 0b011;
			}
			case VPSHUFB -> wb.write((byte) 0x00);
			case VPCMPNEQUB -> {
				wb.write((byte) 0x3e);
				lastByte = (byte) 0x04;
			}
			case VPTESTMB -> wb.write((byte) 0x26);
			case KORD -> wb.write((byte) 0x45);
			case KUNPCKDQ -> wb.write((byte) 0x4b);
			case KUNPCKBW -> wb.write((byte) 0x4b);
			case SHLD -> wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xa5);
			case SHRD -> {
				if (inst.thirdOperand() instanceof Register8) {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xad);
				} else {
					wb.write(DOUBLE_BYTE_OPCODE_PREFIX, (byte) 0xac);
					reg = Registers.toByte((Register) inst.secondOperand());
				}
			}
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof final Register r2
				&& inst.thirdOperand() instanceof final Immediate imm) {
			if (reg != -1) {
				encodeModRM(
						wb,
						(byte) 0b11,
						reg,
						(inst.opcode() == Opcode.VPSRLDQ || inst.opcode() == Opcode.VPSLLDQ)
								? Registers.toByte(r2)
								: Registers.toByte(r1));
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
				&& isThirdR(inst)) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r2));
		} else if ((requiresVex2Prefix(inst) || requiresVex3Prefix(inst) || requiresEvexPrefix(inst))
				&& inst.firstOperand() instanceof final Register r1
				&& !(isFirstMask(inst))
				&& isSecondR(inst)
				&& inst.thirdOperand() instanceof final Register r3) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r3));
		} else if (inst.firstOperand() instanceof final MaskRegister r1
				&& isSecondR(inst)
				&& inst.thirdOperand() instanceof final IndirectOperand io) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r1),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
			wb.write(lastByte);
		} else if (inst.firstOperand() instanceof final MaskRegister r1
				&& isSecondR(inst)
				&& inst.thirdOperand() instanceof final Register r3) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r3));
			if (inst.opcode() != Opcode.VPTESTMB
					&& inst.opcode() != Opcode.KORD
					&& inst.opcode() != Opcode.KUNPCKDQ
					&& inst.opcode() != Opcode.KUNPCKBW) {
				wb.write(lastByte);
			}
		} else if (inst.firstOperand() instanceof final Register r1
				&& isSecondR(inst)
				&& inst.thirdOperand() instanceof final IndirectOperand io) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r1),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			if (io.hasBase() && isSP(io.getBase())) {
				// FIXME: this can be replaced with wb.write((byte) 0x24);
				encodeSIB(wb, (byte) 0b00, (byte) 0b100, (byte) 0b100);
			}
			encodeIndirectOperand(wb, io);
		} else if (inst.firstOperand() instanceof final Register r1
				&& inst.secondOperand() instanceof final Register r2
				&& inst.thirdOperand() == Register8.CL) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r2), Registers.toByte(r1));
		}
	}

	private static boolean isThirdR(final Instruction inst) {
		return inst.hasThirdOperand() && inst.thirdOperand() instanceof Register;
	}

	private static boolean isThirdM(final Instruction inst) {
		return inst.hasThirdOperand() && inst.thirdOperand() instanceof IndirectOperand;
	}

	private static boolean isSecondR(final Instruction inst) {
		return inst.hasSecondOperand() && inst.secondOperand() instanceof Register;
	}

	private static boolean isFirstMask(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof MaskRegister;
	}

	private static void encodeFourOperandsInstruction(final WriteOnlyByteBuffer wb, final Instruction inst) {
		switch (inst.opcode()) {
			case VPALIGNR -> wb.write((byte) 0x0f);
			case VPTERNLOGD -> wb.write((byte) 0x25);
			default -> throw new IllegalArgumentException(String.format("Unknown opcode: '%s'.", inst.opcode()));
		}

		if (inst.firstOperand() instanceof final Register r1
				&& isSecondR(inst)
				&& inst.thirdOperand() instanceof final IndirectOperand io
				&& inst.fourthOperand() instanceof final Immediate imm) {
			encodeModRM(
					wb,
					getMod(io),
					Registers.toByte(r1),
					isSimpleIndirectOperand(io) ? Registers.toByte(io.getBase()) : (byte) 0b100);
			encodeIndirectOperand(wb, io);
			encodeImmediate(wb, imm);
		} else if (inst.firstOperand() instanceof final Register r1
				&& isSecondR(inst)
				&& inst.thirdOperand() instanceof final Register r3
				&& inst.fourthOperand() instanceof final Immediate imm) {
			encodeModRM(wb, (byte) 0b11, Registers.toByte(r1), Registers.toByte(r3));
			encodeImmediate(wb, imm);
		}
	}

	private static boolean isFirstEER(final Instruction inst) {
		return inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final Register r
				&& Registers.requiresEvexExtension(r);
	}

	private static boolean requiresVex2Prefix(final Instruction inst) {
		if (inst.hasDestinationMask()
				|| (inst.getNumOperands() >= 3 && (countExtensions(inst) >= 2 || countEvexExtensions(inst) > 0))) {
			return false;
		}
		return switch (inst.opcode()) {
			case VPXOR, VPMINUB, VPCMPGTB, VPOR, VPAND, VPANDN, VPSUBB, VPMOVMSKB, VZEROALL -> true;
			case VPCMPEQB ->
				!(isFirstR(inst)
						&& inst.thirdOperand() instanceof final IndirectOperand io
						&& !isSimpleIndirectOperand(io));
			default -> false;
		};
	}

	private static int countEvexExtensions(final Instruction inst) {
		int count = 0;
		if (isFirstEER(inst)) {
			count++;
		}
		if (isSecondEER(inst)) {
			count++;
		}
		if (isThirdEER(inst)) {
			count++;
		}
		if (isFourthEER(inst)) {
			count++;
		}
		return count;
	}

	private static int countExtensions(final Instruction inst) {
		int count = 0;
		if (isFirstER(inst)) {
			count++;
		}
		if (isSecondER(inst)) {
			count++;
		}
		if (isThirdER(inst)) {
			count++;
		}
		if (isFourthER(inst)) {
			count++;
		}
		return count;
	}

	private static boolean requiresVex3Prefix(final Instruction inst) {
		if (isFirstZ(inst)) {
			return false;
		}
		if (inst.getNumOperands() >= 3 && isFirstER(inst) && isSecondER(inst) && isThirdER(inst)) {
			return true;
		}
		return switch (inst.opcode()) {
			case SARX, BZHI, VPSHUFB, VPBROADCASTB, VPBROADCASTD, VPMINUD, VPANDN, KORD, KUNPCKDQ, KORTESTD -> true;
			case VPCMPEQB ->
				(isFirstR(inst) && !isFirstMask(inst))
						&& isSecondR(inst)
						&& inst.thirdOperand() instanceof final IndirectOperand io
						&& !isSimpleIndirectOperand(io);
			default -> false;
		};
	}

	private static boolean isFirstZ(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof RegisterZMM;
	}

	private static boolean requiresEvexPrefix(final Instruction inst) {
		return switch (inst.opcode()) {
			case VPTESTMB, VPORQ, VPXORQ, VMOVNTDQ, VMOVDQU8, VMOVDQU64, VMOVUPS, VPBROADCASTB, VPBROADCASTD -> true;
			case VPCMPEQB -> isFirstMask(inst) && isSecondEER(inst) && (isThirdM(inst) || isThirdR(inst));
			case VPMINUB -> isFirstEER(inst) && isSecondEER(inst);
			default -> false;
		};
	}

	private static byte getVex3OpcodeMap(final Opcode opcode) {
		return switch (opcode) {
			case VPXOR, VMOVDQU, VPCMPEQB, VPANDN, VMOVQ, KMOVQ, KORTESTD, KORD, KUNPCKDQ, VPMOVMSKB -> (byte) 0b01;
			case VPBROADCASTB, VPBROADCASTD, SARX, BZHI, VPSHUFB, VPMINUD, VPCMPEQQ -> (byte) 0b10;
			case VPCMPISTRI, VPALIGNR -> (byte) 0b11;
			default -> throw new IllegalArgumentException(String.format("Unknown VEX3 opcode map for %s.", opcode));
		};
	}

	private static byte getImpliedPrefix(final Instruction inst) {
		return switch (inst.opcode()) {
			case VPXOR,
					VPXORQ,
					VPOR,
					VPORQ,
					VPAND,
					VPANDN,
					VPSUBB,
					VPMINUB,
					VPMINUD,
					VPMOVMSKB,
					VPCMPEQB,
					VPCMPEQD,
					VPCMPEQQ,
					VPCMPNEQB,
					VPCMPNEQUB,
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
					VBROADCASTSS,
					VPTERNLOGD,
					VPTESTMB,
					KORTESTD,
					KORD,
					KUNPCKBW -> (byte) 0b01;
			case VMOVDQU, VMOVDQU64, SARX -> (byte) 0b10;
			case VMOVDQU8, KMOVQ, KMOVD -> (byte) 0b11;
			case VMOVQ ->
				(inst.firstOperand() instanceof final RegisterXMM r
								&& !Registers.requiresEvexExtension(r)
								&& isSecondM(inst))
						? (byte) 0b10
						: (byte) 0b01;
			default -> (byte) 0b00;
		};
	}

	private static void encodeVex2Prefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		wb.write((byte) 0xc5);

		byte v = 0;
		if (inst.getNumOperands() == 3
				&& (inst.opcode() == Opcode.VPSRLDQ || inst.opcode() == Opcode.VPSLLDQ)
				&& inst.firstOperand() instanceof final Register r) {
			v = Registers.combine(Registers.requiresExtension(r), Registers.toByte(r));
		} else if (inst.getNumOperands() == 3 && inst.secondOperand() instanceof final Register r) {
			v = Registers.combine(Registers.requiresExtension(r), Registers.toByte(r));
		}

		encodeVex2Byte(
				wb,
				!(isFirstER(inst) || (inst.hasFirstOperand() && hasExtendedBase(inst.firstOperand()))),
				v,
				isFirstYMM(inst)
						|| isSecondYMM(inst)
						|| inst.opcode() == Opcode.VZEROALL
						|| inst.opcode() == Opcode.KUNPCKBW,
				getImpliedPrefix(inst));
	}

	private static void encodeVex2Byte(
			final WriteOnlyByteBuffer wb, final boolean r, final byte v, final boolean l, final byte p) {
		wb.write(or(r ? (byte) 0b10000000 : 0, shl(and(not(v), (byte) 0b00001111), 3), l ? (byte) 0b00000100 : 0, p));
	}

	private static void encodeVex3Prefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		wb.write((byte) 0xc4);

		final boolean hasExtendedThirdRegister = isThirdER(inst);

		encodeVex3FirstByte(
				wb,
				!((inst.getNumOperands() == 2
								&& ((isFirstER(inst) && isSecondR(inst)) || (isFirstM(inst) && isSecondER(inst))))
						|| (inst.getNumOperands() == 3 && isFirstER(inst))),
				!((inst.getNumOperands() == 2
								&& ((hasExtendedFirstIndex(inst) && isSecondR(inst))
										|| (isFirstR(inst) && hasExtendedIndex(inst.secondOperand()))))
						|| (inst.getNumOperands() == 3 && hasExtendedIndex(inst.thirdOperand()))),
				!((inst.getNumOperands() == 2
								&& ((isFirstR(inst) && hasExtendedBase(inst.secondOperand()))
										|| (hasExtendedBase(inst.firstOperand()) && isSecondR(inst))
										|| (isFirstR(inst) && isSecondER(inst))))
						|| (inst.getNumOperands() == 3 && (hasExtendedThirdRegister))),
				getVex3OpcodeMap(inst.opcode()));

		encodeVex3SecondByte(
				wb,
				isFirstR64(inst) || isSecondR64(inst) || isFirstMask(inst),
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
				isFirstYMM(inst)
						|| isSecondYMM(inst)
						|| (inst.hasThirdOperand() && inst.thirdOperand() instanceof MaskRegister),
				getImpliedPrefix(inst));
	}

	private static boolean isSecondYMM(final Instruction inst) {
		return inst.hasSecondOperand() && inst.secondOperand() instanceof RegisterYMM;
	}

	private static boolean isFirstYMM(final Instruction inst) {
		return inst.hasFirstOperand() && inst.firstOperand() instanceof RegisterYMM;
	}

	private static boolean isSecondR64(final Instruction inst) {
		return inst.hasSecondOperand() && inst.secondOperand() instanceof Register64;
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
			case VMOVDQU64, VMOVQ, VPXORQ, VPORQ -> true;
			default -> false;
		};
	}

	private static byte getEvexOpcodeMap(final Opcode opcode) {
		return switch (opcode) {
			case VMOVUPS, VMOVAPS, VMOVDQU8, VMOVDQU64, VMOVNTDQ, VMOVQ, VPXORQ, VPORQ, VPMINUB -> (byte) 0b001;
			case VBROADCASTSS, VPBROADCASTB, VPBROADCASTD, VPTESTMB, VPMINUD -> (byte) 0b010;
			case VPCMPNEQUB, VPCMPEQB, VPCMPEQD, VPCMPNEQB, VPTERNLOGD -> (byte) 0b011;
			default -> (byte) 0b000;
		};
	}

	private static void encodeEvexPrefix(final WriteOnlyByteBuffer wb, final Instruction inst) {
		wb.write((byte) 0x62);

		encodeEvexFirstByte(
				wb,
				(inst.getNumOperands() == 3 && isFirstER(inst)) || (inst.getNumOperands() == 2 && isSecondER(inst)),
				isThirdEER(inst),
				false,
				(inst.getNumOperands() == 2 && isFirstEER(inst) && (isSecondR(inst) || isSecondM(inst)))
						|| (inst.getNumOperands() == 2 && (isFirstR(inst) || isFirstM(inst)) && isSecondEER(inst))
						|| (inst.getNumOperands() == 3
								&& isFirstEER(inst)
								&& isSecondR(inst)
								&& (isThirdR(inst) || isThirdM(inst)))
						|| (inst.getNumOperands() == 4
								&& isFirstEER(inst)
								&& isSecondR(inst)
								&& (isThirdR(inst) || isThirdM(inst))
								&& inst.fourthOperand() instanceof Immediate),
				getEvexOpcodeMap(inst.opcode()));

		// TODO: refactor this chain of if-elses
		Register rvvvv = null;
		if (inst.getNumOperands() == 3
				&& isFirstMask(inst)
				&& inst.secondOperand() instanceof final Register r
				&& isThirdM(inst)) {
			rvvvv = r;
		} else if ((inst.opcode() == Opcode.VPXORQ
						|| inst.opcode() == Opcode.VPMINUB
						|| inst.opcode() == Opcode.VPMINUD)
				&& inst.getNumOperands() == 3
				&& isFirstR(inst)
				&& inst.secondOperand() instanceof final Register r
				&& isThirdM(inst)) {
			rvvvv = r;
		} else if (inst.getNumOperands() == 3
				&& inst.firstOperand() instanceof final Register r
				&& isSecondR(inst)
				&& isThirdM(inst)) {
			rvvvv = r;
		} else if (inst.getNumOperands() == 3
				&& isFirstR(inst)
				&& inst.secondOperand() instanceof final Register r
				&& isThirdR(inst)) {
			rvvvv = r;
		} else if (inst.opcode() == Opcode.VPTERNLOGD
				&& inst.getNumOperands() == 4
				&& isFirstR(inst)
				&& inst.secondOperand() instanceof final Register r
				&& isThirdM(inst)
				&& inst.fourthOperand() instanceof Immediate) {
			rvvvv = r;
		} else if (inst.getNumOperands() == 4
				&& inst.firstOperand() instanceof final Register r
				&& isSecondR(inst)
				&& isThirdM(inst)
				&& inst.fourthOperand() instanceof Immediate) {
			rvvvv = r;
		} else if (inst.getNumOperands() == 4
				&& isFirstR(inst)
				&& inst.secondOperand() instanceof final Register r
				&& isThirdR(inst)
				&& inst.fourthOperand() instanceof Immediate) {
			rvvvv = r;
		}
		encodeEvexSecondByte(
				wb,
				is64Bits(inst),
				rvvvv == null
						? (byte) 0
						: or(Registers.toByte(rvvvv), Registers.requiresExtension(rvvvv) ? (byte) 0b00001000 : 0),
				getImpliedPrefix(inst));

		encodeEvexThirdByte(
				wb,
				inst.hasDestinationMask() && inst.hasZeroDestinationMask(),
				isFirstZ(inst) || isSecondZ(inst),
				isFirstYMM(inst) || isSecondYMM(inst),
				false,
				rvvvv != null && Registers.requiresEvexExtension(rvvvv),
				inst.hasDestinationMask() ? MaskRegister.toByte(inst.getDestinationMask()) : (byte) 0);
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

	private static void encodeEvexThirdByte(
			final WriteOnlyByteBuffer wb,
			final boolean z,
			final boolean l1,
			final boolean l,
			final boolean b1,
			final boolean v1,
			final byte a) {
		wb.write(or(
				z ? (byte) 0b10000000 : 0,
				l1 ? (byte) 0b01000000 : 0,
				l ? (byte) 0b00100000 : 0,
				b1 ? (byte) 0b00010000 : 0,
				v1 ? 0 : (byte) 0b00001000,
				a));
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
		// no SIB needed for "simple" indirect operands
		if (!isSimpleIndirectOperand(io)) {
			final byte base = io.hasBase() ? Registers.toByte(io.getBase()) : (byte) 0b101;
			encodeSIB(wb, getScale(io), Registers.toByte(io.getIndex()), base);
		}

		if (io.hasDisplacement()) {
			encodeDisplacement(wb, io);
		}
	}

	private static void encodeDisplacement(final WriteOnlyByteBuffer wb, final IndirectOperand io) {
		switch (io.getDisplacementType()) {
			case DisplacementType.SHORT -> wb.write(asByte(io.getDisplacement()));
			case DisplacementType.LONG -> wb.write(asInt(io.getDisplacement()));
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
