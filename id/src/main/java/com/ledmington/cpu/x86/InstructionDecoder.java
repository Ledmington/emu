package com.ledmington.cpu.x86;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ledmington.cpu.x86.IndirectOperand.IndirectOperandBuilder;
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

    public InstructionDecoder() {}

    public List<Instruction> decode(final byte[] code) {
        return decode(code, false);
    }

    public List<Instruction> decode(final byte[] code, final boolean isLittleEndian) {
        final ByteBuffer b = new ByteBuffer(code, isLittleEndian);
        final int length = code.length;
        logger.info("The code is %,d bytes long", length);

        final List<Instruction> instructions = new ArrayList<>();
        while (b.position() < length) {
            final int pos = b.position();
            final Instruction inst = decodeInstruction(b);
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

    private Instruction decodeInstruction(final ByteBuffer b) {
        Objects.requireNonNull(b);

        final Prefixes pref = parsePrefixes(b);

        final byte opcodeFirstByte = b.read1();

        if (isMultibyteOpcode(opcodeFirstByte)) {
            // more than 1 bytes opcode
            return parse2BytesOpcode(b, opcodeFirstByte, pref);
        } else if (isExtendedOpcode(opcodeFirstByte)) {
            // extended opcode group 1
            return parseExtendedOpcodeGroup1(b, opcodeFirstByte, pref);
        } else if (opcodeFirstByte == (byte) 0xc0
                || opcodeFirstByte == (byte) 0xc1
                || opcodeFirstByte == (byte) 0xd0
                || opcodeFirstByte == (byte) 0xd1
                || opcodeFirstByte == (byte) 0xd2
                || opcodeFirstByte == (byte) 0xd3) {
            return parseExtendedOpcodeGroup2(b, opcodeFirstByte, pref);
        } else if (opcodeFirstByte == (byte) 0xc6 || opcodeFirstByte == (byte) 0xc7) {
            return parseExtendedOpcodeGroup11(b, opcodeFirstByte, pref);
        } else if (opcodeFirstByte == (byte) 0xf6 || opcodeFirstByte == (byte) 0xf7) {
            return parseExtendedOpcodeGroup3(b, opcodeFirstByte, pref);
        } else if (opcodeFirstByte == (byte) 0xff) {
            return parseExtendedOpcodeGroup5(b, opcodeFirstByte, pref);
        } else {
            // 1 byte opcode
            return parseSingleByteOpcode(b, opcodeFirstByte, pref);
        }
    }

    private Instruction parseExtendedOpcodeGroup5(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        return switch (modrm.reg()) {
            case (byte) 0x00 /* 000 */ -> new Instruction(
                    Opcode.INC,
                    Registers.fromCode(
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
                                parseIndirectOperand(b, pref, modrm, reg)
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
                    parseIndirectOperand(
                                    b,
                                    pref,
                                    modrm,
                                    Registers.fromCode(
                                            modrm.rm(), false,
                                            pref.rex().ModRMRMExtension(), false))
                            .ptrSize(pref.hasOperandSizeOverridePrefix() ? 32 : 64)
                            .build());
            case (byte) 0x06 /* 110 */ -> new Instruction(
                    Opcode.PUSH,
                    parseIndirectOperand(
                                    b,
                                    pref,
                                    modrm,
                                    Registers.fromCode(
                                            modrm.rm(),
                                            !pref.hasAddressSizeOverridePrefix(),
                                            pref.rex().ModRMRMExtension(),
                                            pref.hasOperandSizeOverridePrefix()))
                            .ptrSize(64)
                            .build());
            case (byte) 0x07 /* 111 */ -> throw new IllegalArgumentException(
                    String.format("Reserved extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            default -> throw new IllegalArgumentException(
                    String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private Instruction parseExtendedOpcodeGroup3(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        return switch (modrm.reg()) {
            case (byte) 0x00 /* 000 */ -> new Instruction(
                    Opcode.TEST,
                    parseIndirectOperand(
                                    b,
                                    pref,
                                    modrm,
                                    Registers.fromCode(
                                            modrm.rm(),
                                            !pref.hasAddressSizeOverridePrefix(),
                                            pref.rex().ModRMRMExtension(),
                                            pref.hasOperandSizeOverridePrefix()))
                            .build(),
                    (opcodeFirstByte == (byte) 0xf6) ? new Immediate(b.read1()) : new Immediate(b.read4LittleEndian()));
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
                        (modrm.mod() != (byte) 0x03) // indirect operand needed
                                ? parseIndirectOperand(b, pref, modrm, r)
                                        .ptrSize(pref.rex().isOperand64Bit() ? 64 : 32)
                                        .build()
                                : r);
            }
            case (byte) 0x01 /* 001 */ -> throw new IllegalArgumentException(
                    String.format("Reserved extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            default -> throw new IllegalArgumentException(
                    String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private Instruction parseExtendedOpcodeGroup11(
            final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final int immediateBytes = pref.hasOperandSizeOverridePrefix() ? 2 : ((opcodeFirstByte == (byte) 0xc6) ? 1 : 4);

        return switch (modrm.reg()) {
            case (byte) 0x00 /* 000 */ -> parse(
                    b,
                    pref,
                    modrm,
                    Optional.of(immediateBytes),
                    Opcode.MOV,
                    Optional.of(pref.rex().isOperand64Bit() ? 64 : 8 * immediateBytes),
                    false);
            default -> throw new IllegalArgumentException(
                    String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private static Instruction parseExtendedOpcodeGroup2(
            final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final Operand op2 = (opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xc1)
                ? new Immediate(b.read1())
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
                                    false)),
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
                                    false)),
                    op2);
            case (byte) 0x06 /* 110 */ -> throw new IllegalArgumentException(
                    String.format("Reserved extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            default -> throw new IllegalArgumentException(
                    String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private Instruction parseExtendedOpcodeGroup1(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        final boolean isRegister8Bit = opcodeFirstByte == (byte) 0x80 || opcodeFirstByte == (byte) 0x82;
        final int immediateBits =
                pref.hasOperandSizeOverridePrefix() ? 16 : ((opcodeFirstByte == (byte) 0x81) ? 32 : 8);
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
                    default -> throw new IllegalArgumentException(
                            String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
                };

        final Register r = (isRegister8Bit)
                ? Register8.fromByte(regByte, pref.hasRexPrefix())
                : (isIndirectOperandNeeded
                        ? (pref.hasAddressSizeOverridePrefix()
                                ? Register32.fromByte(regByte)
                                : Register64.fromByte(regByte))
                        : (Registers.fromCode(
                                regByte,
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRMExtension(),
                                pref.hasOperandSizeOverridePrefix())));

        return new Instruction(
                opcode,
                isIndirectOperandNeeded
                        ? parseIndirectOperand(b, pref, modrm, r)
                                .ptrSize(pref.rex().isOperand64Bit() ? 64 : immediateBits)
                                .build()
                        : r,
                switch (immediateBits) {
                    case 8 -> new Immediate(b.read1());
                    case 16 -> new Immediate(b.read2LittleEndian());
                    case 32 -> new Immediate(b.read4LittleEndian());
                    default -> throw new IllegalArgumentException("Invalid value");
                });
    }

    private Instruction parse2BytesOpcode(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte UD2_OPCODE = (byte) 0x0b;
        final byte MOVSD_OPCODE = (byte) 0x10;
        final byte MOVUPS_OPCODE = (byte) 0x11;
        final byte ENDBR_OPCODE = (byte) 0x1e;
        final byte MOVAPS_OPCODE = (byte) 0x29;
        final byte CMOVE_OPCODE = (byte) 0x44;
        final byte CMOVNE_OPCODE = (byte) 0x45;
        final byte CMOVBE_OPCODE = (byte) 0x46;
        final byte CMOVS_OPCODE = (byte) 0x48;
        final byte PUNPCKLQDQ_OPCODE = (byte) 0x6c;
        final byte MOVQ_OPCODE = (byte) 0x6e;
        final byte MOVDQA_OPCODE = (byte) 0x6f;
        final byte PSHUF_OPCODE = (byte) 0x70;
        final byte JB_DISP32_OPCODE = (byte) 0x82;
        final byte JE_DISP32_OPCODE = (byte) 0x84;
        final byte JNE_DISP32_OPCODE = (byte) 0x85;
        final byte JBE_DISP32_OPCODE = (byte) 0x86;
        final byte JA_DISP32_OPCODE = (byte) 0x87;
        final byte JS_DISP32_OPCODE = (byte) 0x88;
        final byte JNS_DISP32_OPCODE = (byte) 0x89;
        final byte JLE_DISP32_OPCODE = (byte) 0x8e;
        final byte JG_DISP32_OPCODE = (byte) 0x8f;
        final byte SETE_OPCODE = (byte) 0x94;
        final byte SETNE_OPCODE = (byte) 0x95;
        final byte IMUL_OPCODE = (byte) 0xaf;
        final byte MOVZX_BYTE_PTR_OPCODE = (byte) 0xb6;
        final byte MOVZX_WORD_PTR_OPCODE = (byte) 0xb7;
        final byte MOVSX_BYTE_PTR_OPCODE = (byte) 0xbe;
        final byte MOVSX_WORD_PTR_OPCODE = (byte) 0xbf;
        final byte MOVQ_INDIRECT_XMM_OPCODE = (byte) 0xd6;

        final byte opcodeSecondByte = b.read1();
        logger.debug("Read multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        return switch (opcodeSecondByte) {
            case JA_DISP32_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of32(b.read4LittleEndian()));
            case JE_DISP32_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of32(b.read4LittleEndian()));
            case JNE_DISP32_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of32(b.read4LittleEndian()));
            case JBE_DISP32_OPCODE -> new Instruction(Opcode.JBE, RelativeOffset.of32(b.read4LittleEndian()));
            case JG_DISP32_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of32(b.read4LittleEndian()));
            case JS_DISP32_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of32(b.read4LittleEndian()));
            case JNS_DISP32_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of32(b.read4LittleEndian()));
            case JLE_DISP32_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of32(b.read4LittleEndian()));
            case JB_DISP32_OPCODE -> new Instruction(Opcode.JB, RelativeOffset.of32(b.read4LittleEndian()));
            case ENDBR_OPCODE -> {
                final byte x = b.read1();
                if (x == (byte) 0xfa) {
                    yield new Instruction(Opcode.ENDBR64);
                } else if (x == (byte) 0xfb) {
                    yield new Instruction(Opcode.ENDBR32);
                } else {
                    throw new IllegalArgumentException("Invalid value");
                }
            }
            case SETE_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.SETE,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(b, pref, modrm, null)
                                        .ptrSize(8)
                                        .build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                        pref.hasRexPrefix()));
            }
            case SETNE_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.SETNE,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(b, pref, modrm, null)
                                        .ptrSize(8)
                                        .build()
                                : Register8.fromByte(
                                        Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()),
                                        pref.hasRexPrefix()));
            }
            case IMUL_OPCODE -> parseSimple(b, pref, Opcode.IMUL, true);

            case MOVZX_BYTE_PTR_OPCODE, MOVZX_WORD_PTR_OPCODE, MOVSX_BYTE_PTR_OPCODE, MOVSX_WORD_PTR_OPCODE -> {
                final Opcode opcode =
                        (opcodeSecondByte == MOVZX_BYTE_PTR_OPCODE || opcodeSecondByte == MOVZX_WORD_PTR_OPCODE)
                                ? Opcode.MOVZX
                                : Opcode.MOVSX;
                final int ptrSize =
                        (opcodeSecondByte == MOVZX_BYTE_PTR_OPCODE || opcodeSecondByte == MOVSX_BYTE_PTR_OPCODE)
                                ? 8
                                : 16;

                final ModRM modrm = new ModRM(b.read1());
                final Register r1 = Registers.fromCode(
                        modrm.reg(), pref.rex().isOperand64Bit(), pref.rex().ModRMRegExtension(), false);

                if (modrm.mod() != (byte) 0x03) { // indirect operand needed
                    yield new Instruction(
                            opcode,
                            r1,
                            parseIndirectOperand(
                                            b,
                                            pref,
                                            modrm,
                                            Registers.fromCode(
                                                    modrm.rm(),
                                                    !pref.hasAddressSizeOverridePrefix(),
                                                    pref.rex().ModRMRMExtension(),
                                                    pref.hasOperandSizeOverridePrefix()))
                                    .ptrSize(ptrSize)
                                    .build());
                } else {
                    final byte regByte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                    final Register r2 = (ptrSize == 8)
                            ? Register8.fromByte(regByte, pref.hasRexPrefix())
                            : Register16.fromByte(regByte);
                    yield new Instruction(opcode, r1, r2);
                }
            }

            case MOVDQA_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.MOVDQA,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        parseIndirectOperand(
                                        b,
                                        pref,
                                        modrm,
                                        Registers.fromCode(
                                                modrm.rm(),
                                                pref.rex().isOperand64Bit(),
                                                pref.rex().ModRMRMExtension(),
                                                false))
                                .build());
            }
            case PSHUF_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                final byte r1 = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final byte r2 = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                yield new Instruction(
                        pref.hasOperandSizeOverridePrefix() ? Opcode.PSHUFD : Opcode.PSHUFW,
                        pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r1) : RegisterMMX.fromByte(r1),
                        pref.hasOperandSizeOverridePrefix() ? RegisterXMM.fromByte(r2) : RegisterMMX.fromByte(r2),
                        new Immediate(b.read1()));
            }
            case MOVQ_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                final Register r1 = Registers.fromCode(
                        modrm.rm(), pref.rex().isOperand64Bit(), pref.rex().ModRMRMExtension(), false);
                yield new Instruction(
                        Opcode.MOVQ,
                        pref.hasOperandSizeOverridePrefix()
                                ? RegisterXMM.fromByte(regByte)
                                : RegisterMMX.fromByte(regByte),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(b, pref, modrm, r1).build()
                                : r1);
            }
            case MOVQ_INDIRECT_XMM_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        Opcode.MOVQ,
                        parseIndirectOperand(b, pref, modrm, null).ptrSize(64).build(),
                        RegisterXMM.fromByte(regByte));
            }
            case MOVAPS_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.MOVAPS,
                        parseIndirectOperand(
                                        b,
                                        pref,
                                        modrm,
                                        Registers.fromCode(
                                                modrm.rm(),
                                                pref.rex().isOperand64Bit(),
                                                pref.rex().ModRMRMExtension(),
                                                pref.hasOperandSizeOverridePrefix()))
                                .build(),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())));
            }

            case CMOVE_OPCODE ->
            // page 771
            parseSimple(b, pref, Opcode.CMOVE, true);
            case CMOVBE_OPCODE -> parseSimple(b, pref, Opcode.CMOVBE, true);
            case CMOVNE_OPCODE -> parseSimple(b, pref, Opcode.CMOVNE, true);
            case CMOVS_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                final Register r1 = Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix());
                final Register r2 = Registers.fromCode(
                        modrm.rm(),
                        pref.hasAddressSizeOverridePrefix(),
                        pref.rex().ModRMRMExtension(),
                        pref.hasOperandSizeOverridePrefix());
                yield new Instruction(
                        Opcode.CMOVS,
                        r1,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(b, pref, modrm, r2).build()
                                : r2);
            }
            case UD2_OPCODE -> new Instruction(Opcode.UD2);
            case PUNPCKLQDQ_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.PUNPCKLQDQ,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm())));
            }
            case MOVSD_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                final boolean hasRepnePrefix = pref.p1().isPresent();
                yield new Instruction(
                        hasRepnePrefix ? Opcode.MOVSD : Opcode.MOVUPS,
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())),
                        parseIndirectOperand(
                                        b,
                                        pref,
                                        modrm,
                                        Registers.fromCode(
                                                modrm.rm(),
                                                !pref.hasAddressSizeOverridePrefix(),
                                                pref.rex().ModRMRMExtension(),
                                                pref.hasOperandSizeOverridePrefix()))
                                .ptrSize(hasRepnePrefix ? 64 : 128)
                                .build());
            }
            case MOVUPS_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.MOVUPS,
                        parseIndirectOperand(
                                        b,
                                        pref,
                                        modrm,
                                        Registers.fromCode(
                                                modrm.rm(),
                                                !pref.hasAddressSizeOverridePrefix(),
                                                pref.rex().ModRMRMExtension(),
                                                pref.hasOperandSizeOverridePrefix()))
                                .build(),
                        RegisterXMM.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg())));
            }
            default -> throw new IllegalArgumentException(
                    String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private Instruction parseSingleByteOpcode(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
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
        final byte PUSH_IMM8_OPCODE = (byte) 0x6a;
        final byte IMUL_REG_REG_IMM8_OPCODE = (byte) 0x6b;
        final byte JE_DISP8_OPCODE = (byte) 0x74;
        final byte JNE_DISP8_OPCODE = (byte) 0x75;
        final byte JBE_DISP8_OPCODE = (byte) 0x76;
        final byte JA_DISP8_OPCODE = (byte) 0x77;
        final byte JS_DISP8_OPCODE = (byte) 0x78;
        final byte JNS_DISP8_OPCODE = (byte) 0x79;
        final byte JLE_DISP8_OPCODE = (byte) 0x7e;
        final byte JG_DISP8_OPCODE = (byte) 0x7f;
        final byte TEST_R8_OPCODE = (byte) 0x84;
        final byte TEST_OPCODE = (byte) 0x85; // this can work on all non 8-bit registers
        final byte MOV_MEM8_REG8_OPCODE = (byte) 0x88;
        final byte MOV_MEM32_REG32_OPCODE = (byte) 0x89;
        final byte MOV_REG32_MEM32_OPCODE = (byte) 0x8b;
        final byte LEA_OPCODE = (byte) 0x8d;
        final byte NOP_OPCODE = (byte) 0x90;
        final byte CDQE_OPCODE = (byte) 0x98;
        final byte CDQ_OPCODE = (byte) 0x99;
        final byte MOVS_ES_EDI_DS_ESI_BYTE_PTR_OPCODE = (byte) 0xa4;
        final byte MOVS_ES_EDI_DS_ESI_OPCODE = (byte) 0xa5;
        final byte TEST_AL_IMM8_OPCODE = (byte) 0xa8;
        final byte TEST_EAX_IMM32_OPCODE = (byte) 0xa9;
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

        return switch (opcodeFirstByte) {
            case NOP_OPCODE -> new Instruction(Opcode.NOP);
            case RET_OPCODE -> new Instruction(Opcode.RET);
            case LEAVE_OPCODE -> new Instruction(Opcode.LEAVE);
            case INT3_OPCODE -> new Instruction(Opcode.INT3);
            case CDQ_OPCODE -> new Instruction(Opcode.CDQ);
            case CDQE_OPCODE -> new Instruction(pref.rex().isOperand64Bit() ? Opcode.CDQE : Opcode.CWDE);

            case MOV_REG32_MEM32_OPCODE -> parseLEALike(b, pref, Opcode.MOV);
            case MOV_MEM32_REG32_OPCODE -> parse(b, pref, Optional.empty(), Opcode.MOV);
            case MOV_MEM8_REG8_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.MOV,
                        parseIndirectOperand(b, pref, modrm, null).build(),
                        Register8.fromByte(
                                Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix()));
            }
            case TEST_R8_OPCODE -> parseSimple8Bit(b, pref, Opcode.TEST, false);
            case TEST_OPCODE -> parseSimple(b, pref, Opcode.TEST, false);
            case TEST_AL_IMM8_OPCODE -> new Instruction(Opcode.TEST, Register8.AL, new Immediate(b.read1()));
            case TEST_EAX_IMM32_OPCODE -> new Instruction(
                    Opcode.TEST,
                    pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX,
                    new Immediate(b.read4LittleEndian()));

                // jumps
            case JMP_DISP32_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of32(b.read4LittleEndian()));
            case JMP_DISP8_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of8(b.read1()));
            case JE_DISP8_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of8(b.read1()));
            case JA_DISP8_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of8(b.read1()));
            case JNE_DISP8_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of8(b.read1()));
            case JBE_DISP8_OPCODE -> new Instruction(Opcode.JBE, RelativeOffset.of8(b.read1()));
            case JS_DISP8_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of8(b.read1()));
            case JNS_DISP8_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of8(b.read1()));
            case JLE_DISP8_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of8(b.read1()));
            case JG_DISP8_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of8(b.read1()));

            case CALL_OPCODE -> new Instruction(Opcode.CALL, RelativeOffset.of32(b.read4LittleEndian()));

            case IMUL_REG_REG_IMM8_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
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
                        new Immediate(b.read1()));
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
            case MOVSXD_OPCODE -> {
                final ModRM modrm = new ModRM(b.read1());
                yield new Instruction(
                        Opcode.MOVSXD,
                        Registers.fromCode(
                                modrm.reg(),
                                pref.rex().isOperand64Bit(),
                                pref.rex().ModRMRegExtension(),
                                pref.hasOperandSizeOverridePrefix()),
                        parseIndirectOperand(
                                        b,
                                        pref,
                                        modrm,
                                        Registers.fromCode(
                                                modrm.rm(),
                                                pref.hasAddressSizeOverridePrefix(),
                                                pref.rex().ModRMRMExtension(),
                                                pref.hasOperandSizeOverridePrefix()))
                                .ptrSize(32)
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x00) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode =
                        switch (opcodeByte) {
                            case 0 -> Opcode.ADD;
                            case 1 -> Opcode.OR;
                            case 2 -> Opcode.ADC;
                            case 3 -> Opcode.SBB;
                            case 4 -> Opcode.AND;
                            case 5 -> Opcode.SUB;
                            case 6 -> Opcode.XOR;
                            case 7 -> Opcode.CMP;
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                final ModRM modrm = new ModRM(b.read1());
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        opcode,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(
                                                b,
                                                pref,
                                                modrm,
                                                Registers.fromCode(
                                                        modrm.rm(),
                                                        pref.rex().isOperand64Bit(),
                                                        pref.rex().ModRMRMExtension(),
                                                        pref.hasOperandSizeOverridePrefix()))
                                        .build()
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x01) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode =
                        switch (opcodeByte) {
                            case 0 -> Opcode.ADD;
                            case 1 -> Opcode.OR;
                            case 2 -> Opcode.ADC;
                            case 3 -> Opcode.SBB;
                            case 4 -> Opcode.AND;
                            case 5 -> Opcode.SUB;
                            case 6 -> Opcode.XOR;
                            case 7 -> Opcode.CMP;
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                final ModRM modrm = new ModRM(b.read1());
                final Register r = Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix());
                yield new Instruction(
                        opcode,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(
                                                b,
                                                pref,
                                                modrm,
                                                Registers.fromCode(
                                                        modrm.rm(),
                                                        !pref.hasAddressSizeOverridePrefix(),
                                                        pref.rex().ModRMRMExtension(),
                                                        pref.hasOperandSizeOverridePrefix()))
                                        .build()
                                : Registers.fromCode(
                                        modrm.rm(),
                                        pref.rex().isOperand64Bit(),
                                        pref.rex().ModRMRMExtension(),
                                        pref.hasOperandSizeOverridePrefix()),
                        r);
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x02) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode =
                        switch (opcodeByte) {
                            case 0 -> Opcode.ADD;
                            case 1 -> Opcode.OR;
                            case 2 -> Opcode.ADC;
                            case 3 -> Opcode.SBB;
                            case 4 -> Opcode.AND;
                            case 5 -> Opcode.SUB;
                            case 6 -> Opcode.XOR;
                            case 7 -> Opcode.CMP;
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                final ModRM modrm = new ModRM(b.read1());
                final byte regByte = Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg());
                yield new Instruction(
                        opcode,
                        Register8.fromByte(regByte, pref.hasRexPrefix()),
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(
                                                b,
                                                pref,
                                                modrm,
                                                Registers.fromCode(
                                                        modrm.rm(),
                                                        pref.rex().isOperand64Bit(),
                                                        pref.rex().ModRMRMExtension(),
                                                        pref.hasOperandSizeOverridePrefix()))
                                        .build()
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x03) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode =
                        switch (opcodeByte) {
                            case 0 -> Opcode.ADD;
                            case 1 -> Opcode.OR;
                            case 2 -> Opcode.ADC;
                            case 3 -> Opcode.SBB;
                            case 4 -> Opcode.AND;
                            case 5 -> Opcode.SUB;
                            case 6 -> Opcode.XOR;
                            case 7 -> Opcode.CMP;
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                final ModRM modrm = new ModRM(b.read1());
                final Register r = Registers.fromCode(
                        modrm.reg(),
                        pref.rex().isOperand64Bit(),
                        pref.rex().ModRMRegExtension(),
                        pref.hasOperandSizeOverridePrefix());
                yield new Instruction(
                        opcode,
                        r,
                        (modrm.mod() != (byte) 0x03)
                                ? parseIndirectOperand(
                                                b,
                                                pref,
                                                modrm,
                                                Registers.fromCode(
                                                        modrm.rm(),
                                                        !pref.hasAddressSizeOverridePrefix(),
                                                        pref.rex().ModRMRMExtension(),
                                                        pref.hasOperandSizeOverridePrefix()))
                                        .build()
                                : Registers.fromCode(
                                        modrm.rm(),
                                        pref.rex().isOperand64Bit(),
                                        pref.rex().ModRMRMExtension(),
                                        pref.hasOperandSizeOverridePrefix()));
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x04) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode =
                        switch (opcodeByte) {
                            case 0 -> Opcode.ADD;
                            case 1 -> Opcode.OR;
                            case 2 -> Opcode.ADC;
                            case 3 -> Opcode.SBB;
                            case 4 -> Opcode.AND;
                            case 5 -> Opcode.SUB;
                            case 6 -> Opcode.XOR;
                            case 7 -> Opcode.CMP;
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                yield new Instruction(opcode, Register8.AL, new Immediate(b.read1()));
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
                final byte m1 = (byte) 0xc7; // 11000111 (just to check that we are doing the correct thing)
                if (BitUtils.and(opcodeFirstByte, m1) != (byte) 0x05) {
                    throw new IllegalArgumentException("Invalid value");
                }
                final byte m2 = (byte) 0x38; // 00111000 (the inverse of m1)
                final byte opcodeByte = BitUtils.shr(BitUtils.and(opcodeFirstByte, m2), 3);
                final Opcode opcode =
                        switch (opcodeByte) {
                            case 0 -> Opcode.ADD;
                            case 1 -> Opcode.OR;
                            case 2 -> Opcode.ADC;
                            case 3 -> Opcode.SBB;
                            case 4 -> Opcode.AND;
                            case 5 -> Opcode.SUB;
                            case 6 -> Opcode.XOR;
                            case 7 -> Opcode.CMP;
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                yield new Instruction(
                        opcode,
                        pref.hasOperandSizeOverridePrefix()
                                ? Register16.AX
                                : (pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX),
                        pref.hasOperandSizeOverridePrefix()
                                ? new Immediate(b.read2LittleEndian())
                                : new Immediate(b.read4LittleEndian()));
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
                        new Immediate(b.read1()));
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
                            case 16 -> new Immediate(b.read2LittleEndian());
                            case 32 -> new Immediate(b.read4LittleEndian());
                            case 64 -> new Immediate(b.read8LittleEndian());
                            default -> throw new IllegalArgumentException("Invalid value");
                        };
                yield new Instruction(pref.rex().isOperand64Bit() ? Opcode.MOVABS : Opcode.MOV, r, imm);
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

            case PUSH_IMM8_OPCODE -> new Instruction(Opcode.PUSH, new Immediate(b.read1()));

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

            case LEA_OPCODE -> // page 1191
            parseLEALike(b, pref, Opcode.LEA);

            case (byte) 0xf0 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized LOCK prefix at byte 0x%08x", b.position()));
            case (byte) 0xf2 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized REPNE prefix at byte 0x%08x", b.position()));
            case (byte) 0xf3 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized REP prefix at byte 0x%08x", b.position()));
            case (byte) 0x66 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized operand size override prefix at byte 0x%08x", b.position()));
            case (byte) 0x67 -> throw new IllegalArgumentException(
                    String.format("Found an unrecognized address size override prefix at byte 0x%08x", b.position()));
            default -> throw new IllegalArgumentException(String.format("Unknown opcode %02x", opcodeFirstByte));
        };
    }

    private Prefixes parsePrefixes(final ByteBuffer b) {
        Optional<Byte> p1 = Optional.empty(); // Legacy Prefix Group 1
        Optional<Byte> p2 = Optional.empty(); // Legacy Prefix Group 2
        boolean hasOperandSizeOverridePrefix = false;
        boolean hasAddressSizeOverridePrefix = false;

        for (int i = 0; i < 4; i++) {
            byte x = b.read1();

            if (isLegacyPrefixGroup1(x)) {
                if (p1.isPresent()) {
                    throw new IllegalStateException(
                            String.format("Found duplicate legacy prefix group 1 at byte 0x%08x", b.position()));
                }
                p1 = Optional.of(x);
            } else if (isLegacyPrefixGroup2(x)) {
                if (p2.isPresent()) {
                    throw new IllegalStateException(
                            String.format("Found duplicate legacy prefix group 2 at byte 0x%08x", b.position()));
                }
                p2 = Optional.of(x);
            } else if (isLegacyPrefixGroup3(x)) {
                if (hasOperandSizeOverridePrefix) {
                    throw new IllegalStateException(
                            String.format("Found duplicate legacy prefix group 3 at byte 0x%08x", b.position()));
                }
                hasOperandSizeOverridePrefix = true;
            } else if (isLegacyPrefixGroup4(x)) {
                if (hasAddressSizeOverridePrefix) {
                    throw new IllegalStateException(
                            String.format("Found duplicate legacy prefix group 4 at byte 0x%08x", b.position()));
                }
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

    private Instruction parse(
            final ByteBuffer b, final Prefixes pref, final Optional<Integer> immediateBytes, final Opcode opcode) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        return parse(b, pref, modrm, immediateBytes, opcode, Optional.empty(), false);
    }

    private Instruction parse(
            final ByteBuffer b,
            final Prefixes pref,
            final ModRM modrm,
            final Optional<Integer> immediateBytes,
            final Opcode opcode,
            final Optional<Integer> pointerSize,
            final boolean ignoreSecondOperand) {
        final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
        final boolean hasOperandSizeOverridePrefix = pref.hasOperandSizeOverridePrefix();
        final RexPrefix rexPrefix = pref.rex();
        final byte rm = modrm.rm();
        final Register operand1 = Registers.fromCode(
                modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension(), hasOperandSizeOverridePrefix);
        Operand operand2 = Registers.fromCode(
                rm, !hasAddressSizeOverridePrefix, rexPrefix.ModRMRMExtension(), hasOperandSizeOverridePrefix);

        // Table at page 530
        final byte mod = modrm.mod();
        if (mod < (byte) 0x00 || mod > (byte) 0x03) {
            throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
        }

        final IndirectOperand.IndirectOperandBuilder iob = parseIndirectOperand(b, pref, modrm, operand2);

        if (pointerSize.isPresent()) {
            logger.debug("Using pointer size: %,d", pointerSize.orElseThrow());
            iob.ptrSize(pointerSize.orElseThrow());
        }

        if (mod != (byte) 0x03 /* 11 */) {
            // indirect operand needed
            operand2 = iob.build();
        } else {
            // indirect operand not needed, so we take the second operand without using the
            // addressSizeOverride
            operand2 = Registers.fromCode(
                    rm, rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension(), hasOperandSizeOverridePrefix);
        }

        if (immediateBytes.isEmpty()) {
            if (ignoreSecondOperand) {
                return new Instruction(opcode, operand2);
            }
            return new Instruction(opcode, operand2, operand1);
        }

        return switch (immediateBytes.orElseThrow()) {
            case 1 -> new Instruction(opcode, operand2, new Immediate(b.read1()));
            case 2 -> new Instruction(opcode, operand2, new Immediate(b.read2LittleEndian()));
            case 4 -> new Instruction(opcode, operand2, new Immediate(b.read4LittleEndian()));
            default -> throw new IllegalArgumentException(
                    String.format("Invalid value for immediate bytes: %,d", immediateBytes.orElseThrow()));
        };
    }

    private IndirectOperandBuilder parseIndirectOperand(
            final ByteBuffer b, final Prefixes pref, final ModRM modrm, final Operand operand2) {
        final RexPrefix rexPrefix = pref.rex();
        final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
        final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
        SIB sib;
        if (modrm.mod() != (byte) 0x03 /* 11 */ && modrm.rm() == (byte) 0x04 /* 100 */) {
            // SIB needed
            final byte _sib = b.read1();
            sib = new SIB(_sib);
            logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);

            final Register base =
                    Registers.fromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension(), false);
            final Register index = Registers.fromCode(
                    sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension(), false);
            if (index.toIntelSyntax().endsWith("sp")) { // an indirect operand of [xxx+rsp+...] is not
                // allowed
                iob.reg2(base);
            } else {
                if (!(modrm.mod() == (byte) 0x00 && base.toIntelSyntax().endsWith("bp"))) {
                    iob.reg1(base);
                }
                iob.reg2(index);
                iob.constant(1 << BitUtils.asInt(sib.scale()));
            }
        } else {
            // SIB not needed
            sib = null;
            if (modrm.mod() == (byte) 0x00 && operand2.toIntelSyntax().endsWith("bp")) {
                iob.reg2(hasAddressSizeOverridePrefix ? Register32.EIP : Register64.RIP);
            } else {
                iob.reg2((Register) operand2);
            }
        }

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

    // TODO: change name of method
    private Instruction parseSimple(
            final ByteBuffer b, final Prefixes pref, final Opcode opcode, final boolean invertOperands) {
        final boolean hasOperandSizeOverridePrefix = pref.hasOperandSizeOverridePrefix();
        final RexPrefix rexPrefix = pref.rex();
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final Register operand1 = Registers.fromCode(
                modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension(), hasOperandSizeOverridePrefix);
        final Register operand2 = Registers.fromCode(
                modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension(), hasOperandSizeOverridePrefix);

        if (invertOperands) {
            return new Instruction(opcode, operand1, operand2);
        }
        return new Instruction(opcode, operand2, operand1);
    }

    // TODO: change name of method
    private Instruction parseSimple8Bit(
            final ByteBuffer b, final Prefixes pref, final Opcode opcode, final boolean invertOperands) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final Register operand1 =
                Register8.fromByte(Registers.combine(pref.rex().ModRMRegExtension(), modrm.reg()), pref.hasRexPrefix());
        final Register operand2 =
                Register8.fromByte(Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm()), pref.hasRexPrefix());

        if (invertOperands) {
            return new Instruction(opcode, operand1, operand2);
        }
        return new Instruction(opcode, operand2, operand1);
    }

    /**
     * Parses a LEA-like instruction.
     * opcode operand, indirect-operand
     */
    private Instruction parseLEALike(final ByteBuffer b, final Prefixes pref, final Opcode opcode) {
        return parseLEALike(b, pref, opcode, Optional.empty());
    }

    private Instruction parseLEALike(
            final ByteBuffer b, final Prefixes pref, final Opcode opcode, final Optional<Integer> pointerSize) {
        final boolean hasAddressSizeOverridePrefix = pref.hasAddressSizeOverridePrefix();
        final boolean hasOperandSizeOverridePrefix = pref.hasOperandSizeOverridePrefix();
        final RexPrefix rexPrefix = pref.rex();
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final byte rm = modrm.rm();
        final Register operand1 = Registers.fromCode(
                modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension(), hasOperandSizeOverridePrefix);
        final Register operand2 = Registers.fromCode(
                rm, !hasAddressSizeOverridePrefix, rexPrefix.ModRMRMExtension(), hasOperandSizeOverridePrefix);

        // Table at page 530
        final byte mod = modrm.mod();
        if (mod < (byte) 0x00 || mod > (byte) 0x03) {
            throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
        }

        final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
        SIB sib;
        if (rm == (byte) 0x04 /* 100 */) {
            final byte _sib = b.read1();
            sib = new SIB(_sib);
            logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);

            final Register base =
                    Registers.fromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension(), false);
            final Register index = Registers.fromCode(
                    sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension(), false);
            if (index.toIntelSyntax().endsWith("sp")) { // an indirect operand of [xxx+rsp+...] is not
                // allowed
                iob.reg2(base);
            } else {
                if (!(mod == (byte) 0x00 && base.toIntelSyntax().endsWith("bp"))) {
                    iob.reg1(base);
                }
                iob.reg2(index);
                iob.constant(1 << BitUtils.asInt(sib.scale()));
            }
        } else {
            sib = new SIB((byte) 0x00);
            if (mod == (byte) 0x00 && operand2.toIntelSyntax().endsWith("bp")) {
                iob.reg2(hasAddressSizeOverridePrefix ? Register32.EIP : Register64.RIP);
            } else {
                iob.reg2(operand2);
            }
        }

        if ((mod == (byte) 0x00 && rm == (byte) 0x05)
                || (mod == (byte) 0x00 && sib.base() == (byte) 0x05)
                || mod == (byte) 0x02) {
            final int disp32 = b.read4LittleEndian();
            iob.displacement(disp32);
        } else if (mod == (byte) 0x01) {
            final byte disp8 = b.read1();
            iob.displacement(disp8);
        }

        if (pointerSize.isPresent()) {
            iob.ptrSize(pointerSize.orElseThrow());
        }

        return new Instruction(opcode, operand1, iob.build());
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

    private boolean isLegacyPrefixGroup3(final byte prefix) {
        final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
        return prefix == OPERAND_SIZE_OVERRIDE_PREFIX;
    }

    private boolean isLegacyPrefixGroup4(final byte prefix) {
        final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;
        return prefix == ADDRESS_SIZE_OVERRIDE_PREFIX;
    }

    private boolean isMultibyteOpcode(final byte opcode) {
        final byte MULTIBYTE_OPCODE_PREFIX = (byte) 0x0f;
        return opcode == MULTIBYTE_OPCODE_PREFIX;
    }
}
