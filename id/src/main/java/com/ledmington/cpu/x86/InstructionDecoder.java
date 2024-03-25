package com.ledmington.cpu.x86;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ledmington.cpu.x86.IndirectOperand.IndirectOperandBuilder;
import com.ledmington.cpu.x86.Instruction.Prefix;
import com.ledmington.cpu.x86.exc.ReservedOpcode;
import com.ledmington.cpu.x86.exc.UnknownOpcode;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ByteBuffer;
import com.ledmington.utils.MiniLogger;

/**
 * Reference Intel® 64 and IA-32 Architectures Software Developer's Manual
 * volume 2.
 * Legacy prefixes : Paragraph 2.1.1.
 * Instruction opcodes : Appendix A. (pag. 2839)
 */
public final class InstructionDecoder {

    private static final MiniLogger logger = MiniLogger.getLogger("x86-asm");
    private ByteBuffer b = null;

    public InstructionDecoder() {}

    public List<Instruction> decode(final byte[] code) {
        return decode(code, false);
    }

    public List<Instruction> decode(final byte[] code, final boolean isLittleEndian) {
        this.b = new ByteBuffer(code, isLittleEndian);
        final int length = code.length;
        logger.info("The code is %,d bytes long", length);

        final List<Instruction> instructions = new ArrayList<>();
        while (b.position() < length) {
            final int pos = b.position();
            final Instruction inst = decodeInstruction();
            { // Debugging info
                final int codeLen = b.position() - pos;
                final StringBuilder sb = new StringBuilder();
                b.setPosition(pos);
                sb.append(String.format("%02x", b.read1()));
                for (int i = 1; i < codeLen; i++) {
                    sb.append(String.format(" %02x", b.read1()));
                }
                logger.info("%08x: %-24s %s", pos, sb.toString(), inst.toIntelSyntax());
            }
            instructions.add(inst);
        }

        return instructions;
    }

    private Instruction decodeInstruction() {
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
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        return switch (modrm.reg()) {
            case 0 -> new Instruction(
                    Opcode.INC,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(pref.hasOperandSizeOverridePrefix() ? 16 : 8)
                                    .build()
                            : Register8.fromByte(
                                    Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()), pref.hasRexPrefix()));
            case 1 -> new Instruction(
                    Opcode.DEC,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(pref.hasOperandSizeOverridePrefix() ? 16 : 8)
                                    .build()
                            : Register8.fromByte(
                                    Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()), pref.hasRexPrefix()));
            case 2, 3, 4, 5, 6, 7 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup5(final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        return switch (modrm.reg()) {
            case (byte) 0x00 /* 000 */ -> new Instruction(
                    Opcode.INC,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(
                                            pref.hasOperandSizeOverridePrefix()
                                                    ? 16
                                                    : (pref.rex().isOperand64Bit() ? 64 : 32))
                                    .build()
                            : Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix()));
            case (byte) 0x01 /* 001 */ -> new Instruction(
                    Opcode.DEC,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(
                                            pref.hasOperandSizeOverridePrefix()
                                                    ? 16
                                                    : (pref.rex().isOperand64Bit() ? 64 : 32))
                                    .build()
                            : Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix()));
            case (byte) 0x02 /* 010 */ -> {
                // near CALL
                final Register reg = Registers.fromCode(
                        modrm.rm(),
                        !pref.hasAddressSizeOverridePrefix(),
                        pref.rex().ModRMRMExtension(),
                        false);
                yield (modrm.mod() != (byte) 0x03) // indirect operand needed
                        ? (new Instruction(
                                Opcode.CALL,
                                parseIndirectOperand(pref, modrm)
                                        .ptrSize(
                                                pref.hasOperandSizeOverridePrefix()
                                                        ? 16
                                                        : (pref.hasAddressSizeOverridePrefix() ? 64 : reg.bits()))
                                        .build()))
                        : (new Instruction(Opcode.CALL, reg));
            }
            case (byte) 0x03 /* 011 */ -> // far CALL
            new Instruction(
                    Opcode.CALL,
                    parseIndirectOperand(pref, modrm)
                            .ptrSize(pref.hasOperandSizeOverridePrefix() ? 32 : 64)
                            .build());
            case (byte) 0x04 /* 100 */ -> new Instruction(
                    Opcode.JMP,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(pref.hasOperandSizeOverridePrefix() ? 16 : 64)
                                    .build()
                            : Registers.fromCode(
                                    modrm.rm(), true,
                                    pref.rex().ModRMRMExtension(), false));
            case (byte) 0x05 /* 101 */ -> new Instruction(
                    Opcode.JMP,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(
                                            pref.hasAddressSizeOverridePrefix()
                                                    ? 16
                                                    : (pref.hasOperandSizeOverridePrefix() ? 32 : 64))
                                    .build()
                            : Registers.fromCode(
                                    modrm.rm(), true,
                                    pref.rex().ModRMRMExtension(), false));
            case (byte) 0x06 /* 110 */ -> new Instruction(
                    Opcode.PUSH, parseIndirectOperand(pref, modrm).ptrSize(64).build());
            case (byte) 0x07 /* 111 */ -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup3(final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final boolean isRegister8Bit = opcodeFirstByte == (byte) 0xf6;

        final boolean isIndirectOperandNeeded = modrm.mod() != (byte) 0x03;

        return switch (modrm.reg()) {
            case (byte) 0x00 /* 000 */ -> new Instruction(
                    Opcode.TEST,
                    isIndirectOperandNeeded
                            ? parseIndirectOperand(pref, modrm).build()
                            : (isRegister8Bit
                                    ? Register8.fromByte(
                                            Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                            pref.hasRexPrefix())
                                    : Registers.fromCode(
                                            modrm.rm(),
                                            pref.rex().isOperand64Bit(),
                                            pref.rex().ModRMRMExtension(),
                                            pref.hasOperandSizeOverridePrefix())),
                    isRegister8Bit ? imm8() : (pref.hasOperandSizeOverridePrefix() ? imm16() : imm32()));
            case (byte) 0x02 /* 010 */ -> new Instruction(
                    Opcode.NOT,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()));
            case (byte) 0x03 /* 011 */ -> {
                final Register r = Registers.fromCode(
                        modrm.rm(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRMExtension(),
                        pref.hasOperandSizeOverridePrefix());
                yield new Instruction(
                        Opcode.NEG,
                        isIndirectOperandNeeded
                                ? parseIndirectOperand(pref, modrm)
                                        .ptrSize(pref.rex().isOperand64Bit() ? 64 : 32)
                                        .build()
                                : r);
            }
            case (byte) 0x04 /* 100 */ -> new Instruction(
                    Opcode.MUL,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(
                                            (opcodeFirstByte == (byte) 0xf6)
                                                    ? 8
                                                    : (pref.hasOperandSizeOverridePrefix()
                                                            ? 16
                                                            : (pref.rex().isOperand64Bit() ? 64 : 32)))
                                    .build()
                            : ((opcodeFirstByte == (byte) 0xf6)
                                    ?
                                    // R8
                                    Register8.fromByte(
                                            Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                            pref.hasRexPrefix())
                                    : Registers.fromCode(
                                            modrm.rm(),
                                            pref.rex().isOperand64Bit(),
                                            pref.rex().ModRMRMExtension(),
                                            pref.hasOperandSizeOverridePrefix())));
            case (byte) 0x06 /* 110 */ -> new Instruction(
                    Opcode.DIV,
                    (modrm.mod() != (byte) 0x03)
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(
                                            (opcodeFirstByte == (byte) 0xf6)
                                                    ? 8
                                                    : (pref.hasOperandSizeOverridePrefix()
                                                            ? 16
                                                            : (pref.rex().isOperand64Bit() ? 64 : 32)))
                                    .build()
                            : ((opcodeFirstByte == (byte) 0xf6)
                                    ?
                                    // R8
                                    Register8.fromByte(
                                            Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                            pref.hasRexPrefix())
                                    : Registers.fromCode(
                                            modrm.rm(),
                                            pref.rex().isOperand64Bit(),
                                            pref.rex().ModRMRMExtension(),
                                            pref.hasOperandSizeOverridePrefix())));
            case (byte) 0x07 /* 111 */ -> new Instruction(
                    Opcode.IDIV,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()));
            case (byte) 0x01 /* 001 */ -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup11(final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final int immediateBits =
                pref.hasOperandSizeOverridePrefix() ? 16 : ((opcodeFirstByte == (byte) 0xc6) ? 8 : 32);

        return switch (modrm.reg()) {
            case (byte) 0x00 /* 000 */ -> parse(
                    pref,
                    modrm,
                    Optional.of(immediateBits),
                    Opcode.MOV,
                    Optional.of(pref.rex().isOperand64Bit() ? 64 : immediateBits));
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup2(final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final Operand op2 = (opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xc1)
                ? imm8()
                : ((opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd1)
                        ? new Immediate(1)
                        : Register8.CL);
        final boolean reg8bit =
                opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xd0 || opcodeFirstByte == (byte) 0xd2;

        return switch (modrm.reg()) {
            case (byte) 0x04 /* 100 */ -> new Instruction(
                    Opcode.SHL,
                    reg8bit
                            ? (Register8.fromByte(
                                    Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()), pref.hasRexPrefix()))
                            : (Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix())),
                    op2);
            case (byte) 0x05 /* 101 */ -> new Instruction(
                    Opcode.SHR,
                    reg8bit
                            ? (Register8.fromByte(
                                    Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()), pref.hasRexPrefix()))
                            : (Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix())),
                    op2);
            case (byte) 0x06 /* 110 */ -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
            case (byte) 0x07 /* 111 */ -> new Instruction(
                    Opcode.SAR,
                    reg8bit
                            ? (Register8.fromByte(
                                    Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()), pref.hasRexPrefix()))
                            : (Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix())),
                    op2);
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup1(final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final boolean isRegister8Bit = opcodeFirstByte == (byte) 0x80 || opcodeFirstByte == (byte) 0x82;
        final int immediateBits =
                (opcodeFirstByte != (byte) 0x81) ? 8 : (pref.hasOperandSizeOverridePrefix() ? 16 : 32);
        final boolean isIndirectOperandNeeded = modrm.mod() != (byte) 0x03;
        final byte regByte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());

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
                                    .ptrSize(pref.rex().isOperand64Bit() ? 64 : immediateBits)
                                    .build()
                            : Register8.fromByte(regByte, pref.hasRexPrefix()),
                    imm8());
        } else {
            logger.debug("immediate bits: %,d", immediateBits);
            return new Instruction(
                    opcode,
                    isIndirectOperandNeeded
                            ? parseIndirectOperand(pref, modrm)
                                    .ptrSize(
                                            pref.hasOperandSizeOverridePrefix()
                                                    ? 16
                                                    : (pref.rex().isOperand64Bit() ? 64 : immediateBits))
                                    .build()
                            : Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix()),
                    switch (immediateBits) {
                        case 8 -> imm8();
                        case 16 -> imm16();
                        case 32 -> imm32();
                        default -> throw new IllegalArgumentException(
                                String.format("Invalid value of immediate bits: %,d", immediateBits));
                    });
        }
    }

    private Instruction parseExtendedOpcodeGroup7(
            final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
        final ModRM modrm = modrm();

        if (modrm.mod() == (byte) 0x03) {
            return switch (modrm.reg()) {
                case (byte) 0x02 /* 010 */ -> {
                    yield switch (modrm.rm()) {
                        case (byte) 0x00 /* 000 */ -> new Instruction(Opcode.XGETBV);
                        default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
                    };
                }
                default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
            };
        } else {
            throw new RuntimeException("Not implemented");
        }
    }

    private Instruction parseExtendedOpcodeGroup16(
            final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
        final ModRM modrm = modrm();

        if (modrm.mod() == (byte) 0x03 /* 11 */) {
            throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
        }

        final Opcode[] opcodes =
                new Opcode[] {Opcode.PREFETCHNTA, Opcode.PREFETCHT0, Opcode.PREFETCHT1, Opcode.PREFETCHT2};

        return switch (modrm.reg()) {
            case 0, 1, 2, 3 -> new Instruction(
                    opcodes[BitUtils.and(modrm.reg(), (byte) 0x03)],
                    parseIndirectOperand(pref, modrm).ptrSize(8).build());
            case 4, 5, 6, 7 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup8(
            final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
        final ModRM modrm = modrm();

        return switch (modrm.reg()) {
            case 0, 1, 2, 3 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
            case 4 -> new Instruction(
                    Opcode.BT,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(b.read1()));
            case 5 -> new Instruction(
                    Opcode.BTS,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(b.read1()));
            case 6 -> new Instruction(
                    Opcode.BTR,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(b.read1()));
            case 7 -> new Instruction(
                    Opcode.BTC,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(b.read1()));
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
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
        logger.debug("Read multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        return switch (opcodeSecondByte) {
            case GROUP7_OPCODE -> parseExtendedOpcodeGroup7(opcodeFirstByte, opcodeSecondByte, pref);
            case GROUP8_OPCODE -> parseExtendedOpcodeGroup8(opcodeFirstByte, opcodeSecondByte, pref);
            case GROUP9_OPCODE -> parseExtendedOpcodeGroup9(opcodeFirstByte, opcodeSecondByte, pref);
            case GROUP15_OPCODE -> parseExtendedOpcodeGroup15(opcodeFirstByte, opcodeSecondByte, pref);
            case GROUP16_OPCODE -> parseExtendedOpcodeGroup16(opcodeFirstByte, opcodeSecondByte, pref);

            case JA_DISP32_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of32(b.read4LittleEndian()));
            case JAE_DISP32_OPCODE -> new Instruction(Opcode.JAE, RelativeOffset.of32(b.read4LittleEndian()));
            case JE_DISP32_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of32(b.read4LittleEndian()));
            case JNE_DISP32_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of32(b.read4LittleEndian()));
            case JBE_DISP32_OPCODE -> new Instruction(Opcode.JBE, RelativeOffset.of32(b.read4LittleEndian()));
            case JG_DISP32_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of32(b.read4LittleEndian()));
            case JS_DISP32_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of32(b.read4LittleEndian()));
            case JNS_DISP32_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of32(b.read4LittleEndian()));
            case JP_DISP32_OPCODE -> new Instruction(Opcode.JP, RelativeOffset.of32(b.read4LittleEndian()));
            case JL_DISP32_OPCODE -> new Instruction(Opcode.JL, RelativeOffset.of32(b.read4LittleEndian()));
            case JGE_DISP32_OPCODE -> new Instruction(Opcode.JGE, RelativeOffset.of32(b.read4LittleEndian()));
            case JLE_DISP32_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of32(b.read4LittleEndian()));
            case JB_DISP32_OPCODE -> new Instruction(Opcode.JB, RelativeOffset.of32(b.read4LittleEndian()));
            case ENDBR_OPCODE -> {
                final byte x = b.read1();
                if (x == (byte) 0xfa) {
                    yield new Instruction(Opcode.ENDBR64);
                } else if (x == (byte) 0xfb) {
                    yield new Instruction(Opcode.ENDBR32);
                } else if (pref.p1().isPresent() && pref.p1().orElseThrow() == (byte) 0xf3) {
                    final ModRM modrm = new ModRM(x);
                    yield new Instruction(
                            Opcode.RDSSPQ,
                            Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
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
                                .ptrSize(
                                        pref.hasOperandSizeOverridePrefix()
                                                ? 16
                                                : (pref.rex().isOperand64Bit() ? 64 : 32))
                                .build());
            }
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
                final Opcode opcode = setOpcodes[BitUtils.and(opcodeSecondByte, (byte) 0x0f)];
                final ModRM modrm = modrm();
                yield new Instruction(
                        opcode,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).ptrSize(8).build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
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
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Registers.fromCode(
                                        modrm.rm(),
                                        !pref.hasAddressSizeOverridePrefix(),
                                        pref.rex().ModRMRMExtension(),
                                        pref.hasOperandSizeOverridePrefix()));
            }
            case BT_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.BT,
                        Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }
            case BTR_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.BTR,
                        Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }
            case BTS_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.BTS,
                        Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }
            case BTC_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.BTC,
                        Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }

            case XCHG_INDIRECT8_R8_OPCODE -> {
                final ModRM modrm = modrm();
                final Operand op1 = parseIndirectOperand(pref, modrm).build();
                final Operand op2 = Register8.fromByte(
                        Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix());
                if (pref.p1().isPresent() && pref.p1().orElseThrow() == (byte) 0xf0) {
                    yield new Instruction(Prefix.LOCK, Opcode.CMPXCHG, op1, op2);
                }
                yield new Instruction(Opcode.CMPXCHG, op1, op2);
            }
            case XCHG_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                final Operand op1 = parseIndirectOperand(pref, modrm).build();
                final Operand op2 = Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix());
                if (pref.p1().isPresent() && pref.p1().orElseThrow() == (byte) 0xf0) {
                    yield new Instruction(Prefix.LOCK, Opcode.CMPXCHG, op1, op2);
                }
                yield new Instruction(Opcode.CMPXCHG, op1, op2);
            }

            case MOVZX_BYTE_PTR_OPCODE, MOVZX_WORD_PTR_OPCODE, MOVSX_BYTE_PTR_OPCODE, MOVSX_WORD_PTR_OPCODE -> {
                final Opcode opcode =
                        (opcodeSecondByte == MOVZX_BYTE_PTR_OPCODE || opcodeSecondByte == MOVZX_WORD_PTR_OPCODE)
                                ? Opcode.MOVZX
                                : Opcode.MOVSX;
                final int ptrSize =
                        (opcodeSecondByte == MOVZX_BYTE_PTR_OPCODE || opcodeSecondByte == MOVSX_BYTE_PTR_OPCODE)
                                ? 8
                                : 16;

                final ModRM modrm = modrm();
                final Register r1 = Registers.fromCode(
                        modrm.reg(), pref.rex().isOperand64Bit(), pref.rex().ModRMRegExtension(), false);

                if (modrm.mod() != (byte) 0x03) { // indirect operand needed
                    yield new Instruction(
                            opcode,
                            r1,
                            parseIndirectOperand(pref, modrm).ptrSize(ptrSize).build());
                } else {
                    final byte regByte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                    final Register r2 = (ptrSize == 8)
                            ? Register8.fromByte(regByte, pref.hasRexPrefix())
                            : Register16.fromByte(regByte);
                    yield new Instruction(opcode, r1, r2);
                }
            }

            case MOVDQA_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.MOVDQA,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        parseIndirectOperand(pref, modrm).build());
            }
            case PSHUF_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1 = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2 = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        pref.hasOperandSizeOverridePrefix() ? Opcode.PSHUFD : Opcode.PSHUFW,
                        pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(r1),
                        pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r2) : RegisterMMX.fromByte(r2),
                        imm8());
            }
            case SHUFPx_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1 = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2 = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        pref.hasOperandSizeOverridePrefix() ? Opcode.SHUFPD : Opcode.SHUFPS,
                        RegisterXMM.fromByte(r1),
                        RegisterXMM.fromByte(r2),
                        imm8());
            }
            case XADD_INDIRECT8_R8_OPCODE -> {
                final ModRM modrm = modrm();
                if (pref.p1().isPresent() && pref.p1().orElseThrow() == (byte) 0xf0) {
                    yield new Instruction(
                            Prefix.LOCK,
                            Opcode.XADD,
                            parseIndirectOperand(pref, modrm).build(),
                            Register8.fromByte(
                                    Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()),
                                    pref.hasRexPrefix()));
                }
                yield new Instruction(
                        Opcode.XADD,
                        parseIndirectOperand(pref, modrm).build(),
                        Register8.fromByte(
                                Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()));
            }
            case XADD_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                if (pref.p1().isPresent() && pref.p1().orElseThrow() == (byte) 0xf0) {
                    yield new Instruction(
                            Prefix.LOCK,
                            Opcode.XADD,
                            parseIndirectOperand(pref, modrm).build(),
                            Registers.fromCode(
                                    modrm.reg(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRegExtension(),
                                    pref.hasOperandSizeOverridePrefix()));
                }
                yield new Instruction(
                        Opcode.XADD,
                        parseIndirectOperand(pref, modrm).build(),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }
            case MOVQ_OPCODE -> {
                final ModRM modrm = modrm();
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final Register r1 = Registers.fromCode(
                        modrm.rm(), pref.rex().isOperand64Bit(), pref.rex().ModRMRMExtension(), false);
                yield new Instruction(
                        Opcode.MOVQ,
                        pref.hasOperandSizeOverridePrefix()
                                ? RegisterXMM.fromByte(regByte)
                                : RegisterMMX.fromByte(regByte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : r1);
            }
            case MOVQ_INDIRECT_XMM_OPCODE -> {
                final ModRM modrm = modrm();
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        Opcode.MOVQ,
                        parseIndirectOperand(pref, modrm).ptrSize(64).build(),
                        RegisterXMM.fromByte(regByte));
            }
            case MOVQ_R128_INDIRECT64_OPCODE -> {
                final ModRM modrm = modrm();
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        Opcode.MOVQ,
                        RegisterXMM.fromByte(regByte),
                        parseIndirectOperand(pref, modrm).ptrSize(64).build());
            }
            case MOVHLPS_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.MOVHLPS,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm())));
            }
            case MOVHPS_OPCODE -> {
                final ModRM modrm = modrm();
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        Opcode.MOVHPS,
                        RegisterXMM.fromByte(regByte),
                        parseIndirectOperand(pref, modrm).ptrSize(64).build());
            }
            case PAND_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        Opcode.PAND,
                        RegisterXMM.fromByte(r1Byte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : RegisterXMM.fromByte(r2Byte));
            }
            case PADDQ_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        Opcode.PADDQ,
                        RegisterXMM.fromByte(r1Byte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : RegisterXMM.fromByte(r2Byte));
            }
            case PSUBQ_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        Opcode.PSUBQ,
                        RegisterXMM.fromByte(r1Byte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : RegisterXMM.fromByte(r2Byte));
            }
            case POR_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        Opcode.POR,
                        RegisterXMM.fromByte(r1Byte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : RegisterXMM.fromByte(r2Byte));
            }
            case PXOR_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        Opcode.PXOR,
                        RegisterXMM.fromByte(r1Byte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : RegisterXMM.fromByte(r2Byte));
            }
            case PCMPEQD_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(Opcode.PCMPEQD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
            }
            case XORPS_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(Opcode.XORPS, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
            }
            case ADDSD_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(Opcode.ADDSD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
            }
            case DIVSD_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(Opcode.DIVSD, RegisterXMM.fromByte(r1Byte), RegisterXMM.fromByte(r2Byte));
            }
            case MOVAPx_R128_INDIRECT128_OPCODE -> {
                final ModRM modrm = modrm();
                final byte r1Byte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2Byte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS,
                        RegisterXMM.fromByte(r1Byte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : RegisterXMM.fromByte(r2Byte));
            }
            case MOVAPx_INDIRECT128_R128_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        pref.hasOperandSizeOverridePrefix() ? Opcode.MOVAPD : Opcode.MOVAPS,
                        parseIndirectOperand(pref, modrm).build(),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())));
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
                final Opcode opcode = cmovOpcodes[BitUtils.and(opcodeSecondByte, (byte) 0x0f)];
                final ModRM modrm = modrm();
                final Register r1 = Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix());
                yield new Instruction(
                        opcode,
                        r1,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Registers.fromCode(
                                        modrm.rm(),
                                        pref.rex().isOperand64Bit(),
                                        pref.rex().ModRMRMExtension(),
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
                final byte regByte = BitUtils.and(opcodeSecondByte, (byte) 0x07);
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
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm())));
            }
            case PUNPCKLQDQ_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.PUNPCKLQDQ,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm())));
            }
            case PUNPCKHQDQ_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.PUNPCKHQDQ,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm())));
            }
            case MOVSD_OPCODE -> {
                final ModRM modrm = modrm();
                final boolean hasRepnePrefix = pref.p1().isPresent();
                yield new Instruction(
                        hasRepnePrefix ? Opcode.MOVSD : Opcode.MOVUPS,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        parseIndirectOperand(pref, modrm)
                                .ptrSize(hasRepnePrefix ? 64 : 128)
                                .build());
            }
            case MOVUPS_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.MOVUPS,
                        parseIndirectOperand(pref, modrm).build(),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())));
            }
            case CVTSI2SD_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.CVTSI2SD,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }
            case UCOMISx_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        pref.hasOperandSizeOverridePrefix() ? Opcode.UCOMISD : Opcode.UCOMISS,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        parseIndirectOperand(pref, modrm)
                                .ptrSize(pref.hasOperandSizeOverridePrefix() ? 64 : 32)
                                .build());
            }
            default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
        };
    }

    private Instruction parseExtendedOpcodeGroup15(
            final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
        final ModRM modrm = modrm();

        if (modrm.mod() != (byte) 0x03) {
            throw new IllegalArgumentException("Not implemented");
        } else {
            return switch (modrm.reg()) {
                case (byte) 0x05 /* 101 */ -> new Instruction(
                        Opcode.INCSSPQ,
                        Registers.fromCode(modrm.rm(), true, pref.rex().ModRMRMExtension(), false));
                default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
            };
        }
    }

    private Instruction parseExtendedOpcodeGroup9(
            final byte opcodeFirstByte, final byte opcodeSecondByte, final Prefixes pref) {
        final ModRM modrm = modrm();

        if (modrm.mod() == (byte) 0x03) {
            if (pref.p1().isPresent() && pref.p1().orElseThrow() == (byte) 0xf3) {
                throw new IllegalArgumentException("Not implemented");
            } else {
                return switch (modrm.reg()) {
                    case 0, 1, 2, 3, 4, 5 -> throw new ReservedOpcode(opcodeFirstByte, opcodeSecondByte);
                    case 6 -> new Instruction(
                            Opcode.RDRAND,
                            Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix()));
                    case 7 -> new Instruction(
                            Opcode.RDSEED,
                            Registers.fromCode(
                                    modrm.rm(),
                                    pref.rex().isOperand64Bit(),
                                    pref.rex().ModRMRMExtension(),
                                    pref.hasOperandSizeOverridePrefix()));
                    default -> throw new UnknownOpcode(opcodeFirstByte, opcodeSecondByte);
                };
            }
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    private Instruction parseSingleByteOpcode(final byte opcodeFirstByte, final Prefixes pref) {
        final byte OPCODE_REG_MASK = (byte) 0x07;

        final byte ADD_INDIRECT8_R8_OPCODE = (byte) 0x00;
        final byte ADD_INDIRECT32_R32_OPCODE = (byte) 0x01;
        final byte ADD_R8_INDIRECT8_OPCODE = (byte) 0x02;
        final byte ADD_R32_INDIRECT32_OPCODE = (byte) 0x03;
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
                                Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()));
            }
            case MOV_R8_INDIRECT8_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.MOV,
                        Register8.fromByte(
                                Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()),
                        parseIndirectOperand(pref, modrm).build());
            }
            case TEST_R8_R8_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.TEST,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                        pref.hasRexPrefix()),
                        Register8.fromByte(
                                Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()));
            }
            case TEST_R32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.TEST,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Registers.fromCode(
                                        modrm.rm(),
                                        pref.rex().isOperand64Bit(),
                                        pref.rex().ModRMRMExtension(),
                                        pref.hasOperandSizeOverridePrefix()),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
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
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                        pref.hasRexPrefix()),
                        Register8.fromByte(
                                Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()));
            }
            case XCHG_INDIRECT32_R32_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.XCHG,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Registers.fromCode(
                                        modrm.rm(),
                                        pref.rex().isOperand64Bit(),
                                        pref.rex().ModRMRMExtension(),
                                        pref.hasOperandSizeOverridePrefix()),
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()));
            }

                // jumps
            case JMP_DISP32_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of32(b.read4LittleEndian()));
            case JMP_DISP8_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of8(b.read1()));
            case JB_DISP8_OPCODE -> new Instruction(Opcode.JB, RelativeOffset.of8(b.read1()));
            case JAE_DISP8_OPCODE -> new Instruction(Opcode.JAE, RelativeOffset.of8(b.read1()));
            case JE_DISP8_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of8(b.read1()));
            case JA_DISP8_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of8(b.read1()));
            case JNE_DISP8_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of8(b.read1()));
            case JBE_DISP8_OPCODE -> new Instruction(Opcode.JBE, RelativeOffset.of8(b.read1()));
            case JS_DISP8_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of8(b.read1()));
            case JNS_DISP8_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of8(b.read1()));
            case JP_DISP8_OPCODE -> new Instruction(Opcode.JP, RelativeOffset.of8(b.read1()));
            case JL_DISP8_OPCODE -> new Instruction(Opcode.JL, RelativeOffset.of8(b.read1()));
            case JGE_DISP8_OPCODE -> new Instruction(Opcode.JGE, RelativeOffset.of8(b.read1()));
            case JLE_DISP8_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of8(b.read1()));
            case JG_DISP8_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of8(b.read1()));

            case CALL_OPCODE -> new Instruction(Opcode.CALL, RelativeOffset.of32(b.read4LittleEndian()));

            case IMUL_REG_REG_IMM8_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.IMUL,
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                false),
                        Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
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
                                pref.rex().ModRMRegExtension(),
                                false),
                        parseIndirectOperand(pref, modrm).build(),
                        imm32());
            }
            case MOVS_ES_EDI_DS_ESI_BYTE_PTR_OPCODE -> {
                final Operand op1 = IndirectOperand.builder()
                        .reg2(new SegmentRegister(
                                Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
                        .ptrSize(8)
                        .build();
                final Operand op2 = IndirectOperand.builder()
                        .reg2(new SegmentRegister(
                                Register16.DS, pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI))
                        .ptrSize(8)
                        .build();
                logger.debug(pref.toString());
                if (pref.p1().isPresent()) {
                    if (pref.p1().orElseThrow() == (byte) 0xf2) {
                        yield new Instruction(Instruction.Prefix.REPNZ, Opcode.MOVS, op1, op2);
                    } else if (pref.p1().orElseThrow() == (byte) 0xf3) {
                        yield new Instruction(Instruction.Prefix.REP, Opcode.MOVS, op1, op2);
                    } else {
                        yield new Instruction(Instruction.Prefix.LOCK, Opcode.MOVS, op1, op2);
                    }
                } else {
                    yield new Instruction(Opcode.MOVS, op1, op2);
                }
            }
            case MOVS_ES_EDI_DS_ESI_OPCODE -> {
                final int size = pref.hasOperandSizeOverridePrefix() ? 16 : 32;
                final Operand op1 = IndirectOperand.builder()
                        .reg2(new SegmentRegister(
                                Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
                        .ptrSize(size)
                        .build();
                final Operand op2 = IndirectOperand.builder()
                        .reg2(new SegmentRegister(
                                Register16.DS, pref.hasAddressSizeOverridePrefix() ? Register32.ESI : Register64.RSI))
                        .ptrSize(size)
                        .build();
                logger.debug(pref.toString());
                if (pref.p1().isPresent()) {
                    if (pref.p1().orElseThrow() == (byte) 0xf2) {
                        yield new Instruction(Instruction.Prefix.REPNZ, Opcode.MOVS, op1, op2);
                    } else if (pref.p1().orElseThrow() == (byte) 0xf3) {
                        yield new Instruction(Instruction.Prefix.REP, Opcode.MOVS, op1, op2);
                    } else {
                        yield new Instruction(Instruction.Prefix.LOCK, Opcode.MOVS, op1, op2);
                    }
                } else {
                    yield new Instruction(Opcode.MOVS, op1, op2);
                }
            }
            case STOS_R8_OPCODE -> {
                final Operand op1 = IndirectOperand.builder()
                        .reg2(new SegmentRegister(
                                Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
                        .build();
                final Operand op2 = Register8.AL;
                if (pref.p1().isPresent()) {
                    if (pref.p1().orElseThrow() == (byte) 0xf2) {
                        yield new Instruction(Instruction.Prefix.REPNZ, Opcode.STOS, op1, op2);
                    } else if (pref.p1().orElseThrow() == (byte) 0xf3) {
                        yield new Instruction(Instruction.Prefix.REP, Opcode.STOS, op1, op2);
                    } else {
                        yield new Instruction(Instruction.Prefix.LOCK, Opcode.STOS, op1, op2);
                    }
                } else {
                    yield new Instruction(Opcode.STOS, op1, op2);
                }
            }
            case STOS_R32_OPCODE -> {
                final Operand op1 = IndirectOperand.builder()
                        .reg2(new SegmentRegister(
                                Register16.ES, pref.hasAddressSizeOverridePrefix() ? Register32.EDI : Register64.RDI))
                        .build();
                final Operand op2 = pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX;
                if (pref.p1().isPresent()) {
                    if (pref.p1().orElseThrow() == (byte) 0xf2) {
                        yield new Instruction(Instruction.Prefix.REPNZ, Opcode.STOS, op1, op2);
                    } else if (pref.p1().orElseThrow() == (byte) 0xf3) {
                        yield new Instruction(Instruction.Prefix.REP, Opcode.STOS, op1, op2);
                    } else {
                        yield new Instruction(Instruction.Prefix.LOCK, Opcode.STOS, op1, op2);
                    }
                } else {
                    yield new Instruction(Opcode.STOS, op1, op2);
                }
            }
            case MOVSXD_OPCODE -> {
                final ModRM modrm = modrm();
                yield new Instruction(
                        Opcode.MOVSXD,
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        parseIndirectOperand(pref, modrm).ptrSize(32).build());
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct
                // thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x00) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode = opcodeTable[opcodeByte];
                final ModRM modrm = modrm();
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        opcode,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct
                // thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x01) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct
                // thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x02) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode = opcodeTable[opcodeByte];
                final ModRM modrm = modrm();
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        opcode,
                        Register8.fromByte(regByte, pref.hasRexPrefix()),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(pref, modrm).build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct
                // thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x03) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct
                // thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x04) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct
                // thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x05) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode = opcodeTable[opcodeByte];
                yield new Instruction(
                        opcode,
                        pref.hasOperandSizeOverridePrefix()
                                ? Register16.AX
                                : (pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX),
                        pref.hasOperandSizeOverridePrefix() ? imm16() : imm32());
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
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                final Immediate imm =
                        switch (size) {
                            case 16 -> imm16();
                            case 32 -> imm32();
                            case 64 -> imm64();
                            default -> throw new IllegalArgumentException("Invalid value");
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

            case (byte) 0x66 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized operand size override prefix at byte 0x%08x", b.position()));
            case (byte) 0x67 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized address size override prefix at byte 0x%08x", b.position()));
            case (byte) 0xf0 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized LOCK prefix at byte 0x%08x", b.position()));
            case (byte) 0xf2 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized REPNE prefix at byte 0x%08x", b.position()));
            case (byte) 0xf3 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized REP prefix at byte 0x%08x", b.position()));
            default -> throw new UnknownOpcode(opcodeFirstByte);
        };
    }

    private Prefixes parsePrefixes() {
        Optional<Byte> p1 = Optional.empty(); // Legacy Prefix Group 1
        Optional<Byte> p2 = Optional.empty(); // Legacy Prefix Group 2
        boolean hasOperandSizeOverridePrefix = false;
        boolean hasAddressSizeOverridePrefix = false;

        // FIXME: is there a better way to do this?
        // (technically there is no limit to the number of prefixes an instruction can have)
        while (true) {
            byte x = b.read1();

            if (isLegacyPrefixGroup1(x)) {
                p1 = Optional.of(x);
            } else if (isLegacyPrefixGroup2(x)) {
                p2 = Optional.of(x);
            } else if (isOperandSizeOverridePrefix(x)) {
                hasOperandSizeOverridePrefix = true;
            } else if (isAddressSizeOverridePrefix(x)) {
                hasAddressSizeOverridePrefix = true;
            } else {
                b.goBack(1);
                break;
            }
        }

        final byte rexByte = b.read1();
        final RexPrefix rexPrefix;
        final boolean isREX = RexPrefix.isREXPrefix(rexByte);
        if (isREX) {
            rexPrefix = new RexPrefix(rexByte);
            logger.debug("Found REX prefix: 0x%02x -> %s", rexByte, rexPrefix);
        } else {
            rexPrefix = new RexPrefix((byte) 0x40);
            b.goBack(1);
        }

        return new Prefixes(p1, p2, hasOperandSizeOverridePrefix, hasAddressSizeOverridePrefix, isREX, rexPrefix);
    }

    // Parses an instruction like OP Indirect,RXX
    private Instruction parseMR(final Prefixes pref, final Opcode opcode) {
        final ModRM modrm = modrm();
        return new Instruction(
                opcode,
                (modrm.mod() != (byte) 0x03)
                        ? parseIndirectOperand(pref, modrm).build()
                        : Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix()));
    }

    // Parses an instruction like OP RXX,Indirect
    private Instruction parseRM(final Prefixes pref, final Opcode opcode) {
        final ModRM modrm = modrm();
        final boolean isIndirectOperandNeeded = modrm.mod() != (byte) 0x03;
        return new Instruction(
                opcode,
                Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix()),
                isIndirectOperandNeeded
                        ? parseIndirectOperand(pref, modrm).build()
                        : Registers.fromCode(
                                modrm.rm(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix()));
    }

    private Instruction parse(
            final Prefixes pref,
            final ModRM modrm,
            final Optional<Integer> immediateBits,
            final Opcode opcode,
            final Optional<Integer> pointerSize) {
        final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
        final boolean hasOperandSizeOverridePrefix = pref.hasOperandSizeOverridePrefix();
        final RexPrefix rexPrefix = pref.rex();
        final byte rm = modrm.rm();
        Operand operand1 = Registers.fromCode(
                rm, !hasAddressSizeOverridePrefix, rexPrefix.ModRMRMExtension(), hasOperandSizeOverridePrefix);
        final Register operand2 = Registers.fromCode(
                modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension(), hasOperandSizeOverridePrefix);

        // Table at page 530
        final byte mod = modrm.mod();
        if (mod < (byte) 0x00 || mod > (byte) 0x03) {
            throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
        }

        final IndirectOperand.IndirectOperandBuilder iob = parseIndirectOperand(pref, modrm);

        if (pointerSize.isPresent()) {
            logger.debug("Using pointer size: %,d", pointerSize.orElseThrow());
            iob.ptrSize(pointerSize.orElseThrow());
        }

        if (mod != (byte) 0x03 /* 11 */) {
            // indirect operand needed
            operand1 = iob.build();
        } else {
            // indirect operand not needed, so we take the second operand without using the
            // addressSizeOverride
            operand1 = Registers.fromCode(
                    rm, rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension(), hasOperandSizeOverridePrefix);
        }

        if (immediateBits.isEmpty()) {
            return new Instruction(opcode, operand1, operand2);
        }

        return switch (immediateBits.orElseThrow()) {
            case 8 -> new Instruction(opcode, operand1, imm8());
            case 16 -> new Instruction(opcode, operand1, imm16());
            case 32 -> new Instruction(opcode, operand1, imm32());
            default -> throw new IllegalArgumentException(
                    String.format("Invalid value for immediate bytes: %,d", immediateBits.orElseThrow()));
        };
    }

    private IndirectOperandBuilder parseIndirectOperand(final Prefixes pref, final ModRM modrm) {
        Operand operand2 = Registers.fromCode(
                modrm.rm(), !pref.hasAddressSizeOverridePrefix(), pref.rex().ModRMRMExtension(), false);
        final RexPrefix rexPrefix = pref.rex();
        final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
        final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
        SIB sib;
        if (modrm.mod() != (byte) 0x03 /* 11 */ && modrm.rm() == (byte) 0x04 /* 100 */) {
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
            sib = null;
            if (modrm.mod() == (byte) 0x00 && operand2.toIntelSyntax().endsWith("bp")) {
                operand2 = hasAddressSizeOverridePrefix ? Register32.EIP : Register64.RIP;
            }
        }

        if (pref.p2().isPresent() && pref.p2().orElseThrow() == (byte) 0x2e) {
            operand2 = new SegmentRegister(Register16.CS, (Register) operand2);
        }
        iob.reg2((Register) operand2);

        if (modrm.mod() != (byte) 0x03 /* 11 */) {
            // indirect operand needed
            if ((modrm.mod() == (byte) 0x00 && modrm.rm() == (byte) 0x05)
                    || (modrm.mod() == (byte) 0x00 && sib != null && sib.base() == (byte) 0x05)
                    || modrm.mod() == (byte) 0x02) {
                final int disp32 = b.read4LittleEndian();
                iob.displacement(disp32);
            } else if (modrm.mod() == (byte) 0x01) {
                final byte disp8 = b.read1();
                iob.displacement(disp8);
            }
        }

        return iob;
    }

    private boolean isExtendedOpcode(final byte opcode) {
        return opcode == (byte) 0x80 || opcode == (byte) 0x81 || opcode == (byte) 0x82 || opcode == (byte) 0x83;
    }

    private ModRM modrm() {
        final byte m = b.read1();
        logger.debug("Read ModRM byte : 0x%02x", m);
        return new ModRM(m);
    }

    private SIB sib() {
        final byte s = b.read1();
        logger.debug("Read SIB byte : 0x%02x", s);
        return new SIB(s);
    }

    private Immediate imm8() {
        return new Immediate(b.read1());
    }

    private Immediate imm16() {
        return new Immediate(b.read2LittleEndian());
    }

    private Immediate imm32() {
        return new Immediate(b.read4LittleEndian());
    }

    private Immediate imm64() {
        return new Immediate(b.read8LittleEndian());
    }

    private boolean isLegacyPrefixGroup1(final byte prefix) {
        final byte LOCK_PREFIX = (byte) 0xf0;
        final byte REPNE_PREFIX = (byte) 0xf2; // REPNE / REPNZ
        final byte REP_PREFIX = (byte) 0xf3; // REP / REPE / REPZ
        return prefix == LOCK_PREFIX || prefix == REPNE_PREFIX || prefix == REP_PREFIX;
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
        final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
        return prefix == OPERAND_SIZE_OVERRIDE_PREFIX;
    }

    private boolean isAddressSizeOverridePrefix(final byte prefix) {
        final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
        return prefix == ADDRESS_SIZE_OVERRIDE_PREFIX;
    }

    private boolean isMultibyteOpcode(final byte opcode) {
        final byte MULTIBYTE_OPCODE_PREFIX = (byte) 0x0f;
        return opcode == MULTIBYTE_OPCODE_PREFIX;
    }
}
