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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.cpu.x86.exc.ReservedOpcode;
import com.ledmington.cpu.x86.exc.UnknownOpcode;
import com.ledmington.cpu.x86.exc.UnrecognizedPrefix;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;
import com.ledmington.utils.SuppressFBWarnings;

/**
 * Reference Intel® 64 and IA-32 Architectures Software Developer's Manual volume 2. Legacy prefixes : Paragraph 2.1.1.
 * Instruction opcodes : Appendix A. (pag. 2839)
 */
public final class InstructionDecoderV1 implements InstructionDecoder {

	private static final MiniLogger logger = MiniLogger.getLogger("x86-asm");

	private static final byte MULTIBYTE_OPCODE_PREFIX = (byte) 0x0f;
	private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
	private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
	private static final byte MODRM_MOD_NO_DISP = (byte) 0b00000011;

	private final ReadOnlyByteBuffer b;

	/**
	 * Creates an InstructionDecoder with the given byte buffer.
	 *
	 * @param b The byte buffer to read bytes from.
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "We are taking a reference to an immutable object.")
	public InstructionDecoderV1(final ReadOnlyByteBuffer b) {
		this.b = Objects.requireNonNull(b);
	}

	/**
	 * Creates an InstructionDecoder by wrapping the given byte array.
	 *
	 * @param code A non-null and non empty byte array.
	 */
	public InstructionDecoderV1(final byte[] code) {
		this.b = new ReadOnlyByteBufferV1(Objects.requireNonNull(code));
	}

	@Override
	public List<Instruction> decodeAll(final int nBytesToDecode) {
		logger.info("The code is %,d bytes long", nBytesToDecode);

		final List<Instruction> instructions = new ArrayList<>();
		while (b.getPosition() < nBytesToDecode) {
			final long pos = b.getPosition();
			final Instruction inst = decode();
			{ // Debugging info
				final long codeLen = b.getPosition() - pos;
				b.setPosition(pos);
				logger.info(
						"%08x: %-24s %s",
						pos,
						IntStream.range(0, (int) codeLen)
								.mapToObj(i -> String.format("%02x", b.read1()))
								.collect(Collectors.joining(" ")),
						inst.toIntelSyntax());
			}
			instructions.add(inst);
		}

		return instructions;
	}

	@Override
	public Instruction decode() {
		final Prefixes pref = parsePrefixes();

		final byte opcodeFirstByte = b.read1();

		if (isMultibyteOpcode(opcodeFirstByte)) {
			// more than 1 bytes opcode
			return parse2BytesOpcode(opcodeFirstByte, pref);
		} else if (isExtendedOpcode(opcodeFirstByte)) {
			// extended opcode group 1
			return parseExtendedOpcodeGroup1(opcodeFirstByte, pref);
		} else if (opcodeFirstByte == (byte) 0xc0
				|| opcodeFirstByte == (byte) 0xc1
				|| opcodeFirstByte == (byte) 0xd0
				|| opcodeFirstByte == (byte) 0xd1
				|| opcodeFirstByte == (byte) 0xd2
				|| opcodeFirstByte == (byte) 0xd3) {
			return parseExtendedOpcodeGroup2(opcodeFirstByte, pref);
		} else if (opcodeFirstByte == (byte) 0xc6 || opcodeFirstByte == (byte) 0xc7) {
			return parseExtendedOpcodeGroup11(opcodeFirstByte, pref);
		} else if (opcodeFirstByte == (byte) 0xf6 || opcodeFirstByte == (byte) 0xf7) {
			return parseExtendedOpcodeGroup3(opcodeFirstByte, pref);
		} else if (opcodeFirstByte == (byte) 0xfe) {
			return parseExtendedOpcodeGroup4(opcodeFirstByte, pref);
		} else if (opcodeFirstByte == (byte) 0xff) {
			return parseExtendedOpcodeGroup5(opcodeFirstByte, pref);
		} else {
			// 1 byte opcode
			return parseSingleByteOpcode(opcodeFirstByte, pref);
		}
	}

	private Instruction parseExtendedOpcodeGroup4(final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		return switch (modrm.reg()) {
			case 0 -> new Instruction(
					Opcode.INC,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.hasOperandSizeOverridePrefix()
													? PointerSize.WORD_PTR
													: PointerSize.BYTE_PTR)
									.build()
							: Register8.fromByte(
									Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
									pref.hasRexPrefix()));
			case 1 -> new Instruction(
					Opcode.DEC,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.hasOperandSizeOverridePrefix()
													? PointerSize.WORD_PTR
													: PointerSize.BYTE_PTR)
									.build()
							: Register8.fromByte(
									Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
									pref.hasRexPrefix()));
			case 2, 3, 4, 5, 6, 7 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private Instruction parseExtendedOpcodeGroup5(final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		return switch (modrm.reg()) {
			case 0b00000000 -> new Instruction(
					Opcode.INC,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.hasOperandSizeOverridePrefix()
													? PointerSize.WORD_PTR
													: (pref.rex().isOperand64Bit()
															? PointerSize.QWORD_PTR
															: PointerSize.DWORD_PTR))
									.build()
							: Registers.fromCode(
									modrm.rm(),
									pref.rex().isOperand64Bit(),
									pref.rex().getModRMRMExtension(),
									pref.hasOperandSizeOverridePrefix()));
			case 0b00000001 -> new Instruction(
					Opcode.DEC,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.hasOperandSizeOverridePrefix()
													? PointerSize.WORD_PTR
													: (pref.rex().isOperand64Bit()
															? PointerSize.QWORD_PTR
															: PointerSize.DWORD_PTR))
									.build()
							: Registers.fromCode(
									modrm.rm(),
									pref.rex().isOperand64Bit(),
									pref.rex().getModRMRMExtension(),
									pref.hasOperandSizeOverridePrefix()));
			case 0b00000010 -> {
				// near CALL
				final Register reg = Registers.fromCode(
						modrm.rm(),
						!pref.hasAddressSizeOverridePrefix(),
						pref.rex().getModRMRMExtension(),
						false);
				yield (modrm.mod() != MODRM_MOD_NO_DISP) // indirect operand needed
						? (new Instruction(
								Opcode.CALL,
								parseIndirectOperand(pref, modrm)
										.pointer(
												pref.hasOperandSizeOverridePrefix()
														? PointerSize.WORD_PTR
														: (pref.hasAddressSizeOverridePrefix()
																? PointerSize.QWORD_PTR
																: PointerSize.fromSize(reg.bits())))
										.build()))
						: (new Instruction(Opcode.CALL, reg));
			}
			case 0b00000011 -> // far CALL
			new Instruction(
					Opcode.CALL,
					parseIndirectOperand(pref, modrm)
							.pointer(
									pref.hasOperandSizeOverridePrefix() ? PointerSize.DWORD_PTR : PointerSize.QWORD_PTR)
							.build());
			case 0b00000100 -> new Instruction(
					Opcode.JMP,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.hasOperandSizeOverridePrefix()
													? PointerSize.WORD_PTR
													: PointerSize.QWORD_PTR)
									.build()
							: Registers.fromCode(
									modrm.rm(), true,
									pref.rex().getModRMRMExtension(), false));
			case 0b00000101 -> new Instruction(
					Opcode.JMP,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.hasAddressSizeOverridePrefix()
													? PointerSize.WORD_PTR
													: (pref.hasOperandSizeOverridePrefix()
															? PointerSize.DWORD_PTR
															: PointerSize.QWORD_PTR))
									.build()
							: Registers.fromCode(
									modrm.rm(), true,
									pref.rex().getModRMRMExtension(), false));
			case 0b00000110 -> new Instruction(
					Opcode.PUSH,
					parseIndirectOperand(pref, modrm)
							.pointer(PointerSize.QWORD_PTR)
							.build());
			case 0b00000111 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private Instruction parseExtendedOpcodeGroup3(final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isRegister8Bit = opcodeFirstByte == (byte) 0xf6;

		final boolean isIndirectOperandNeeded = modrm.mod() != MODRM_MOD_NO_DISP;

		return switch (modrm.reg()) {
			case 0b000 -> new Instruction(
					Opcode.TEST,
					isIndirectOperandNeeded
							? parseIndirectOperand(pref, modrm).build()
							: (isRegister8Bit
									? Register8.fromByte(
											Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
											pref.hasRexPrefix())
									: Registers.fromCode(
											modrm.rm(),
											pref.rex().isOperand64Bit(),
											pref.rex().getModRMRMExtension(),
											pref.hasOperandSizeOverridePrefix())),
					isRegister8Bit ? imm8() : (pref.hasOperandSizeOverridePrefix() ? imm16() : imm32()));
			case 0b010 -> new Instruction(
					Opcode.NOT,
					Registers.fromCode(
							modrm.rm(),
							pref.rex().isOperand64Bit(),
							pref.rex().getModRMRMExtension(),
							pref.hasOperandSizeOverridePrefix()));
			case 0b011 -> {
				final Register r = Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().getModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						Opcode.NEG,
						isIndirectOperandNeeded
								? parseIndirectOperand(pref, modrm)
										.pointer(
												pref.rex().isOperand64Bit()
														? PointerSize.QWORD_PTR
														: PointerSize.DWORD_PTR)
										.build()
								: r);
			}
			case 0b00000100 -> new Instruction(
					Opcode.MUL,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											(opcodeFirstByte == (byte) 0xf6)
													? PointerSize.BYTE_PTR
													: (pref.hasOperandSizeOverridePrefix()
															? PointerSize.WORD_PTR
															: (pref.rex().isOperand64Bit()
																	? PointerSize.QWORD_PTR
																	: PointerSize.DWORD_PTR)))
									.build()
							: ((opcodeFirstByte == (byte) 0xf6)
									?
									// R8
									Register8.fromByte(
											Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
											pref.hasRexPrefix())
									: Registers.fromCode(
											modrm.rm(),
											pref.rex().isOperand64Bit(),
											pref.rex().getModRMRMExtension(),
											pref.hasOperandSizeOverridePrefix())));
			case 0b00000110 -> new Instruction(
					Opcode.DIV,
					(modrm.mod() != MODRM_MOD_NO_DISP)
							? parseIndirectOperand(pref, modrm)
									.pointer(
											(opcodeFirstByte == (byte) 0xf6)
													? PointerSize.BYTE_PTR
													: (pref.hasOperandSizeOverridePrefix()
															? PointerSize.WORD_PTR
															: (pref.rex().isOperand64Bit()
																	? PointerSize.QWORD_PTR
																	: PointerSize.DWORD_PTR)))
									.build()
							: ((opcodeFirstByte == (byte) 0xf6)
									?
									// R8
									Register8.fromByte(
											Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
											pref.hasRexPrefix())
									: Registers.fromCode(
											modrm.rm(),
											pref.rex().isOperand64Bit(),
											pref.rex().getModRMRMExtension(),
											pref.hasOperandSizeOverridePrefix())));
			case 0b00000111 -> new Instruction(
					Opcode.IDIV,
					Registers.fromCode(
							modrm.rm(),
							pref.rex().isOperand64Bit(),
							pref.rex().getModRMRMExtension(),
							pref.hasOperandSizeOverridePrefix()));
			case 0b00000001 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private Instruction parseExtendedOpcodeGroup11(final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final int immediateBits =
				pref.hasOperandSizeOverridePrefix() ? 16 : ((opcodeFirstByte == (byte) 0xc6) ? 8 : 32);

		final byte zeroReg = 0b00000000;
		if (modrm.reg() != zeroReg) {
			throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		}
		return parseMOV(
				pref,
				modrm,
				immediateBits,
				pref.rex().isOperand64Bit() ? PointerSize.QWORD_PTR : PointerSize.fromSize(immediateBits));
	}

	private Instruction parseExtendedOpcodeGroup2(final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isImmediate8Bit = opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xc1;
		final boolean isImmediate1 = opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd1;

		final Operand op2 = isImmediate8Bit ? imm8() : (isImmediate1 ? new Immediate((byte) 1) : Register8.CL);
		final boolean isReg8Bit =
				opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd2;

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b00000100 -> Opcode.SHL;
					case 0b00000101 -> Opcode.SHR;
					case 0b00000111 -> Opcode.SAR;
					case 0b00000110 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return new Instruction(
				opcode,
				isReg8Bit
						? (Register8.fromByte(
								Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()), pref.hasRexPrefix()))
						: (Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix())),
				op2);
	}

	private Instruction parseExtendedOpcodeGroup1(final byte opcodeFirstByte, final Prefixes pref) {
		final byte opcodeSecondByte = b.read1();

		final ModRM modrm = new ModRM(opcodeSecondByte);

		final boolean isRegister8Bit = opcodeFirstByte == (byte) 0x80 || opcodeFirstByte == (byte) 0x82;
		final int immediateBits =
				(opcodeFirstByte != (byte) 0x81) ? 8 : (pref.hasOperandSizeOverridePrefix() ? 16 : 32);
		final boolean isIndirectOperandNeeded = modrm.mod() != 0b11;
		final byte regByte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());

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
							? parseIndirectOperand(pref, modrm)
									.pointer(
											pref.rex().isOperand64Bit()
													? PointerSize.QWORD_PTR
													: PointerSize.fromSize(immediateBits))
									.build()
							: Register8.fromByte(regByte, pref.hasRexPrefix()),
					imm8());
		} else {
			final Operand r = isIndirectOperandNeeded
					? parseIndirectOperand(pref, modrm)
							.pointer(
									pref.hasOperandSizeOverridePrefix()
											? PointerSize.WORD_PTR
											: (pref.rex().isOperand64Bit()
													? PointerSize.QWORD_PTR
													: PointerSize.fromSize(immediateBits)))
							.build()
					: Registers.fromCode(
							modrm.rm(),
							pref.rex().isOperand64Bit(),
							pref.rex().getModRMRMExtension(),
							pref.hasOperandSizeOverridePrefix());
			final int operandBits =
					(r instanceof IndirectOperand) ? (((IndirectOperand) r).explicitPtrSize()) : ((Register) r).bits();

			final Immediate imm =
					switch (immediateBits) {
						case 8 -> switch (operandBits) {
							case 8 -> imm8();
							case 16 -> new Immediate((short) b.read1());
							case 32 -> new Immediate((int) b.read1());
							case 64 -> new Immediate((long) b.read1());
							default -> throw new IllegalArgumentException(String.format(
									"Immediate bits were %,d and operand bits were %,d", immediateBits, operandBits));
						};
						case 16 -> switch (operandBits) {
							case 16 -> imm16();
							case 32 -> new Immediate((int) b.read2LE());
							case 64 -> new Immediate((long) b.read2LE());
							default -> throw new IllegalArgumentException(String.format(
									"Immediate bits were %,d and operand bits were %,d", immediateBits, operandBits));
						};
						default -> switch (operandBits) {
							case 32 -> imm32();
							case 64 -> new Immediate((long) b.read4LE());
							default -> throw new IllegalArgumentException(String.format(
									"Immediate bits were %,d and operand bits were %,d", immediateBits, operandBits));
						};
					};
			return new Instruction(opcode, r, imm);
		}
	}

	private Instruction parseExtendedOpcodeGroup7(final byte opcodeFirstByte, final byte opcodeSecondByte) {
		final ModRM modrm = modrm();

		if (modrm.mod() != MODRM_MOD_NO_DISP) {
			notImplemented();
		}

		if (modrm.reg() != 0b00000010 || modrm.rm() != 0b00000000) {
			throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		return new Instruction(Opcode.XGETBV);
	}

	private Instruction parseExtendedOpcodeGroup16(
			final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm();

		if (modrm.mod() == MODRM_MOD_NO_DISP) {
			throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		final Opcode[] opcodes =
				new Opcode[] {Opcode.PREFETCHNTA, Opcode.PREFETCHT0, Opcode.PREFETCHT1, Opcode.PREFETCHT2};

		return switch (modrm.reg()) {
			case 0, 1, 2, 3 -> new Instruction(
					opcodes[BitUtils.and(modrm.reg(), (byte) 0b00000011)],
					parseIndirectOperand(pref, modrm)
							.pointer(PointerSize.BYTE_PTR)
							.build());
			case 4, 5, 6, 7 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private Instruction parseExtendedOpcodeGroup8(
			final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm();

		final Opcode opcode =
				switch (modrm.reg()) {
					case 0b00000100 -> Opcode.BT;
					case 0b00000101 -> Opcode.BTS;
					case 0b00000110 -> Opcode.BTR;
					case 0b00000111 -> Opcode.BTC;
					case 0b00000000, 0b00000001, 0b00000010, 0b00000011 -> throw new ReservedOpcode(
							opcodeFirstByte, opcodeSecondByte);
					default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
				};

		return new Instruction(
				opcode,
				Registers.fromCode(
						modrm.rm(),
						pref.rex().isOperand64Bit(),
						pref.rex().getModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix()),
				new Immediate(b.read1()));
	}

	private Instruction parse2BytesOpcode(final byte opcodeFirstByte, final Prefixes pref) {
		final byte GROUP7_OPCODE = (byte) 0x01;
		final byte UD2_OPCODE = (byte) 0x0b;
		final byte MOVSD_OPCODE = (byte) 0x10;
		final byte MOVUPS_OPCODE = (byte) 0x11;
		final byte MOVHLPS_OPCODE = (byte) 0x12;
		final byte MOVHPS_OPCODE = (byte) 0x16;
		final byte GROUP16_OPCODE = (byte) 0x18;
		final byte ENDBR_OPCODE = (byte) 0x1e;
		final byte NOP_OPCODE = (byte) 0x1f;
		final byte MOVAPx_R128_INDIRECT128_OPCODE = (byte) 0x28;
		final byte MOVAPx_INDIRECT128_R128_OPCODE = (byte) 0x29;
		final byte CVTSI2SD_OPCODE = (byte) 0x2a;
		final byte UCOMISx_OPCODE = (byte) 0x2e;
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
		final byte PUNPCKLDQ_OPCODE = (byte) 0x62;
		final byte PUNPCKLQDQ_OPCODE = (byte) 0x6c;
		final byte PUNPCKHQDQ_OPCODE = (byte) 0x6d;
		final byte MOVQ_OPCODE = (byte) 0x6e;
		final byte MOVDQA_OPCODE = (byte) 0x6f;
		final byte PSHUF_OPCODE = (byte) 0x70;
		final byte PCMPEQD_OPCODE = (byte) 0x76;
		final byte MOVQ_R128_INDIRECT64_OPCODE = (byte) 0x7e;
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
		final byte BT_INDIRECT32_R32_OPCODE = (byte) 0xa3;
		final byte BTS_INDIRECT32_R32_OPCODE = (byte) 0xab;
		final byte GROUP15_OPCODE = (byte) 0xae;
		final byte IMUL_OPCODE = (byte) 0xaf;
		final byte XCHG_INDIRECT8_R8_OPCODE = (byte) 0xb0;
		final byte XCHG_INDIRECT32_R32_OPCODE = (byte) 0xb1;
		final byte BTR_INDIRECT32_R32_OPCODE = (byte) 0xb3;
		final byte MOVZX_BYTE_PTR_OPCODE = (byte) 0xb6;
		final byte MOVZX_WORD_PTR_OPCODE = (byte) 0xb7;
		final byte GROUP8_OPCODE = (byte) 0xba;
		final byte BTC_INDIRECT32_R32_OPCODE = (byte) 0xbb;
		final byte MOVSX_BYTE_PTR_OPCODE = (byte) 0xbe;
		final byte MOVSX_WORD_PTR_OPCODE = (byte) 0xbf;
		final byte XADD_INDIRECT8_R8_OPCODE = (byte) 0xc0;
		final byte XADD_INDIRECT32_R32_OPCODE = (byte) 0xc1;
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
		final byte MOVQ_INDIRECT_XMM_OPCODE = (byte) 0xd6;
		final byte PAND_OPCODE = (byte) 0xdb;
		final byte POR_OPCODE = (byte) 0xeb;
		final byte PXOR_OPCODE = (byte) 0xef;
		final byte PSUBQ_OPCODE = (byte) 0xfb;

		final Opcode[] cmovOpcodes = new Opcode[] {
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

		final Opcode[] setOpcodes = new Opcode[] {
			null,
			null,
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
			case GROUP7_OPCODE -> parseExtendedOpcodeGroup7(opcodeFirstByte, opcodeSecondByte);
			case GROUP8_OPCODE -> parseExtendedOpcodeGroup8(opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP9_OPCODE -> parseExtendedOpcodeGroup9(opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP15_OPCODE -> parseExtendedOpcodeGroup15(opcodeFirstByte, opcodeSecondByte, pref);
			case GROUP16_OPCODE -> parseExtendedOpcodeGroup16(opcodeFirstByte, opcodeSecondByte, pref);

				// conditional jumps
			case JA_DISP32_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of(b.read4LE()));
			case JAE_DISP32_OPCODE -> new Instruction(Opcode.JAE, RelativeOffset.of(b.read4LE()));
			case JE_DISP32_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of(b.read4LE()));
			case JNE_DISP32_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of(b.read4LE()));
			case JBE_DISP32_OPCODE -> new Instruction(Opcode.JBE, RelativeOffset.of(b.read4LE()));
			case JG_DISP32_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of(b.read4LE()));
			case JS_DISP32_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of(b.read4LE()));
			case JNS_DISP32_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of(b.read4LE()));
			case JP_DISP32_OPCODE -> new Instruction(Opcode.JP, RelativeOffset.of(b.read4LE()));
			case JL_DISP32_OPCODE -> new Instruction(Opcode.JL, RelativeOffset.of(b.read4LE()));
			case JGE_DISP32_OPCODE -> new Instruction(Opcode.JGE, RelativeOffset.of(b.read4LE()));
			case JLE_DISP32_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of(b.read4LE()));
			case JB_DISP32_OPCODE -> new Instruction(Opcode.JB, RelativeOffset.of(b.read4LE()));

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
									pref.rex().getModRMRMExtension(),
									false));
				} else {
					throw new IllegalArgumentException(String.format("Invalid value (0x%02x)", x));
				}
			}
			case NOP_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.NOP,
						parseIndirectOperand(pref, modrm)
								.pointer(
										pref.hasOperandSizeOverridePrefix()
												? PointerSize.WORD_PTR
												: (pref.rex().isOperand64Bit()
														? PointerSize.QWORD_PTR
														: PointerSize.DWORD_PTR))
								.build());
			}

				// conditional set bytes
			case SETE_OPCODE,
					SETB_OPCODE,
					SETLE_OPCODE,
					SETAE_OPCODE,
					SETNE_OPCODE,
					SETBE_OPCODE,
					SETA_OPCODE,
					SETL_OPCODE,
					SETGE_OPCODE,
					SETG_OPCODE -> {
				final Opcode opcode = setOpcodes[BitUtils.and(opcodeSecondByte, (byte) 0b00001111)];
				final ModRM modrm = modrm();
				yield new Instruction(
						opcode,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm)
										.pointer(PointerSize.BYTE_PTR)
										.build()
								: Register8.fromByte(
										Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
										pref.hasRexPrefix()));
			}

			case CPUID_OPCODE -> new Instruction(Opcode.CPUID);
			case IMUL_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.IMUL,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Registers.fromCode(
										modrm.rm(),
										!pref.hasAddressSizeOverridePrefix(),
										pref.rex().getModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()));
			}

				// Bit tests
			case BT_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.BT,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BTR_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.BTR,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BTS_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.BTS,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case BTC_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.BTC,
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}

			case XCHG_INDIRECT8_R8_OPCODE -> {
				final ModRM modrm = modrm();
				final Operand op1 = parseIndirectOperand(pref, modrm).build();
				final Operand op2 = Register8.fromByte(
						Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg()), pref.hasRexPrefix());

				yield new Instruction(pref.p1().orElse(null), Opcode.CMPXCHG, op1, op2);
			}
			case XCHG_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				final Operand op1 = parseIndirectOperand(pref, modrm).build();
				final Operand op2 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().getModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(pref.p1().orElse(null), Opcode.CMPXCHG, op1, op2);
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

				final ModRM modrm = modrm();
				final Register r1 = Registers.fromCode(
						modrm.reg(), pref.rex().isOperand64Bit(), pref.rex().getModRMRegExtension(), false);

				if (modrm.mod() != MODRM_MOD_NO_DISP) { // indirect operand needed
					yield new Instruction(
							opcode,
							r1,
							parseIndirectOperand(pref, modrm).pointer(ptrSize).build());
				} else {
					final byte regByte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
					final Register r2 = (ptrSize == PointerSize.BYTE_PTR)
							? Register8.fromByte(regByte, pref.hasRexPrefix())
							: Register16.fromByte(regByte);
					yield new Instruction(opcode, r1, r2);
				}
			}

			case MOVDQA_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.MOVDQA,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						parseIndirectOperand(pref, modrm).build());
			}
			case PSHUF_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1 = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2 = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.PSHUFD : Opcode.PSHUFW,
						pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(r1),
						pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r2) : RegisterMMX.fromByte(r2),
						imm8());
			}
			case SHUFPx_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1 = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2 = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.SHUFPD : Opcode.SHUFPS,
						RegisterXMM.fromByte(r1),
						RegisterXMM.fromByte(r2),
						imm8());
			}
			case XADD_INDIRECT8_R8_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						pref.p1().orElse(null),
						Opcode.XADD,
						parseIndirectOperand(pref, modrm).build(),
						Register8.fromByte(
								Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg()),
								pref.hasRexPrefix()));
			}
			case XADD_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						pref.p1().orElse(null),
						Opcode.XADD,
						parseIndirectOperand(pref, modrm).build(),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case MOVQ_OPCODE -> {
				final ModRM modrm = modrm();
				final byte regByte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final Register r1 = Registers.fromCode(
						modrm.rm(), pref.rex().isOperand64Bit(), pref.rex().getModRMRMExtension(), false);
				yield new Instruction(
						Opcode.MOVQ,
						pref.hasOperandSizeOverridePrefix()
								? RegisterXMM.fromByte(regByte)
								: RegisterMMX.fromByte(regByte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: r1);
			}
			case MOVQ_INDIRECT_XMM_OPCODE -> {
				final ModRM modrm = modrm();
				final byte regByte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				yield new Instruction(
						Opcode.MOVQ,
						parseIndirectOperand(pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build(),
						RegisterXMM.fromByte(regByte));
			}
			case MOVQ_R128_INDIRECT64_OPCODE -> {
				final ModRM modrm = modrm();
				final byte regByte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				yield new Instruction(
						Opcode.MOVQ,
						RegisterXMM.fromByte(regByte),
						parseIndirectOperand(pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
			}
			case MOVHLPS_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.MOVHLPS,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm())));
			}
			case MOVHPS_OPCODE -> {
				final ModRM modrm = modrm();
				final byte regByte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				yield new Instruction(
						Opcode.MOVHPS,
						RegisterXMM.fromByte(regByte),
						parseIndirectOperand(pref, modrm)
								.pointer(PointerSize.QWORD_PTR)
								.build());
			}
			case PAND_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						Opcode.PAND,
						RegisterXMM.fromByte(r1Byte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: RegisterXMM.fromByte(r2Byte));
			}
			case PADDQ_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						Opcode.PADDQ,
						RegisterXMM.fromByte(r1Byte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: RegisterXMM.fromByte(r2Byte));
			}
			case PSUBQ_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						Opcode.PSUBQ,
						RegisterXMM.fromByte(r1Byte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: RegisterXMM.fromByte(r2Byte));
			}
			case POR_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						Opcode.POR,
						RegisterXMM.fromByte(r1Byte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: RegisterXMM.fromByte(r2Byte));
			}
			case PXOR_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						Opcode.PXOR,
						RegisterXMM.fromByte(r1Byte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: RegisterXMM.fromByte(r2Byte));
			}
			case PCMPEQD_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(Opcode.PCMPEQD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case XORPS_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(Opcode.XORPS, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case ADDSD_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(Opcode.ADDSD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case DIVSD_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(Opcode.DIVSD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
			}
			case MOVAPx_R128_INDIRECT128_OPCODE -> {
				final ModRM modrm = modrm();
				final byte r1Byte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				final byte r2Byte = Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm());
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS,
						RegisterXMM.fromByte(r1Byte),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: RegisterXMM.fromByte(r2Byte));
			}
			case MOVAPx_INDIRECT128_R128_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS,
						parseIndirectOperand(pref, modrm).build(),
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())));
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
				final Opcode opcode = cmovOpcodes[BitUtils.and(opcodeSecondByte, (byte) 0b00001111)];
				final ModRM modrm = modrm();
				final Register r1 = Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().getModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix());
				yield new Instruction(
						opcode,
						r1,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().getModRMRMExtension(),
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
				final byte regByte = BitUtils.and(opcodeSecondByte, (byte) 0b00000111);
				yield new Instruction(
						Opcode.BSWAP,
						pref.rex().isOperand64Bit()
								? Register64.fromByte(
										Registers.combine(pref.rex().opcodeRegExtension(), regByte))
								: Register32.fromByte(
										Registers.combine(pref.rex().opcodeRegExtension(), regByte)));
			}
			case UD2_OPCODE -> new Instruction(Opcode.UD2);
			case PUNPCKLDQ_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.PUNPCKLDQ,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm())));
			}
			case PUNPCKLQDQ_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.PUNPCKLQDQ,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm())));
			}
			case PUNPCKHQDQ_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.PUNPCKHQDQ,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm())));
			}
			case MOVSD_OPCODE -> {
				final ModRM modrm = modrm();
				final boolean hasRepnePrefix =
						pref.p1().isPresent() && pref.p1().orElseThrow() == InstructionPrefix.REPNZ;
				yield new Instruction(
						hasRepnePrefix ? Opcode.MOVSD : Opcode.MOVUPS,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						parseIndirectOperand(pref, modrm)
								.pointer(hasRepnePrefix ? PointerSize.QWORD_PTR : PointerSize.XMMWORD_PTR)
								.build());
			}
			case MOVUPS_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.MOVUPS,
						parseIndirectOperand(pref, modrm).build(),
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())));
			}
			case CVTSI2SD_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.CVTSI2SD,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case UCOMISx_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						pref.hasOperandSizeOverridePrefix() ? Opcode.UCOMISD : Opcode.UCOMISS,
						RegisterXMM.fromByte(Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg())),
						parseIndirectOperand(pref, modrm)
								.pointer(
										pref.hasOperandSizeOverridePrefix()
												? PointerSize.QWORD_PTR
												: PointerSize.DWORD_PTR)
								.build());
			}
			default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		};
	}

	private Instruction parseExtendedOpcodeGroup15(
			final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm();

		if (modrm.mod() != MODRM_MOD_NO_DISP) {
			throw new IllegalArgumentException("Not implemented");
		}

		final byte INCSSPQOpcode = 0b00000101;
		if (modrm.reg() != INCSSPQOpcode) {
			throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
		}

		return new Instruction(
				Opcode.INCSSPQ, Registers.fromCode(modrm.rm(), true, pref.rex().getModRMRMExtension(), false));
	}

	private Instruction parseExtendedOpcodeGroup9(
			final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
		final ModRM modrm = modrm();

		if (modrm.mod() != MODRM_MOD_NO_DISP) {
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
						pref.rex().getModRMRMExtension(),
						pref.hasOperandSizeOverridePrefix()));
	}

	private Instruction parseSingleByteOpcode(final byte opcodeFirstByte, final Prefixes pref) {
		final byte OPCODE_REG_MASK = 0b00000111;

		final byte ADD_INDIRECT8_R8_OPCODE = (byte) 0x00;
		final byte ADD_INDIRECT32_R32_OPCODE = (byte) 0x01;
		final byte ADD_R8_INDIRECT8_OPCODE = (byte) 0x02;
		final byte ADD_R32_INDIRECT32_OPCODE = 0b00000011;
		final byte ADD_AL_IMM8_OPCODE = (byte) 0x04;
		final byte ADD_EAX_IMM32_OPCODE = (byte) 0x05;
		final byte OR_INDIRECT8_R8_OPCODE = (byte) 0x08;
		final byte OR_INDIRECT32_R32_OPCODE = (byte) 0x09;
		final byte OR_R8_INDIRECT8_OPCODE = (byte) 0x0a;
		final byte OR_R32_INDIRECT32_OPCODE = (byte) 0x0b;
		final byte OR_AL_IMM8_OPCODE = (byte) 0x0c;
		final byte OR_EAX_IMM32_OPCODE = (byte) 0x0d;
		final byte ADC_INDIRECT8_R8_OPCODE = (byte) 0x10;
		final byte ADC_INDIRECT32_R32_OPCODE = (byte) 0x11;
		final byte ADC_R8_INDIRECT8_OPCODE = (byte) 0x12;
		final byte ADC_R32_INDIRECT32_OPCODE = (byte) 0x13;
		final byte ADC_AL_IMM8_OPCODE = (byte) 0x14;
		final byte ADC_EAX_IMM32_OPCODE = (byte) 0x15;
		final byte SBB_INDIRECT8_R8_OPCODE = (byte) 0x18;
		final byte SBB_INDIRECT32_R32_OPCODE = (byte) 0x19;
		final byte SBB_R8_INDIRECT8_OPCODE = (byte) 0x1a;
		final byte SBB_R32_INDIRECT32_OPCODE = (byte) 0x1b;
		final byte SBB_AL_IMM8_OPCODE = (byte) 0x1c;
		final byte SBB_EAX_IMM32_OPCODE = (byte) 0x1d;
		final byte AND_INDIRECT8_R8_OPCODE = (byte) 0x20;
		final byte AND_INDIRECT32_R32_OPCODE = (byte) 0x21;
		final byte AND_R8_INDIRECT8_OPCODE = (byte) 0x22;
		final byte AND_R32_INDIRECT32_OPCODE = (byte) 0x23;
		final byte AND_AL_IMM8_OPCODE = (byte) 0x24;
		final byte AND_EAX_IMM32_OPCODE = (byte) 0x25;
		final byte SUB_INDIRECT8_R8_OPCODE = (byte) 0x28;
		final byte SUB_INDIRECT32_R32_OPCODE = (byte) 0x29;
		final byte SUB_R8_INDIRECT8_OPCODE = (byte) 0x2a;
		final byte SUB_R32_INDIRECT32_OPCODE = (byte) 0x2b;
		final byte SUB_AL_IMM8_OPCODE = (byte) 0x2c;
		final byte SUB_EAX_IMM32_OPCODE = (byte) 0x2d;
		final byte XOR_INDIRECT8_R8_OPCODE = (byte) 0x30;
		final byte XOR_INDIRECT32_R32_OPCODE = (byte) 0x31;
		final byte XOR_R8_INDIRECT8_OPCODE = (byte) 0x32;
		final byte XOR_R32_INDIRECT32_OPCODE = (byte) 0x33;
		final byte XOR_AL_IMM8_OPCODE = (byte) 0x34;
		final byte XOR_EAX_IMM32_OPCODE = (byte) 0x35;
		final byte CMP_INDIRECT8_R8_OPCODE = (byte) 0x38;
		final byte CMP_INDIRECT32_R32_OPCODE = (byte) 0x39;
		final byte CMP_R8_INDIRECT8_OPCODE = (byte) 0x3a;
		final byte CMP_R32_INDIRECT32_OPCODE = (byte) 0x3b;
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
		final byte IMUL_R32_INDIRECT32_IMM32_OPCODE = (byte) 0x69;
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
		final byte TEST_R32_R32_OPCODE = (byte) 0x85; // this can work on all non 8-bit registers
		final byte XCHG_INDIRECT8_R8_OPCODE = (byte) 0x86;
		final byte XCHG_INDIRECT32_R32_OPCODE = (byte) 0x87;
		final byte MOV_MEM8_REG8_OPCODE = (byte) 0x88;
		final byte MOV_INDIRECT32_R32_OPCODE = (byte) 0x89;
		final byte MOV_R8_INDIRECT8_OPCODE = (byte) 0x8a;
		final byte MOV_R32_INDIRECT32_OPCODE = (byte) 0x8b;
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

		final Opcode[] opcodeTable = new Opcode[] {
			Opcode.ADD, Opcode.OR, Opcode.ADC, Opcode.SBB, Opcode.AND, Opcode.SUB, Opcode.XOR, Opcode.CMP
		};

		return switch (opcodeFirstByte) {
			case NOP_OPCODE -> pref.hasRexPrefix()
					? new Instruction(Opcode.XCHG, Register64.R8, Register64.RAX)
					: new Instruction(Opcode.NOP);
			case RET_OPCODE -> new Instruction(Opcode.RET);
			case LEAVE_OPCODE -> new Instruction(Opcode.LEAVE);
			case INT3_OPCODE -> new Instruction(Opcode.INT3);
			case CDQ_OPCODE -> new Instruction(Opcode.CDQ);
			case SAHF_OPCODE -> new Instruction(Opcode.SAHF);
			case LAHF_OPCODE -> new Instruction(Opcode.LAHF);
			case HLT_OPCODE -> new Instruction(Opcode.HLT);
			case CDQE_OPCODE -> new Instruction(pref.rex().isOperand64Bit() ? Opcode.CDQE : Opcode.CWDE);

			case MOV_R32_INDIRECT32_OPCODE -> parseRM(pref, Opcode.MOV);
			case MOV_INDIRECT32_R32_OPCODE -> parseMR(pref, Opcode.MOV);
			case MOV_MEM8_REG8_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.MOV,
						parseIndirectOperand(pref, modrm).build(),
						Register8.fromByte(
								Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg()),
								pref.hasRexPrefix()));
			}
			case MOV_R8_INDIRECT8_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.MOV,
						Register8.fromByte(
								Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()),
						parseIndirectOperand(pref, modrm).build());
			}
			case TEST_R8_R8_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.TEST,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Register8.fromByte(
										Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
										pref.hasRexPrefix()),
						Register8.fromByte(
								Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg()),
								pref.hasRexPrefix()));
			}
			case TEST_R32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.TEST,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().getModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}
			case TEST_AL_IMM8_OPCODE -> new Instruction(Opcode.TEST, Register8.AL, imm8());
			case TEST_EAX_IMM32_OPCODE -> new Instruction(
					Opcode.TEST,
					pref.hasOperandSizeOverridePrefix()
							? Register16.AX
							: (pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX),
					pref.hasOperandSizeOverridePrefix() ? imm16() : imm32());
			case XCHG_INDIRECT8_R8_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.XCHG,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Register8.fromByte(
										Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
										pref.hasRexPrefix()),
						Register8.fromByte(
								Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg()),
								pref.hasRexPrefix()));
			}
			case XCHG_INDIRECT32_R32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.XCHG,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Registers.fromCode(
										modrm.rm(),
										pref.rex().isOperand64Bit(),
										pref.rex().getModRMRMExtension(),
										pref.hasOperandSizeOverridePrefix()),
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()));
			}

				// jumps
			case JMP_DISP32_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of(b.read4LE()));
			case JMP_DISP8_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of(b.read1()));
			case JB_DISP8_OPCODE -> new Instruction(Opcode.JB, RelativeOffset.of(b.read1()));
			case JAE_DISP8_OPCODE -> new Instruction(Opcode.JAE, RelativeOffset.of(b.read1()));
			case JE_DISP8_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of(b.read1()));
			case JA_DISP8_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of(b.read1()));
			case JNE_DISP8_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of(b.read1()));
			case JBE_DISP8_OPCODE -> new Instruction(Opcode.JBE, RelativeOffset.of(b.read1()));
			case JS_DISP8_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of(b.read1()));
			case JNS_DISP8_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of(b.read1()));
			case JP_DISP8_OPCODE -> new Instruction(Opcode.JP, RelativeOffset.of(b.read1()));
			case JL_DISP8_OPCODE -> new Instruction(Opcode.JL, RelativeOffset.of(b.read1()));
			case JGE_DISP8_OPCODE -> new Instruction(Opcode.JGE, RelativeOffset.of(b.read1()));
			case JLE_DISP8_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of(b.read1()));
			case JG_DISP8_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of(b.read1()));

			case CALL_OPCODE -> new Instruction(Opcode.CALL, RelativeOffset.of(b.read4LE()));

			case IMUL_REG_REG_IMM8_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.IMUL,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								false),
						Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								false),
						imm8());
			}
			case PUSH_IMM32_OPCODE -> new Instruction(Opcode.PUSH, imm32());
			case IMUL_R32_INDIRECT32_IMM32_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.IMUL,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								false),
						parseIndirectOperand(pref, modrm).build(),
						imm32());
			}
			case MOVS_ES_EDI_DS_ESI_BYTE_PTR_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.reg2(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.pointer(PointerSize.BYTE_PTR)
						.build();
				final Operand op2 = IndirectOperand.builder()
						.reg2(new SegmentRegister(
								Register16.DS, pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI))
						.pointer(PointerSize.BYTE_PTR)
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.MOVS, op1, op2);
			}
			case MOVS_ES_EDI_DS_ESI_OPCODE -> {
				final PointerSize size =
						pref.hasOperandSizeOverridePrefix() ? PointerSize.WORD_PTR : PointerSize.DWORD_PTR;
				final Operand op1 = IndirectOperand.builder()
						.reg2(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.pointer(size)
						.build();
				final Operand op2 = IndirectOperand.builder()
						.reg2(new SegmentRegister(
								Register16.DS, pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI))
						.pointer(size)
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.MOVS, op1, op2);
			}
			case STOS_R8_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.reg2(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.build();
				yield new Instruction(pref.p1().orElse(null), Opcode.STOS, op1, Register8.AL);
			}
			case STOS_R32_OPCODE -> {
				final Operand op1 = IndirectOperand.builder()
						.reg2(new SegmentRegister(
								Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
						.build();
				final Operand op2 = pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX;
				yield new Instruction(pref.p1().orElse(null), Opcode.STOS, op1, op2);
			}
			case MOVSXD_OPCODE -> {
				final ModRM modrm = modrm();
				yield new Instruction(
						Opcode.MOVSXD,
						Registers.fromCode(
								modrm.reg(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRegExtension(),
								pref.hasOperandSizeOverridePrefix()),
						parseIndirectOperand(pref, modrm)
								.pointer(PointerSize.DWORD_PTR)
								.build());
			}

				// OP Indirect8,R8
			case ADD_INDIRECT8_R8_OPCODE,
					ADC_INDIRECT8_R8_OPCODE,
					AND_INDIRECT8_R8_OPCODE,
					XOR_INDIRECT8_R8_OPCODE,
					OR_INDIRECT8_R8_OPCODE,
					SBB_INDIRECT8_R8_OPCODE,
					SUB_INDIRECT8_R8_OPCODE,
					CMP_INDIRECT8_R8_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0b00000000) {
					invalidValue();
				}
				final byte m2 = (byte) 0b00111000; // (the inverse of m1)
				final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				final ModRM modrm = modrm();
				final byte regByte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				yield new Instruction(
						opcode,
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Register8.fromByte(
										Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
										pref.hasRexPrefix()),
						Register8.fromByte(regByte, pref.hasRexPrefix()));
			}

				// OP Indirect32,R32
			case ADD_INDIRECT32_R32_OPCODE,
					ADC_INDIRECT32_R32_OPCODE,
					AND_INDIRECT32_R32_OPCODE,
					XOR_INDIRECT32_R32_OPCODE,
					OR_INDIRECT32_R32_OPCODE,
					SBB_INDIRECT32_R32_OPCODE,
					SUB_INDIRECT32_R32_OPCODE,
					CMP_INDIRECT32_R32_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0b00000001) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				yield parseMR(pref, opcode);
			}

				// OP R8,Indirect8
			case ADD_R8_INDIRECT8_OPCODE,
					ADC_R8_INDIRECT8_OPCODE,
					AND_R8_INDIRECT8_OPCODE,
					XOR_R8_INDIRECT8_OPCODE,
					OR_R8_INDIRECT8_OPCODE,
					SBB_R8_INDIRECT8_OPCODE,
					SUB_R8_INDIRECT8_OPCODE,
					CMP_R8_INDIRECT8_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0b00000010) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				final ModRM modrm = modrm();
				final byte regByte = Registers.combine(pref.rex().getModRMRegExtension(), modrm.reg());
				yield new Instruction(
						opcode,
						Register8.fromByte(regByte, pref.hasRexPrefix()),
						(modrm.mod() != MODRM_MOD_NO_DISP)
								? parseIndirectOperand(pref, modrm).build()
								: Register8.fromByte(
										Registers.combine(pref.rex().getModRMRMExtension(), modrm.rm()),
										pref.hasRexPrefix()));
			}

				// OP R32,Indirect32
			case ADD_R32_INDIRECT32_OPCODE,
					ADC_R32_INDIRECT32_OPCODE,
					AND_R32_INDIRECT32_OPCODE,
					XOR_R32_INDIRECT32_OPCODE,
					OR_R32_INDIRECT32_OPCODE,
					SBB_R32_INDIRECT32_OPCODE,
					SUB_R32_INDIRECT32_OPCODE,
					CMP_R32_INDIRECT32_OPCODE -> {
				// (just to check that we are doing the correct thing)
				final byte m1 = (byte) 0b11000111;
				if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0b00000011) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				yield parseRM(pref, opcode);
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
				if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0b00000100) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				yield new Instruction(opcode, Register8.AL, imm8());
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
				if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0b00000101) {
					invalidValue();
				}
				final byte m2 = ~m1;
				final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
				final Opcode opcode = opcodeTable[opcodeByte];
				final Register r = pref.hasOperandSizeOverridePrefix()
						? Register16.AX
						: (pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX);
				final Immediate imm;
				if (pref.hasOperandSizeOverridePrefix()) {
					imm = switch (r.bits()) {
						case 16 -> imm16();
						case 32 -> new Immediate((int) b.read2LE());
						default -> new Immediate((long) b.read2LE());
					};
				} else {
					imm = r.bits() == 32 ? imm32() : new Immediate((long) b.read4LE());
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
				final byte regByte = BitUtils.and(opcodeFirstByte, OPCODE_REG_MASK);
				yield new Instruction(
						Opcode.MOV,
						Register8.fromByte(
								Registers.combine(pref.rex().opcodeRegExtension(), regByte), pref.hasRexPrefix()),
						imm8());
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
				final byte regByte = Registers.combine(
						pref.rex().opcodeRegExtension(), BitUtils.and(opcodeFirstByte, OPCODE_REG_MASK));
				final int size =
						pref.hasOperandSizeOverridePrefix() ? 16 : (pref.rex().isOperand64Bit() ? 64 : 32);
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
							case 16 -> imm16();
							case 32 -> imm32();
							case 64 -> imm64();
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
				final byte regByte = BitUtils.and(opcodeFirstByte, OPCODE_REG_MASK);
				yield new Instruction(
						Opcode.XCHG,
						Registers.fromCode(
								regByte,
								pref.rex().isOperand64Bit(),
								pref.rex().opcodeRegExtension(),
								pref.hasOperandSizeOverridePrefix()),
						pref.hasOperandSizeOverridePrefix()
								? Register16.AX
								: (pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX));
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
				final byte regByte = BitUtils.and(opcodeFirstByte, OPCODE_REG_MASK);
				yield new Instruction(
						Opcode.PUSH,
						Registers.fromCode(
								regByte, true, pref.rex().opcodeRegExtension(), pref.hasOperandSizeOverridePrefix()));
			}

			case PUSH_IMM8_OPCODE -> new Instruction(Opcode.PUSH, imm8());

				// POP 16/64-bit
			case POP_EAX_OPCODE,
					POP_EBX_OPCODE,
					POP_ECX_OPCODE,
					POP_EDX_OPCODE,
					POP_ESI_OPCODE,
					POP_EDI_OPCODE,
					POP_ESP_OPCODE,
					POP_EBP_OPCODE -> {
				final byte regByte = BitUtils.and(opcodeFirstByte, OPCODE_REG_MASK);
				yield new Instruction(
						Opcode.POP,
						Registers.fromCode(
								regByte, true, pref.rex().opcodeRegExtension(), pref.hasOperandSizeOverridePrefix()));
			}

			case LEA_OPCODE -> parseRM(pref, Opcode.LEA);

			case OPERAND_SIZE_OVERRIDE_PREFIX -> throw new UnrecognizedPrefix("operand size override", b.getPosition());
			case ADDRESS_SIZE_OVERRIDE_PREFIX -> throw new UnrecognizedPrefix("address size override", b.getPosition());
			case (byte) 0xf0 -> throw new UnrecognizedPrefix("LOCK", b.getPosition());
			case (byte) 0xf2 -> throw new UnrecognizedPrefix("REPNE", b.getPosition());
			case (byte) 0xf3 -> throw new UnrecognizedPrefix("REP", b.getPosition());
			default -> {
				final long pos = b.getPosition();
				logger.debug(
						"Unknown opcode: 0x%02x 0x%02x 0x%02x 0x%02x",
						opcodeFirstByte, b.read1(), b.read1(), b.read1());
				b.setPosition(pos);
				throw new UnknownOpcode(opcodeFirstByte);
			}
		};
	}

	private void invalidValue() {
		throw new IllegalArgumentException("Invalid value");
	}

	private Prefixes parsePrefixes() {
		Optional<InstructionPrefix> p1 = Optional.empty(); // Legacy Prefix Group 1
		Optional<Byte> p2 = Optional.empty(); // Legacy Prefix Group 2
		boolean hasOperandSizeOverridePrefix = false;
		boolean hasAddressSizeOverridePrefix = false;

		// FIXME: is there a better way to do this?
		// (technically there is no limit to the number of prefixes an instruction can
		// have)
		while (true) {
			byte x = b.read1();

			if (isLegacyPrefixGroup1(x)) {
				p1 = Optional.of(InstructionPrefix.fromByte(x));
			} else if (isLegacyPrefixGroup2(x)) {
				p2 = Optional.of(x);
			} else if (isOperandSizeOverridePrefix(x)) {
				hasOperandSizeOverridePrefix = true;
			} else if (isAddressSizeOverridePrefix(x)) {
				hasAddressSizeOverridePrefix = true;
			} else {
				b.setPosition(b.getPosition() - 1);
				break;
			}
		}

		final byte rexByte = b.read1();
		final RexPrefix rexPrefix;
		final boolean isREX = RexPrefix.isREXPrefix(rexByte);
		if (isREX) {
			rexPrefix = new RexPrefix(rexByte);
		} else {
			rexPrefix = new RexPrefix((byte) 0x40);
			b.setPosition(b.getPosition() - 1);
		}

		return new Prefixes(p1, p2, hasOperandSizeOverridePrefix, hasAddressSizeOverridePrefix, isREX, rexPrefix);
	}

	// Parses an instruction like OP Indirect,RXX
	private Instruction parseMR(final Prefixes pref, final Opcode opcode) {
		final ModRM modrm = modrm();
		return new Instruction(
				opcode,
				(modrm.mod() != MODRM_MOD_NO_DISP)
						? parseIndirectOperand(pref, modrm).build()
						: Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()),
				Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().getModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix()));
	}

	// Parses an instruction like OP RXX,Indirect
	private Instruction parseRM(final Prefixes pref, final Opcode opcode) {
		final ModRM modrm = modrm();
		final boolean isIndirectOperandNeeded = modrm.mod() != MODRM_MOD_NO_DISP;
		return new Instruction(
				opcode,
				Registers.fromCode(
						modrm.reg(),
						pref.rex().isOperand64Bit(),
						pref.rex().getModRMRegExtension(),
						pref.hasOperandSizeOverridePrefix()),
				isIndirectOperandNeeded
						? parseIndirectOperand(pref, modrm).build()
						: Registers.fromCode(
								modrm.rm(),
								pref.rex().isOperand64Bit(),
								pref.rex().getModRMRMExtension(),
								pref.hasOperandSizeOverridePrefix()));
	}

	private Instruction parseMOV(
			final Prefixes pref, final ModRM modrm, final int immediateBits, final PointerSize pointerSize) {
		final boolean hasOperandSizeOverridePrefix = pref.hasOperandSizeOverridePrefix();
		final RexPrefix rexPrefix = pref.rex();
		final byte rm = modrm.rm();
		Operand operand1;

		// Table at page 530
		final byte mod = modrm.mod();
		if (mod < (byte) 0x00 || mod > 0b00000011) {
			throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
		}

		final IndirectOperandBuilder iob = parseIndirectOperand(pref, modrm);

		iob.pointer(pointerSize);

		if (mod != MODRM_MOD_NO_DISP) {
			// indirect operand needed
			operand1 = iob.build();
		} else {
			// indirect operand not needed, so we take the second operand without using the
			// addressSizeOverride
			operand1 = Registers.fromCode(
					rm, rexPrefix.isOperand64Bit(), rexPrefix.getModRMRMExtension(), hasOperandSizeOverridePrefix);
		}

		return switch (immediateBits) {
			case 8 -> new Instruction(Opcode.MOV, operand1, imm8());
			case 16 -> new Instruction(Opcode.MOV, operand1, imm16());
			case 32 -> new Instruction(Opcode.MOV, operand1, imm32());
			default -> throw new IllegalArgumentException(
					String.format("Invalid value for immediate bytes: %,d", immediateBits));
		};
	}

	private IndirectOperandBuilder parseIndirectOperand(final Prefixes pref, final ModRM modrm) {
		Operand operand2 = Registers.fromCode(
				modrm.rm(), !pref.hasAddressSizeOverridePrefix(), pref.rex().getModRMRMExtension(), false);
		final RexPrefix rexPrefix = pref.rex();
		final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
		final IndirectOperandBuilder iob = IndirectOperand.builder();
		SIB sib;
		if (modrm.mod() != MODRM_MOD_NO_DISP && modrm.rm() == 0b00000100) {
			// SIB needed
			sib = sib();

			final Register base =
					Registers.fromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension(), false);
			final Register index = Registers.fromCode(
					sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension(), false);
			// an indirect operand of [xxx+rsp+...] is not allowed
			if (index.toIntelSyntax().endsWith("sp")) {
				operand2 = base;
			} else {
				if (!(modrm.mod() == (byte) 0x00 && base.toIntelSyntax().endsWith("bp"))) {
					iob.reg1(base);
				}
				operand2 = index;
				iob.constant(1 << BitUtils.asInt(sib.scale()));
			}
		} else {
			// SIB not needed
			sib = new SIB((byte) 0x00);
			if (modrm.mod() == (byte) 0x00 && operand2.toIntelSyntax().endsWith("bp")) {
				operand2 = hasAddressSizeOverridePrefix ? Register32.EIP : Register64.RIP;
			}
		}

		if (pref.p2().isPresent() && pref.p2().orElseThrow() == (byte) 0x2e) {
			operand2 = new SegmentRegister(Register16.CS, (Register) operand2);
		}
		iob.reg2((Register) operand2);

		if (modrm.mod() != MODRM_MOD_NO_DISP) {
			// indirect operand needed
			if (modrm.mod() == (byte) 0x00 && modrm.rm() == (byte) 0x05
					|| modrm.mod() == (byte) 0x00 && sib.base() == (byte) 0x05
					|| modrm.mod() == (byte) 0x02) {
				final int disp32 = b.read4LE();
				iob.disp(disp32);
			} else if (modrm.mod() == (byte) 0x01) {
				final byte disp8 = b.read1();
				iob.disp(disp8);
			}
		}

		return iob;
	}

	private boolean isExtendedOpcode(final byte opcode) {
		/*
		 * The extended opcodes are 0x80, 0x81, 0x82, 0x83.
		 * So the mask to be used is 0xfc (11111100).
		 */
		final byte extendedOpcodeMask = (byte) 0b11111100;
		final byte expectedBits = (byte) 0b10000000;
		return BitUtils.and(opcode, extendedOpcodeMask) == expectedBits;
	}

	private ModRM modrm() {
		final byte m = b.read1();
		return new ModRM(m);
	}

	private SIB sib() {
		final byte s = b.read1();
		return new SIB(s);
	}

	private Immediate imm8() {
		return new Immediate(b.read1());
	}

	private Immediate imm16() {
		return new Immediate(b.read2LE());
	}

	private Immediate imm32() {
		return new Immediate(b.read4LE());
	}

	private Immediate imm64() {
		return new Immediate(b.read8LE());
	}

	// TODO: remove when not used anymore
	private void notImplemented() {
		throw new Error("Not implemented");
	}

	private boolean isLegacyPrefixGroup1(final byte prefix) {
		return prefix == InstructionPrefix.LOCK.getCode()
				|| prefix == InstructionPrefix.REPNZ.getCode()
				|| prefix == InstructionPrefix.REP.getCode();
	}

	private boolean isLegacyPrefixGroup2(final byte prefix) {
		final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
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

	private boolean isOperandSizeOverridePrefix(final byte prefix) {
		return prefix == OPERAND_SIZE_OVERRIDE_PREFIX;
	}

	private boolean isAddressSizeOverridePrefix(final byte prefix) {
		return prefix == ADDRESS_SIZE_OVERRIDE_PREFIX;
	}

	private boolean isMultibyteOpcode(final byte opcode) {
		return opcode == MULTIBYTE_OPCODE_PREFIX;
	}

	@Override
	public String toString() {
		return "InstructionDecoderV1(b=" + b + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + b.hashCode();
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
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final InstructionDecoderV1 id = (InstructionDecoderV1) other;
		return this.b.equals(id.b);
	}
}
