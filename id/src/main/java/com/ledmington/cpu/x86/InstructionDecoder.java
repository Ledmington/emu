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
import static com.ledmington.utils.BitUtils.asShort;
import static com.ledmington.utils.BitUtils.not;
import static com.ledmington.utils.BitUtils.shr;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
					Arrays.stream(RegisterZMM.values()))
			.flatMap(x -> x)
			.collect(Collectors.toUnmodifiableMap(Operand::toIntelSyntax, x -> x));

	private InstructionDecoder() {}

	public static Instruction fromIntelSyntax(final String input) {
		final CharacterIterator it = new StringCharacterIterator(input);

		InstructionPrefix prefix = null;
		final Opcode opcode;

		String opcodeString = readUntilWhitespace(it);
		switch (opcodeString) {
			case "lock" -> {
				prefix = InstructionPrefix.LOCK;
				skipWhitespaces(it);
				opcodeString = readUntilWhitespace(it);
			}
			case "rep" -> {
				prefix = InstructionPrefix.REP;
				skipWhitespaces(it);
				opcodeString = readUntilWhitespace(it);
			}
			case "repnz" -> {
				prefix = InstructionPrefix.REPNZ;
				skipWhitespaces(it);
				opcodeString = readUntilWhitespace(it);
			}
		}

		if (!fromStringToOpcode.containsKey(opcodeString)) {
			throw new IllegalArgumentException(String.format("Unknown opcode '%s'.", opcodeString));
		}
		opcode = fromStringToOpcode.get(opcodeString);

		skipWhitespaces(it);

		if (it.current() == CharacterIterator.DONE) {
			return new Instruction(prefix, opcode);
		}

		final String[] args = input.substring(it.getIndex()).split(",");
		if (args.length > 4) {
			throw new IllegalArgumentException(String.format("Too many arguments: '%s'.", input));
		}

		final Operand firstOperand = args.length < 1 ? null : parseOperand(args[0].strip(), null, null);
		final Operand secondOperand =
				args.length < 2 ? null : parseOperand(args[1].strip(), firstOperand, opcodeString);
		final Operand thirdOperand = args.length < 3 ? null : parseOperand(args[2].strip(), null, null);
		final Operand fourthOperand = args.length < 4 ? null : parseOperand(args[3].strip(), null, null);

		return new Instruction(prefix, opcode, firstOperand, secondOperand, thirdOperand, fourthOperand);
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

	private static Operand parseOperand(final String input, final Operand previousOperand, final String opcodeString) {
		if (fromStringToRegister.containsKey(input)) {
			// It's a register
			return fromStringToRegister.get(input);
		}
		if (input.startsWith("0x")) {
			// It's an immediate
			final String imm = input.substring(2);
			if ("movabs".equals(opcodeString)) {
				return new Immediate(Long.parseUnsignedLong(imm, 16));
			}
			if (imm.length() <= 2) {
				return new Immediate(asByte(Integer.parseUnsignedInt(imm, 16)));
			} else if (imm.length() <= 4) {
				return new Immediate(asShort(Integer.parseUnsignedInt(imm, 16)));
			} else if (imm.length() <= 8) {
				return new Immediate(Integer.parseUnsignedInt(imm, 16));
			} else if (imm.length() <= 16) {
				return new Immediate(Long.parseUnsignedLong(imm, 16));
			} else {
				throw new IllegalArgumentException(String.format("Immediate too long: '%s'.", input));
			}
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
				iob.pointer(PointerSize.fromSize(previousOperand.bits()));
				it.setIndex(0);
			}
		}

		skipWhitespaces(it);

		Register16 segReg = null;

		// Must begin with '['
		if (it.current() != '[') {
			// try reading segment register
			final String seg = readUntil(it, ':').strip();
			if (fromStringToRegister.containsKey(seg)) {
				segReg = (Register16) fromStringToRegister.get(seg);
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
			Register reg = fromStringToRegister.get(baseString);
			if (segReg != null) {
				reg = new SegmentRegister(segReg, reg);
			}
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
			final boolean isFirstBitSet =
					displacementString.length() >= 2 && Integer.parseInt(displacementString, 16) > 128;
			final int disp = Integer.parseInt(sign + displacementString, 16);
			if (displacementString.length() > 2 || isFirstBitSet) {
				iob.displacement(disp);
			} else {
				iob.displacement(asByte(disp));
			}
		}

		return iob.build();
	}

	public static List<Instruction> fromHex(final byte[] bytes, final int nBytesToDecode) {
		return fromHex(new ReadOnlyByteBufferV1(bytes, true, 1), nBytesToDecode);
	}

	public static List<Instruction> fromHex(final ReadOnlyByteBuffer b, final int nBytesToDecode) {
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
						InstructionEncoder.toIntelSyntax(inst));
			}
			InstructionChecker.check(inst);
			instructions.add(inst);
		}

		return instructions;
	}

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

			case (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83 ->
				parseExtendedOpcodeGroup1(b, opcodeFirstByte, pref);

			case (byte) 0xc0, (byte) 0xc1, (byte) 0xd0, (byte) 0xd1, (byte) 0xd2, (byte) 0xd3 ->
				parseExtendedOpcodeGroup2(b, opcodeFirstByte, pref);

			case (byte) 0xc6, (byte) 0xc7 -> parseExtendedOpcodeGroup11(b, opcodeFirstByte, pref);

			case (byte) 0xf6, (byte) 0xf7 -> parseExtendedOpcodeGroup3(b, opcodeFirstByte, pref);

			case (byte) 0xfe -> parseExtendedOpcodeGroup4(b, opcodeFirstByte, pref);

			case (byte) 0xff -> parseExtendedOpcodeGroup5(b, opcodeFirstByte, pref);

			// 1 byte opcode
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
			case 0 -> new Instruction(Opcode.INC, arg);
			case 1 -> new Instruction(Opcode.DEC, arg);
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
				new Instruction(
						Opcode.INC,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(getPointerSize(pref))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));

			case 0b00000001 ->
				new Instruction(
						Opcode.DEC,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(getPointerSize(pref))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));

			case 0b00000010 -> {
				// near CALL
				final Register reg = Registers.fromCode(
						modrm.rm(),
						!pref.hasAddressSizeOverridePrefix(),
						pref.rex().hasModRMRMExtension(),
						false);
				yield isIndirectOperandNeeded(modrm)
						? new Instruction(
								Opcode.CALL,
								parseIndirectOperand(b, pref, modrm)
										.pointer(
												pref.hasOperandSizeOverridePrefix()
														? PointerSize.WORD_PTR
														: pref.hasAddressSizeOverridePrefix()
																? PointerSize.QWORD_PTR
																: PointerSize.fromSize(reg.bits()))
										.build())
						: new Instruction(Opcode.CALL, reg);
			}
			case 0b00000011 -> // far CALL
				new Instruction(
						Opcode.CALL,
						parseIndirectOperand(b, pref, modrm)
								.pointer(
										pref.hasOperandSizeOverridePrefix()
												? PointerSize.DWORD_PTR
												: PointerSize.QWORD_PTR)
								.build());
			case 0b00000100 ->
				new Instruction(
						Opcode.JMP,
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
			case 0b00000101 ->
				new Instruction(
						Opcode.JMP,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.DWORD_PTR)
										.build()
								: Registers.fromCode(
										modrm.rm(),
										!pref.hasAddressSizeOverridePrefix(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));
			case 0b00000110 ->
				new Instruction(
						Opcode.PUSH,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
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
					yield new Instruction(
							Opcode.TEST,
							iob.pointer(PointerSize.fromSize(imm.bits())).build(),
							imm);
				} else {
					yield new Instruction(
							Opcode.TEST,
							isRegister8Bit
									? Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix())
									: Registers.fromCode(
											modrm.rm(),
											pref.rex().isOperand64Bit(),
											pref.rex().hasModRMRMExtension(),
											pref.hasOperandSizeOverridePrefix()),
							isRegister8Bit ? imm8(b) : pref.hasOperandSizeOverridePrefix() ? imm16(b) : imm32(b));
				}
			}
			case 0b010 ->
				new Instruction(
						Opcode.NOT,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
			case 0b011 -> {
				final Register r = Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						Opcode.NEG,
						isIndirectOperandNeeded
								? parseIndirectOperand(b, pref, modrm)
										.pointer(
												pref.rex().isOperand64Bit()
														? PointerSize.QWORD_PTR
														: PointerSize.DWORD_PTR)
										.build()
								: r);
			}
			case 0b00000100 ->
				new Instruction(
						Opcode.MUL,
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
												pref.hasOperandSizeOverridePrefix()));
			case 0b00000110 ->
				new Instruction(
						Opcode.DIV,
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
												pref.hasOperandSizeOverridePrefix()));
			case 0b00000111 ->
				new Instruction(
						Opcode.IDIV,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
			case 0b00000001 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup11(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final int immediateBits = pref.hasOperandSizeOverridePrefix() ? 16 : (opcodeFirstByte == (byte) 0xc6) ? 8 : 32;

		final byte zeroReg = 0b00000000;
		if (modrm.reg() != zeroReg) {
			throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		}
		return parseMOV(
				b,
				pref,
				modrm,
				immediateBits,
				pref.rex().isOperand64Bit() ? PointerSize.QWORD_PTR : PointerSize.fromSize(immediateBits));
	}

	private static Instruction parseExtendedOpcodeGroup2(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isImmediate8Bit = opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xc1;
		final boolean isImmediate1 = opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd1;

		final Operand op2 = isImmediate8Bit ? imm8(b) : isImmediate1 ? new Immediate((byte) 1) : Register8.CL;
		final boolean isReg8Bit =
				opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd2;

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

		return new Instruction(
				opcode,
				isReg8Bit
						? Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix())
						: Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
				op2);
	}

	private static Instruction parseExtendedOpcodeGroup1(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isRegister8Bit = opcodeFirstByte == (byte) 0x80 || opcodeFirstByte == (byte) 0x82;
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
			return new Instruction(
					opcode,
					isIndirectOperandNeeded
							? parseIndirectOperand(b, pref, modrm)
									.pointer(
											pref.rex().isOperand64Bit()
													? PointerSize.QWORD_PTR
													: PointerSize.fromSize(immediateBits))
									.build()
							: Register8.fromByte(regByte, pref.hasRexPrefix()),
					imm8(b));
		} else {
			final Operand r = isIndirectOperandNeeded
					? parseIndirectOperand(b, pref, modrm)
							.pointer(
									pref.hasOperandSizeOverridePrefix()
											? PointerSize.WORD_PTR
											: pref.rex().isOperand64Bit()
													? PointerSize.QWORD_PTR
													: PointerSize.fromSize(immediateBits))
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
			return new Instruction(opcode, r, imm);
		}
	}

	private static Instruction parseExtendedOpcodeGroup7(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			notImplemented();
		}

		if (modrm.reg() != 0b00000010 || modrm.rm() != 0b00000000) {
			throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		return new Instruction(Opcode.XGETBV);
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
				new Instruction(
						opcodes[and(modrm.reg(), (byte) 0b00000011)],
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build());
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

		return new Instruction(
				opcode,
				Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix()),
				new Immediate(b.read1()));
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

		return new Instruction(opcode);
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

		return new Instruction(opcode);
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

		return new Instruction(opcode, RegisterXMM.fromByte(getByteFromRM(pref, modrm)), imm8(b));
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
		final boolean extension = (pref.hasRexPrefix() && pref.rex().r())
				|| (pref.vex2().isPresent() && !pref.vex2().orElseThrow().r())
				|| (pref.vex3().isPresent() && !pref.vex3().orElseThrow().r())
				|| (pref.evex().isPresent() && !pref.evex().orElseThrow().r());
		return Registers.combine(extension, modrm.reg());
	}

	private static byte getByteFromRM(final Prefixes pref, final ModRM modrm) {
		final boolean extension = (pref.hasRexPrefix() && pref.rex().b())
				|| (pref.vex3().isPresent() && !pref.vex3().orElseThrow().b())
				|| (pref.evex().isPresent() && !pref.evex().orElseThrow().b());
		return Registers.combine(extension, modrm.rm());
	}

	private static Instruction parse2BytesOpcode(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
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
		final byte XORPS_OPCODE = (byte) 0x57;
		final byte ADDSD_OPCODE = (byte) 0x58;
		final byte DIVSD_OPCODE = (byte) 0x5e;
		final byte PUNPCKLBW_OPCODE = (byte) 0x60;
		final byte PUNPCKLWD_OPCODE = (byte) 0x61;
		final byte PUNPCKLDQ_OPCODE = (byte) 0x62;
		final byte PCMPGTB_OPCODE = (byte) 0x64;
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
		final byte JB_DISP32_OPCODE = (byte) 0x82;
		final byte JAE_DISP32_OPCODE = (byte) 0x83;
		final byte JE_DISP32_OPCODE = (byte) 0x84;
		final byte JNE_DISP32_OPCODE = (byte) 0x85;
		final byte JBE_DISP32_OPCODE = (byte) 0x86;
		final byte JA_DISP32_OPCODE = (byte) 0x87;
		final byte JS_DISP32_OPCODE = (byte) 0x88;
		final byte JNS_DISP32_OPCODE = (byte) 0x89;
		final byte JP_DISP32_OPCODE = (byte) 0x8a;
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
		final byte SETL_OPCODE = (byte) 0x9c;
		final byte SETGE_OPCODE = (byte) 0x9d;
		final byte SETLE_OPCODE = (byte) 0x9e;
		final byte SETG_OPCODE = (byte) 0x9f;
		final byte CPUID_OPCODE = (byte) 0xa2;
		final byte BT_M32_R32_OPCODE = (byte) 0xa3;
		final byte BTS_M32_R32_OPCODE = (byte) 0xab;
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
			null,
			null,
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
			case JA_DISP32_OPCODE -> new Instruction(Opcode.JA, imm32(b));
			case JAE_DISP32_OPCODE -> new Instruction(Opcode.JAE, imm32(b));
			case JE_DISP32_OPCODE -> new Instruction(Opcode.JE, imm32(b));
			case JNE_DISP32_OPCODE -> new Instruction(Opcode.JNE, imm32(b));
			case JBE_DISP32_OPCODE -> new Instruction(Opcode.JBE, imm32(b));
			case JG_DISP32_OPCODE -> new Instruction(Opcode.JG, imm32(b));
			case JS_DISP32_OPCODE -> new Instruction(Opcode.JS, imm32(b));
			case JNS_DISP32_OPCODE -> new Instruction(Opcode.JNS, imm32(b));
			case JP_DISP32_OPCODE -> new Instruction(Opcode.JP, imm32(b));
			case JL_DISP32_OPCODE -> new Instruction(Opcode.JL, imm32(b));
			case JGE_DISP32_OPCODE -> new Instruction(Opcode.JGE, imm32(b));
			case JLE_DISP32_OPCODE -> new Instruction(Opcode.JLE, imm32(b));
			case JB_DISP32_OPCODE -> new Instruction(Opcode.JB, imm32(b));

			case ENDBR_OPCODE -> {
				final byte x = b.read1();
				if (x == (byte) 0xfa) {
					yield new Instruction(Opcode.ENDBR64);
				} else if (x == (byte) 0xfb) {
					yield new Instruction(Opcode.ENDBR32);
				} else if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP) {
					final ModRM modrm = new ModRM(x);
					yield new Instruction(
							Opcode.RDSSPQ,
							Registers.fromCode(
									modrm.rm(),
									pref.rex().isOperand64Bit(),
									pref.rex().hasModRMRMExtension(),
									false));
				} else {
					throw new IllegalArgumentException(String.format("Invalid value (0x%02x)", x));
				}
			}
			case NOP_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.NOP,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(getPointerSize(pref))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));
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
					SETL_OPCODE,
					SETGE_OPCODE,
					SETG_OPCODE -> {
				final Opcode opcode = setOpcodes[and(opcodeSecondByte, (byte) 0b00001111)];
				final ModRM modrm = modrm(b);
				yield new Instruction(
						opcode,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.BYTE_PTR)
										.build()
								: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()));
			}

			case CPUID_OPCODE -> new Instruction(Opcode.CPUID);
			case IMUL_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r1 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						Opcode.IMUL,
						r1,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.fromSize(r1.bits()))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));
			}

			// Bit tests
			case BT_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.BT,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BTR_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.BTR,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BTS_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.BTS,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BTC_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.BTC,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BSF_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.BSF,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BSR_R32_M32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r1 = Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						Opcode.BSR,
						r1,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.fromSize(r1.bits()))
								.build());
			}

			case XCHG_M8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Operand op1 = parseIndirectOperand(b, pref, modrm)
						.pointer(PointerSize.BYTE_PTR)
						.build();
				final Operand op2 = Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix());

				yield new Instruction(pref.p1().orElse(null), Opcode.CMPXCHG, op1, op2);
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
				yield new Instruction(pref.p1().orElse(null), Opcode.CMPXCHG, op1, r2);
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
					yield new Instruction(
							opcode,
							r1,
							parseIndirectOperand(b, pref, modrm)
									.pointer(ptrSize)
									.build());
				} else {
					final byte regByte = getByteFromRM(pref, modrm);
					final Register r2 = (ptrSize == PointerSize.BYTE_PTR)
							? Register8.fromByte(regByte, pref.hasRexPrefix())
							: Register16.fromByte(regByte);
					yield new Instruction(opcode, r1, r2);
				}
			}

			case MOVxQx_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref, modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				final boolean movdqa = pref.hasOperandSizeOverridePrefix();
				final boolean movdqu = pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP;
				yield new Instruction(
						movdqa ? Opcode.MOVDQA : (movdqu ? Opcode.MOVDQU : Opcode.MOVQ),
						(movdqa || movdqu) ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(modrm.reg()),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer((movdqa || movdqu) ? PointerSize.XMMWORD_PTR : PointerSize.QWORD_PTR)
										.build()
								: RegisterXMM.fromByte(r2));
			}
			case MOVDQA_M128_R128_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOVDQA,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build(),
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)));
			}
			case PSHUF_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref, modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.PSHUFD : Opcode.PSHUFW,
						pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(r1),
						pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r2) : RegisterMMX.fromByte(r2),
						imm8(b));
			}
			case SHUFPx_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref, modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.SHUFPD : Opcode.SHUFPS,
						RegisterXMM.fromByte(r1),
						RegisterXMM.fromByte(r2),
						imm8(b));
			}
			case PEXTRW_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1 = getByteFromReg(pref, modrm);
				final byte r2 = getByteFromRM(pref, modrm);
				yield new Instruction(Opcode.PEXTRW, Register32.fromByte(r1), RegisterMMX.fromByte(r2), imm8(b));
			}
			case XADD_M8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						pref.p1().orElse(null),
						Opcode.XADD,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build(),
						Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()));
			}
			case XADD_M32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r2 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						pref.p1().orElse(null),
						Opcode.XADD,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.fromSize(r2.bits()))
								.build(),
						r2);
			}
			case MOVQ_MOVD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Opcode code = pref.rex().isOperand64Bit() ? Opcode.MOVQ : Opcode.MOVD;
				final byte regByte = getByteFromReg(pref, modrm);
				final Register r1 = pref.hasOperandSizeOverridePrefix()
						? RegisterXMM.fromByte(regByte)
						: RegisterMMX.fromByte(regByte);
				final Register r2 = Registers.fromCode(
						modrm.rm(), pref.rex().isOperand64Bit(), pref.rex().hasModRMRMExtension(), false);
				yield new Instruction(
						code,
						r1,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(
												code == Opcode.MOVQ
														? PointerSize.fromSize(r1.bits())
														: (r1.bits() == 64
																? PointerSize.QWORD_PTR
																: PointerSize.DWORD_PTR))
										.build()
								: r2);
			}
			case MOVQ_M_XMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref, modrm);
				yield new Instruction(
						Opcode.MOVQ,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build(),
						RegisterXMM.fromByte(regByte));
			}
			case MOVQ_R128_M64_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref, modrm);
				yield new Instruction(
						Opcode.MOVQ,
						RegisterXMM.fromByte(regByte),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
			}
			case MOVHLPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOVHLPS,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case MOVHPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref, modrm);
				yield new Instruction(
						Opcode.MOVHPS,
						RegisterXMM.fromByte(regByte),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
			}
			case MOVHPS_MOVHPD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte regByte = getByteFromReg(pref, modrm);
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.MOVHPD : Opcode.MOVHPS,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build(),
						RegisterXMM.fromByte(regByte));
			}
			case PMINUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PMINUB,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						getXMMArgument(b, modrm, pref, getByteFromRM(pref, modrm)));
			}
			case PMAXUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PMAXUB, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PAND_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PAND, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PADDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PADDQ, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PSUBB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PSUBB, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PSUBW_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PSUBW, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PSUBD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PSUBD, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PSUBQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PSUBQ, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case POR_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(Opcode.POR, RegisterXMM.fromByte(r1Byte), getXMMArgument(b, modrm, pref, r2Byte));
			}
			case PXOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PXOR,
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(r1Byte)
								: RegisterMMX.fromByte(r1Byte),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(
												pref.hasOperandSizeOverridePrefix()
														? PointerSize.XMMWORD_PTR
														: PointerSize.QWORD_PTR)
										.build()
								: (pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r2Byte)
										: RegisterMMX.fromByte(r2Byte)));
			}
			case PCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PCMPEQB,
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(r1Byte)
								: RegisterMMX.fromByte(r1Byte),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(
												pref.hasOperandSizeOverridePrefix()
														? PointerSize.XMMWORD_PTR
														: PointerSize.QWORD_PTR)
										.build()
								: (pref.hasOperandSizeOverridePrefix()
										? RegisterXMM.fromByte(r2Byte)
										: RegisterMMX.fromByte(r2Byte)));
			}
			case PCMPEQW_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PCMPEQW,
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(r1Byte)
								: RegisterMMX.fromByte(r1Byte),
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(r2Byte)
								: RegisterMMX.fromByte(r2Byte));
			}
			case PCMPEQD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						Opcode.PCMPEQD,
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(r1Byte)
								: RegisterMMX.fromByte(r1Byte),
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(r2Byte)
								: RegisterMMX.fromByte(r2Byte));
			}
			case XORPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(Opcode.XORPS, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case ADDSD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(Opcode.ADDSD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case DIVSD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(Opcode.DIVSD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case MOVAPx_R128_M128_OPCODE -> {
				final ModRM modrm = modrm(b);
				final byte r1Byte = getByteFromReg(pref, modrm);
				final byte r2Byte = getByteFromRM(pref, modrm);
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS,
						RegisterXMM.fromByte(r1Byte),
						getXMMArgument(b, modrm, pref, r2Byte));
			}
			case MOVAPx_M128_R128_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build(),
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)));
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
				yield new Instruction(
						opcode,
						r1,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.fromSize(r1.bits()))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));
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
				yield new Instruction(
						Opcode.BSWAP,
						pref.rex().isOperand64Bit()
								? Register64.fromByte(
										Registers.combine(pref.rex().hasOpcodeRegExtension(), regByte))
								: Register32.fromByte(
										Registers.combine(pref.rex().hasOpcodeRegExtension(), regByte)));
			}
			case UD2_OPCODE -> new Instruction(Opcode.UD2);
			case SYSCALL_OPCODE -> new Instruction(Opcode.SYSCALL);
			case PUNPCKLBW_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PUNPCKLBW,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case PUNPCKLWD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PUNPCKLWD,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case PUNPCKLDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PUNPCKLDQ,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case PUNPCKLQDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PUNPCKLQDQ,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case PUNPCKHQDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PUNPCKHQDQ,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case PCMPGTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PCMPGTB,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case MOVSD_OPCODE -> {
				final ModRM modrm = modrm(b);
				final boolean hasRepnePrefix =
						pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REPNZ;
				yield new Instruction(
						hasRepnePrefix ? Opcode.MOVSD : Opcode.MOVUPS,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(hasRepnePrefix ? PointerSize.QWORD_PTR : PointerSize.XMMWORD_PTR)
								.build());
			}
			case MOVUPS_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOVUPS,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build(),
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)));
			}
			case CVTSI2SD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.CVTSI2SD,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case UCOMISx_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.UCOMISD : Opcode.UCOMISS,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(
										pref.hasOperandSizeOverridePrefix()
												? PointerSize.QWORD_PTR
												: PointerSize.DWORD_PTR)
								.build());
			}
			case PMOVMSKB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.PMOVMSKB,
						Register32.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case MOVNTDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOVNTDQ,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build(),
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)));
			}
			case LDDQU_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.LDDQU,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build());
			}
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseTableA4(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final byte PSHUFB_OPCODE = (byte) 0x00;
		final byte MOVBE_OPCODE = (byte) 0xf0;

		final byte x = b.read1();
		final ModRM modrm = modrm(b);

		return switch (x) {
			case PSHUFB_OPCODE ->
				new Instruction(
						Opcode.PSHUFB,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			case MOVBE_OPCODE ->
				new Instruction(
						Opcode.MOVBE,
						Register32.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build());
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
				new Instruction(
						Opcode.PALIGNR,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.XMMWORD_PTR)
										.build()
								: RegisterXMM.fromByte(getByteFromRM(pref, modrm)),
						imm8(b));
			case PCMPISTRI_OPCODE ->
				new Instruction(
						Opcode.PCMPISTRI,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.XMMWORD_PTR)
										.build()
								: RegisterXMM.fromByte(getByteFromRM(pref, modrm)),
						imm8(b));
			default -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte, x);
		};
	}

	private static Instruction parseExtendedOpcodeGroup15(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			notImplemented();
		}

		return switch (modrm.reg()) {
			case (byte) 0b101 -> new Instruction(Opcode.INCSSPQ, Register64.fromByte(getByteFromRM(pref, modrm)));
			case (byte) 0b111 -> new Instruction(Opcode.SFENCE);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private static Instruction parseExtendedOpcodeGroup9(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm(b);

		if (isIndirectOperandNeeded(modrm)) {
			notImplemented();
		}

		if (pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REP) {
			notImplemented();
		}

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0, 1, 2, 3, 4, 5 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					case 6 -> Opcode.RDRAND;
					case 7 -> Opcode.RDSEED;
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return new Instruction(
				opcode,
				Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix()));
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
		final byte OR_M8_R8_OPCODE = (byte) 0x08;
		final byte OR_M32_R32_OPCODE = (byte) 0x09;
		final byte OR_R8_M8_OPCODE = (byte) 0x0a;
		final byte OR_R32_M32_OPCODE = (byte) 0x0b;
		final byte OR_AL_IMM8_OPCODE = (byte) 0x0c;
		final byte OR_EAX_IMM32_OPCODE = (byte) 0x0d;
		final byte ADC_M8_R8_OPCODE = (byte) 0x10;
		final byte ADC_M32_R32_OPCODE = (byte) 0x11;
		final byte ADC_R8_M8_OPCODE = (byte) 0x12;
		final byte ADC_R32_M32_OPCODE = (byte) 0x13;
		final byte ADC_AL_IMM8_OPCODE = (byte) 0x14;
		final byte ADC_EAX_IMM32_OPCODE = (byte) 0x15;
		final byte SBB_M8_R8_OPCODE = (byte) 0x18;
		final byte SBB_M32_R32_OPCODE = (byte) 0x19;
		final byte SBB_R8_M8_OPCODE = (byte) 0x1a;
		final byte SBB_R32_M32_OPCODE = (byte) 0x1b;
		final byte SBB_AL_IMM8_OPCODE = (byte) 0x1c;
		final byte SBB_EAX_IMM32_OPCODE = (byte) 0x1d;
		final byte AND_M8_R8_OPCODE = (byte) 0x20;
		final byte AND_M32_R32_OPCODE = (byte) 0x21;
		final byte AND_R8_M8_OPCODE = (byte) 0x22;
		final byte AND_R32_M32_OPCODE = (byte) 0x23;
		final byte AND_AL_IMM8_OPCODE = (byte) 0x24;
		final byte AND_EAX_IMM32_OPCODE = (byte) 0x25;
		final byte SUB_M8_R8_OPCODE = (byte) 0x28;
		final byte SUB_M32_R32_OPCODE = (byte) 0x29;
		final byte SUB_R8_M8_OPCODE = (byte) 0x2a;
		final byte SUB_R32_M32_OPCODE = (byte) 0x2b;
		final byte SUB_AL_IMM8_OPCODE = (byte) 0x2c;
		final byte SUB_EAX_IMM32_OPCODE = (byte) 0x2d;
		final byte XOR_M8_R8_OPCODE = (byte) 0x30;
		final byte XOR_M32_R32_OPCODE = (byte) 0x31;
		final byte XOR_R8_M8_OPCODE = (byte) 0x32;
		final byte XOR_R32_M32_OPCODE = (byte) 0x33;
		final byte XOR_AL_IMM8_OPCODE = (byte) 0x34;
		final byte XOR_EAX_IMM32_OPCODE = (byte) 0x35;
		final byte CMP_M8_R8_OPCODE = (byte) 0x38;
		final byte CMP_M32_R32_OPCODE = (byte) 0x39;
		final byte CMP_R8_M8_OPCODE = (byte) 0x3a;
		final byte CMP_R32_M32_OPCODE = (byte) 0x3b;
		final byte CMP_AL_IMM8_OPCODE = (byte) 0x3c;
		final byte CMP_EAX_IMM32_OPCODE = (byte) 0x3d;
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
		final byte MOVSXD_OPCODE = (byte) 0x63;
		final byte PUSH_IMM32_OPCODE = (byte) 0x68;
		final byte IMUL_R32_M32_IMM32_OPCODE = (byte) 0x69;
		final byte PUSH_IMM8_OPCODE = (byte) 0x6a;
		final byte IMUL_REG_REG_IMM8_OPCODE = (byte) 0x6b;
		final byte JB_DISP8_OPCODE = (byte) 0x72;
		final byte JAE_DISP8_OPCODE = (byte) 0x73;
		final byte JE_DISP8_OPCODE = (byte) 0x74;
		final byte JNE_DISP8_OPCODE = (byte) 0x75;
		final byte JBE_DISP8_OPCODE = (byte) 0x76;
		final byte JA_DISP8_OPCODE = (byte) 0x77;
		final byte JS_DISP8_OPCODE = (byte) 0x78;
		final byte JNS_DISP8_OPCODE = (byte) 0x79;
		final byte JP_DISP8_OPCODE = (byte) 0x7a;
		final byte JL_DISP8_OPCODE = (byte) 0x7c;
		final byte JGE_DISP8_OPCODE = (byte) 0x7d;
		final byte JLE_DISP8_OPCODE = (byte) 0x7e;
		final byte JG_DISP8_OPCODE = (byte) 0x7f;
		final byte TEST_R8_R8_OPCODE = (byte) 0x84;
		final byte TEST_R32_R32_OPCODE = (byte) 0x85;
		final byte XCHG_M8_R8_OPCODE = (byte) 0x86;
		final byte XCHG_M32_R32_OPCODE = (byte) 0x87;
		final byte MOV_MEM8_REG8_OPCODE = (byte) 0x88;
		final byte MOV_M32_R32_OPCODE = (byte) 0x89;
		final byte MOV_R8_M8_OPCODE = (byte) 0x8a;
		final byte MOV_R32_M32_OPCODE = (byte) 0x8b;
		final byte LEA_OPCODE = (byte) 0x8d;
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
		final byte SAHF_OPCODE = (byte) 0x9e;
		final byte LAHF_OPCODE = (byte) 0x9f;
		final byte MOVS_ES_EDI_DS_ESI_BYTE_PTR_OPCODE = (byte) 0xa4;
		final byte MOVS_ES_EDI_DS_ESI_OPCODE = (byte) 0xa5;
		final byte TEST_AL_IMM8_OPCODE = (byte) 0xa8;
		final byte TEST_EAX_IMM32_OPCODE = (byte) 0xa9;
		final byte STOS_R8_OPCODE = (byte) 0xaa;
		final byte STOS_R32_OPCODE = (byte) 0xab;
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
		final byte RET_OPCODE = (byte) 0xc3;
		final byte LEAVE_OPCODE = (byte) 0xc9;
		final byte INT3_OPCODE = (byte) 0xcc;
		final byte CALL_OPCODE = (byte) 0xe8;
		final byte JMP_DISP32_OPCODE = (byte) 0xe9;
		final byte JMP_DISP8_OPCODE = (byte) 0xeb;
		final byte HLT_OPCODE = (byte) 0xf4;
		final byte CLD_OPCODE = (byte) 0xfc;

		final Opcode[] opcodeTable = {
			Opcode.ADD, Opcode.OR, Opcode.ADC, Opcode.SBB, Opcode.AND, Opcode.SUB, Opcode.XOR, Opcode.CMP
		};

		return switch (opcodeFirstByte) {
			case NOP_OPCODE ->
				pref.hasOperandSizeOverridePrefix()
						? new Instruction(Opcode.XCHG, Register16.AX, Register16.AX)
						: (pref.hasRexPrefix()
								? (pref.rex().isOperand64Bit()
										? new Instruction(Opcode.XCHG, Register64.R8, Register64.RAX)
										: new Instruction(Opcode.XCHG, Register32.R8D, Register32.EAX))
								: new Instruction(Opcode.NOP));
			case RET_OPCODE -> new Instruction(Opcode.RET);
			case LEAVE_OPCODE -> new Instruction(Opcode.LEAVE);
			case INT3_OPCODE -> new Instruction(Opcode.INT3);
			case CDQ_OPCODE -> new Instruction(Opcode.CDQ);
			case SAHF_OPCODE -> new Instruction(Opcode.SAHF);
			case LAHF_OPCODE -> new Instruction(Opcode.LAHF);
			case HLT_OPCODE -> new Instruction(Opcode.HLT);
			case CLD_OPCODE -> new Instruction(Opcode.CLD);
			case CDQE_OPCODE -> new Instruction(pref.rex().isOperand64Bit() ? Opcode.CDQE : Opcode.CWDE);

			case MOV_R32_M32_OPCODE -> parseRxMx(b, pref, Opcode.MOV);
			case MOV_M32_R32_OPCODE -> parseMxRx(b, pref, Opcode.MOV);
			case MOV_MEM8_REG8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOV,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build(),
						Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()));
			}
			case MOV_R8_M8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOV,
						Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.BYTE_PTR)
								.build());
			}
			case TEST_R8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.TEST,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.BYTE_PTR)
										.build()
								: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()),
						Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()));
			}
			case TEST_R32_R32_OPCODE -> {
				final ModRM modrm = modrm(b);
				final Register r2 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().hasModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						Opcode.TEST,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.fromSize(r2.bits()))
										.build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()),
						r2);
			}
			case TEST_AL_IMM8_OPCODE -> new Instruction(Opcode.TEST, Register8.AL, imm8(b));
			case TEST_EAX_IMM32_OPCODE ->
				new Instruction(
						Opcode.TEST, getFirstRegister(pref), pref.hasOperandSizeOverridePrefix() ? imm16(b) : imm32(b));
			case XCHG_M8_R8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.XCHG,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.BYTE_PTR)
										.build()
								: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()),
						Register8.fromByte(getByteFromReg(pref, modrm), pref.hasRexPrefix()));
			}
			case XCHG_M32_R32_OPCODE -> parseMxRx(b, pref, Opcode.XCHG);

			// jumps
			case JMP_DISP32_OPCODE -> new Instruction(Opcode.JMP, imm32(b));
			case JMP_DISP8_OPCODE -> new Instruction(Opcode.JMP, imm8(b));
			case JB_DISP8_OPCODE -> new Instruction(Opcode.JB, imm8(b));
			case JAE_DISP8_OPCODE -> new Instruction(Opcode.JAE, imm8(b));
			case JE_DISP8_OPCODE -> new Instruction(Opcode.JE, imm8(b));
			case JA_DISP8_OPCODE -> new Instruction(Opcode.JA, imm8(b));
			case JNE_DISP8_OPCODE -> new Instruction(Opcode.JNE, imm8(b));
			case JBE_DISP8_OPCODE -> new Instruction(Opcode.JBE, imm8(b));
			case JS_DISP8_OPCODE -> new Instruction(Opcode.JS, imm8(b));
			case JNS_DISP8_OPCODE -> new Instruction(Opcode.JNS, imm8(b));
			case JP_DISP8_OPCODE -> new Instruction(Opcode.JP, imm8(b));
			case JL_DISP8_OPCODE -> new Instruction(Opcode.JL, imm8(b));
			case JGE_DISP8_OPCODE -> new Instruction(Opcode.JGE, imm8(b));
			case JLE_DISP8_OPCODE -> new Instruction(Opcode.JLE, imm8(b));
			case JG_DISP8_OPCODE -> new Instruction(Opcode.JG, imm8(b));

			case CALL_OPCODE -> new Instruction(Opcode.CALL, imm32(b));

			case IMUL_REG_REG_IMM8_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.IMUL,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								false),
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								false),
						imm8(b));
			}
			case PUSH_IMM32_OPCODE -> new Instruction(Opcode.PUSH, imm32(b));
			case IMUL_R32_M32_IMM32_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.IMUL,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								false),
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
										false),
						imm32(b));
			}
			case MOVS_ES_EDI_DS_ESI_BYTE_PTR_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.pointer(PointerSize.BYTE_PTR)
						.base(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.build();
				final Operand op2 = IndirectOperand.builder()
						.pointer(PointerSize.BYTE_PTR)
						.base(new SegmentRegister(
								Register16.DS, pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI))
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.MOVS, op1, op2);
			}
			case MOVS_ES_EDI_DS_ESI_OPCODE -> {
				final PointerSize size =
						pref.hasOperandSizeOverridePrefix() ? PointerSize.WORD_PTR : PointerSize.DWORD_PTR;
				final Operand op1 = IndirectOperand.builder()
						.pointer(size)
						.base(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.build();
				final Operand op2 = IndirectOperand.builder()
						.pointer(size)
						.base(new SegmentRegister(
								Register16.DS, pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI))
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.MOVS, op1, op2);
			}
			case STOS_R8_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.pointer(PointerSize.BYTE_PTR)
						.base(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.STOS, op1, Register8.AL);
			}
			case STOS_R32_OPCODE -> {
				final Operand op2 = pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX;
				final Operand op1 = IndirectOperand.builder()
						.pointer(PointerSize.fromSize(op2.bits()))
						.base(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.STOS, op1, op2);
			}
			case MOVSXD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.MOVSXD,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.DWORD_PTR)
										.build()
								: Registers.fromCode(
										modrm.rm(),
										false,
										pref.rex().hasModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));
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
				yield new Instruction(
						opcode,
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.BYTE_PTR)
										.build()
								: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()),
						Register8.fromByte(regByte, pref.hasRexPrefix()));
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
				yield new Instruction(
						opcode,
						Register8.fromByte(regByte, pref.hasRexPrefix()),
						isIndirectOperandNeeded(modrm)
								? parseIndirectOperand(b, pref, modrm)
										.pointer(PointerSize.BYTE_PTR)
										.build()
								: Register8.fromByte(getByteFromRM(pref, modrm), pref.hasRexPrefix()));
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
				yield new Instruction(opcode, Register8.AL, imm8(b));
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
				yield new Instruction(opcode, r, imm);
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
				yield new Instruction(
						Opcode.MOV,
						Register8.fromByte(
								Registers.combine(pref.rex().hasOpcodeRegExtension(), regByte), pref.hasRexPrefix()),
						imm8(b));
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
				yield new Instruction(pref.rex().isOperand64Bit() ? Opcode.MOVABS : Opcode.MOV, r, imm);
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
				yield new Instruction(
						Opcode.XCHG,
						Registers.fromCode(
								regByte,
								pref.rex().isOperand64Bit(),
								pref.rex().hasOpcodeRegExtension(),
								pref.hasOperandSizeOverridePrefix()),
						getFirstRegister(pref));
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
				yield new Instruction(
						Opcode.PUSH,
						Registers.fromCode(
								regByte,
								true,
								pref.rex().hasOpcodeRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}

			case PUSH_IMM8_OPCODE -> new Instruction(Opcode.PUSH, imm8(b));

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
				yield new Instruction(
						Opcode.POP,
						Registers.fromCode(
								regByte,
								true,
								pref.rex().hasOpcodeRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}

			case LEA_OPCODE -> parseRxMx(b, pref, Opcode.LEA);

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

	private static Instruction parseVex2Opcodes(
			final ReadOnlyByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
		final byte VPCMPGTB_OPCODE = (byte) 0x64;
		final byte VMOVD_OPCODE = (byte) 0x6e;
		final byte VMOVDQU_RYMM_M256_OPCODE = (byte) 0x6f;
		final byte VPSxLDQ_OPCODE = (byte) 0x73;
		final byte VPCMPEQB_OPCODE = (byte) 0x74;
		final byte VZEROALL_OPCODE = (byte) 0x77;
		final byte VMOVQ_OPCODE = (byte) 0x7e;
		final byte VMOVDQU_M256_RYMM_OPCODE = (byte) 0x7f;
		final byte VPMOVMSKB_OPCODE = (byte) 0xd7;
		final byte VPMINUB_OPCODE = (byte) 0xda;
		final byte VPAND_OPCODE = (byte) 0xdb;
		final byte VPANDN_OPCODE = (byte) 0xdf;
		final byte VPOR_OPCODE = (byte) 0xeb;
		final byte VPXOR_OPCODE = (byte) 0xef;
		final byte VPSUBB_OPCODE = (byte) 0xf8;

		final Vex2Prefix vex2 = pref.vex2().orElseThrow();

		return switch (opcodeFirstByte) {
			case VZEROALL_OPCODE -> new Instruction(Opcode.VZEROALL);
			case VPCMPGTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPCMPGTB,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex2)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPXOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPXOR,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex2)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPOR,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterYMM.fromByte(getByteFromV(vex2)),
						RegisterYMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPAND_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPAND,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterYMM.fromByte(getByteFromV(vex2)),
						RegisterYMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPANDN_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPANDN,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex2)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPSUBB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPSUBB,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex2)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPMINUB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPMINUB,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterYMM.fromByte(getByteFromV(vex2)),
						getYMMArgument(b, modrm, pref, getByteFromRM(pref, modrm)));
			}
			case VMOVDQU_RYMM_M256_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVDQU,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build());
			}
			case VMOVDQU_M256_RYMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVDQU,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build(),
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)));
			}
			case VMOVD_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVD,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build());
			}
			case VMOVQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVQ,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
			}
			case VPMOVMSKB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPMOVMSKB,
						Register32.fromByte(getByteFromReg(pref, modrm)),
						RegisterYMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPCMPEQB,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterYMM.fromByte(getByteFromV(vex2)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build());
			}
			case VPSxLDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						modrm.reg() == (byte) 0b010 ? Opcode.VPSRLDQ : Opcode.VPSLLDQ,
						RegisterXMM.fromByte(getByteFromV(vex2)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)),
						imm8(b));
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
		final byte VPCMPISTRI_OPCODE = (byte) 0x63;
		final byte VMOVDQU_RYMM_M256_OPCODE = (byte) 0x6f;
		final byte VPCMPEQB_OPCODE = (byte) 0x74;
		final byte VPBROADCASTB_OPCODE = (byte) 0x78;
		final byte VPANDN_OPCODE = (byte) 0xdf;
		final byte VMOVDQU_M256_RYMM_OPCODE = (byte) 0x7f;
		final byte VPXOR_OPCODE = (byte) 0xef;
		final byte BZHI_OPCODE = (byte) 0xf5;
		final byte SARX_OPCODE = (byte) 0xf7;

		final Vex3Prefix vex3 = pref.vex3().orElseThrow();

		return switch (opcodeFirstByte) {
			case VPSHUFB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPSHUFB,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex3)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPALIGNR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPALIGNR,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex3)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.XMMWORD_PTR)
								.build(),
						imm8(b));
			}
			case VPCMPISTRI_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPCMPISTRI,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)),
						imm8(b));
			}
			case VMOVDQU_RYMM_M256_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVDQU,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build());
			}
			case VMOVDQU_M256_RYMM_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVDQU,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build(),
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)));
			}
			case VPCMPEQB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPCMPEQB,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterYMM.fromByte(getByteFromV(vex3)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.YMMWORD_PTR)
								.build());
			}
			case VPBROADCASTB_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPBROADCASTB,
						RegisterYMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case SARX_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.SARX,
						Register32.fromByte(getByteFromReg(pref, modrm)),
						Register32.fromByte(getByteFromRM(pref, modrm)),
						Register32.fromByte(getByteFromV(vex3)));
			}
			case BZHI_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.BZHI,
						Register32.fromByte(getByteFromReg(pref, modrm)),
						Register32.fromByte(getByteFromRM(pref, modrm)),
						Register32.fromByte(getByteFromV(vex3)));
			}
			case VPANDN_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPANDN,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex3)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
			}
			case VPXOR_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VPXOR,
						RegisterXMM.fromByte(getByteFromReg(pref, modrm)),
						RegisterXMM.fromByte(getByteFromV(vex3)),
						RegisterXMM.fromByte(getByteFromRM(pref, modrm)));
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
		final byte VMOVDQU64_OPCODE = (byte) 0x6f;
		final byte VMOVNTDQ_OPCODE = (byte) 0xe7;

		final EvexPrefix evex = pref.evex().orElseThrow();

		return switch (opcodeFirstByte) {
			case VMOVUPS_R512_M512_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVUPS,
						RegisterZMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build());
			}
			case VMOVUPS_M512_R512_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVUPS,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build(),
						RegisterZMM.fromByte(getByteFromReg(pref, modrm)));
			}
			case VMOVDQU64_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVDQU64,
						RegisterZMM.fromByte(getByteFromReg(pref, modrm)),
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build());
			}
			case VMOVNTDQ_OPCODE -> {
				final ModRM modrm = modrm(b);
				yield new Instruction(
						Opcode.VMOVNTDQ,
						parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.ZMMWORD_PTR)
								.build(),
						RegisterZMM.fromByte(getByteFromReg(pref, modrm)));
			}
			default -> {
				final long pos = b.getPosition();
				logger.debug("Unknown opcode: 0x%02x.", opcodeFirstByte);
				b.setPosition(pos);
				throw new UnknownOpcode(opcodeFirstByte);
			}
		};
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

		Optional<Vex2Prefix> vex2;
		{
			final byte vex2Byte = b.read1();
			if (Vex2Prefix.isVEX2Prefix(vex2Byte)) {
				vex2 = Optional.of(Vex2Prefix.of(b.read1()));
			} else {
				b.setPosition(b.getPosition() - 1);
				vex2 = Optional.empty();
			}
		}

		Optional<Vex3Prefix> vex3;
		{
			final byte vex3Byte = b.read1();
			if (Vex3Prefix.isVEX3Prefix(vex3Byte)) {
				vex3 = Optional.of(Vex3Prefix.of(b.read1(), b.read1()));
			} else {
				b.setPosition(b.getPosition() - 1);
				vex3 = Optional.empty();
			}
		}

		Optional<EvexPrefix> evex;
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
			if (count >= 2) {
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
		return new Instruction(
				opcode,
				isIndirectOperandNeeded(modrm)
						? parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.fromSize(r2.bits()))
								.build()
						: Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
				r2);
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
		return new Instruction(
				opcode,
				r1,
				isIndirectOperandNeeded
						? parseIndirectOperand(b, pref, modrm)
								.pointer(PointerSize.fromSize(r1.bits()))
								.build()
						: Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().hasModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
	}

	private static Instruction parseMOV(
			final ReadOnlyByteBuffer b,
			final Prefixes pref,
			final ModRM modrm,
			final int immediateBits,
			final PointerSize pointerSize) {
		final boolean hasOperandSizeOverridePrefix = pref.hasOperandSizeOverridePrefix();
		final RexPrefix rexPrefix = pref.rex();
		final byte rm = modrm.rm();

		// Table at page 530
		final byte mod = modrm.mod();
		if (mod < (byte) 0b00 || mod > 0b11) {
			throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
		}

		final IndirectOperandBuilder iob = parseIndirectOperand(b, pref, modrm);

		iob.pointer(pointerSize);

		final Operand operand1;

		if (isIndirectOperandNeeded(modrm)) {
			operand1 = iob.build();
		} else {
			// indirect operand not needed, so we take the second operand without using the
			// addressSizeOverride
			operand1 = Registers.fromCode(
					rm, rexPrefix.isOperand64Bit(), rexPrefix.hasModRMRMExtension(), hasOperandSizeOverridePrefix);
		}

		return switch (immediateBits) {
			case 8 -> new Instruction(Opcode.MOV, operand1, imm8(b));
			case 16 -> new Instruction(Opcode.MOV, operand1, imm16(b));
			case 32 -> new Instruction(Opcode.MOV, operand1, imm32(b));
			default ->
				throw new IllegalArgumentException(
						String.format("Invalid value for immediate bits: %,d.", immediateBits));
		};
	}

	private static boolean isBP(final Register r) {
		return r == Register32.EBP || r == Register32.R13D || r == Register64.RBP || r == Register64.R13;
	}

	private static boolean isSP(final Register r) {
		return r == Register32.ESP || r == Register64.RSP;
	}

	private static IndirectOperandBuilder parseIndirectOperand(
			final ReadOnlyByteBuffer b, final Prefixes pref, final ModRM modrm) {
		final boolean baseRegisterExtension = (pref.hasRexPrefix() && pref.rex().b())
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

			final boolean sibIndexExtension = (pref.hasRexPrefix() && pref.rex().x())
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
				baseRegister = new SegmentRegister(Register16.CS, baseRegister);
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
