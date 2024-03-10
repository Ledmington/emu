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

    // single byte opcodes
    private static final byte ADD_OPCODE = (byte) 0x01;
    private static final byte SBB_OPCODE = (byte) 0x19;
    private static final byte SBB_AL_IMM8_OPCODE = (byte) 0x1c;
    private static final byte AND_OPCODE = (byte) 0x21;
    private static final byte AND_RAX_IMM32_OPCODE = (byte) 0x25;
    private static final byte SUB_OPCODE = (byte) 0x29;
    private static final byte XOR_OPCODE = (byte) 0x31;
    private static final byte CMP_INDIRECT8_R8_OPCODE = (byte) 0x38;
    private static final byte CMP_INDIRECT32_R32_OPCODE = (byte) 0x39;
    private static final byte CMP_RAX_IMM32_OPCODE = (byte) 0x3d;
    private static final byte PUSH_EAX_OPCODE = (byte) 0x50;
    private static final byte PUSH_ECX_OPCODE = (byte) 0x51;
    private static final byte PUSH_EDX_OPCODE = (byte) 0x52;
    private static final byte PUSH_EBX_OPCODE = (byte) 0x53;
    private static final byte PUSH_ESP_OPCODE = (byte) 0x54;
    private static final byte PUSH_EBP_OPCODE = (byte) 0x55;
    private static final byte PUSH_ESI_OPCODE = (byte) 0x56;
    private static final byte PUSH_EDI_OPCODE = (byte) 0x57;
    private static final byte POP_EAX_OPCODE = (byte) 0x58;
    private static final byte POP_ECX_OPCODE = (byte) 0x59;
    private static final byte POP_EDX_OPCODE = (byte) 0x5a;
    private static final byte POP_EBX_OPCODE = (byte) 0x5b;
    private static final byte POP_ESP_OPCODE = (byte) 0x5c;
    private static final byte POP_EBP_OPCODE = (byte) 0x5d;
    private static final byte POP_ESI_OPCODE = (byte) 0x5e;
    private static final byte POP_EDI_OPCODE = (byte) 0x5f;
    private static final byte IMUL_REG_REG_IMM8_OPCODE = (byte) 0x6b;
    private static final byte JE_DISP8_OPCODE = (byte) 0x74;
    private static final byte JNE_DISP8_OPCODE = (byte) 0x75;
    private static final byte JS_DISP8_OPCODE = (byte) 0x78;
    private static final byte JNS_DISP8_OPCODE = (byte) 0x79;
    private static final byte JLE_DISP8_OPCODE = (byte) 0x7e;
    private static final byte TEST_R8_OPCODE = (byte) 0x84;
    private static final byte TEST_OPCODE = (byte) 0x85; // this can work on all non 8-bit registers
    private static final byte MOV_MEM_REG_OPCODE = (byte) 0x89;
    private static final byte MOV_REG_MEM_OPCODE = (byte) 0x8b;
    private static final byte LEA_OPCODE = (byte) 0x8d;
    private static final byte NOP_OPCODE = (byte) 0x90;
    private static final byte CDQE_OPCODE = (byte) 0x98;
    private static final byte CDQ_OPCODE = (byte) 0x99;
    private static final byte MOVS_ES_EDI_DS_ESI_BYTE_PTR_OPCODE = (byte) 0xa4;
    private static final byte MOVS_ES_EDI_DS_ESI_OPCODE = (byte) 0xa5;
    private static final byte TEST_AL_IMM8_OPCODE = (byte) 0xa8;
    private static final byte TEST_EAX_IMM32_OPCODE = (byte) 0xa9;
    private static final byte MOV_IMM8_TO_AL_OPCODE = (byte) 0xb0;
    private static final byte MOV_IMM8_TO_CL_OPCODE = (byte) 0xb1;
    private static final byte MOV_IMM8_TO_DL_OPCODE = (byte) 0xb2;
    private static final byte MOV_IMM8_TO_BL_OPCODE = (byte) 0xb3;
    private static final byte MOV_IMM8_TO_AH_OPCODE = (byte) 0xb4;
    private static final byte MOV_IMM8_TO_CH_OPCODE = (byte) 0xb5;
    private static final byte MOV_IMM8_TO_DH_OPCODE = (byte) 0xb6;
    private static final byte MOV_IMM8_TO_BH_OPCODE = (byte) 0xb7;
    private static final byte MOV_IMM32_TO_EAX_OPCODE = (byte) 0xb8;
    private static final byte MOV_IMM32_TO_ECX_OPCODE = (byte) 0xb9;
    private static final byte MOV_IMM32_TO_EDX_OPCODE = (byte) 0xba;
    private static final byte MOV_IMM32_TO_EBX_OPCODE = (byte) 0xbb;
    private static final byte MOV_IMM32_TO_ESP_OPCODE = (byte) 0xbc;
    private static final byte MOV_IMM32_TO_EBP_OPCODE = (byte) 0xbd;
    private static final byte MOV_IMM32_TO_ESI_OPCODE = (byte) 0xbe;
    private static final byte MOV_IMM32_TO_EDI_OPCODE = (byte) 0xbf;
    private static final byte RET_OPCODE = (byte) 0xc3;
    private static final byte LEAVE_OPCODE = (byte) 0xc9;
    private static final byte INT3_OPCODE = (byte) 0xcc;
    private static final byte CALL_OPCODE = (byte) 0xe8;
    private static final byte JMP_DISP32_OPCODE = (byte) 0xe9;
    private static final byte JMP_DISP8_OPCODE = (byte) 0xeb;

    // two bytes opcodes
    private static final byte UD2_OPCODE = (byte) 0x0b;
    private static final byte CMOVE_OPCODE = (byte) 0x44;
    private static final byte JE_DISP32_OPCODE = (byte) 0x84;
    private static final byte JNE_DISP32_OPCODE = (byte) 0x85;
    private static final byte JBE_DISP32_OPCODE = (byte) 0x86;
    private static final byte JA_DISP32_OPCODE = (byte) 0x87;
    private static final byte JS_DISP32_OPCODE = (byte) 0x88;
    private static final byte JNS_DISP32_OPCODE = (byte) 0x89;
    private static final byte JLE_DISP32_OPCODE = (byte) 0x8e;
    private static final byte JG_DISP32_OPCODE = (byte) 0x8f;
    private static final byte IMUL_OPCODE = (byte) 0xaf;
    private static final byte MOVZX_BYTE_PTR_OPCODE = (byte) 0xb6;
    private static final byte MOVZX_WORD_PTR_OPCODE = (byte) 0xb7;
    private static final byte MOVSX_BYTE_PTR_OPCODE = (byte) 0xbe;
    private static final byte MOVSX_WORD_PTR_OPCODE = (byte) 0xbf;

    // extended opcodes

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
        } else if (opcodeFirstByte == (byte) 0xc0 || opcodeFirstByte == (byte) 0xc1) {
            return parseExtendedOpcodeGroup2(b, opcodeFirstByte, pref);
        } else if (opcodeFirstByte == (byte) 0xc6 || opcodeFirstByte == (byte) 0xc7) {
            return parseExtendedOpcodeGroup11(b, opcodeFirstByte, pref);
        } else if (opcodeFirstByte == (byte) 0xff) {
            return parseExtendedOpcodeGroup3(b, opcodeFirstByte, pref);
        } else {
            // 1 byte opcode
            return parseSingleByteOpcode(b, opcodeFirstByte, pref);
        }
    }

    private Instruction parseExtendedOpcodeGroup3(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        final byte opcodeSecondByte = b.read1();
        logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

        final ModRM modrm = new ModRM(opcodeSecondByte);
        logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

        return switch (modrm.reg()) {
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
                                parseIndirectOperand(b, pref.rex(), modrm, pref.hasAddressSizeOverridePrefix(), reg)
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
                                    pref.rex(),
                                    modrm,
                                    pref.hasAddressSizeOverridePrefix(),
                                    Registers.fromCode(
                                            modrm.rm(), false, pref.rex().ModRMRMExtension(), false))
                            .ptrSize(pref.hasOperandSizeOverridePrefix() ? 32 : 64)
                            .build());
            case (byte) 0x07 /* 111 */ -> throw new IllegalArgumentException(
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

        return switch (modrm.reg()) {
            case (byte) 0x05 /* 101 */ -> new Instruction(
                    Opcode.SHR,
                    Registers.fromCode(
                            modrm.rm(), pref.rex().isOperand64Bit(), pref.rex().ModRMRMExtension(), false),
                    new Immediate(b.read1()));
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

        final int immediateBytes = pref.hasOperandSizeOverridePrefix() ? 2 : ((opcodeFirstByte == (byte) 0x81) ? 4 : 1);

        return switch (modrm.reg()) {
                // full table at page 2856
            case (byte) 0x00 /* 000 */ -> new Instruction(
                    Opcode.ADD,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(b.read1()));
            case (byte) 0x01 /* 001 */ -> parse(
                    b, pref, modrm, Optional.of(immediateBytes), Opcode.OR, Optional.empty(), false);
            case (byte) 0x04 /* 100 */ -> new Instruction(
                    Opcode.AND,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(
                            (opcodeFirstByte == (byte) 0x83) ? ((long) b.read1()) : ((long) b.read4LittleEndian())));
            case (byte) 0x05 /* 101 */ -> new Instruction(
                    Opcode.SUB,
                    Registers.fromCode(
                            modrm.rm(),
                            pref.rex().isOperand64Bit(),
                            pref.rex().ModRMRMExtension(),
                            pref.hasOperandSizeOverridePrefix()),
                    new Immediate(b.read4LittleEndian()));
            case (byte) 0x07 /* 111 */ -> parse(
                    b,
                    pref,
                    modrm,
                    Optional.of(immediateBytes),
                    Opcode.CMP,
                    Optional.of(pref.rex().isOperand64Bit() ? 64 : 8 * immediateBytes),
                    false);
            default -> throw new IllegalArgumentException(
                    String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private Instruction parse2BytesOpcode(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
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
                                            pref.rex(),
                                            modrm,
                                            pref.hasAddressSizeOverridePrefix(),
                                            Registers.fromCode(
                                                    modrm.rm(),
                                                    !pref.hasAddressSizeOverridePrefix(),
                                                    pref.rex().ModRMRMExtension(),
                                                    pref.hasOperandSizeOverridePrefix()))
                                    .ptrSize(ptrSize)
                                    .build());
                } else {
                    final byte regByte = Registers.combine(pref.rex().ModRMRMExtension(), modrm.rm());
                    final Register r2 = (ptrSize == 8) ? Register8.fromByte(regByte) : Register16.fromByte(regByte);
                    yield new Instruction(opcode, r1, r2);
                }
            }

            case CMOVE_OPCODE ->
            // page 771
            parseSimple(b, pref, Opcode.CMOVE, true);
            case UD2_OPCODE -> new Instruction(Opcode.UD2);
            default -> throw new IllegalArgumentException(
                    String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
        };
    }

    private Instruction parseSingleByteOpcode(final ByteBuffer b, final byte opcodeFirstByte, final Prefixes pref) {
        return switch (opcodeFirstByte) {
            case NOP_OPCODE -> new Instruction(Opcode.NOP);
            case RET_OPCODE -> new Instruction(Opcode.RET);
            case LEAVE_OPCODE -> new Instruction(Opcode.LEAVE);
            case INT3_OPCODE -> new Instruction(Opcode.INT3);
            case CDQ_OPCODE -> new Instruction(Opcode.CDQ);
            case CDQE_OPCODE -> new Instruction(pref.rex().isOperand64Bit() ? Opcode.CDQE : Opcode.CWDE);
            case SBB_OPCODE -> parseSimple(b, pref, Opcode.SBB, false);
            case SBB_AL_IMM8_OPCODE -> new Instruction(Opcode.SBB, Register8.AL, new Immediate(b.read1()));
            case MOV_REG_MEM_OPCODE -> parseLEALike(b, pref, Opcode.MOV);
            case MOV_MEM_REG_OPCODE -> parse(b, pref, Optional.empty(), Opcode.MOV);
            case TEST_R8_OPCODE -> parseSimple8Bit(b, pref.rex(), Opcode.TEST, false);
            case TEST_OPCODE -> parseSimple(b, pref, Opcode.TEST, false);
            case TEST_AL_IMM8_OPCODE -> new Instruction(Opcode.TEST, Register8.AL, new Immediate(b.read1()));
            case TEST_EAX_IMM32_OPCODE -> new Instruction(
                    Opcode.TEST,
                    pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX,
                    new Immediate(b.read4LittleEndian()));
            case XOR_OPCODE -> parseSimple(b, pref, Opcode.XOR, false);
            case SUB_OPCODE -> parseSimple(b, pref, Opcode.SUB, false);
            case ADD_OPCODE -> parseSimple(b, pref, Opcode.ADD, false);
            case CMP_INDIRECT8_R8_OPCODE -> parseSimple8Bit(b, pref.rex(), Opcode.CMP, false);
            case CMP_INDIRECT32_R32_OPCODE -> parse(b, pref, Optional.empty(), Opcode.CMP);
            case CMP_RAX_IMM32_OPCODE -> new Instruction(
                    Opcode.CMP,
                    pref.rex().isOperand64Bit() ? Register64.RAX : Register32.EAX,
                    new Immediate(b.read4LittleEndian()));
            case JMP_DISP32_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of32(b.read4LittleEndian()));
            case JMP_DISP8_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of8(b.read1()));
            case JE_DISP8_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of8(b.read1()));
            case JNE_DISP8_OPCODE -> new Instruction(Opcode.JNE, RelativeOffset.of8(b.read1()));
            case JS_DISP8_OPCODE -> new Instruction(Opcode.JS, RelativeOffset.of8(b.read1()));
            case JNS_DISP8_OPCODE -> new Instruction(Opcode.JNS, RelativeOffset.of8(b.read1()));
            case JLE_DISP8_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of8(b.read1()));
            case CALL_OPCODE -> new Instruction(Opcode.CALL, RelativeOffset.of32(b.read4LittleEndian()));
            case AND_OPCODE -> parse(b, pref, Optional.empty(), Opcode.AND);
            case AND_RAX_IMM32_OPCODE -> new Instruction(
                    Opcode.AND, Register64.RAX, new Immediate((long) b.read4LittleEndian()));
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

                // MOV 8/16-bit
            case MOV_IMM8_TO_AL_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R8B : Register8.AL,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_CL_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R9B : Register8.CL,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_DL_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R10B : Register8.DL,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_BL_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R11B : Register8.BL,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_AH_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R12B : Register8.AH,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_CH_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R13B : Register8.CH,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_DH_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R14B : Register8.DH,
                    new Immediate(b.read1()));
            case MOV_IMM8_TO_BH_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.rex().opcodeRegExtension() ? Register8.R15B : Register8.BH,
                    new Immediate(b.read1()));

                // MOV 32-bit
            case MOV_IMM32_TO_EAX_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R8W : Register16.AX)
                            : (pref.rex().opcodeRegExtension() ? Register32.R8D : Register32.EAX),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_EBX_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R11W : Register16.BX)
                            : (pref.rex().opcodeRegExtension() ? Register32.R11D : Register32.EBX),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_ECX_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R9W : Register16.CX)
                            : (pref.rex().opcodeRegExtension() ? Register32.R9D : Register32.ECX),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_EDX_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R10W : Register16.DX)
                            : (pref.rex().opcodeRegExtension() ? Register32.R10D : Register32.EDX),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_ESP_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R12W : Register16.SP)
                            : (pref.rex().opcodeRegExtension() ? Register32.R12D : Register32.ESP),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_EBP_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R13W : Register16.BP)
                            : (pref.rex().opcodeRegExtension() ? Register32.R13D : Register32.EBP),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_ESI_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R14W : Register16.SI)
                            : (pref.rex().opcodeRegExtension() ? Register32.R14D : Register32.ESI),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));
            case MOV_IMM32_TO_EDI_OPCODE -> new Instruction(
                    Opcode.MOV,
                    pref.hasOperandSizeOverridePrefix()
                            ? (pref.rex().opcodeRegExtension() ? Register16.R15W : Register16.DI)
                            : (pref.rex().opcodeRegExtension() ? Register32.R15D : Register32.EDI),
                    new Immediate(pref.hasOperandSizeOverridePrefix() ? b.read2LittleEndian() : b.read4LittleEndian()));

                // PUSH
            case PUSH_EAX_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R8 : Register64.RAX);
            case PUSH_EBX_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R11 : Register64.RBX);
            case PUSH_ECX_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R9 : Register64.RCX);
            case PUSH_EDX_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R10 : Register64.RDX);
            case PUSH_ESP_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R12 : Register64.RSP);
            case PUSH_EBP_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R13 : Register64.RBP);
            case PUSH_ESI_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R14 : Register64.RSI);
            case PUSH_EDI_OPCODE -> new Instruction(
                    Opcode.PUSH, pref.rex().extension() ? Register64.R15 : Register64.RDI);

                // POP
            case POP_EAX_OPCODE -> new Instruction(Opcode.POP, pref.rex().extension() ? Register64.R8 : Register64.RAX);
            case POP_EBX_OPCODE -> new Instruction(
                    Opcode.POP, pref.rex().extension() ? Register64.R11 : Register64.RBX);
            case POP_ECX_OPCODE -> new Instruction(Opcode.POP, pref.rex().extension() ? Register64.R9 : Register64.RCX);
            case POP_EDX_OPCODE -> new Instruction(
                    Opcode.POP, pref.rex().extension() ? Register64.R10 : Register64.RDX);
            case POP_ESP_OPCODE -> new Instruction(
                    Opcode.POP, pref.rex().extension() ? Register64.R12 : Register64.RSP);
            case POP_EBP_OPCODE -> new Instruction(
                    Opcode.POP, pref.rex().extension() ? Register64.R13 : Register64.RBP);
            case POP_ESI_OPCODE -> new Instruction(
                    Opcode.POP, pref.rex().extension() ? Register64.R14 : Register64.RSI);
            case POP_EDI_OPCODE -> new Instruction(
                    Opcode.POP, pref.rex().extension() ? Register64.R15 : Register64.RDI);

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

        return new Prefixes(p1, p2, hasOperandSizeOverridePrefix, hasAddressSizeOverridePrefix, rexPrefix);
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

        final IndirectOperand.IndirectOperandBuilder iob =
                parseIndirectOperand(b, rexPrefix, modrm, hasAddressSizeOverridePrefix, operand2);

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
            final ByteBuffer b,
            final RexPrefix rexPrefix,
            final ModRM modrm,
            final boolean hasAddressSizeOverridePrefix,
            final Operand operand2) {
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
            final ByteBuffer b, final RexPrefix rexPrefix, final Opcode opcode, final boolean invertOperands) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final Register operand1 = Register8.fromByte(Registers.combine(rexPrefix.ModRMRegExtension(), modrm.reg()));
        final Register operand2 = Register8.fromByte(Registers.combine(rexPrefix.ModRMRMExtension(), modrm.rm()));

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
