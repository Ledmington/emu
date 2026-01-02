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
package com.ledmington.cpu;

import static com.ledmington.utils.BitUtils.and;
import static com.ledmington.utils.BitUtils.asByte;
import static com.ledmington.utils.BitUtils.asInt;
import static com.ledmington.utils.BitUtils.asShort;
import static com.ledmington.utils.BitUtils.not;
import static com.ledmington.utils.BitUtils.or;
import static com.ledmington.utils.BitUtils.shr;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ledmington.cpu.x86.EvexPrefix;
import com.ledmington.cpu.x86.GeneralInstruction;
import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.IndirectOperandBuilder;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionBuilder;
import com.ledmington.cpu.x86.InstructionPrefix;
import com.ledmington.cpu.x86.MaskRegister;
import com.ledmington.cpu.x86.ModRM;
import com.ledmington.cpu.x86.NullRegister;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Operand;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Prefixes;
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
import com.ledmington.cpu.x86.RexPrefix;
import com.ledmington.cpu.x86.SIB;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.cpu.x86.SegmentedAddress;
import com.ledmington.cpu.x86.Vex2Prefix;
import com.ledmington.cpu.x86.Vex3Prefix;
import com.ledmington.cpu.x86.exc.DecodingException;
import com.ledmington.cpu.x86.exc.InvalidLegacyOpcode;
import com.ledmington.cpu.x86.exc.ReservedOpcode;
import com.ledmington.cpu.x86.exc.UnknownOpcode;
import com.ledmington.cpu.x86.exc.UnrecognizedPrefix;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

/**
 * Reference Intel® 64 and IA-32 Architectures Software Developer's Manual volume 2. Legacy prefixes: Paragraph 2.1.1.
 * Instruction opcodes: Appendix A. (pag. 2839)
 */
// FIXME: reduce these suppressions
@SuppressWarnings({
	"PMD.AvoidLiteralsInIfCondition",
	"PMD.NPathComplexity",
	"PMD.CyclomaticComplexity",
	"PMD.UselessParentheses",
	"PMD.TooManyMethods",
	"PMD.AvoidDuplicateLiterals",
	"PMD.CouplingBetweenObjects",
	"PMD.TooManyStaticImports",
	"PMD.NcssCount",
	"PMD.CognitiveComplexity",
	"PMD.TooFewBranchesForSwitch",
	"PMD.NullAssignment"
})
public final class InstructionDecoder {

	private static final MiniLogger logger = MiniLogger.getLogger("x86-asm");

	private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
	private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
	private static final byte MODRM_MOD_NO_DISP = (byte) 0b11;
	private static final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
	private static final Map<String, Opcode> fromStringToOpcode =
			Arrays.stream(Opcode.values()).collect(Collectors.toUnmodifiableMap(Opcode::mnemonic, x -> x));
	private static final Map<String, Register> fromStringToRegister = Stream.of(
					Arrays.stream(Register8.values()),
					Arrays.stream(Register16.values()),
					Arrays.stream(Register32.values()),
					Arrays.stream(Register64.values()),
					Arrays.stream(RegisterMMX.values()),
					Arrays.stream(RegisterXMM.values()),
					Arrays.stream(RegisterYMM.values()),
					Arrays.stream(RegisterZMM.values()),
					Arrays.stream(MaskRegister.values()),
					Arrays.stream(SegmentRegister.values()))
			.flatMap(x -> x)
			.collect(Collectors.toUnmodifiableMap(Operand::toIntelSyntax, x -> x));
	private static final Map<String, SegmentRegister> fromStringToSegment = Arrays.stream(SegmentRegister.values())
			.collect(Collectors.toUnmodifiableMap(Operand::toIntelSyntax, x -> x));
	private static final Map<String, MaskRegister> fromStringToMask =
			Arrays.stream(MaskRegister.values()).collect(Collectors.toUnmodifiableMap(Operand::toIntelSyntax, x -> x));

	private InstructionDecoder() {}

	/**
	 * Decodes an x86_64 instruction from its representation in Intel's syntax.
	 *
	 * <p>Note: Intel's syntax is ambiguous when dealing with displacements.
	 *
	 * @param input The instruction to decode.
	 * @return The decoded instruction.
	 */
	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	public static Instruction fromIntelSyntax(final String input) {
		final CharacterIterator it = new StringCharacterIterator(input);

		final InstructionBuilder ib = Instruction.builder();

		String opcodeString = readUntilWhitespace(it);
		if ("lock".equals(opcodeString) || "rep".equals(opcodeString) || "repnz".equals(opcodeString)) {
			final String finalOpcodeString = opcodeString;
			ib.prefix(Arrays.stream(InstructionPrefix.values())
					.filter(p -> p.name().toLowerCase(Locale.US).equals(finalOpcodeString))
					.findAny()
					.orElseThrow());
			skipWhitespaces(it);
			opcodeString = readUntilWhitespace(it);
		}

		if ("bnd".equals(opcodeString)) {
			final int pos = it.getIndex();
			skipWhitespaces(it);
			final String tmp = readUntilWhitespace(it);
			if ("jmp".equals(tmp)) {
				opcodeString = "bnd jmp";
			} else {
				it.setIndex(pos);
			}
		}

		if (!fromStringToOpcode.containsKey(opcodeString)) {
			throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", opcodeString));
		}
		final Opcode opcode = fromStringToOpcode.get(opcodeString);
		ib.opcode(opcode);

		skipWhitespaces(it);

		if (it.current() == CharacterIterator.DONE) {
			return ib.build();
		}

		final String[] args = input.substring(it.getIndex()).split(",");
		if (args.length > 4) {
			throw new IllegalArgumentException(String.format("Too many operands: '%s'.", input));
		}

		args[0] = args[0].strip();
		if (args[0].endsWith("}")) {
			final String[] splitted = args[0].split("\\{");
			args[0] = splitted[0];
			final String maskString = splitted[1].split("}")[0].strip();
			if (fromStringToMask.containsKey(maskString)) {
				ib.mask(fromStringToMask.get(maskString));
			} else {
				throw new IllegalArgumentException(String.format("Unknown destination mask: '%s'.", maskString));
			}
			if (splitted.length >= 3) {
				if ("z}".equals(splitted[2])) {
					ib.maskZero();
				} else {
					throw new IllegalArgumentException(String.format("Unknown destination mask: '%s'.", maskString));
				}
			}
		}

		final Operand firstOperand = parseOperand(args[0].strip(), null, Optional.empty());
		ib.op(firstOperand);

		if (args.length >= 2) {
			final Operand secondOperand = parseOperand(args[1].strip(), firstOperand, Optional.empty());
			ib.op(secondOperand);

			if (args.length >= 3) {
				final Optional<Integer> compressedDisplacement =
						((opcode == Opcode.VPTERNLOGD || opcode == Opcode.VPMINUB || opcode == Opcode.VPMINUD)
										&& firstOperand instanceof Register r1
										&& Registers.requiresEvexExtension(r1)
										&& secondOperand instanceof Register r2
										&& Registers.requiresEvexExtension(r2))
								? Optional.of(32)
								: Optional.empty();

				ib.op(parseOperand(args[2].strip(), null, compressedDisplacement));

				if (args.length == 4) {
					ib.op(parseOperand(args[3].strip(), null, Optional.empty()));
				}
			}
		}

		return ib.build();
	}

	private static void skipWhitespaces(final CharacterIterator it) {
		while (it.current() != CharacterIterator.DONE && it.current() == ' ') {
			it.next();
		}
	}

	private static String readUntilWhitespace(final CharacterIterator it) {
		return readUntil(it, ' ');
	}

	private static String readUntil(final CharacterIterator it, final char ch) {
		final StringBuilder sb = new StringBuilder();
		for (; it.current() != CharacterIterator.DONE && it.current() != ch; it.next()) {
			sb.append(it.current());
		}
		return sb.toString();
	}

	private static Immediate parseImmediate(final String imm) {
		if (imm.length() <= 2) {
			return new Immediate(asByte(Integer.parseUnsignedInt(imm, 16)));
		}
		if (imm.length() <= 4) {
			return new Immediate(asShort(Integer.parseUnsignedInt(imm, 16)));
		}
		if (imm.length() <= 8) {
			return new Immediate(Integer.parseUnsignedInt(imm, 16));
		}
		if (imm.length() <= 16) {
			return new Immediate(Long.parseUnsignedLong(imm, 16));
		}
		throw new IllegalArgumentException(String.format("Immediate too long: '%s'.", imm));
	}

	private static Operand parseOperand(
			final String input, final Operand previousOperand, final Optional<Integer> compressedDisplacement) {
		if (fromStringToRegister.containsKey(input)) {
			// It's a register
			return fromStringToRegister.get(input);
		}
		if (input.startsWith("ds:")) {
			return new SegmentedAddress(SegmentRegister.DS, parseImmediate(input.split("0x")[1]));
		}
		if (input.startsWith("0x")) {
			return parseImmediate(input.substring(2));
		}

		// It's an indirect operand
		final IndirectOperandBuilder iob = IndirectOperand.builder();
		final CharacterIterator it = new StringCharacterIterator(input);
		String pointer;
		pointer = readUntilWhitespace(it);
		skipWhitespaces(it);
		pointer = pointer + ' ' + readUntilWhitespace(it);

		switch (pointer) {
			case "BYTE PTR" -> iob.pointer(PointerSize.BYTE_PTR);
			case "WORD PTR" -> iob.pointer(PointerSize.WORD_PTR);
			case "DWORD PTR" -> iob.pointer(PointerSize.DWORD_PTR);
			case "QWORD PTR" -> iob.pointer(PointerSize.QWORD_PTR);
			case "XMMWORD PTR" -> iob.pointer(PointerSize.XMMWORD_PTR);
			case "YMMWORD PTR" -> iob.pointer(PointerSize.YMMWORD_PTR);
			case "ZMMWORD PTR" -> iob.pointer(PointerSize.ZMMWORD_PTR);
			default -> {
				// This might be a LEA-like instruction, meaning that the displacement has no explicit pointer size in
				// front
				// of it, so we just rewind the iterator.
				iob.pointer(
						previousOperand == null ? PointerSize.QWORD_PTR : PointerSize.fromSize(previousOperand.bits()));
				it.setIndex(0);
			}
		}

		skipWhitespaces(it);

		// Must begin with '['
		if (it.current() != '[') {
			// try reading segment register
			final String seg = readUntil(it, ':').strip();
			if (fromStringToSegment.containsKey(seg)) {
				final SegmentRegister segReg = fromStringToSegment.get(seg);
				iob.segment(segReg);
				skipWhitespaces(it);
				it.next();
			} else {
				throw new IllegalArgumentException(String.format("Invalid indirect operand: '%s'.", input));
			}
		}
		it.next();

		// Read until ']'
		String indirectOperandString = readUntil(it, ']').strip();

		// check that there is nothing else after the ']'
		it.next();
		skipWhitespaces(it);
		if (it.current() != CharacterIterator.DONE) {
			throw new IllegalArgumentException(String.format("Invalid indirect operand: '%s'.", input));
		}

		String baseString = null;
		String indexString = null;
		String scaleString = null;
		String displacementString = null;

		if (indirectOperandString.contains("0x")) {
			final int idx = indirectOperandString.indexOf("0x");
			// add back the sign of the displacement
			displacementString = (indirectOperandString.charAt(idx - 1))
					+ indirectOperandString.substring(idx + 2).strip();
			indirectOperandString = indirectOperandString.substring(0, idx - 1).strip();
		}
		if (indirectOperandString.contains("*")) {
			final int idx = indirectOperandString.indexOf('*');
			scaleString = indirectOperandString.substring(idx + 1).strip();
			indirectOperandString = indirectOperandString.substring(0, idx).strip();
		}
		if (indirectOperandString.contains("+")) {
			final int idx = indirectOperandString.indexOf('+');
			indexString = indirectOperandString.substring(idx + 1).strip();
			indirectOperandString = indirectOperandString.substring(0, idx).strip();
		} else if (scaleString != null) {
			indexString = indirectOperandString;
			indirectOperandString = "";
		}
		if (!indirectOperandString.isBlank()) {
			baseString = indirectOperandString.strip();
		}

		if (baseString != null) {
			final Register reg = fromStringToRegister.get(baseString);
			iob.base(reg);
		}
		if (indexString != null) {
			iob.index(fromStringToRegister.get(indexString));
		}
		if (scaleString != null) {
			iob.scale(
					switch (scaleString) {
						case "1" -> 1;
						case "2" -> 2;
						case "4" -> 4;
						case "8" -> 8;
						default ->
							throw new IllegalArgumentException(
									String.format("Invalid scale in indirect operand: '%s'.", scaleString));
					});
		}
		if (displacementString != null) {
			// save the sign for later
			final boolean isNegative = displacementString.charAt(0) == '-';
			// remove the sign
			if (displacementString.charAt(0) == '+' || displacementString.charAt(0) == '-') {
				displacementString = displacementString.substring(1);
			}
			final char sign = isNegative ? '-' : '+';
			final int disp32 = Integer.parseInt(sign + displacementString, 16);
			if (displacementString.length() == 8) {
				iob.displacement(disp32);
			} else {
				int disp8 = disp32;
				if (compressedDisplacement.isPresent()) {
					disp8 /= compressedDisplacement.orElseThrow();
				}
				iob.displacement(asByte(disp8));
			}
		}

		return iob.build();
	}

	/**
	 * Decodes the given bytes into a list of instructions, if possible.
	 *
	 * @param bytes The bytes to be decoded.
	 * @param nBytesToDecode The number of bytes to decode.
	 * @param checkInstructions When enabled, checks the instructions for validity as soon as they are decoded.
	 * @return The list of decoded instructions.
	 */
	public static List<Instruction> fromHex(
			final byte[] bytes, final int nBytesToDecode, final boolean checkInstructions) {
		return fromHex(new ReadOnlyByteBufferV1(bytes, true, 1), nBytesToDecode, checkInstructions);
	}

	/**
	 * Decodes the provided bytes into a list of instructions, if possible.
	 *
	 * @param b The byte buffer to read bytes from.
	 * @param nBytesToDecode The number of bytes to decode.
	 * @param checkInstructions When enabled, checks the instructions for validity as soon as they are decoded.
	 * @return The list of decoded instructions.
	 */
	public static List<Instruction> fromHex(
			final ReadOnlyByteBuffer b, final int nBytesToDecode, final boolean checkInstructions) {
		if (nBytesToDecode < 0) {
			throw new IllegalArgumentException(String.format("Negative bytes: %,d.", nBytesToDecode));
		}

		logger.info("The code is %,d bytes long.", nBytesToDecode);

		final List<Instruction> instructions = new ArrayList<>();
		while (b.getPosition() < nBytesToDecode) {
			final long pos = b.getPosition();
			final Instruction inst = fromHex(b);
			{ // Debugging info
				final long codeLen = b.getPosition() - pos;
				b.setPosition(pos);
				logger.debug(
						"%08x: %-24s %s",
						pos,
						IntStream.range(0, (int) codeLen)
								.mapToObj(i -> String.format("%02x", b.read1()))
								.collect(Collectors.joining(" ")),
						InstructionEncoder.toIntelSyntax(inst, checkInstructions, 0, false));
			}
			if (checkInstructions) {
				InstructionChecker.check(inst);
			}
			instructions.add(inst);
		}

		return instructions;
	}

	/**
	 * Decodes a single instruction from the given byte buffer.
	 *
	 * @param b The buffer to read bytes from.
	 * @return The decoded instruction.
	 */
	public static Instruction fromHex(final ReadOnlyByteBuffer b) {
		Objects.requireNonNull(b);
		final Prefixes pref = parsePrefixes(b);

		final byte opcodeFirstByte = b.read1();

		if (pref.vex2().isPresent()) {
			return parseVex2Opcodes(b, opcodeFirstByte, pref);
		}

		if (pref.vex3().isPresent()) {
			return parseVex3Opcodes(b, opcodeFirstByte, pref);
		}

		if (pref.evex().isPresent()) {
			return parseEvexOpcodes(b, opcodeFirstByte, pref);
		}

		return switch (opcodeFirstByte) {
			case (byte) 0x0f -> parse2BytesOpcode(b, opcodeFirstByte, pref);

			case (byte) 0x80, (byte) 0x81, (byte) 0x83 -> parseExtendedOpcodeGroup1(b, opcodeFirstByte, pref);

			case (byte) 0xc0, (byte) 0xc1, (byte) 0xd0, (byte) 0xd1, (byte) 0xd2, (byte) 0xd3 ->
				parseExtendedOpcodeGroup2(b, opcodeFirstByte, pref);

			case (byte) 0xc6, (byte) 0xc7 -> parseExtendedOpcodeGroup11(b, opcodeFirstByte, pref);

			case (byte) 0xf6, (byte) 0xf7 -> parseExtendedOpcodeGroup3(b, opcodeFirstByte, pref);

			case (byte) 0xfe -> parseExtendedOpcodeGroup4(b, opcodeFirstByte, pref);

			case (byte) 0xff -> parseExtendedOpcodeGroup5(b, opcodeFirstByte, pref);

			default -> parseSingleByteOpcode(b, opcodeFirstByte, pref);
		};
	}

	private static boolean isIndirectOperandNeeded(final ModRM modrm) {
		return modrm.mod() != MODRM_MOD_NO_DISP;
	}

	private static Instruction parseExtendedOpcodeGroup4(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);
		final Operand arg = isIndirectOperandNeeded(modrm)
				? parseIndirectOperand(b, pref, modrm)
						.pointer(pref.hasOperandSizeOverridePrefix() ? PointerSize.WORD_PTR : PointerSize.BYTE_PTR)
						.build()
				: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix());

		return switch (modrm.reg()) {
			case 0 -> Instruction.builder().opcode(Opcode.INC).op(arg).build();
			case 1 -> Instruction.builder().opcode(Opcode.DEC).op(arg).build();
			case 2, 3, 4, 5, 6, 7 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup5(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		return switch (modrm.reg()) {
			case 0b00000000 ->
				Instruction.builder()
						.opcode(Opcode.INC)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(getPointerSize(pref))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b00000001 ->
				Instruction.builder()
						.opcode(Opcode.DEC)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(getPointerSize(pref))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b00000010 -> {
				// near CALL
				final Register reg = Registers.fromCode(
						modrm.rm(),
						!pref.hasAddressSizeOverridePrefix(),
						pref.rex().hasModRMRMExtension(),
						false);
				yield isIndirectOperandNeeded(modrm)
						? Instruction.builder()
								.opcode(Opcode.CALL)
								.op(parseIndirectOperand(b, pref, modrm)
										.pointer(
												pref.hasOperandSizeOverridePrefix()
														? PointerSize.WORD_PTR
														: pref.hasAddressSizeOverridePrefix()
																? PointerSize.QWORD_PTR
																: PointerSize.fromSize(reg.bits()))
										.build())
								.build()
						: Instruction.builder().opcode(Opcode.CALL).op(reg).build();
			}
			case 0b00000011 -> // far CALL
				Instruction.builder()
						.opcode(Opcode.CALL)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(
										pref.hasOperandSizeOverridePrefix()
												? PointerSize.DWORD_PTR
												: PointerSize.QWORD_PTR)
								.build())
						.build();
			case 0b00000100 -> {
				final InstructionBuilder ib = Instruction.builder()
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														pref.hasOperandSizeOverridePrefix()
																? PointerSize.WORD_PTR
																: PointerSize.QWORD_PTR)
												.build()
										: Registers.fromCode(
												modrm.rm(),
												!pref.hasAddressSizeOverridePrefix(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()));
				if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REPNZ) {
					ib.opcode(Opcode.BND_JMP);
				} else {
					ib.opcode(Opcode.JMP);
				}
				yield ib.build();
			}
			case 0b00000101 ->
				Instruction.builder()
						.opcode(Opcode.JMP)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.DWORD_PTR)
												.build()
										: Registers.fromCode(
												modrm.rm(),
												!pref.hasAddressSizeOverridePrefix(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b00000110 ->
				Instruction.builder()
						.opcode(Opcode.PUSH)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			case 0b00000111 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static PointerSize getPointerSize(final Prefixes pref) {
		return pref.hasOperandSizeOverridePrefix()
				? PointerSize.WORD_PTR
				: pref.rex().isOperand64Bit() ? PointerSize.QWORD_PTR : PointerSize.DWORD_PTR;
	}

	private static Instruction parseExtendedOpcodeGroup3(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isRegister8Bit = opcodeFirstByte == (byte) 0xf6;

		final boolean isIndirectOperandNeeded = isIndirectOperandNeeded(modrm);

		return switch (modrm.reg()) {
			case 0b000 -> {
				if (isIndirectOperandNeeded) {
					final IndirectOperandBuilder iob = parseIndirectOperand(b, pref, modrm);
					final Immediate imm =
							isRegister8Bit ? imm8(b) : pref.hasOperandSizeOverridePrefix() ? imm16(b) : imm32(b);
					yield Instruction.builder()
							.opcode(Opcode.TEST)
							.op(iob.pointer(PointerSize.fromSize(imm.bits())).build())
							.op(imm)
							.build();
				} else {
					yield Instruction.builder()
							.opcode(Opcode.TEST)
							.op(
									isRegister8Bit
											? Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix())
											: Registers.fromCode(
													modrm.rm(),
													pref.rex().isOperand64Bit(),
													pref.rex().hasModRMRMExtension(),
													pref.hasOperandSizeOverridePrefix()))
							.op(isRegister8Bit ? imm8(b) : pref.hasOperandSizeOverridePrefix() ? imm16(b) : imm32(b))
							.build();
				}
			}
			case 0b010 ->
				Instruction.builder()
						.opcode(Opcode.NOT)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b011 -> {
				final Register r = Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield Instruction.builder()
						.opcode(Opcode.NEG)
						.op(
								isIndirectOperandNeeded
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														pref.rex().isOperand64Bit()
																? PointerSize.QWORD_PTR
																: PointerSize.DWORD_PTR)
												.build()
										: r)
						.build();
			}
			case 0b100 ->
				Instruction.builder()
						.opcode(Opcode.MUL)
						.op(
								isIndirectOperandNeeded
										? parseIndirectOperand(b, pref, modrm)
												.pointer(isRegister8Bit ? PointerSize.BYTE_PTR : getPointerSize(pref))
												.build()
										: isRegister8Bit
												?
												// R8
												Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix())
												: Registers.fromCode(
														modrm.rm(),
														pref.rex().isOperand64Bit(),
														pref.rex().hasModRMRMExtension(),
														pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b101 ->
				Instruction.builder()
						.opcode(Opcode.IMUL)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b110 ->
				Instruction.builder()
						.opcode(Opcode.DIV)
						.op(
								isIndirectOperandNeeded
										? parseIndirectOperand(b, pref, modrm)
												.pointer(isRegister8Bit ? PointerSize.BYTE_PTR : getPointerSize(pref))
												.build()
										: isRegister8Bit
												?
												// R8
												Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix())
												: Registers.fromCode(
														modrm.rm(),
														pref.rex().isOperand64Bit(),
														pref.rex().hasModRMRMExtension(),
														pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b111 ->
				Instruction.builder()
						.opcode(Opcode.IDIV)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			case 0b001 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup11(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();
		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean is8Bit = opcodeFirstByte == (byte) 0xc6;
		final int immediateBits = pref.hasOperandSizeOverridePrefix()
				? 16
				: (is8Bit ? 8 : (pref.rex().isOperand64Bit() ? 64 : 32));

		if (!isIndirectOperandNeeded(modrm) && modrm.reg() == (byte) 0b111 && modrm.rm() == (byte) 0b000) {
			return new GeneralInstruction(Opcode.XBEGIN, imm32(b));
		}

		if (modrm.reg() == (byte) 0b000) {
			return new GeneralInstruction(
					Opcode.MOV,
					isIndirectOperandNeeded(modrm)
							? parseIndirectOperand(b, pref, modrm)
									.pointer(is8Bit ? PointerSize.BYTE_PTR : PointerSize.fromSize(immediateBits))
									.build()
							: (is8Bit
									? Register8.fromByte(modrm.rm(), pref.hasRexPrefix())
									: Registers.fromCode(
											modrm.rm(),
											pref.rex().isOperand64Bit(),
											pref.rex().hasModRMRMExtension(),
											pref.hasOperandSizeOverridePrefix())),
					switch (immediateBits) {
						case 8 -> imm8(b);
						case 16 -> imm16(b);
						case 32, 64 -> imm32(b);
						default ->
							throw new AssertionError(String.format("Unknown immediate bits: %,d.", immediateBits));
					});
		}

		throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
	}

	private static Instruction parseExtendedOpcodeGroup2(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isImmediate8Bit = opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xc1;
		final boolean isImmediate1 = opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd1;
		final boolean isReg8Bit =
				opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd2;

		final Operand op1 = isReg8Bit
				? Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix())
				: (isIndirectOperandNeeded(modrm)
						? parseIndirectOperand(b, pref, modrm)
								.pointer(pref.rex().isOperand64Bit() ? PointerSize.QWORD_PTR : PointerSize.DWORD_PTR)
								.build()
						: Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
		final Operand op2 = isImmediate8Bit ? imm8(b) : isImmediate1 ? new Immediate((byte) 1) : Register8.CL;

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b000 -> Opcode.ROL;
					case 0b001 -> Opcode.ROR;
					case 0b010 -> Opcode.RCL;
					case 0b011 -> Opcode.RCR;
					case 0b100 -> Opcode.SHL;
					case 0b101 -> Opcode.SHR;
					case 0b111 -> Opcode.SAR;
					case 0b110 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return Instruction.builder().opcode(opcode).op(op1).op(op2).build();
	}

	private static Instruction parseExtendedOpcodeGroup1(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isRegister8Bit = opcodeFirstByte == (byte) 0x80;
		final int immediateBits =
				(opcodeFirstByte == (byte) 0x81) ? (pref.hasOperandSizeOverridePrefix() ? 16 : 32) : 8;
		final boolean isIndirectOperandNeeded = modrm.mod() != 0b11;
		final byte regByte = getByteFromRM(pref, modrm);

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0 -> Opcode.ADD;
					case 1 -> Opcode.OR;
					case 2 -> Opcode.ADC;
					case 3 -> Opcode.SBB;
					case 4 -> Opcode.AND;
					case 5 -> Opcode.SUB;
					case 6 -> Opcode.XOR;
					case 7 -> Opcode.CMP;
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		if (isRegister8Bit) {
			// OP R8, imm8
			return Instruction.builder()
					.opcode(opcode)
					.op(
							isIndirectOperandNeeded
									? parseIndirectOperand(b, pref, modrm)
											.pointer(
													pref.rex().isOperand64Bit()
															? PointerSize.QWORD_PTR
															: PointerSize.fromSize(immediateBits))
											.build()
									: Register8.fromByte(regByte, pref.hasRexPrefix()))
					.op(imm8(b))
					.build();
		} else {
			final Operand r = isIndirectOperandNeeded
					? parseIndirectOperand(b, pref, modrm)
							.pointer(
									pref.hasOperandSizeOverridePrefix()
											? PointerSize.WORD_PTR
											: pref.rex().isOperand64Bit()
													? PointerSize.QWORD_PTR
													: PointerSize.DWORD_PTR)
							.build()
					: Registers.fromCode(
							modrm.rm(),
							pref.rex().isOperand64Bit(),
							pref.rex().hasModRMRMExtension(),
							pref.hasOperandSizeOverridePrefix());
			final int operandBits = r.bits();

			final Immediate imm =
					switch (immediateBits) {
						case 8 ->
							switch (operandBits) {
								case 8, 16, 32, 64 -> imm8(b);
								default ->
									throw new IllegalArgumentException(String.format(
											"Immediate bits were %,d and operand bits were %,d.",
											immediateBits, operandBits));
							};
						case 16 ->
							switch (operandBits) {
								case 16 -> imm16(b);
								case 32 -> new Immediate((int) b.read2LE());
								case 64 -> new Immediate((long) b.read2LE());
								default ->
									throw new IllegalArgumentException(String.format(
											"Immediate bits were %,d and operand bits were %,d.",
											immediateBits, operandBits));
							};
						default ->
							switch (operandBits) {
								case 32, 64 -> imm32(b);
								default ->
									throw new IllegalArgumentException(String.format(
											"Immediate bits were %,d and operand bits were %,d.",
											immediateBits, operandBits));
							};
					};
			return Instruction.builder().opcode(opcode).op(r).op(imm).build();
		}
	}

	private static Instruction parseExtendedOpcodeGroup7(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			notImplemented();
		}

		if (modrm.reg() != 0b010) {
			throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		return switch (modrm.rm()) {
			case (byte) 0b000 -> Instruction.builder().opcode(Opcode.XGETBV).build();
			case (byte) 0b001 -> Instruction.builder().opcode(Opcode.XSETBV).build();
			case (byte) 0b100 -> Instruction.builder().opcode(Opcode.VMFUNC).build();
			case (byte) 0b101 -> Instruction.builder().opcode(Opcode.XEND).build();
			case (byte) 0b110 -> Instruction.builder().opcode(Opcode.XTEST).build();
			case (byte) 0b111 -> Instruction.builder().opcode(Opcode.ENCLU).build();
			default -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup16(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		if (!isIndirectOperandNeeded(modrm)) {
			throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		final Opcode[] opcodes = {Opcode.PREFETCHNTA, Opcode.PREFETCHT0, Opcode.PREFETCHT1, Opcode.PREFETCHT2};

		return switch (modrm.reg()) {
			case 0, 1, 2, 3 ->
				Instruction.builder()
						.opcode(opcodes[and(modrm.reg(), (byte) 0b00000011)])
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build())
						.build();
			case 4, 5, 6, 7 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup8(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b100 -> Opcode.BT;
					case 0b101 -> Opcode.BTS;
					case 0b110 -> Opcode.BTR;
					case 0b111 -> Opcode.BTC;
					case 0b000, 0b001, 0b010, 0b011 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return Instruction.builder()
				.opcode(opcode)
				.op(Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix()))
				.op(new Immediate(b.read1()))
				.build();
	}

	private static Instruction parseExtendedOpcodeGroup12(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b010 -> Opcode.PSRLW;
					case 0b100 -> Opcode.PSRAW;
					case 0b110 -> Opcode.PSLLW;
					case 0b000, 0b001, 0b011, 0b101, 0b111 ->
						throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return Instruction.builder().opcode(opcode).build();
	}

	private static Instruction parseExtendedOpcodeGroup13(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b010 -> Opcode.PSRLD;
					case 0b100 -> Opcode.PSRAD;
					case 0b110 -> Opcode.PSLLD;
					case 0b000, 0b001, 0b011, 0b101, 0b111 ->
						throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return Instruction.builder().opcode(opcode).build();
	}

	private static Instruction parseExtendedOpcodeGroup14(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b010 -> Opcode.PSRLQ;
					case 0b011 -> {
						if (pref.hasOperandSizeOverridePrefix()) {
							yield Opcode.PSRLDQ;
						}
						throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					}
					case 0b110 -> Opcode.PSLLQ;
					case 0b111 -> {
						if (pref.hasOperandSizeOverridePrefix()) {
							yield Opcode.PSLLDQ;
						}
						throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					}
					case 0b000, 0b001, 0b100, 0b101 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return Instruction.builder()
				.opcode(opcode)
				.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
				.op(imm8(b))
				.build();
	}

	private static Operand getXMMArgument(
			final ReadOnlyByteBuffer b, final ModRM modrm, final Prefixes pref, final byte r2Byte) {
		return isIndirectOperandNeeded(modrm)
				? parseIndirectOperand(b, pref, modrm)
						.pointer(PointerSize.XMMWORD_PTR)
						.build()
				: RegisterXMM.fromByte(r2Byte);
	}

	private static Operand getYMMArgument(
			final ReadOnlyByteBuffer b, final ModRM modrm, final Prefixes pref, final byte r2Byte) {
		return isIndirectOperandNeeded(modrm)
				? parseIndirectOperand(b, pref, modrm)
						.pointer(PointerSize.YMMWORD_PTR)
						.build()
				: RegisterYMM.fromByte(r2Byte);
	}

	private static byte getByteFromReg(final Prefixes pref, final ModRM modrm) {
		return Registers.combine(pref.hasRexPrefix() && pref.rex().r(), modrm.reg());
	}

	private static byte getByteFromReg(final RexPrefix rex, final ModRM modrm) {
		return Registers.combine(rex.r(), modrm.reg());
	}

	private static byte getByteFromReg(final Vex2Prefix vex2, final ModRM modrm) {
		return Registers.combine(!vex2.r(), modrm.reg());
	}

	private static byte getByteFromReg(final Vex3Prefix vex3, final ModRM modrm) {
		return Registers.combine(!vex3.r(), modrm.reg());
	}

	private static byte getByteFromReg(final EvexPrefix evex, final ModRM modrm) {
		return Registers.combine(!evex.r(), modrm.reg());
	}

	private static byte getByteFromRM(final Prefixes pref, final ModRM modrm) {
		return Registers.combine(pref.hasRexPrefix() && pref.rex().b(), modrm.rm());
	}

	private static byte getByteFromRM(final Vex3Prefix vex3, final ModRM modrm) {
		return Registers.combine(!vex3.b(), modrm.rm());
	}

	private static byte getByteFromRM(final EvexPrefix evex, final ModRM modrm) {
		return Registers.combine(!evex.b(), modrm.rm());
	}

	private static Instruction parse2BytesOpcode(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte SLDT_OPCODE = (byte) 0x00;
		final byte GROUP7_OPCODE = (byte) 0x01;
		final byte SYSCALL_OPCODE = (byte) 0x05;
		final byte UD2_OPCODE = (byte) 0x0b;
		final byte MOVSD_OPCODE = (byte) 0x10;
		final byte MOVUPS_OPCODE = (byte) 0x11;
		final byte MOVHLPS_OPCODE = (byte) 0x12;
		final byte MOVHPS_OPCODE = (byte) 0x16;
		final byte MOVHPS_MOVHPD_OPCODE = (byte) 0x17;
		final byte GROUP16_OPCODE = (byte) 0x18;
		final byte ENDBR_OPCODE = (byte) 0x1e;
		final byte NOP_OPCODE = (byte) 0x1f;
		final byte MOVAPx_R128_M128_OPCODE = (byte) 0x28;
		final byte MOVAPx_M128_R128_OPCODE = (byte) 0x29;
		final byte CVTSI2SD_OPCODE = (byte) 0x2a;
		final byte MOVNTPS_OPCODE = (byte) 0x2b;
		final byte UCOMISx_OPCODE = (byte) 0x2e;
		final byte TABLE_A4_OPCODE = (byte) 0x38;
		final byte TABLE_A5_OPCODE = (byte) 0x3a;
		final byte CMOVB_OPCODE = (byte) 0x42;
		final byte CMOVAE_OPCODE = (byte) 0x43;
		final byte CMOVE_OPCODE = (byte) 0x44;
		final byte CMOVNE_OPCODE = (byte) 0x45;
		final byte CMOVBE_OPCODE = (byte) 0x46;
		final byte CMOVA_OPCODE = (byte) 0x47;
		final byte CMOVS_OPCODE = (byte) 0x48;
		final byte CMOVNS_OPCODE = (byte) 0x49;
		final byte CMOVL_OPCODE = (byte) 0x4c;
		final byte CMOVGE_OPCODE = (byte) 0x4d;
		final byte CMOVLE_OPCODE = (byte) 0x4e;
		final byte CMOVG_OPCODE = (byte) 0x4f;
		final byte MOVMSKPS_OPCODE = (byte) 0x50;
		final byte ANDPD_OPCODE = (byte) 0x54;
		final byte XORPS_OPCODE = (byte) 0x57;
		final byte ADDSD_OPCODE = (byte) 0x58;
		final byte DIVxx_OPCODE = (byte) 0x5e;
		final byte PUNPCKLBW_OPCODE = (byte) 0x60;
		final byte PUNPCKLWD_OPCODE = (byte) 0x61;
		final byte PUNPCKLDQ_OPCODE = (byte) 0x62;
		final byte PCMPGTB_OPCODE = (byte) 0x64;
		final byte PUNPCKHDQ_OPCODE = (byte) 0x6a;
		final byte PUNPCKLQDQ_OPCODE = (byte) 0x6c;
		final byte PUNPCKHQDQ_OPCODE = (byte) 0x6d;
		final byte MOVQ_MOVD_OPCODE = (byte) 0x6e;
		final byte MOVxQx_OPCODE = (byte) 0x6f;
		final byte PSHUF_OPCODE = (byte) 0x70;
		final byte GROUP12_OPCODE = (byte) 0x71;
		final byte GROUP13_OPCODE = (byte) 0x72;
		final byte GROUP14_OPCODE = (byte) 0x73;
		final byte PCMPEQB_OPCODE = (byte) 0x74;
		final byte PCMPEQW_OPCODE = (byte) 0x75;
		final byte PCMPEQD_OPCODE = (byte) 0x76;
		final byte MOVQ_R128_M64_OPCODE = (byte) 0x7e;
		final byte MOVDQA_M128_R128_OPCODE = (byte) 0x7f;
		final byte JO_DISP32_OPCODE = (byte) 0x80;
		final byte JNO_DISP32_OPCODE = (byte) 0x81;
		final byte JB_DISP32_OPCODE = (byte) 0x82;
		final byte JAE_DISP32_OPCODE = (byte) 0x83;
		final byte JE_DISP32_OPCODE = (byte) 0x84;
		final byte JNE_DISP32_OPCODE = (byte) 0x85;
		final byte JBE_DISP32_OPCODE = (byte) 0x86;
		final byte JA_DISP32_OPCODE = (byte) 0x87;
		final byte JS_DISP32_OPCODE = (byte) 0x88;
		final byte JNS_DISP32_OPCODE = (byte) 0x89;
		final byte JP_DISP32_OPCODE = (byte) 0x8a;
		final byte JNP_DISP32_OPCODE = (byte) 0x8b;
		final byte JL_DISP32_OPCODE = (byte) 0x8c;
		final byte JGE_DISP32_OPCODE = (byte) 0x8d;
		final byte JLE_DISP32_OPCODE = (byte) 0x8e;
		final byte JG_DISP32_OPCODE = (byte) 0x8f;
		final byte SETO_OPCODE = (byte) 0x90;
		final byte SETNO_OPCODE = (byte) 0x91;
		final byte SETB_OPCODE = (byte) 0x92;
		final byte SETAE_OPCODE = (byte) 0x93;
		final byte SETE_OPCODE = (byte) 0x94;
		final byte SETNE_OPCODE = (byte) 0x95;
		final byte SETBE_OPCODE = (byte) 0x96;
		final byte SETA_OPCODE = (byte) 0x97;
		final byte SETS_OPCODE = (byte) 0x98;
		final byte SETNS_OPCODE = (byte) 0x99;
		final byte SETL_OPCODE = (byte) 0x9c;
		final byte SETGE_OPCODE = (byte) 0x9d;
		final byte SETLE_OPCODE = (byte) 0x9e;
		final byte SETG_OPCODE = (byte) 0x9f;
		final byte CPUID_OPCODE = (byte) 0xa2;
		final byte BT_M32_R32_OPCODE = (byte) 0xa3;
		final byte SHLD_OPCODE = (byte) 0xa5;
		final byte BTS_M32_R32_OPCODE = (byte) 0xab;
		final byte SHRD_R_R_IMM_OPCODE = (byte) 0xac;
		final byte SHRD_R_R_CL_OPCODE = (byte) 0xad;
		final byte GROUP15_OPCODE = (byte) 0xae;
		final byte IMUL_OPCODE = (byte) 0xaf;
		final byte XCHG_M8_R8_OPCODE = (byte) 0xb0;
		final byte XCHG_M32_R32_OPCODE = (byte) 0xb1;
		final byte BTR_M32_R32_OPCODE = (byte) 0xb3;
		final byte MOVZX_BYTE_PTR_OPCODE = (byte) 0xb6;
		final byte MOVZX_WORD_PTR_OPCODE = (byte) 0xb7;
		final byte GROUP8_OPCODE = (byte) 0xba;
		final byte BTC_M32_R32_OPCODE = (byte) 0xbb;
		final byte BSF_OPCODE = (byte) 0xbc;
		final byte BSR_R32_M32_OPCODE = (byte) 0xbd;
		final byte MOVSX_BYTE_PTR_OPCODE = (byte) 0xbe;
		final byte MOVSX_WORD_PTR_OPCODE = (byte) 0xbf;
		final byte XADD_M8_R8_OPCODE = (byte) 0xc0;
		final byte XADD_M32_R32_OPCODE = (byte) 0xc1;
		final byte PEXTRW_OPCODE = (byte) 0xc5;
		final byte SHUFPx_OPCODE = (byte) 0xc6;
		final byte GROUP9_OPCODE = (byte) 0xc7;
		final byte BSWAP_EAX_OPCODE = (byte) 0xc8;
		final byte BSWAP_ECX_OPCODE = (byte) 0xc9;
		final byte BSWAP_EDX_OPCODE = (byte) 0xca;
		final byte BSWAP_EBX_OPCODE = (byte) 0xcb;
		final byte BSWAP_ESI_OPCODE = (byte) 0xcc;
		final byte BSWAP_EDI_OPCODE = (byte) 0xcd;
		final byte BSWAP_ESP_OPCODE = (byte) 0xce;
		final byte BSWAP_EBP_OPCODE = (byte) 0xcf;
		final byte PADDQ_OPCODE = (byte) 0xd4;
		final byte MOVQ_M_XMM_OPCODE = (byte) 0xd6;
		final byte PMOVMSKB_OPCODE = (byte) 0xd7;
		final byte PMINUB_OPCODE = (byte) 0xda;
		final byte PMAXUB_OPCODE = (byte) 0xde;
		final byte PAND_OPCODE = (byte) 0xdb;
		final byte MOVNTDQ_OPCODE = (byte) 0xe7;
		final byte POR_OPCODE = (byte) 0xeb;
		final byte PXOR_OPCODE = (byte) 0xef;
		final byte LDDQU_OPCODE = (byte) 0xf0;
		final byte PSUBB_OPCODE = (byte) 0xf8;
		final byte PSUBW_OPCODE = (byte) 0xf9;
		final byte PSUBD_OPCODE = (byte) 0xfa;
		final byte PSUBQ_OPCODE = (byte) 0xfb;
		final byte PADDD_OPCODE = (byte) 0xfe;

		final Opcode[] cmovOpcodes = {
			null,
			null,
			Opcode.CMOVB,
			Opcode.CMOVAE,
			Opcode.CMOVE,
			Opcode.CMOVNE,
			Opcode.CMOVBE,
			Opcode.CMOVA,
			Opcode.CMOVS,
			Opcode.CMOVNS,
			null,
			null,
			Opcode.CMOVL,
			Opcode.CMOVGE,
			Opcode.CMOVLE,
			Opcode.CMOVG
		};

		final Opcode[] setOpcodes = {
			Opcode.SETO,
			Opcode.SETNO,
			Opcode.SETB,
			Opcode.SETAE,
			Opcode.SETE,
			Opcode.SETNE,
			Opcode.SETBE,
			Opcode.SETA,
			Opcode.SETS,
			Opcode.SETNS,
			null,
			null,
			Opcode.SETL,
			Opcode.SETGE,
			Opcode.SETLE,
			Opcode.SETG
		};

		final byte opcodeSecondByte = b.read1();

		return switch (opcodeSecondByte) {
			case GROUP7_OPCODE -> parseExtendedOpcodeGroup7(b, opcodeFirstByte, opcodeSecondByte);
			case GROUP8_OPCODE -> parseExtendedOpcodeGroup8(b, opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP9_OPCODE -> parseExtendedOpcodeGroup9(b, opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP12_OPCODE -> parseExtendedOpcodeGroup12(b, opcodeFirstByte, opcodeSecondByte);
			case GROUP13_OPCODE -> parseExtendedOpcodeGroup13(b, opcodeFirstByte, opcodeSecondByte);
			case GROUP14_OPCODE -> parseExtendedOpcodeGroup14(b, opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP15_OPCODE -> parseExtendedOpcodeGroup15(b, opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP16_OPCODE -> parseExtendedOpcodeGroup16(b, opcodeFirstByte, opcodeSecondByte, pref);
			case TABLE_A4_OPCODE -> parseTableA4(b, opcodeFirstByte, opcodeSecondByte, pref);
			case TABLE_A5_OPCODE -> parseTableA5(b, opcodeFirstByte, opcodeSecondByte, pref);

			// conditional jumps
			case JA_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JA).op(imm32(b)).build();
			case JAE_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JAE).op(imm32(b)).build();
			case JE_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JE).op(imm32(b)).build();
			case JNE_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JNE).op(imm32(b)).build();
			case JBE_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JBE).op(imm32(b)).build();
			case JG_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JG).op(imm32(b)).build();
			case JS_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JS).op(imm32(b)).build();
			case JNS_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JNS).op(imm32(b)).build();
			case JP_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JP).op(imm32(b)).build();
			case JL_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JL).op(imm32(b)).build();
			case JGE_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JGE).op(imm32(b)).build();
			case JLE_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JLE).op(imm32(b)).build();
			case JB_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JB).op(imm32(b)).build();
			case JNP_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JNP).op(imm32(b)).build();
			case JO_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JO).op(imm32(b)).build();
			case JNO_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JNO).op(imm32(b)).build();

			case ENDBR_OPCODE -> {
				final byte x = b.read1();
				if (x == (byte) 0xfa) {
					yield Instruction.builder().opcode(Opcode.ENDBR64).build();
				} else if (x == (byte) 0xfb) {
					yield Instruction.builder().opcode(Opcode.ENDBR32).build();
				} else if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP) {
					final ModRM modrm = new ModRM(x);
					yield Instruction.builder()
							.opcode(Opcode.RDSSPQ)
							.op(Registers.fromCode(
									modrm.rm(),
									pref.rex().isOperand64Bit(),
									pref.rex().hasModRMRMExtension(),
									false))
							.build();
				} else {
					throw new IllegalArgumentException(String.format("Invalid value (0x%02x)", x));
				}
			}
			case SLDT_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.SLDT)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.WORD_PTR)
								.build())
						.build();
			}
			case NOP_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.NOP)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(getPointerSize(pref))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			}

			// conditional set bytes
			case SETE_OPCODE,
					SETO_OPCODE,
					SETNO_OPCODE,
					SETB_OPCODE,
					SETLE_OPCODE,
					SETAE_OPCODE,
					SETNE_OPCODE,
					SETBE_OPCODE,
					SETA_OPCODE,
					SETS_OPCODE,
					SETNS_OPCODE,
					SETL_OPCODE,
					SETGE_OPCODE,
					SETG_OPCODE -> {
				final Opcode opcode = setOpcodes[and(opcodeSecondByte, (byte) 0b00001111)];
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(opcode)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.BYTE_PTR)
												.build()
										: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()))
						.build();
			}

			case CPUID_OPCODE -> Instruction.builder().opcode(Opcode.CPUID).build();
			case IMUL_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r1 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield Instruction.builder()
						.opcode(Opcode.IMUL)
						.op(r1)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.fromSize(r1.bits()))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			}

			// Bit tests
			case BT_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.BT)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case BTR_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.BTR)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case BTS_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.BTS)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case BTC_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.BTC)
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case BSF_OPCODE -> {
				final ModRM modrm = modrm(b);
				final boolean hasRepPrefix = pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP;
				yield Instruction.builder()
						.opcode(hasRepPrefix ? Opcode.TZCNT : Opcode.BSF)
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case BSR_R32_M32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r1 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield Instruction.builder()
						.opcode(Opcode.BSR)
						.op(r1)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.fromSize(r1.bits()))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			}

			case SHLD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.SHLD,
						Register64.fromByte(getByteFromRM(pref, modrm)),
						Register64.fromByte(getByteFromReg(pref.rex(), modrm)),
						Register8.CL);
			}
			case SHRD_R_R_IMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.SHRD,
						Register64.fromByte(getByteFromRM(pref, modrm)),
						Register64.fromByte(getByteFromReg(pref.rex(), modrm)),
						imm8(b));
			}
			case SHRD_R_R_CL_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.SHRD,
						Register64.fromByte(getByteFromRM(pref, modrm)),
						Register64.fromByte(getByteFromReg(pref.rex(), modrm)),
						Register8.CL);
			}
			case XCHG_M8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Operand op1 = parseIndirectOperand(b, pref, modrm)
						.pointer(PointerSize.BYTE_PTR)
						.build();
				final Operand op2 = Register8.fromByte(getByteFromReg(pref.rex(), modrm), pref.hasRexPrefix());
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.CMPXCHG).op(op1).op(op2).build();
			}
			case XCHG_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r2 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				final Operand op1 = parseIndirectOperand(b, pref, modrm)
						.pointer(PointerSize.fromSize(r2.bits()))
						.build();
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.CMPXCHG).op(op1).op(r2).build();
			}

			case MOVZX_BYTE_PTR_OPCODE, MOVZX_WORD_PTR_OPCODE, MOVSX_BYTE_PTR_OPCODE, MOVSX_WORD_PTR_OPCODE -> {
				final Opcode opcode =
						(opcodeSecondByte == MOVZX_BYTE_PTR_OPCODE || opcodeSecondByte == MOVZX_WORD_PTR_OPCODE)
								? Opcode.MOVZX
								: Opcode.MOVSX;
				final PointerSize ptrSize =
						(opcodeSecondByte == MOVZX_BYTE_PTR_OPCODE || opcodeSecondByte == MOVSX_BYTE_PTR_OPCODE)
								? PointerSize.BYTE_PTR
								: PointerSize.WORD_PTR;

				final ModRM modrm = modrm(b);
				final Register r1 = Registers.fromCode(
						modrm.reg(), pref.rex().isOperand64Bit(), pref.rex().hasModRMRegExtension(), false);

				if (isIndirectOperandNeeded(modrm)) {
					yield Instruction.builder()
							.opcode(opcode)
							.op(r1)
							.op(parseIndirectOperand(b, pref, modrm)
									.pointer(ptrSize)
									.build())
							.build();
				} else {
					final byte regByte = getByteFromRM(pref, modrm);
					final Register r2 = (ptrSize == PointerSize.BYTE_PTR)
							? Register8.fromByte(regByte, pref.hasRexPrefix())
							: Register16.fromByte(regByte);
					yield Instruction.builder().opcode(opcode).op(r1).op(r2).build();
				}
			}

			case MOVxQx_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref.rex(), modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				final boolean movdqa = pref.hasOperandSizeOverridePrefix();
				final boolean movdqu = pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP;
				yield Instruction.builder()
						.opcode(movdqa ? Opcode.MOVDQA : (movdqu ? Opcode.MOVDQU : Opcode.MOVQ))
						.op((movdqa || movdqu) ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(modrm.reg()))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														(movdqa || movdqu)
																? PointerSize.XMMWORD_PTR
																: PointerSize.QWORD_PTR)
												.build()
										: RegisterXMM.fromByte(r2))
						.build();
			}
			case MOVDQA_M128_R128_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVDQA)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.build();
			}
			case PSHUF_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref.rex(), modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(pref.hasOperandSizeOverridePrefix() ? Opcode.PSHUFD : Opcode.PSHUFW)
						.op(pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(r1))
						.op(pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r2) : RegisterMMX.fromByte(r2))
						.op(imm8(b))
						.build();
			}
			case SHUFPx_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref.rex(), modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(pref.hasOperandSizeOverridePrefix() ? Opcode.SHUFPD : Opcode.SHUFPS)
						.op(RegisterXMM.fromByte(r1))
						.op(RegisterXMM.fromByte(r2))
						.op(imm8(b))
						.build();
			}
			case PEXTRW_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref.rex(), modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PEXTRW)
						.op(Register32.fromByte(r1))
						.op(RegisterMMX.fromByte(r2))
						.op(imm8(b))
						.build();
			}
			case XADD_M8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.XADD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build())
						.op(Register8.fromByte(getByteFromReg(pref.rex(), modrm), pref.hasRexPrefix()))
						.build();
			}
			case XADD_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r2 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.XADD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.fromSize(r2.bits()))
								.build())
						.op(r2)
						.build();
			}
			case MOVQ_MOVD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Opcode code = pref.rex().isOperand64Bit() ? Opcode.MOVQ : Opcode.MOVD;
				final byte regByte = getByteFromReg(pref.rex(), modrm);
				final Register r1 = pref.hasOperandSizeOverridePrefix()
						? RegisterXMM.fromByte(regByte)
						: RegisterMMX.fromByte(regByte);
				final Register r2 = Registers.fromCode(
						modrm.rm(), pref.rex().isOperand64Bit(), pref.rex().hasModRMRMExtension(), false);
				yield Instruction.builder()
						.opcode(code)
						.op(r1)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														code == Opcode.MOVQ
																? PointerSize.fromSize(r1.bits())
																: (r1.bits() == 64
																		? PointerSize.QWORD_PTR
																		: PointerSize.DWORD_PTR))
												.build()
										: r2)
						.build();
			}
			case MOVQ_M_XMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref.rex(), modrm);
				yield Instruction.builder()
						.opcode(Opcode.MOVQ)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(regByte))
						.build();
			}
			case MOVQ_R128_M64_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref.rex(), modrm);
				yield Instruction.builder()
						.opcode(Opcode.MOVQ)
						.op(RegisterXMM.fromByte(regByte))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			}
			case MOVHLPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVHLPS)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case MOVHPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref.rex(), modrm);
				yield Instruction.builder()
						.opcode(Opcode.MOVHPS)
						.op(RegisterXMM.fromByte(regByte))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			}
			case MOVHPS_MOVHPD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref.rex(), modrm);
				yield Instruction.builder()
						.opcode(pref.hasOperandSizeOverridePrefix() ? Opcode.MOVHPD : Opcode.MOVHPS)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(regByte))
						.build();
			}
			case PMINUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PMINUB)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(getXMMArgument(b, modrm, pref, getByteFromRM(pref, modrm)))
						.build();
			}
			case PMAXUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PMAXUB)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PAND_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PAND)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PADDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PADDQ)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PADDD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PADDD)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PSUBB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PSUBB)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PSUBW_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PSUBW)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PSUBD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PSUBD)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PSUBQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PSUBQ)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case POR_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.POR)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case PXOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PXOR)
						.op(
								pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r1Byte)
										: RegisterMMX.fromByte(r1Byte))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														pref.hasOperandSizeOverridePrefix()
																? PointerSize.XMMWORD_PTR
																: PointerSize.QWORD_PTR)
												.build()
										: (pref.hasOperandSizeOverridePrefix()
												? RegisterXMM.fromByte(r2Byte)
												: RegisterMMX.fromByte(r2Byte)))
						.build();
			}
			case PCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PCMPEQB)
						.op(
								pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r1Byte)
										: RegisterMMX.fromByte(r1Byte))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														pref.hasOperandSizeOverridePrefix()
																? PointerSize.XMMWORD_PTR
																: PointerSize.QWORD_PTR)
												.build()
										: (pref.hasOperandSizeOverridePrefix()
												? RegisterXMM.fromByte(r2Byte)
												: RegisterMMX.fromByte(r2Byte)))
						.build();
			}
			case PCMPEQW_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PCMPEQW)
						.op(
								pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r1Byte)
										: RegisterMMX.fromByte(r1Byte))
						.op(
								pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r2Byte)
										: RegisterMMX.fromByte(r2Byte))
						.build();
			}
			case PCMPEQD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.PCMPEQD)
						.op(
								pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r1Byte)
										: RegisterMMX.fromByte(r1Byte))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														pref.hasOperandSizeOverridePrefix()
																? PointerSize.XMMWORD_PTR
																: PointerSize.QWORD_PTR)
												.build()
										: (pref.hasOperandSizeOverridePrefix()
												? RegisterXMM.fromByte(r2Byte)
												: RegisterMMX.fromByte(r2Byte)))
						.build();
			}
			case XORPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.XORPS)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(RegisterXMM.fromByte(r2Byte))
						.build();
			}
			case ADDSD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(Opcode.ADDSD)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(RegisterXMM.fromByte(r2Byte))
						.build();
			}
			case DIVxx_OPCODE -> {
				final ModRM modrm = modrm(b);
				final InstructionBuilder ib = Instruction.builder();
				ib.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)));
				if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP) {
					ib.opcode(Opcode.DIVSS)
							.op(
									isIndirectOperandNeeded(modrm)
											? parseIndirectOperand(b, pref, modrm)
													.pointer(PointerSize.DWORD_PTR)
													.build()
											: RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
				} else if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REPNZ) {
					ib.opcode(Opcode.DIVSD)
							.op(
									isIndirectOperandNeeded(modrm)
											? parseIndirectOperand(b, pref, modrm)
													.pointer(PointerSize.QWORD_PTR)
													.build()
											: RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
				} else if (pref.hasOperandSizeOverridePrefix()) {
					ib.opcode(Opcode.DIVPD)
							.op(
									isIndirectOperandNeeded(modrm)
											? parseIndirectOperand(b, pref, modrm)
													.pointer(PointerSize.XMMWORD_PTR)
													.build()
											: RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
				} else {
					ib.opcode(Opcode.DIVPS)
							.op(
									isIndirectOperandNeeded(modrm)
											? parseIndirectOperand(b, pref, modrm)
													.pointer(PointerSize.XMMWORD_PTR)
													.build()
											: RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
				}
				yield ib.build();
			}
			case ANDPD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				yield Instruction.builder()
						.opcode(Opcode.ANDPD)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.build();
			}
			case MOVAPx_R128_M128_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref.rex(), modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield Instruction.builder()
						.opcode(pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS)
						.op(RegisterXMM.fromByte(r1Byte))
						.op(getXMMArgument(b, modrm, pref, r2Byte))
						.build();
			}
			case MOVAPx_M128_R128_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.build();
			}

			case CMOVE_OPCODE,
					CMOVAE_OPCODE,
					CMOVB_OPCODE,
					CMOVBE_OPCODE,
					CMOVNE_OPCODE,
					CMOVNS_OPCODE,
					CMOVL_OPCODE,
					CMOVGE_OPCODE,
					CMOVG_OPCODE,
					CMOVLE_OPCODE,
					CMOVA_OPCODE,
					CMOVS_OPCODE -> {
				final Opcode opcode = cmovOpcodes[and(opcodeSecondByte, (byte) 0b00001111)];
				final ModRM modrm = modrm(b);
				final Register r1 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield Instruction.builder()
						.opcode(opcode)
						.op(r1)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.fromSize(r1.bits()))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case BSWAP_EAX_OPCODE,
					BSWAP_EBX_OPCODE,
					BSWAP_ECX_OPCODE,
					BSWAP_EDX_OPCODE,
					BSWAP_ESI_OPCODE,
					BSWAP_EDI_OPCODE,
					BSWAP_ESP_OPCODE,
					BSWAP_EBP_OPCODE -> {
				final byte regByte = and(opcodeSecondByte, (byte) 0b00000111);
				yield Instruction.builder()
						.opcode(Opcode.BSWAP)
						.op(
								pref.rex().isOperand64Bit()
										? Register64.fromByte(
												Registers.combine(pref.rex().hasOpcodeRegExtension(), regByte))
										: Register32.fromByte(
												Registers.combine(pref.rex().hasOpcodeRegExtension(), regByte)))
						.build();
			}
			case UD2_OPCODE -> Instruction.builder().opcode(Opcode.UD2).build();
			case SYSCALL_OPCODE -> Instruction.builder().opcode(Opcode.SYSCALL).build();
			case PUNPCKLBW_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PUNPCKLBW)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case PUNPCKLWD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PUNPCKLWD)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case PUNPCKLDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PUNPCKLDQ)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case PUNPCKLQDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PUNPCKLQDQ)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case PUNPCKHQDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PUNPCKHQDQ)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case PUNPCKHDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PUNPCKHDQ)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case PCMPGTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PCMPGTB)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case MOVSD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final boolean hasRepnePrefix =
						pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REPNZ;
				yield Instruction.builder()
						.opcode(hasRepnePrefix ? Opcode.MOVSD : Opcode.MOVUPS)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(hasRepnePrefix ? PointerSize.QWORD_PTR : PointerSize.XMMWORD_PTR)
								.build())
						.build();
			}
			case MOVUPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVUPS)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.build();
			}
			case CVTSI2SD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.CVTSI2SD)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case MOVNTPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVNTPS)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.build();
			}
			case UCOMISx_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(pref.hasOperandSizeOverridePrefix() ? Opcode.UCOMISD : Opcode.UCOMISS)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(
										pref.hasOperandSizeOverridePrefix()
												? PointerSize.QWORD_PTR
												: PointerSize.DWORD_PTR)
								.build())
						.build();
			}
			case PMOVMSKB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.PMOVMSKB)
						.op(Register32.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case MOVMSKPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVMSKPS)
						.op(Register32.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case MOVNTDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVNTDQ)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.build();
			}
			case LDDQU_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.LDDQU)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.build();
			}
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseTableA4(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final byte PSHUFB_OPCODE = (byte) 0x00;
		final byte PMINUD_OPCODE = (byte) 0x3b;
		final byte MOVBE_OPCODE = (byte) 0xf0;

		final byte x = b.read1();
		final ModRM modrm = modrm(b);

		return switch (x) {
			case PSHUFB_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.PSHUFB)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.build();
			case MOVBE_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.MOVBE)
						.op(Register32.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build())
						.build();
			case PMINUD_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.PMINUD)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.build();
			default -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte, x);
		};
	}

	private static Instruction parseTableA5(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final byte PALIGNR_OPCODE = (byte) 0x0f;
		final byte PCMPISTRI_OPCODE = (byte) 0x63;

		final byte x = b.read1();
		final ModRM modrm = modrm(b);

		return switch (x) {
			case PALIGNR_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.PALIGNR)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.XMMWORD_PTR)
												.build()
										: RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.op(imm8(b))
						.build();
			case PCMPISTRI_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.PCMPISTRI)
						.op(RegisterXMM.fromByte(getByteFromReg(pref.rex(), modrm)))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.XMMWORD_PTR)
												.build()
										: RegisterXMM.fromByte(getByteFromRM(pref, modrm)))
						.op(imm8(b))
						.build();
			default -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte, x);
		};
	}

	private static Instruction parseExtendedOpcodeGroup15(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			return switch (modrm.reg()) {
				case (byte) 0b000 ->
					new GeneralInstruction(
							Opcode.FXSAVE,
							parseIndirectOperand(b, pref, modrm)
									.pointer(PointerSize.QWORD_PTR)
									.build());
				case (byte) 0b001 ->
					new GeneralInstruction(
							Opcode.FXRSTOR,
							parseIndirectOperand(b, pref, modrm)
									.pointer(PointerSize.QWORD_PTR)
									.build());
				case (byte) 0b011 ->
					new GeneralInstruction(
							Opcode.STMXCSR,
							parseIndirectOperand(b, pref, modrm)
									.pointer(PointerSize.DWORD_PTR)
									.build());
				case (byte) 0b100 ->
					new GeneralInstruction(
							Opcode.XSAVE,
							parseIndirectOperand(b, pref, modrm)
									.pointer(PointerSize.QWORD_PTR)
									.build());
				case (byte) 0b101 ->
					new GeneralInstruction(
							Opcode.XRSTOR,
							parseIndirectOperand(b, pref, modrm)
									.pointer(PointerSize.QWORD_PTR)
									.build());
				default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
			};
		}

		return switch (modrm.reg()) {
			case (byte) 0b101 ->
				Instruction.builder()
						.opcode(Opcode.INCSSPQ)
						.op(Register64.fromByte(getByteFromRM(pref, modrm)))
						.build();
			case (byte) 0b111 -> Instruction.builder().opcode(Opcode.SFENCE).build();
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup9(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			return switch (modrm.reg()) {
				case (byte) 0b100 ->
					new GeneralInstruction(
							Opcode.XSAVEC,
							parseIndirectOperand(b, pref, modrm)
									.pointer(PointerSize.QWORD_PTR)
									.build());
				default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
			};
		}

		if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP) {
			notImplemented();
		}

		final Opcode opcode =
				switch (modrm.reg()) {
					case (byte) 0b000, (byte) 0b001, (byte) 0b010, (byte) 0b011, (byte) 0b100, (byte) 0b101 ->
						throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					case (byte) 0b110 -> Opcode.RDRAND;
					case (byte) 0b111 -> Opcode.RDSEED;
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return Instruction.builder()
				.opcode(opcode)
				.op(Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix()))
				.build();
	}

	/** Returns AX, EAX or RAX depending on the given prefixes. */
	private static Register getFirstRegister(final Prefixes pref) {
		return pref.hasOperandSizeOverridePrefix()
				? Register16.AX
				: pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX;
	}

	private static Instruction parseSingleByteOpcode(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte OPCODE_REG_MASK = 0b00000111;

		final byte ADD_M8_R8_OPCODE = (byte) 0x00;
		final byte ADD_M32_R32_OPCODE = (byte) 0x01;
		final byte ADD_R8_M8_OPCODE = (byte) 0x02;
		final byte ADD_R32_M32_OPCODE = (byte) 0x03;
		final byte ADD_AL_IMM8_OPCODE = (byte) 0x04;
		final byte ADD_EAX_IMM32_OPCODE = (byte) 0x05;
		final byte BAD_PUSH_ES_OPCODE = (byte) 0x06;
		final byte BAD_POP_ES_OPCODE = (byte) 0x07;
		final byte OR_M8_R8_OPCODE = (byte) 0x08;
		final byte OR_M32_R32_OPCODE = (byte) 0x09;
		final byte OR_R8_M8_OPCODE = (byte) 0x0a;
		final byte OR_R32_M32_OPCODE = (byte) 0x0b;
		final byte OR_AL_IMM8_OPCODE = (byte) 0x0c;
		final byte OR_EAX_IMM32_OPCODE = (byte) 0x0d;
		final byte BAD_PUSH_CS_OPCODE = (byte) 0x0e;
		final byte ADC_M8_R8_OPCODE = (byte) 0x10;
		final byte ADC_M32_R32_OPCODE = (byte) 0x11;
		final byte ADC_R8_M8_OPCODE = (byte) 0x12;
		final byte ADC_R32_M32_OPCODE = (byte) 0x13;
		final byte ADC_AL_IMM8_OPCODE = (byte) 0x14;
		final byte ADC_EAX_IMM32_OPCODE = (byte) 0x15;
		final byte BAD_PUSH_SS_OPCODE = (byte) 0x16;
		final byte BAD_POP_SS_OPCODE = (byte) 0x17;
		final byte SBB_M8_R8_OPCODE = (byte) 0x18;
		final byte SBB_M32_R32_OPCODE = (byte) 0x19;
		final byte SBB_R8_M8_OPCODE = (byte) 0x1a;
		final byte SBB_R32_M32_OPCODE = (byte) 0x1b;
		final byte SBB_AL_IMM8_OPCODE = (byte) 0x1c;
		final byte SBB_EAX_IMM32_OPCODE = (byte) 0x1d;
		final byte BAD_PUSH_DS_OPCODE = (byte) 0x1e;
		final byte BAD_POP_DS_OPCODE = (byte) 0x1f;
		final byte AND_M8_R8_OPCODE = (byte) 0x20;
		final byte AND_M32_R32_OPCODE = (byte) 0x21;
		final byte AND_R8_M8_OPCODE = (byte) 0x22;
		final byte AND_R32_M32_OPCODE = (byte) 0x23;
		final byte AND_AL_IMM8_OPCODE = (byte) 0x24;
		final byte AND_EAX_IMM32_OPCODE = (byte) 0x25;
		final byte BAD_DAA_OPCODE = (byte) 0x27;
		final byte SUB_M8_R8_OPCODE = (byte) 0x28;
		final byte SUB_M32_R32_OPCODE = (byte) 0x29;
		final byte SUB_R8_M8_OPCODE = (byte) 0x2a;
		final byte SUB_R32_M32_OPCODE = (byte) 0x2b;
		final byte SUB_AL_IMM8_OPCODE = (byte) 0x2c;
		final byte SUB_EAX_IMM32_OPCODE = (byte) 0x2d;
		final byte BAD_DAS_OPCODE = (byte) 0x2f;
		final byte XOR_M8_R8_OPCODE = (byte) 0x30;
		final byte XOR_M32_R32_OPCODE = (byte) 0x31;
		final byte XOR_R8_M8_OPCODE = (byte) 0x32;
		final byte XOR_R32_M32_OPCODE = (byte) 0x33;
		final byte XOR_AL_IMM8_OPCODE = (byte) 0x34;
		final byte XOR_EAX_IMM32_OPCODE = (byte) 0x35;
		final byte BAD_AAA_OPCODE = (byte) 0x37;
		final byte CMP_M8_R8_OPCODE = (byte) 0x38;
		final byte CMP_M32_R32_OPCODE = (byte) 0x39;
		final byte CMP_R8_M8_OPCODE = (byte) 0x3a;
		final byte CMP_R32_M32_OPCODE = (byte) 0x3b;
		final byte CMP_AL_IMM8_OPCODE = (byte) 0x3c;
		final byte CMP_EAX_IMM32_OPCODE = (byte) 0x3d;
		final byte BAD_AAS_OPCODE = (byte) 0x3f;
		final byte PUSH_EAX_OPCODE = (byte) 0x50;
		final byte PUSH_ECX_OPCODE = (byte) 0x51;
		final byte PUSH_EDX_OPCODE = (byte) 0x52;
		final byte PUSH_EBX_OPCODE = (byte) 0x53;
		final byte PUSH_ESP_OPCODE = (byte) 0x54;
		final byte PUSH_EBP_OPCODE = (byte) 0x55;
		final byte PUSH_ESI_OPCODE = (byte) 0x56;
		final byte PUSH_EDI_OPCODE = (byte) 0x57;
		final byte POP_EAX_OPCODE = (byte) 0x58;
		final byte POP_ECX_OPCODE = (byte) 0x59;
		final byte POP_EDX_OPCODE = (byte) 0x5a;
		final byte POP_EBX_OPCODE = (byte) 0x5b;
		final byte POP_ESP_OPCODE = (byte) 0x5c;
		final byte POP_EBP_OPCODE = (byte) 0x5d;
		final byte POP_ESI_OPCODE = (byte) 0x5e;
		final byte POP_EDI_OPCODE = (byte) 0x5f;
		final byte BAD_PUSHAD_OPCODE = (byte) 0x60;
		final byte BAD_POPAD_OPCODE = (byte) 0x61;
		final byte MOVSXD_OPCODE = (byte) 0x63;
		final byte PUSH_IMM32_OPCODE = (byte) 0x68;
		final byte IMUL_R32_M32_IMM32_OPCODE = (byte) 0x69;
		final byte PUSH_IMM8_OPCODE = (byte) 0x6a;
		final byte IMUL_REG_REG_IMM8_OPCODE = (byte) 0x6b;
		final byte INS_M8_OPCODE = (byte) 0x6c;
		final byte INS_M32_OPCODE = (byte) 0x6d;
		final byte OUTS_M8_OPCODE = (byte) 0x6e;
		final byte OUTS_M32_OPCODE = (byte) 0x6f;
		final byte JO_DISP8_OPCODE = (byte) 0x70;
		final byte JNO_DISP8_OPCODE = (byte) 0x71;
		final byte JB_DISP8_OPCODE = (byte) 0x72;
		final byte JAE_DISP8_OPCODE = (byte) 0x73;
		final byte JE_DISP8_OPCODE = (byte) 0x74;
		final byte JNE_DISP8_OPCODE = (byte) 0x75;
		final byte JBE_DISP8_OPCODE = (byte) 0x76;
		final byte JA_DISP8_OPCODE = (byte) 0x77;
		final byte JS_DISP8_OPCODE = (byte) 0x78;
		final byte JNS_DISP8_OPCODE = (byte) 0x79;
		final byte JP_DISP8_OPCODE = (byte) 0x7a;
		final byte JNP_DISP8_OPCODE = (byte) 0x7b;
		final byte JL_DISP8_OPCODE = (byte) 0x7c;
		final byte JGE_DISP8_OPCODE = (byte) 0x7d;
		final byte JLE_DISP8_OPCODE = (byte) 0x7e;
		final byte JG_DISP8_OPCODE = (byte) 0x7f;
		final byte UNDEFINED_OLD_MOV_OPCODE = (byte) 0x82;
		final byte TEST_R8_R8_OPCODE = (byte) 0x84;
		final byte TEST_R32_R32_OPCODE = (byte) 0x85;
		final byte XCHG_M8_R8_OPCODE = (byte) 0x86;
		final byte XCHG_M32_R32_OPCODE = (byte) 0x87;
		final byte MOV_MEM8_REG8_OPCODE = (byte) 0x88;
		final byte MOV_M32_R32_OPCODE = (byte) 0x89;
		final byte MOV_R8_M8_OPCODE = (byte) 0x8a;
		final byte MOV_R32_M32_OPCODE = (byte) 0x8b;
		final byte MOV_M16_SEG_OPCODE = (byte) 0x8c;
		final byte LEA_OPCODE = (byte) 0x8d;
		final byte MOV_SEG_M16_OPCODE = (byte) 0x8e;
		final byte POP_M64_OPCODE = (byte) 0x8f;
		final byte NOP_OPCODE = (byte) 0x90;
		final byte XCHG_ECX_EAX_OPCODE = (byte) 0x91;
		final byte XCHG_EDX_EAX_OPCODE = (byte) 0x92;
		final byte XCHG_EBX_EAX_OPCODE = (byte) 0x93;
		final byte XCHG_ESP_EAX_OPCODE = (byte) 0x94;
		final byte XCHG_EBP_EAX_OPCODE = (byte) 0x95;
		final byte XCHG_ESI_EAX_OPCODE = (byte) 0x96;
		final byte XCHG_EDI_EAX_OPCODE = (byte) 0x97;
		final byte CDQE_OPCODE = (byte) 0x98;
		final byte CDQ_OPCODE = (byte) 0x99;
		final byte FAR_CALL_OPCODE = (byte) 0x9a;
		final byte FWAIT_OPCODE = (byte) 0x9b;
		final byte PUSHF_OPCODE = (byte) 0x9c;
		final byte POPF_OPCODE = (byte) 0x9d;
		final byte SAHF_OPCODE = (byte) 0x9e;
		final byte LAHF_OPCODE = (byte) 0x9f;
		final byte MOVABS_R8_S64_OPCODE = (byte) 0xa0;
		final byte MOVABS_R32_S64_OPCODE = (byte) 0xa1;
		final byte MOVABS_S64_R8_OPCODE = (byte) 0xa2;
		final byte MOVABS_S64_R32_OPCODE = (byte) 0xa3;
		final byte MOVS_M8_OPCODE = (byte) 0xa4;
		final byte MOVS_M32_OPCODE = (byte) 0xa5;
		final byte CMPS_M8_OPCODE = (byte) 0xa6;
		final byte CMPS_M32_OPCODE = (byte) 0xa7;
		final byte TEST_AL_IMM8_OPCODE = (byte) 0xa8;
		final byte TEST_EAX_IMM32_OPCODE = (byte) 0xa9;
		final byte STOS_R8_OPCODE = (byte) 0xaa;
		final byte STOS_R32_OPCODE = (byte) 0xab;
		final byte LODS_R8_OPCODE = (byte) 0xac;
		final byte LODS_R32_OPCODE = (byte) 0xad;
		final byte SCAS_R8_OPCODE = (byte) 0xae;
		final byte SCAS_R32_OPCODE = (byte) 0xaf;
		final byte MOV_AL_IMM8_OPCODE = (byte) 0xb0;
		final byte MOV_CL_IMM8_OPCODE = (byte) 0xb1;
		final byte MOV_DL_IMM8_OPCODE = (byte) 0xb2;
		final byte MOV_BL_IMM8_OPCODE = (byte) 0xb3;
		final byte MOV_AH_IMM8_OPCODE = (byte) 0xb4;
		final byte MOV_CH_IMM8_OPCODE = (byte) 0xb5;
		final byte MOV_DH_IMM8_OPCODE = (byte) 0xb6;
		final byte MOV_BH_IMM8_OPCODE = (byte) 0xb7;
		final byte MOV_EAX_IMM32_OPCODE = (byte) 0xb8;
		final byte MOV_ECX_IMM32_OPCODE = (byte) 0xb9;
		final byte MOV_EDX_IMM32_OPCODE = (byte) 0xba;
		final byte MOV_EBX_IMM32_OPCODE = (byte) 0xbb;
		final byte MOV_ESP_IMM32_OPCODE = (byte) 0xbc;
		final byte MOV_EBP_IMM32_OPCODE = (byte) 0xbd;
		final byte MOV_ESI_IMM32_OPCODE = (byte) 0xbe;
		final byte MOV_EDI_IMM32_OPCODE = (byte) 0xbf;
		final byte RET_I16_OPCODE = (byte) 0xc2;
		final byte RET_OPCODE = (byte) 0xc3;
		final byte ENTER_OPCODE = (byte) 0xc8;
		final byte LEAVE_OPCODE = (byte) 0xc9;
		final byte RETF_I16_OPCODE = (byte) 0xca;
		final byte RETF_OPCODE = (byte) 0xcb;
		final byte INT3_OPCODE = (byte) 0xcc;
		final byte INT_OPCODE = (byte) 0xcd;
		final byte BAD_INTO_OPCODE = (byte) 0xce;
		final byte IRET_OPCODE = (byte) 0xcf;
		final byte BAD_AAM_OPCODE = (byte) 0xd4;
		final byte BAD_AAD_OPCODE = (byte) 0xd5;
		final byte BAD_SALC_OPCODE = (byte) 0xd6;
		final byte XLAT_OPCODE = (byte) 0xd7;
		final byte FADD_M32_OPCODE = (byte) 0xd8;
		final byte FLD_M32_OPCODE = (byte) 0xd9;
		final byte FIADD_M32_OPCODE = (byte) 0xda;
		final byte FILD_M32_OPCODE = (byte) 0xdb;
		final byte FADD_M64_OPCODE = (byte) 0xdc;
		final byte FLD_M64_OPCODE = (byte) 0xdd;
		final byte FIADD_M16_OPCODE = (byte) 0xde;
		final byte FILD_M16_OPCODE = (byte) 0xdf;
		final byte LOOPNE_OPCODE = (byte) 0xe0;
		final byte LOOPE_OPCODE = (byte) 0xe1;
		final byte LOOP_OPCODE = (byte) 0xe2;
		final byte JRCXZ_OPCODE = (byte) 0xe3;
		final byte IN_AL_OPCODE = (byte) 0xe4;
		final byte IN_EAX_OPCODE = (byte) 0xe5;
		final byte OUT_AL_OPCODE = (byte) 0xe6;
		final byte OUT_EAX_OPCODE = (byte) 0xe7;
		final byte CALL_OPCODE = (byte) 0xe8;
		final byte JMP_DISP32_OPCODE = (byte) 0xe9;
		final byte BAD_FAR_JMP_OPCODE = (byte) 0xea;
		final byte JMP_DISP8_OPCODE = (byte) 0xeb;
		final byte IN_AL_DX_OPCODE = (byte) 0xec;
		final byte IN_EAX_DX_OPCODE = (byte) 0xed;
		final byte OUT_DX_AL_OPCODE = (byte) 0xee;
		final byte OUT_DX_EAX_OPCODE = (byte) 0xef;
		final byte BAD_INT1_OPCODE = (byte) 0xf1;
		final byte HLT_OPCODE = (byte) 0xf4;
		final byte CMC_OPCODE = (byte) 0xf5;
		final byte CLC_OPCODE = (byte) 0xf8;
		final byte STC_OPCODE = (byte) 0xf9;
		final byte CLI_OPCODE = (byte) 0xfa;
		final byte STI_OPCODE = (byte) 0xfb;
		final byte CLD_OPCODE = (byte) 0xfc;
		final byte STD_OPCODE = (byte) 0xfd;

		final Opcode[] opcodeTable = {
			Opcode.ADD, Opcode.OR, Opcode.ADC, Opcode.SBB, Opcode.AND, Opcode.SUB, Opcode.XOR, Opcode.CMP
		};

		final Register[] segments = {
			SegmentRegister.ES,
			SegmentRegister.CS,
			SegmentRegister.SS,
			SegmentRegister.DS,
			SegmentRegister.FS,
			SegmentRegister.GS,
			NullRegister.getInstance(),
			NullRegister.getInstance()
		};

		return switch (opcodeFirstByte) {
			case NOP_OPCODE ->
				pref.hasOperandSizeOverridePrefix()
						? Instruction.builder()
								.opcode(Opcode.XCHG)
								.op(Register16.AX)
								.op(Register16.AX)
								.build()
						: (pref.hasRexPrefix()
								? (pref.rex().isOperand64Bit()
										? Instruction.builder()
												.opcode(Opcode.XCHG)
												.op(Register64.R8)
												.op(Register64.RAX)
												.build()
										: Instruction.builder()
												.opcode(Opcode.XCHG)
												.op(Register32.R8D)
												.op(Register32.EAX)
												.build())
								: Instruction.builder().opcode(Opcode.NOP).build());
			case RET_I16_OPCODE ->
				Instruction.builder().opcode(Opcode.RET).op(imm16(b)).build();
			case RET_OPCODE -> Instruction.builder().opcode(Opcode.RET).build();
			case RETF_I16_OPCODE ->
				Instruction.builder().opcode(Opcode.RETF).op(imm16(b)).build();
			case RETF_OPCODE -> Instruction.builder().opcode(Opcode.RETF).build();
			case ENTER_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.ENTER)
						.op(imm16(b))
						.op(imm8(b))
						.build();
			case LEAVE_OPCODE -> Instruction.builder().opcode(Opcode.LEAVE).build();
			case INT3_OPCODE -> Instruction.builder().opcode(Opcode.INT3).build();
			case INT_OPCODE ->
				Instruction.builder().opcode(Opcode.INT).op(imm8(b)).build();
			case IRET_OPCODE -> Instruction.builder().opcode(Opcode.IRET).build();
			case CDQ_OPCODE -> Instruction.builder().opcode(Opcode.CDQ).build();
			case SAHF_OPCODE -> Instruction.builder().opcode(Opcode.SAHF).build();
			case LAHF_OPCODE -> Instruction.builder().opcode(Opcode.LAHF).build();
			case HLT_OPCODE -> Instruction.builder().opcode(Opcode.HLT).build();
			case CLC_OPCODE -> Instruction.builder().opcode(Opcode.CLC).build();
			case STC_OPCODE -> Instruction.builder().opcode(Opcode.STC).build();
			case CLI_OPCODE -> Instruction.builder().opcode(Opcode.CLI).build();
			case STI_OPCODE -> Instruction.builder().opcode(Opcode.STI).build();
			case CLD_OPCODE -> Instruction.builder().opcode(Opcode.CLD).build();
			case STD_OPCODE -> Instruction.builder().opcode(Opcode.STD).build();
			case FWAIT_OPCODE -> Instruction.builder().opcode(Opcode.FWAIT).build();
			case PUSHF_OPCODE -> Instruction.builder().opcode(Opcode.PUSHF).build();
			case POPF_OPCODE -> Instruction.builder().opcode(Opcode.POPF).build();
			case CDQE_OPCODE ->
				Instruction.builder()
						.opcode(pref.rex().isOperand64Bit() ? Opcode.CDQE : Opcode.CWDE)
						.build();
			case CMC_OPCODE -> Instruction.builder().opcode(Opcode.CMC).build();
			case XLAT_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.XLAT)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RBX)
								.build())
						.build();

			case FADD_M32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FADD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build())
						.build();
			}
			case FADD_M64_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FADD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			}
			case FLD_M32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FLD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build())
						.build();
			}
			case FLD_M64_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FLD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			}
			case FIADD_M32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FIADD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build())
						.build();
			}
			case FIADD_M16_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FIADD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.WORD_PTR)
								.build())
						.build();
			}
			case FILD_M32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FILD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build())
						.build();
			}
			case FILD_M16_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.FILD)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.WORD_PTR)
								.build())
						.build();
			}
			case LOOPNE_OPCODE ->
				Instruction.builder().opcode(Opcode.LOOPNE).op(imm8(b)).build();
			case LOOPE_OPCODE ->
				Instruction.builder().opcode(Opcode.LOOPE).op(imm8(b)).build();
			case LOOP_OPCODE ->
				Instruction.builder().opcode(Opcode.LOOP).op(imm8(b)).build();
			case JRCXZ_OPCODE ->
				Instruction.builder().opcode(Opcode.JRCXZ).op(imm8(b)).build();
			case IN_AL_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.IN)
						.op(Register8.AL)
						.op(imm8(b))
						.build();
			case IN_EAX_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.IN)
						.op(Register32.EAX)
						.op(imm8(b))
						.build();
			case IN_AL_DX_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.IN)
						.op(Register8.AL)
						.op(Register16.DX)
						.build();
			case IN_EAX_DX_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.IN)
						.op(Register32.EAX)
						.op(Register16.DX)
						.build();
			case OUT_AL_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.OUT)
						.op(imm8(b))
						.op(Register8.AL)
						.build();
			case OUT_EAX_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.OUT)
						.op(imm8(b))
						.op(Register32.EAX)
						.build();
			case OUT_DX_AL_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.OUT)
						.op(Register16.DX)
						.op(Register8.AL)
						.build();
			case OUT_DX_EAX_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.OUT)
						.op(Register16.DX)
						.op(Register32.EAX)
						.build();

			case MOV_R32_M32_OPCODE -> parseRxMx(b, pref, Opcode.MOV);
			case MOV_M32_R32_OPCODE -> parseMxRx(b, pref, Opcode.MOV);
			case MOV_M16_SEG_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOV)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.WORD_PTR)
								.build())
						.op(segments[modrm.reg()])
						.build();
			}
			case MOV_SEG_M16_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOV)
						.op(segments[modrm.reg()])
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.WORD_PTR)
								.build())
						.build();
			}
			case MOV_MEM8_REG8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOV)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build())
						.op(Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()))
						.build();
			}
			case MOV_R8_M8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOV)
						.op(Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build())
						.build();
			}
			case TEST_R8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.TEST)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.BYTE_PTR)
												.build()
										: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()))
						.op(Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()))
						.build();
			}
			case TEST_R32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r2 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield Instruction.builder()
						.opcode(Opcode.TEST)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.fromSize(r2.bits()))
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.op(r2)
						.build();
			}
			case TEST_AL_IMM8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.TEST)
						.op(Register8.AL)
						.op(imm8(b))
						.build();
			case TEST_EAX_IMM32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.TEST)
						.op(getFirstRegister(pref))
						.op(pref.hasOperandSizeOverridePrefix() ? imm16(b) : imm32(b))
						.build();
			case XCHG_M8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.XCHG)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.BYTE_PTR)
												.build()
										: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()))
						.op(Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()))
						.build();
			}
			case XCHG_M32_R32_OPCODE -> parseMxRx(b, pref, Opcode.XCHG);

			// jumps
			case JMP_DISP32_OPCODE ->
				Instruction.builder().opcode(Opcode.JMP).op(imm32(b)).build();
			case JMP_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JMP).op(imm8(b)).build();
			case JB_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JB).op(imm8(b)).build();
			case JAE_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JAE).op(imm8(b)).build();
			case JE_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JE).op(imm8(b)).build();
			case JA_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JA).op(imm8(b)).build();
			case JNE_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JNE).op(imm8(b)).build();
			case JBE_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JBE).op(imm8(b)).build();
			case JS_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JS).op(imm8(b)).build();
			case JNS_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JNS).op(imm8(b)).build();
			case JP_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JP).op(imm8(b)).build();
			case JL_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JL).op(imm8(b)).build();
			case JGE_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JGE).op(imm8(b)).build();
			case JLE_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JLE).op(imm8(b)).build();
			case JG_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JG).op(imm8(b)).build();
			case JNP_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JNP).op(imm8(b)).build();
			case JO_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JO).op(imm8(b)).build();
			case JNO_DISP8_OPCODE ->
				Instruction.builder().opcode(Opcode.JNO).op(imm8(b)).build();

			case CALL_OPCODE ->
				Instruction.builder().opcode(Opcode.CALL).op(imm32(b)).build();

			case IMUL_REG_REG_IMM8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.IMUL)
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								false))
						.op(Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								false))
						.op(imm8(b))
						.build();
			}
			case PUSH_IMM32_OPCODE ->
				Instruction.builder().opcode(Opcode.PUSH).op(imm32(b)).build();
			case IMUL_R32_M32_IMM32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.IMUL)
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								false))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(
														pref.rex().isOperand64Bit()
																? PointerSize.QWORD_PTR
																: PointerSize.DWORD_PTR)
												.build()
										: Registers.fromCode(
												modrm.rm(),
												pref.rex().isOperand64Bit(),
												pref.rex().hasModRMRMExtension(),
												false))
						.op(imm32(b))
						.build();
			}
			case MOVABS_R8_S64_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.MOVABS)
						.op(Register8.AL)
						.op(new SegmentedAddress(SegmentRegister.DS, imm64(b)))
						.build();
			case MOVABS_R32_S64_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.MOVABS)
						.op(Register32.EAX)
						.op(new SegmentedAddress(SegmentRegister.DS, imm64(b)))
						.build();
			case MOVABS_S64_R8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.MOVABS)
						.op(new SegmentedAddress(SegmentRegister.DS, imm64(b)))
						.op(Register8.AL)
						.build();
			case MOVABS_S64_R32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.MOVABS)
						.op(new SegmentedAddress(SegmentRegister.DS, imm64(b)))
						.op(Register32.EAX)
						.build();
			case MOVS_M8_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.pointer(PointerSize.BYTE_PTR)
						.segment(SegmentRegister.ES)
						.base(pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI)
						.build();
				final Operand op2 = IndirectOperand.builder()
						.pointer(PointerSize.BYTE_PTR)
						.segment(SegmentRegister.DS)
						.base(pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI)
						.build();
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.MOVS).op(op1).op(op2).build();
			}
			case MOVS_M32_OPCODE -> {
				final PointerSize size =
						pref.hasOperandSizeOverridePrefix() ? PointerSize.WORD_PTR : PointerSize.DWORD_PTR;
				final Operand op1 = IndirectOperand.builder()
						.pointer(size)
						.segment(SegmentRegister.ES)
						.base(pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI)
						.build();
				final Operand op2 = IndirectOperand.builder()
						.pointer(size)
						.segment(SegmentRegister.DS)
						.base(pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI)
						.build();
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.MOVS).op(op1).op(op2).build();
			}
			case CMPS_M8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.CMPS)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RSI)
								.build())
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.ES)
								.base(Register64.RDI)
								.build())
						.build();
			case CMPS_M32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.CMPS)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RSI)
								.build())
						.op(IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.segment(SegmentRegister.ES)
								.base(Register64.RDI)
								.build())
						.build();
			case STOS_R8_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.pointer(PointerSize.BYTE_PTR)
						.segment(SegmentRegister.ES)
						.base(pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI)
						.build();
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.STOS).op(op1).op(Register8.AL).build();
			}
			case STOS_R32_OPCODE -> {
				final Operand op2 = pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX;
				final Operand op1 = IndirectOperand.builder()
						.pointer(PointerSize.fromSize(op2.bits()))
						.segment(SegmentRegister.ES)
						.base(pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI)
						.build();
				final InstructionBuilder ib = Instruction.builder();
				if (pref.p1().isPresent()) {
					ib.prefix(pref.p1().orElseThrow());
				}
				yield ib.opcode(Opcode.STOS).op(op1).op(op2).build();
			}
			case LODS_R8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.LODS)
						.op(Register8.AL)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RSI)
								.build())
						.build();
			case LODS_R32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.LODS)
						.op(Register32.EAX)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RSI)
								.build())
						.build();
			case SCAS_R8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.SCAS)
						.op(Register8.AL)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.ES)
								.base(Register64.RDI)
								.build())
						.build();
			case SCAS_R32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.SCAS)
						.op(Register32.EAX)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.segment(SegmentRegister.ES)
								.base(Register64.RDI)
								.build())
						.build();
			case MOVSXD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.MOVSXD)
						.op(Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.DWORD_PTR)
												.build()
										: Registers.fromCode(
												modrm.rm(),
												false,
												pref.rex().hasModRMRMExtension(),
												pref.hasOperandSizeOverridePrefix()))
						.build();
			}

			// OP Indirect8,R8
			case ADD_M8_R8_OPCODE,
					ADC_M8_R8_OPCODE,
					AND_M8_R8_OPCODE,
					XOR_M8_R8_OPCODE,
					OR_M8_R8_OPCODE,
					SBB_M8_R8_OPCODE,
					SUB_M8_R8_OPCODE,
					CMP_M8_R8_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (and(opcodeFirstByte, m1) != (byte) 0b00000000) {
					invalidValue();
				}
				final byte m2 = (byte) 0b00111000; // (the inverse of m1)
				final byte opcodeByte = shr(and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref, modrm);
				yield Instruction.builder()
						.opcode(opcode)
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.BYTE_PTR)
												.build()
										: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()))
						.op(Register8.fromByte(regByte, pref.hasRexPrefix()))
						.build();
			}

			// OP Indirect32,R32
			case ADD_M32_R32_OPCODE,
					ADC_M32_R32_OPCODE,
					AND_M32_R32_OPCODE,
					XOR_M32_R32_OPCODE,
					OR_M32_R32_OPCODE,
					SBB_M32_R32_OPCODE,
					SUB_M32_R32_OPCODE,
					CMP_M32_R32_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (and(opcodeFirstByte, m1) != (byte) 0b00000001) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = shr(and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				yield parseMxRx(b, pref, opcode);
			}

			// OP R8,Indirect8
			case ADD_R8_M8_OPCODE,
					ADC_R8_M8_OPCODE,
					AND_R8_M8_OPCODE,
					XOR_R8_M8_OPCODE,
					OR_R8_M8_OPCODE,
					SBB_R8_M8_OPCODE,
					SUB_R8_M8_OPCODE,
					CMP_R8_M8_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (and(opcodeFirstByte, m1) != (byte) 0b00000010) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = shr(and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref, modrm);
				yield Instruction.builder()
						.opcode(opcode)
						.op(Register8.fromByte(regByte, pref.hasRexPrefix()))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.BYTE_PTR)
												.build()
										: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()))
						.build();
			}

			// OP R32,Indirect32
			case ADD_R32_M32_OPCODE,
					ADC_R32_M32_OPCODE,
					AND_R32_M32_OPCODE,
					XOR_R32_M32_OPCODE,
					OR_R32_M32_OPCODE,
					SBB_R32_M32_OPCODE,
					SUB_R32_M32_OPCODE,
					CMP_R32_M32_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (and(opcodeFirstByte, m1) != (byte) 0b00000011) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = shr(and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				yield parseRxMx(b, pref, opcode);
			}

			// OP AL,Imm8
			case ADD_AL_IMM8_OPCODE,
					ADC_AL_IMM8_OPCODE,
					AND_AL_IMM8_OPCODE,
					XOR_AL_IMM8_OPCODE,
					OR_AL_IMM8_OPCODE,
					SBB_AL_IMM8_OPCODE,
					SUB_AL_IMM8_OPCODE,
					CMP_AL_IMM8_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (and(opcodeFirstByte, m1) != (byte) 0b00000100) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = shr(and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				yield Instruction.builder()
						.opcode(opcode)
						.op(Register8.AL)
						.op(imm8(b))
						.build();
			}

			// OP EAX,Imm32 or OP AX,Imm16
			case ADD_EAX_IMM32_OPCODE,
					ADC_EAX_IMM32_OPCODE,
					AND_EAX_IMM32_OPCODE,
					XOR_EAX_IMM32_OPCODE,
					OR_EAX_IMM32_OPCODE,
					SBB_EAX_IMM32_OPCODE,
					SUB_EAX_IMM32_OPCODE,
					CMP_EAX_IMM32_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (and(opcodeFirstByte, m1) != (byte) 0b00000101) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = shr(and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				final Register r = getFirstRegister(pref);
				final Immediate imm;
				if (pref.hasOperandSizeOverridePrefix()) {
					imm = switch (r.bits()) {
						case 16 -> imm16(b);
						case 32 -> new Immediate((int) b.read2LE());
						default -> new Immediate((long) b.read2LE());
					};
				} else {
					imm = imm32(b);
				}
				yield Instruction.builder().opcode(opcode).op(r).op(imm).build();
			}

			// MOV 8-bit
			case MOV_AL_IMM8_OPCODE,
					MOV_BL_IMM8_OPCODE,
					MOV_CL_IMM8_OPCODE,
					MOV_DL_IMM8_OPCODE,
					MOV_AH_IMM8_OPCODE,
					MOV_BH_IMM8_OPCODE,
					MOV_CH_IMM8_OPCODE,
					MOV_DH_IMM8_OPCODE -> {
				final byte regByte = and(opcodeFirstByte, OPCODE_REG_MASK);
				yield Instruction.builder()
						.opcode(Opcode.MOV)
						.op(Register8.fromByte(
								Registers.combine(pref.rex().hasOpcodeRegExtension(), regByte), pref.hasRexPrefix()))
						.op(imm8(b))
						.build();
			}

			// MOV 16, 32 or 64 bits
			case MOV_EAX_IMM32_OPCODE,
					MOV_EBX_IMM32_OPCODE,
					MOV_ECX_IMM32_OPCODE,
					MOV_EDX_IMM32_OPCODE,
					MOV_ESP_IMM32_OPCODE,
					MOV_EBP_IMM32_OPCODE,
					MOV_ESI_IMM32_OPCODE,
					MOV_EDI_IMM32_OPCODE -> {
				final byte regByte =
						Registers.combine(pref.rex().hasOpcodeRegExtension(), and(opcodeFirstByte, OPCODE_REG_MASK));
				final int size =
						pref.hasOperandSizeOverridePrefix() ? 16 : pref.rex().isOperand64Bit() ? 64 : 32;
				final Register r =
						switch (size) {
							case 16 -> Register16.fromByte(regByte);
							case 32 -> Register32.fromByte(regByte);
							case 64 -> Register64.fromByte(regByte);
							default -> {
								invalidValue();
								yield null;
							}
						};
				final Immediate imm =
						switch (size) {
							case 16 -> imm16(b);
							case 32 -> imm32(b);
							case 64 -> imm64(b);
							default -> {
								invalidValue();
								yield null;
							}
						};
				yield Instruction.builder()
						.opcode(pref.rex().isOperand64Bit() ? Opcode.MOVABS : Opcode.MOV)
						.op(r)
						.op(imm)
						.build();
			}

			// XCHG
			case XCHG_EBX_EAX_OPCODE,
					XCHG_ECX_EAX_OPCODE,
					XCHG_EDX_EAX_OPCODE,
					XCHG_ESI_EAX_OPCODE,
					XCHG_EDI_EAX_OPCODE,
					XCHG_ESP_EAX_OPCODE,
					XCHG_EBP_EAX_OPCODE -> {
				final byte regByte = and(opcodeFirstByte, OPCODE_REG_MASK);
				yield Instruction.builder()
						.opcode(Opcode.XCHG)
						.op(Registers.fromCode(
								regByte,
								pref.rex().isOperand64Bit(),
								pref.rex().hasOpcodeRegExtension(),
								pref.hasOperandSizeOverridePrefix()))
						.op(getFirstRegister(pref))
						.build();
			}

			// PUSH 16/64-bit
			case PUSH_EAX_OPCODE,
					PUSH_EBX_OPCODE,
					PUSH_ECX_OPCODE,
					PUSH_EDX_OPCODE,
					PUSH_ESI_OPCODE,
					PUSH_EDI_OPCODE,
					PUSH_ESP_OPCODE,
					PUSH_EBP_OPCODE -> {
				final byte regByte = and(opcodeFirstByte, OPCODE_REG_MASK);
				yield Instruction.builder()
						.opcode(Opcode.PUSH)
						.op(Registers.fromCode(
								regByte, true, pref.rex().hasOpcodeRegExtension(), pref.hasOperandSizeOverridePrefix()))
						.build();
			}

			case PUSH_IMM8_OPCODE ->
				Instruction.builder().opcode(Opcode.PUSH).op(imm8(b)).build();

			// POP 16/64-bit
			case POP_EAX_OPCODE,
					POP_EBX_OPCODE,
					POP_ECX_OPCODE,
					POP_EDX_OPCODE,
					POP_ESI_OPCODE,
					POP_EDI_OPCODE,
					POP_ESP_OPCODE,
					POP_EBP_OPCODE -> {
				final byte regByte = and(opcodeFirstByte, OPCODE_REG_MASK);
				yield Instruction.builder()
						.opcode(Opcode.POP)
						.op(Registers.fromCode(
								regByte, true, pref.rex().hasOpcodeRegExtension(), pref.hasOperandSizeOverridePrefix()))
						.build();
			}
			case POP_M64_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.POP)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			}

			case LEA_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r = Registers.fromCode(
						getByteFromReg(pref, modrm),
						pref.rex().isOperand64Bit(),
						pref.rex().b(),
						pref.hasOperandSizeOverridePrefix());
				yield Instruction.builder()
						.opcode(Opcode.LEA)
						.op(r)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.fromSize(r.bits()))
								.build())
						.build();
			}
			case INS_M8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.INS)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.ES)
								.base(Register64.RDI)
								.build())
						.op(Register16.DX)
						.build();
			case INS_M32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.INS)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.segment(SegmentRegister.ES)
								.base(Register64.RDI)
								.build())
						.op(Register16.DX)
						.build();
			case OUTS_M8_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.OUTS)
						.op(Register16.DX)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.BYTE_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RSI)
								.build())
						.build();
			case OUTS_M32_OPCODE ->
				Instruction.builder()
						.opcode(Opcode.OUTS)
						.op(Register16.DX)
						.op(IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.segment(SegmentRegister.DS)
								.base(Register64.RSI)
								.build())
						.build();

			case BAD_PUSH_ES_OPCODE -> throw new InvalidLegacyOpcode(BAD_PUSH_ES_OPCODE, "push es");
			case BAD_POP_ES_OPCODE -> throw new InvalidLegacyOpcode(BAD_POP_ES_OPCODE, "pop es");
			case BAD_PUSH_CS_OPCODE -> throw new InvalidLegacyOpcode(BAD_PUSH_CS_OPCODE, "push cs");
			case BAD_PUSH_SS_OPCODE -> throw new InvalidLegacyOpcode(BAD_PUSH_SS_OPCODE, "push ss");
			case BAD_POP_SS_OPCODE -> throw new InvalidLegacyOpcode(BAD_POP_SS_OPCODE, "pop ss");
			case BAD_PUSH_DS_OPCODE -> throw new InvalidLegacyOpcode(BAD_PUSH_DS_OPCODE, "push ds");
			case BAD_POP_DS_OPCODE -> throw new InvalidLegacyOpcode(BAD_POP_DS_OPCODE, "pop ds");
			case BAD_DAA_OPCODE -> throw new InvalidLegacyOpcode(BAD_DAA_OPCODE, "daa");
			case BAD_DAS_OPCODE -> throw new InvalidLegacyOpcode(BAD_DAS_OPCODE, "das");
			case BAD_AAA_OPCODE -> throw new InvalidLegacyOpcode(BAD_AAA_OPCODE, "aaa");
			case BAD_AAS_OPCODE -> throw new InvalidLegacyOpcode(BAD_AAS_OPCODE, "aas");
			case BAD_PUSHAD_OPCODE -> throw new InvalidLegacyOpcode(BAD_PUSHAD_OPCODE, "pushad");
			case BAD_POPAD_OPCODE -> throw new InvalidLegacyOpcode(BAD_POPAD_OPCODE, "popad");
			// Does this error make sense in an emulator?
			case FAR_CALL_OPCODE ->
				throw new DecodingException("Far call to different segments (byte 0x9a) is not allowed.");
			case UNDEFINED_OLD_MOV_OPCODE -> throw new DecodingException("Byte 0x82 is undefined in x86_64.");
			case BAD_INTO_OPCODE -> throw new InvalidLegacyOpcode(BAD_INTO_OPCODE, "into");
			case BAD_AAM_OPCODE -> throw new InvalidLegacyOpcode(BAD_AAM_OPCODE, "aam");
			case BAD_AAD_OPCODE -> throw new InvalidLegacyOpcode(BAD_AAD_OPCODE, "aad");
			case BAD_SALC_OPCODE -> throw new InvalidLegacyOpcode(BAD_SALC_OPCODE, "salc");
			// Does this error make sense in an emulator?
			case BAD_FAR_JMP_OPCODE ->
				throw new DecodingException("Far call to explicit segment (byte 0xea) is not allowed.");
			case BAD_INT1_OPCODE -> throw new InvalidLegacyOpcode(BAD_INT1_OPCODE, "int1");
			case (byte) 0x0f -> throw new UnrecognizedPrefix("double byte", b.getPosition());
			case (byte) 0xc5 -> throw new UnrecognizedPrefix("VEX2", b.getPosition());
			case (byte) 0xc4 -> throw new UnrecognizedPrefix("VEX3", b.getPosition());
			case (byte) 0x62 -> throw new UnrecognizedPrefix("EVEX", b.getPosition());
			case OPERAND_SIZE_OVERRIDE_PREFIX -> throw new UnrecognizedPrefix("operand size override", b.getPosition());
			case ADDRESS_SIZE_OVERRIDE_PREFIX -> throw new UnrecognizedPrefix("address size override", b.getPosition());
			case (byte) 0xf0 -> throw new UnrecognizedPrefix("LOCK", b.getPosition());
			case (byte) 0xf2 -> throw new UnrecognizedPrefix("REPNE", b.getPosition());
			case (byte) 0xf3 -> throw new UnrecognizedPrefix("REP", b.getPosition());
			default -> {
				final long pos = b.getPosition();
				logger.debug("Unknown opcode: 0x%02x.", opcodeFirstByte);
				b.setPosition(pos);
				throw new UnknownOpcode(opcodeFirstByte);
			}
		};
	}

	private static byte getByteFromV(final Vex2Prefix vex2) {
		return and(not(vex2.v()), (byte) 0b00001111);
	}

	private static byte getByteFromV(final Vex3Prefix vex3) {
		return and(not(vex3.v()), (byte) 0b00001111);
	}

	private static byte getByteFromV(final EvexPrefix evex) {
		return and(not(evex.v()), (byte) 0b00001111);
	}

	private static Instruction parseVex2Opcodes(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte KUNPCKBW_OPCODE = (byte) 0x4b;
		final byte VPCMPGTB_OPCODE = (byte) 0x64;
		final byte VMOVD_OPCODE = (byte) 0x6e;
		final byte VMOVDQU_RYMM_M256_OPCODE = (byte) 0x6f;
		final byte VPSxLDQ_OPCODE = (byte) 0x73;
		final byte VPCMPEQB_OPCODE = (byte) 0x74;
		final byte VPCMPEQD_OPCODE = (byte) 0x76;
		final byte VZEROALL_OPCODE = (byte) 0x77;
		final byte VMOVQ_RXMM_M64_OPCODE = (byte) 0x7e;
		final byte VMOVDQU_M256_RYMM_OPCODE = (byte) 0x7f;
		final byte KMOVD_RK_R32_OPCODE = (byte) 0x92;
		final byte KMOVD_R32_RK_OPCODE = (byte) 0x93;
		final byte VMOVQ_M64_RXMM_OPCODE = (byte) 0xd6;
		final byte VPMOVMSKB_OPCODE = (byte) 0xd7;
		final byte VPMINUB_OPCODE = (byte) 0xda;
		final byte VPAND_OPCODE = (byte) 0xdb;
		final byte VPANDN_OPCODE = (byte) 0xdf;
		final byte VMOVNTDQ_OPCODE = (byte) 0xe7;
		final byte VPOR_OPCODE = (byte) 0xeb;
		final byte VPXOR_OPCODE = (byte) 0xef;
		final byte VPSUBB_OPCODE = (byte) 0xf8;

		final Vex2Prefix vex2 = pref.vex2().orElseThrow();

		return switch (opcodeFirstByte) {
			case VZEROALL_OPCODE ->
				Instruction.builder().opcode(Opcode.VZEROALL).build();
			case VPCMPGTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPCMPGTB)
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex2)))
						.op(RegisterXMM.fromByte(modrm.rm()))
						.build();
			}
			case VPXOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPXOR)
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex2)))
						.op(RegisterXMM.fromByte(modrm.rm()))
						.build();
			}
			case VPOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPOR)
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex2)))
						.op(RegisterYMM.fromByte(modrm.rm()))
						.build();
			}
			case VPAND_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPAND)
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex2)))
						.op(RegisterYMM.fromByte(modrm.rm()))
						.build();
			}
			case VPANDN_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPANDN)
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex2)))
						.op(RegisterXMM.fromByte(modrm.rm()))
						.build();
			}
			case VPSUBB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPSUBB)
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex2)))
						.op(RegisterXMM.fromByte(modrm.rm()))
						.build();
			}
			case VPMINUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPMINUB)
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex2)))
						.op(getYMMArgument(b, modrm, pref, modrm.rm()))
						.build();
			}
			case VMOVNTDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVNTDQ)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.build();
			}
			case VMOVDQU_RYMM_M256_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVDQU)
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VMOVDQU_M256_RYMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVDQU)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.build();
			}
			case VMOVD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVD)
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build())
						.build();
			}
			case VMOVQ_RXMM_M64_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVQ)
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.build();
			}
			case VMOVQ_M64_RXMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVQ)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build())
						.op(RegisterXMM.fromByte(getByteFromReg(vex2, modrm)))
						.build();
			}
			case VPMOVMSKB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPMOVMSKB)
						.op(Register32.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterYMM.fromByte(modrm.rm()))
						.build();
			}
			case VPCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPCMPEQB)
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex2)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VPCMPEQD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPCMPEQD)
						.op(RegisterYMM.fromByte(getByteFromReg(vex2, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex2)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VPSxLDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(modrm.reg() == (byte) 0b111 ? Opcode.VPSLLDQ : Opcode.VPSRLDQ)
						.op(RegisterXMM.fromByte(getByteFromV(vex2)))
						.op(RegisterXMM.fromByte(modrm.rm()))
						.op(imm8(b))
						.build();
			}
			case KMOVD_RK_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KMOVD)
						.op(MaskRegister.fromByte(modrm.reg()))
						.op(Register32.fromByte(getByteFromRM(pref, modrm)))
						.build();
			}
			case KMOVD_R32_RK_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KMOVD)
						.op(Register32.fromByte(getByteFromReg(pref, modrm)))
						.op(MaskRegister.fromByte(modrm.rm()))
						.build();
			}
			case KUNPCKBW_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KUNPCKBW)
						.op(MaskRegister.fromByte(getByteFromReg(vex2, modrm)))
						.op(MaskRegister.fromByte(getByteFromV(vex2)))
						.op(MaskRegister.fromByte(modrm.rm()))
						.build();
			}
			default -> {
				final long pos = b.getPosition();
				logger.debug("Unknown opcode: 0x%02x.", opcodeFirstByte);
				b.setPosition(pos);
				throw new UnknownOpcode(opcodeFirstByte);
			}
		};
	}

	private static Instruction parseVex3Opcodes(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte VPSHUFB_OPCODE = (byte) 0x00;
		final byte VPALIGNR_OPCODE = (byte) 0x0f;
		final byte VPCMPEQQ_OPCODE = (byte) 0x29;
		final byte VPMINUD_OPCODE = (byte) 0x3b;
		final byte KORD_OPCODE = (byte) 0x45;
		final byte KUNPCKDQ_OPCODE = (byte) 0x4b;
		final byte VPBROADCASTD_OPCODE = (byte) 0x58;
		final byte VPCMPISTRI_OPCODE = (byte) 0x63;
		final byte VMOVDQU_RYMM_M256_OPCODE = (byte) 0x6f;
		final byte VPCMPEQB_OPCODE = (byte) 0x74;
		final byte VMOVQ_OPCODE = (byte) 0x7e;
		final byte VPBROADCASTB_OPCODE = (byte) 0x78;
		final byte KMOVQ_RK_R64_OPCODE = (byte) 0x92;
		final byte KMOVQ_R64_RK_OPCODE = (byte) 0x93;
		final byte KORTESTD_OPCODE = (byte) 0x98;
		final byte VPANDN_OPCODE = (byte) 0xdf;
		final byte VMOVDQU_M256_RYMM_OPCODE = (byte) 0x7f;
		final byte VPMOVMSKB_OPCODE = (byte) 0xd7;
		final byte VPXOR_OPCODE = (byte) 0xef;
		final byte BZHI_OPCODE = (byte) 0xf5;
		final byte SARX_OPCODE = (byte) 0xf7;

		final Vex3Prefix vex3 = pref.vex3().orElseThrow();

		return switch (opcodeFirstByte) {
			case VMOVQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVQ)
						.op(Register64.fromByte(getByteFromRM(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.build();
			}
			case KMOVQ_RK_R64_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KMOVQ)
						.op(MaskRegister.fromByte(modrm.reg()))
						.op(Register64.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case KMOVQ_R64_RK_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KMOVQ)
						.op(Register64.fromByte(getByteFromReg(vex3, modrm)))
						.op(MaskRegister.fromByte(modrm.rm()))
						.build();
			}
			case VPSHUFB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPSHUFB)
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex3)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case VPALIGNR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPALIGNR)
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex3)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.op(imm8(b))
						.build();
			}
			case VPCMPISTRI_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPCMPISTRI)
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.op(imm8(b))
						.build();
			}
			case VMOVDQU_RYMM_M256_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVDQU)
						.op(RegisterYMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VMOVDQU_M256_RYMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVDQU)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.op(RegisterYMM.fromByte(getByteFromReg(vex3, modrm)))
						.build();
			}
			case VPCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPCMPEQB)
						.op(RegisterYMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex3)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VPCMPEQQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPCMPEQQ)
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex3)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build())
						.build();
			}
			case VPMINUD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPMINUD)
						.op(RegisterYMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterYMM.fromByte(getByteFromV(vex3)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VPBROADCASTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPBROADCASTB)
						.op(RegisterYMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case VPBROADCASTD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPBROADCASTD)
						.op(RegisterYMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case SARX_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.SARX)
						.op(Register32.fromByte(getByteFromReg(vex3, modrm)))
						.op(Register32.fromByte(getByteFromRM(vex3, modrm)))
						.op(Register32.fromByte(getByteFromV(vex3)))
						.build();
			}
			case BZHI_OPCODE -> {
				final ModRM modrm = modrm(b);
				if (vex3.w()) {
					yield Instruction.builder()
							.opcode(Opcode.BZHI)
							.op(Register64.fromByte(getByteFromReg(vex3, modrm)))
							.op(Register64.fromByte(getByteFromRM(vex3, modrm)))
							.op(Register64.fromByte(getByteFromV(vex3)))
							.build();
				} else {
					yield Instruction.builder()
							.opcode(Opcode.BZHI)
							.op(Register32.fromByte(getByteFromReg(vex3, modrm)))
							.op(Register32.fromByte(getByteFromRM(vex3, modrm)))
							.op(Register32.fromByte(getByteFromV(vex3)))
							.build();
				}
			}
			case VPANDN_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPANDN)
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex3)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case VPXOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPXOR)
						.op(RegisterXMM.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromV(vex3)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case KORTESTD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.KORTESTD, MaskRegister.fromByte(modrm.reg()), MaskRegister.fromByte(modrm.rm()));
			}
			case KORD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KORD)
						.op(MaskRegister.fromByte(getByteFromReg(vex3, modrm)))
						.op(MaskRegister.fromByte(getByteFromV(vex3)))
						.op(MaskRegister.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case KUNPCKDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.KUNPCKDQ)
						.op(MaskRegister.fromByte(getByteFromReg(vex3, modrm)))
						.op(MaskRegister.fromByte(getByteFromV(vex3)))
						.op(MaskRegister.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			case VPMOVMSKB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPMOVMSKB)
						.op(Register32.fromByte(getByteFromReg(vex3, modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(vex3, modrm)))
						.build();
			}
			default -> {
				final long pos = b.getPosition();
				logger.debug("Unknown opcode: 0x%02x.", opcodeFirstByte);
				b.setPosition(pos);
				throw new UnknownOpcode(opcodeFirstByte);
			}
		};
	}

	private static Instruction parseEvexOpcodes(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte VMOVUPS_R512_M512_OPCODE = (byte) 0x10;
		final byte VMOVUPS_M512_R512_OPCODE = (byte) 0x11;
		final byte VBROADCASTSS_OPCODE = (byte) 0x18;
		final byte VPCMPEQD_OPCODE = (byte) 0x1f;
		final byte VPTERNLOGD_OPCODE = (byte) 0x25;
		final byte VPTESTMB_OPCODE = (byte) 0x26;
		final byte VMOVAPS_OPCODE = (byte) 0x29;
		final byte VPMINUD_OPCODE = (byte) 0x3b;
		final byte VPCMPNEQUB_OPCODE = (byte) 0x3e;
		final byte VPCMPEQB_OPCODE = (byte) 0x3f;
		final byte VMOVQ_RX_M128_OPCODE = (byte) 0x6e;
		final byte VMOVDQU64_RZMM_M512_OPCODE = (byte) 0x6f;
		final byte VPBROADCASTB_OPCODE = (byte) 0x7a;
		final byte VPBROADCASTD_OPCODE = (byte) 0x7c;
		final byte VMOVQ_R64_RX_OPCODE = (byte) 0x7e;
		final byte VMOVDQU64_M512_RZMM_OPCODE = (byte) 0x7f;
		final byte VPMINUB_OPCODE = (byte) 0xda;
		final byte VMOVNTDQ_OPCODE = (byte) 0xe7;
		final byte VPORQ_OPCODE = (byte) 0xeb;
		final byte VPXORQ_OPCODE = (byte) 0xef;

		final EvexPrefix evex = pref.evex().orElseThrow();

		return switch (opcodeFirstByte) {
			case VMOVUPS_R512_M512_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVUPS)
						.op(RegisterZMM.fromByte(getByteFromReg(evex, modrm)))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build())
						.build();
			}
			case VMOVUPS_M512_R512_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVUPS)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build())
						.op(RegisterZMM.fromByte(getByteFromReg(evex, modrm)))
						.build();
			}
			case VBROADCASTSS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VBROADCASTSS)
						.op(RegisterZMM.fromByte(getByteFromReg(evex, modrm)))
						.op(RegisterXMM.fromByte(getByteFromRM(evex, modrm)))
						.build();
			}
			case VPBROADCASTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPBROADCASTB)
						.op(getZmmFromReg(evex, modrm))
						.op(Register32.fromByte(getByteFromRM(evex, modrm)))
						.build();
			}
			case VPBROADCASTD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPBROADCASTD)
						.op(getZmmFromReg(evex, modrm))
						.op(Register32.fromByte(getByteFromRM(evex, modrm)))
						.build();
			}
			case VMOVAPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVAPS)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build())
						.op(RegisterZMM.fromByte(getByteFromReg(evex, modrm)))
						.build();
			}
			case VMOVDQU64_RZMM_M512_OPCODE -> {
				final ModRM modrm = modrm(b);
				final InstructionBuilder ib = Instruction.builder();
				if (evex.a() != (byte) 0) {
					ib.mask(MaskRegister.fromByte(evex.a()));
				}
				yield ib.opcode(evex.w() ? Opcode.VMOVDQU64 : Opcode.VMOVDQU8)
						.op(getZmmFromReg(evex, modrm))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build())
						.build();
			}
			case VMOVDQU64_M512_RZMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				final InstructionBuilder ib = Instruction.builder();
				if (evex.a() != (byte) 0) {
					ib.mask(MaskRegister.fromByte(evex.a()));
				}
				yield ib.opcode(evex.w() ? Opcode.VMOVDQU64 : Opcode.VMOVDQU8)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build())
						.op(getZmmFromReg(evex, modrm))
						.build();
			}
			case VMOVNTDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VMOVNTDQ)
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build())
						.op(RegisterZMM.fromByte(getByteFromReg(evex, modrm)))
						.build();
			}
			case VMOVQ_R64_RX_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.VMOVQ,
						Register64.fromByte(getByteFromRM(evex, modrm)),
						RegisterXMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))));
			}
			case VMOVQ_RX_M128_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.VMOVQ,
						RegisterXMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
			}
			case VPCMPNEQUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final InstructionBuilder ib = Instruction.builder();
				if (evex.a() != (byte) 0) {
					ib.mask(MaskRegister.fromByte(evex.a()));
				}
				final byte r2 = or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex));
				final Instruction tmp = ib.opcode(Opcode.VPCMPNEQUB)
						.op(MaskRegister.fromByte(modrm.reg()))
						.op(evex.l() ? RegisterYMM.fromByte(r2) : RegisterXMM.fromByte(r2))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(evex.l() ? PointerSize.YMMWORD_PTR : PointerSize.XMMWORD_PTR)
								.build())
						.build();
				// For some unknown (to me) reason, after VPCMPNEQUB instructions there is an extra 0x04 byte which is
				// not needed... why?
				b.read1();
				yield tmp;
			}
			case VPCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex));
				final byte r2 = or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm));
				final InstructionBuilder ib = Instruction.builder()
						.op(MaskRegister.fromByte(getByteFromReg(evex, modrm)))
						.op(evex.l() ? RegisterYMM.fromByte(r1) : RegisterXMM.fromByte(r1))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(evex.l() ? PointerSize.YMMWORD_PTR : PointerSize.XMMWORD_PTR)
												.build()
										: (evex.l() ? RegisterYMM.fromByte(r2) : RegisterXMM.fromByte(r2)));
				if (evex.a() != (byte) 0) {
					ib.mask(MaskRegister.fromByte(evex.a()));
				}
				final byte nextByte = b.read1();
				switch (nextByte) {
					case (byte) 0x00 -> ib.opcode(Opcode.VPCMPEQB);
					case (byte) 0x04 -> ib.opcode(Opcode.VPCMPNEQB);
					default ->
						throw new IllegalArgumentException(
								String.format("Unknown opcode corresponding to byte 0x%02x.", nextByte));
				}
				yield ib.build();
			}
			case VPCMPEQD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex));
				final byte r2 = or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm));
				final Instruction tmp = Instruction.builder()
						.opcode(Opcode.VPCMPEQD)
						.op(MaskRegister.fromByte(getByteFromReg(evex, modrm)))
						.op(evex.l() ? RegisterYMM.fromByte(r1) : RegisterXMM.fromByte(r1))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(evex.l() ? PointerSize.YMMWORD_PTR : PointerSize.XMMWORD_PTR)
												.build()
										: (evex.l() ? RegisterYMM.fromByte(r2) : RegisterXMM.fromByte(r2)))
						.build();
				b.read1();
				yield tmp;
			}
			case VPXORQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPXORQ)
						.op(RegisterYMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))))
						.op(RegisterYMM.fromByte(or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex))))
						.op(parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build())
						.build();
			}
			case VPTERNLOGD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPTERNLOGD)
						.op(RegisterYMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))))
						.op(RegisterYMM.fromByte(or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex))))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.YMMWORD_PTR)
												.build()
										: RegisterYMM.fromByte(
												or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm))))
						.op(imm8(b))
						.build();
			}
			case VPTESTMB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield Instruction.builder()
						.opcode(Opcode.VPTESTMB)
						.op(MaskRegister.fromByte(getByteFromReg(evex, modrm)))
						.op(RegisterYMM.fromByte(or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex))))
						.op(RegisterYMM.fromByte(or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm))))
						.build();
			}
			case VPMINUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.VPMINUB,
						RegisterYMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))),
						RegisterYMM.fromByte(or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex))),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.YMMWORD_PTR)
										.build()
								: RegisterYMM.fromByte(
										or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm))));
			}
			case VPMINUD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final InstructionBuilder ib = Instruction.builder()
						.opcode(Opcode.VPMINUD)
						.op(RegisterYMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))))
						.op(RegisterYMM.fromByte(or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex))))
						.op(
								isIndirectOperandNeeded(modrm)
										? parseIndirectOperand(b, pref, modrm)
												.pointer(PointerSize.YMMWORD_PTR)
												.build()
										: RegisterYMM.fromByte(
												or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm))));
				if (evex.a() != (byte) 0b000) {
					ib.mask(MaskRegister.fromByte(evex.a()));
				}
				if (evex.z()) {
					ib.maskZero();
				}
				yield ib.build();
			}
			case VPORQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new GeneralInstruction(
						Opcode.VPORQ,
						RegisterYMM.fromByte(or(evex.r1() ? 0 : (byte) 0b00010000, getByteFromReg(evex, modrm))),
						RegisterYMM.fromByte(or(evex.v1() ? 0 : (byte) 0b00010000, getByteFromV(evex))),
						RegisterYMM.fromByte(or(evex.x() ? 0 : (byte) 0b00010000, getByteFromRM(evex, modrm))));
			}
			default -> {
				final long pos = b.getPosition();
				logger.debug("Unknown opcode: 0x%02x.", opcodeFirstByte);
				b.setPosition(pos);
				throw new UnknownOpcode(opcodeFirstByte);
			}
		};
	}

	private static RegisterZMM getZmmFromReg(final EvexPrefix evex, final ModRM modrm) {
		return RegisterZMM.fromByte(or(getByteFromReg(evex, modrm), evex.r1() ? 0 : (byte) 0b00010000));
	}

	private static void invalidValue() {
		throw new IllegalArgumentException("Invalid value.");
	}

	private static Prefixes parsePrefixes(final ReadOnlyByteBuffer b) {
		Optional<InstructionPrefix> p1 = Optional.empty(); // Legacy Prefix Group 1
		Optional<Byte> p2 = Optional.empty(); // Legacy Prefix Group 2
		boolean hasOperandSizeOverridePrefix = false;
		boolean hasAddressSizeOverridePrefix = false;

		// FIXME: is there a better way to do this?
		// (technically there is no limit to the number of prefixes an x86 instruction can have)
		while (true) {
			final byte x = b.read1();

			if (isLegacyPrefixGroup1(x)) {
				p1 = Optional.of(InstructionPrefix.fromByte(x));
			} else if (isLegacyPrefixGroup2(x)) {
				p2 = Optional.of(x);
			} else if (isOperandSizeOverridePrefix(x)) {
				hasOperandSizeOverridePrefix = true;
			} else if (isAddressSizeOverridePrefix(x)) {
				hasAddressSizeOverridePrefix = true;
			} else {
				// This byte is not a prefix, go back 1 byte and start parsing
				b.setPosition(b.getPosition() - 1);
				break;
			}
		}

		final byte rexByte = b.read1();
		final RexPrefix rexPrefix;
		final boolean isREX = RexPrefix.isREXPrefix(rexByte);
		if (isREX) {
			rexPrefix = RexPrefix.of(rexByte);
		} else {
			rexPrefix = RexPrefix.of((byte) 0x40);
			b.setPosition(b.getPosition() - 1);
		}

		final Optional<Vex2Prefix> vex2;
		{
			final byte vex2Byte = b.read1();
			if (Vex2Prefix.isVEX2Prefix(vex2Byte)) {
				vex2 = Optional.of(Vex2Prefix.of(b.read1()));
			} else {
				b.setPosition(b.getPosition() - 1);
				vex2 = Optional.empty();
			}
		}

		final Optional<Vex3Prefix> vex3;
		{
			final byte vex3Byte = b.read1();
			if (Vex3Prefix.isVEX3Prefix(vex3Byte)) {
				vex3 = Optional.of(Vex3Prefix.of(b.read1(), b.read1()));
			} else {
				b.setPosition(b.getPosition() - 1);
				vex3 = Optional.empty();
			}
		}

		final Optional<EvexPrefix> evex;
		{
			final byte evexByte = b.read1();
			if (EvexPrefix.isEvexPrefix(evexByte)) {
				evex = Optional.of(EvexPrefix.of(b.read1(), b.read1(), b.read1()));
			} else {
				b.setPosition(b.getPosition() - 1);
				evex = Optional.empty();
			}
		}

		// Check that the combination of prefixes is valid
		// FIXME: is this correct?
		{
			final boolean hasNormalPrefix =
					p1.isPresent() || p2.isPresent() || hasAddressSizeOverridePrefix || hasOperandSizeOverridePrefix;
			int count = 0;
			if (hasNormalPrefix) {
				count++;
			}
			if (vex2.isPresent()) {
				count++;
			}
			if (vex3.isPresent()) {
				count++;
			}
			if (evex.isPresent()) {
				count++;
			}
			if (count >= 3) {
				throw new IllegalArgumentException("Illegal combination of prefixes.");
			}
		}

		return new Prefixes(
				p1, p2, hasOperandSizeOverridePrefix, hasAddressSizeOverridePrefix, isREX, rexPrefix, vex2, vex3, evex);
	}

	/** Parses an instruction like OP IndirectXX,RXX (where XX can be 16, 32 or 64) */
	private static Instruction parseMxRx(final ReadOnlyByteBuffer b, final Prefixes pref, final Opcode opcode) {
		final ModRM modrm = modrm(b);
		final Register r2 = Registers.fromCode(
				modrm.reg(),
				pref.rex().isOperand64Bit(),
				pref.rex().hasModRMRegExtension(),
				pref.hasOperandSizeOverridePrefix());
		return Instruction.builder()
				.opcode(opcode)
				.op(
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.fromSize(r2.bits()))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()))
				.op(r2)
				.build();
	}

	/** Parses an instruction like OP RXX,IndirectXX (where XX can be 16, 32 or 64) */
	private static Instruction parseRxMx(final ReadOnlyByteBuffer b, final Prefixes pref, final Opcode opcode) {
		final ModRM modrm = modrm(b);
		final boolean isIndirectOperandNeeded = isIndirectOperandNeeded(modrm);
		final Register r1 = Registers.fromCode(
				modrm.reg(),
				pref.rex().isOperand64Bit(),
				pref.rex().hasModRMRegExtension(),
				pref.hasOperandSizeOverridePrefix());
		return Instruction.builder()
				.opcode(opcode)
				.op(r1)
				.op(
						isIndirectOperandNeeded
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.fromSize(r1.bits()))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()))
				.build();
	}

	private static boolean isBP(final Register r) {
		return r == Register32.EBP || r == Register32.R13D || r == Register64.RBP || r == Register64.R13;
	}

	private static boolean isSP(final Register r) {
		return r == Register32.ESP || r == Register64.RSP;
	}

	private static IndirectOperandBuilder parseIndirectOperand(
			final ReadOnlyByteBuffer b, final Prefixes pref, final ModRM modrm) {
		final boolean baseRegisterExtension = (pref.hasRexPrefix() && pref.rex().hasSIBBaseExtension())
				|| (pref.vex3().isPresent() && !pref.vex3().orElseThrow().b())
				|| (pref.evex().isPresent() && !pref.evex().orElseThrow().b());
		Register baseRegister =
				Registers.fromCode(modrm.rm(), !pref.hasAddressSizeOverridePrefix(), baseRegisterExtension, false);
		final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
		final IndirectOperandBuilder iob = IndirectOperand.builder();
		final SIB sib;
		if (isIndirectOperandNeeded(modrm) && modrm.rm() == 0b100) {
			// SIB needed
			sib = sib(b);

			final boolean sibIndexExtension = (pref.hasRexPrefix() && pref.rex().hasSIBIndexExtension())
					|| (pref.vex3().isPresent() && !pref.vex3().orElseThrow().x())
					|| (pref.evex().isPresent() && !pref.evex().orElseThrow().x());

			final Register decodedBase =
					Registers.fromCode(sib.base(), !hasAddressSizeOverridePrefix, baseRegisterExtension, false);
			final Register decodedIndex =
					Registers.fromCode(sib.index(), !hasAddressSizeOverridePrefix, sibIndexExtension, false);

			// ESP or RSP cannot be index registers of an indirect operand
			if (isSP(decodedIndex)) {
				baseRegister = decodedBase;
			} else {
				iob.index(decodedIndex);
				iob.scale(1 << asInt(sib.scale()));

				baseRegister = (isBP(decodedBase) && modrm.mod() == (byte) 0b00) ? null : decodedBase;
			}
		} else {
			// SIB not needed
			sib = new SIB((byte) 0x00);
			if (modrm.mod() == (byte) 0b00 && isBP(baseRegister)) {
				baseRegister = hasAddressSizeOverridePrefix ? Register32.EIP : Register64.RIP;
			}
		}

		if (baseRegister != null) {
			if (pref.p2().isPresent() && pref.p2().orElseThrow() == CS_SEGMENT_OVERRIDE_PREFIX) {
				iob.segment(SegmentRegister.CS);
			}
			iob.base(baseRegister);
		}

		if (isIndirectOperandNeeded(modrm)) {
			// indirect operand needed
			if ((modrm.mod() == (byte) 0b00 && modrm.rm() == (byte) 0b101)
					|| (modrm.mod() == (byte) 0b00 && sib.base() == (byte) 0b101)
					|| modrm.mod() == (byte) 0b10) {
				final int disp32 = b.read4LE();
				iob.displacement(disp32);
			} else if (modrm.mod() == (byte) 0b01) {
				final byte disp8 = b.read1();
				iob.displacement(disp8);
			}
		}

		return iob;
	}

	private static ModRM modrm(final ReadOnlyByteBuffer b) {
		final byte m = b.read1();
		return new ModRM(m);
	}

	private static SIB sib(final ReadOnlyByteBuffer b) {
		final byte s = b.read1();
		return new SIB(s);
	}

	private static Immediate imm8(final ReadOnlyByteBuffer b) {
		return new Immediate(b.read1());
	}

	private static Immediate imm16(final ReadOnlyByteBuffer b) {
		return new Immediate(b.read2LE());
	}

	private static Immediate imm32(final ReadOnlyByteBuffer b) {
		return new Immediate(b.read4LE());
	}

	private static Immediate imm64(final ReadOnlyByteBuffer b) {
		return new Immediate(b.read8LE());
	}

	// TODO: remove when not used anymore
	private static void notImplemented() {
		throw new UnsupportedOperationException("Not implemented");
	}

	private static boolean isLegacyPrefixGroup1(final byte prefix) {
		return prefix == InstructionPrefix.LOCK.getCode()
				|| prefix == InstructionPrefix.REPNZ.getCode()
				|| prefix == InstructionPrefix.REP.getCode();
	}

	private static boolean isLegacyPrefixGroup2(final byte prefix) {
		final byte SS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x36;
		final byte DS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x3e;
		final byte ES_SEGMENT_OVERRIDE_PREFIX = (byte) 0x26;
		final byte FS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x64;
		final byte GS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x65;
		final byte BRANCH_NOT_TAKEN_PREFIX = (byte) 0x2e;
		final byte BRANCH_TAKEN_PREFIX = (byte) 0x3e;
		return prefix == CS_SEGMENT_OVERRIDE_PREFIX
				|| prefix == SS_SEGMENT_OVERRIDE_PREFIX
				|| prefix == DS_SEGMENT_OVERRIDE_PREFIX
				|| prefix == ES_SEGMENT_OVERRIDE_PREFIX
				|| prefix == FS_SEGMENT_OVERRIDE_PREFIX
				|| prefix == GS_SEGMENT_OVERRIDE_PREFIX
				|| prefix == BRANCH_NOT_TAKEN_PREFIX
				|| prefix == BRANCH_TAKEN_PREFIX;
	}

	private static boolean isOperandSizeOverridePrefix(final byte prefix) {
		return prefix == OPERAND_SIZE_OVERRIDE_PREFIX;
	}

	private static boolean isAddressSizeOverridePrefix(final byte prefix) {
		return prefix == ADDRESS_SIZE_OVERRIDE_PREFIX;
	}
}
