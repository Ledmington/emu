package com.ledmington.cpu.x86;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ByteBuffer;
import com.ledmington.utils.MiniLogger;

/**
 * Reference Intel\u00ae 64 and IA-32 Architectures Software Developer’s Manual
 * volume 2.
 * Legacy prefixes : Paragraph 2.1.1.
 * Instruction opcodes : Appendix A. (pag. 2839)
 */
public final class InstructionDecoder {

    private static final MiniLogger logger = MiniLogger.getLogger("x86-asm");

    // single byte opcodes
    private static final byte ADD_OPCODE = (byte) 0x01;
    private static final byte AND_RAX_IMM32_OPCODE = (byte) 0x25;
    private static final byte SUB_OPCODE = (byte) 0x29;
    private static final byte XOR_OPCODE = (byte) 0x31;
    private static final byte CMP_REG_REG_OPCODE = (byte) 0x39;
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
    private static final byte JE_DISP8_OPCODE = (byte) 0x74;
    private static final byte JLE_DISP8_OPCODE = (byte) 0x7e;
    private static final byte TEST_OPCODE = (byte) 0x85;
    private static final byte MOV_MEM_REG_OPCODE = (byte) 0x89;
    private static final byte MOV_REG_MEM_OPCODE = (byte) 0x8b;
    private static final byte LEA_OPCODE = (byte) 0x8d;
    private static final byte NOP_OPCODE = (byte) 0x90;
    private static final byte CDQ_OPCODE = (byte) 0x99;
    private static final byte MOV_IMM32_TO_ECX_OPCODE = (byte) 0xb9;
    private static final byte MOV_IMM32_TO_EDI_OPCODE = (byte) 0xbf;
    private static final byte RET_OPCODE = (byte) 0xc3;
    private static final byte MOV_INDIRECT_IMM32_OPCODE = (byte) 0xc7;
    private static final byte LEAVE_OPCODE = (byte) 0xc9;
    private static final byte INT3_OPCODE = (byte) 0xcc;
    private static final byte CALL_OPCODE = (byte) 0xe8;
    private static final byte JMP_DISP32_OPCODE = (byte) 0xe9;
    private static final byte JMP_DISP8_OPCODE = (byte) 0xeb;

    // two bytes opcodes
    private static final byte CMOVE_OPCODE = (byte) 0x44;
    private static final byte JE_DISP32_OPCODE = (byte) 0x84;
    private static final byte JA_OPCODE = (byte) 0x87;
    private static final byte JG_OPCODE = (byte) 0x8f;

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
            logger.info("%08x: %s", pos, inst.toIntelSyntax());
            instructions.add(inst);
        }

        return instructions;
    }

    private Instruction decodeInstruction(final ByteBuffer b) {
        Objects.requireNonNull(b);

        final Optional<Byte> p1 = readLegacyPrefixGroup1(b);
        final Optional<Byte> p2 = readLegacyPrefixGroup2(b);
        final Optional<Byte> p3 = readLegacyPrefixGroup3(b);
        final Optional<Byte> p4 = readLegacyPrefixGroup4(b);

        final byte rexByte = b.read1();
        final RexPrefix rexPrefix;
        final boolean isREX = RexPrefix.isREXPrefix(rexByte);
        if (isREX) {
            rexPrefix = new RexPrefix(rexByte);
            logger.debug("Found REX prefix: 0x%02x -> %s", rexByte, rexPrefix);
        } else {
            rexPrefix = new RexPrefix((byte) 0x40);
        }

        final byte opcodeFirstByte = isREX ? b.read1() : rexByte;

        if (isMultibyteOpcode(opcodeFirstByte)) {
            // more than 1 bytes opcode
            final byte opcodeSecondByte = b.read1();
            logger.debug("Read multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);
            return switch (opcodeSecondByte) {
                case JA_OPCODE -> new Instruction(Opcode.JA, RelativeOffset.of32(b.read4LittleEndian()));
                case JE_DISP32_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of32(b.read4LittleEndian()));
                case JG_OPCODE -> new Instruction(Opcode.JG, RelativeOffset.of32(b.read4LittleEndian()));
                case CMOVE_OPCODE ->
                // page 771
                parseSimple(b, rexPrefix, Opcode.CMOVE, true);
                default -> throw new IllegalArgumentException(
                        String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            };
        } else if (isExtendedOpcode(opcodeFirstByte)) {
            // more than 1 bytes opcode
            final byte opcodeSecondByte = b.read1();
            logger.debug("Read extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte);

            final ModRM modrm = new ModRM(opcodeSecondByte);
            logger.debug("ModR/M byte: 0x%02x", opcodeSecondByte);

            return switch (modrm.reg()) {
                    // full table at page 2856
                case (byte) 0x00 /* 000 */ -> new Instruction(
                        Opcode.ADD,
                        Register.fromCode(modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension()),
                        new Immediate(b.read1()));
                case (byte) 0x01 /* 001 */ -> parse(b, p4.isPresent(), rexPrefix, modrm, Optional.of(1), Opcode.OR);
                case (byte) 0x04 /* 100 */ -> new Instruction(
                        Opcode.AND,
                        Register.fromCode(modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension()),
                        new Immediate((long) b.read1()));
                case (byte) 0x05 /* 101 */ -> new Instruction(
                        Opcode.SUB,
                        Register.fromCode(modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension()),
                        new Immediate(b.read4LittleEndian()));
                case (byte) 0x07 /* 111 */ -> new Instruction(
                        Opcode.CMP,
                        Register.fromCode(modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRMExtension()),
                        new Immediate(b.read4LittleEndian()));
                default -> throw new IllegalArgumentException(
                        String.format("Unknown extended opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            };
        } else {
            // 1 byte opcode
            return switch (opcodeFirstByte) {
                case NOP_OPCODE -> new Instruction(Opcode.NOP);
                case RET_OPCODE -> new Instruction(Opcode.RET);
                case LEAVE_OPCODE -> new Instruction(Opcode.LEAVE);
                case INT3_OPCODE -> new Instruction(Opcode.INT3);
                case CDQ_OPCODE -> new Instruction(Opcode.CDQ);
                case MOV_REG_MEM_OPCODE -> parse(b, p4.isPresent(), rexPrefix, Optional.empty(), Opcode.MOV);
                case MOV_MEM_REG_OPCODE -> parse(
                        b, !rexPrefix.isOperand64Bit(), rexPrefix, Optional.empty(), Opcode.MOV);
                case TEST_OPCODE -> parseSimple(b, rexPrefix, Opcode.TEST, false);
                case XOR_OPCODE -> parseSimple(b, rexPrefix, Opcode.XOR, false);
                case SUB_OPCODE -> parseSimple(b, rexPrefix, Opcode.SUB, false);
                case ADD_OPCODE -> parseSimple(b, rexPrefix, Opcode.ADD, false);
                case CMP_REG_REG_OPCODE -> parse(
                        b,
                        p4.isPresent(),
                        rexPrefix,
                        Optional.empty(),
                        Opcode.CMP); // parseSimple(b, rexPrefix, Opcode.CMP, false);
                case JMP_DISP32_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of32(b.read4LittleEndian()));
                case JMP_DISP8_OPCODE -> new Instruction(Opcode.JMP, RelativeOffset.of8(b.read1()));
                case JE_DISP8_OPCODE -> new Instruction(Opcode.JE, RelativeOffset.of8(b.read1()));
                case JLE_DISP8_OPCODE -> new Instruction(Opcode.JLE, RelativeOffset.of8(b.read1()));
                case CALL_OPCODE -> new Instruction(Opcode.CALL, RelativeOffset.of32(b.read4LittleEndian()));
                case MOV_IMM32_TO_EDI_OPCODE -> new Instruction(
                        Opcode.MOV, Register32.EDI, new Immediate(b.read4LittleEndian()));
                case MOV_IMM32_TO_ECX_OPCODE -> new Instruction(
                        Opcode.MOV, Register32.ECX, new Immediate(b.read4LittleEndian()));
                case AND_RAX_IMM32_OPCODE -> new Instruction(
                        Opcode.AND, Register64.RAX, new Immediate((long) b.read4LittleEndian()));
                case MOV_INDIRECT_IMM32_OPCODE -> parse(b, p4.isPresent(), rexPrefix, Optional.of(4), Opcode.MOV);

                    // PUSH
                case PUSH_EAX_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R8 : Register64.RAX);
                case PUSH_EBX_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R11 : Register64.RBX);
                case PUSH_ECX_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R9 : Register64.RCX);
                case PUSH_EDX_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R10 : Register64.RDX);
                case PUSH_ESP_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R12 : Register64.RSP);
                case PUSH_EBP_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R13 : Register64.RBP);
                case PUSH_ESI_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R14 : Register64.RSI);
                case PUSH_EDI_OPCODE -> new Instruction(
                        Opcode.PUSH, rexPrefix.extension() ? Register64.R15 : Register64.RDI);

                    // POP
                case POP_EAX_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R8 : Register64.RAX);
                case POP_EBX_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R11 : Register64.RBX);
                case POP_ECX_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R9 : Register64.RCX);
                case POP_EDX_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R10 : Register64.RDX);
                case POP_ESP_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R12 : Register64.RSP);
                case POP_EBP_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R13 : Register64.RBP);
                case POP_ESI_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R14 : Register64.RSI);
                case POP_EDI_OPCODE -> new Instruction(
                        Opcode.POP, rexPrefix.extension() ? Register64.R15 : Register64.RDI);

                case LEA_OPCODE -> // page 1191
                parseLEA(b, p4.isPresent(), rexPrefix);
                default -> throw new IllegalArgumentException(String.format("Unknown opcode %02x", opcodeFirstByte));
            };
        }
    }

    private Instruction parse(
            final ByteBuffer b,
            final boolean hasAddressSizeOverridePrefix,
            final RexPrefix rexPrefix,
            final Optional<Integer> immediateBytes,
            final Opcode opcode) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        return parse(b, hasAddressSizeOverridePrefix, rexPrefix, modrm, immediateBytes, opcode);
    }

    private Instruction parse(
            final ByteBuffer b,
            final boolean hasAddressSizeOverridePrefix,
            final RexPrefix rexPrefix,
            final ModRM modrm,
            final Optional<Integer> immediateBytes,
            final Opcode opcode) {
        final byte rm = modrm.rm();
        final Register operand1 =
                Register.fromCode(modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension());
        Operand operand2 = Register.fromCode(rm, !hasAddressSizeOverridePrefix, rexPrefix.ModRMRMExtension());

        // Table at page 530
        final byte mod = modrm.mod();
        if (mod < (byte) 0x00 || mod > (byte) 0x03) {
            throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
        }

        final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
        SIB sib;
        if (mod != (byte) 0x03 /* 11 */ && rm == (byte) 0x04 /* 100 */) {
            // SIB needed
            final byte _sib = b.read1();
            sib = new SIB(_sib);
            logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);

            final Register base =
                    Register.fromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension());
            final Register index =
                    Register.fromCode(sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension());
            if (index.toIntelSyntax().endsWith("sp")) { // an indirect operand of [xxx+rsp+...] is not allowed
                iob.reg2(base);
            } else {
                if (!(mod == (byte) 0x00 && base.toIntelSyntax().endsWith("bp"))) {
                    iob.reg1(base);
                }
                iob.reg2(index);
                iob.constant(1 << BitUtils.asInt(sib.scale()));
            }
        } else {
            // SIB not needed
            sib = null;
            if (mod == (byte) 0x00 && operand2.toIntelSyntax().endsWith("bp")) {
                iob.reg2(hasAddressSizeOverridePrefix ? Register32.EIP : Register64.RIP);
            } else {
                iob.reg2((Register) operand2);
            }
        }

        if ((mod == (byte) 0x00 && rm == (byte) 0x05)
                || (mod == (byte) 0x00 && sib != null && sib.base() == (byte) 0x05)
                || mod == (byte) 0x02) {
            final int disp32 = b.read4LittleEndian();
            iob.displacement(disp32);
            operand2 = iob.build();
        } else if (mod == (byte) 0x01) {
            final byte disp8 = b.read1();
            iob.displacement(disp8);
            operand2 = iob.build();
        }

        if (immediateBytes.isEmpty()) {
            return new Instruction(opcode, operand2, operand1);
        }

        return switch (immediateBytes.orElseThrow()) {
            case 1 -> new Instruction(opcode, operand2, new Immediate((long) b.read1()));
            case 4 -> new Instruction(opcode, operand2, new Immediate((long) b.read4LittleEndian()));
            default -> throw new IllegalArgumentException(
                    String.format("Invalid value for immediate bytes: %,d", immediateBytes.orElseThrow()));
        };
    }

    private boolean isExtendedOpcode(final byte opcode) {
        return opcode == (byte) 0x80 || opcode == (byte) 0x81 || opcode == (byte) 0x82 || opcode == (byte) 0x83;
    }

    // TODO: change name of method
    private Instruction parseSimple(
            final ByteBuffer b, final RexPrefix rexPrefix, final Opcode opcode, final boolean invertOperands) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final Register operand1 =
                Register.fromCode(modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension());
        final Register operand2 = Register.fromCode(modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.extension());

        if (invertOperands) {
            return new Instruction(opcode, operand1, operand2);
        }
        return new Instruction(opcode, operand2, operand1);
    }

    private Instruction parseLEA(
            final ByteBuffer b, final boolean hasAddressSizeOverridePrefix, final RexPrefix rexPrefix) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final byte rm = modrm.rm();
        final Register operand1 =
                Register.fromCode(modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension());
        final Register operand2 = Register.fromCode(rm, !hasAddressSizeOverridePrefix, rexPrefix.ModRMRMExtension());

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
                    Register.fromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension());
            final Register index =
                    Register.fromCode(sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension());
            if (index.toIntelSyntax().endsWith("sp")) { // an indirect operand of [xxx+rsp+...] is not allowed
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

        return new Instruction(Opcode.LEA, operand1, iob.build());
    }

    private Optional<Byte> readLegacyPrefixGroup1(final ByteBuffer b) {
        final byte prefix1 = b.read1();
        if (isLegacyPrefixGroup1(prefix1)) {
            logger.debug("Found group 1 legacy prefix 0x%02x", prefix1);
            return Optional.of(prefix1);
        } else {
            b.goBack(1);
            return Optional.empty();
        }
    }

    private boolean isLegacyPrefixGroup1(final byte prefix) {
        final byte LOCK_PREFIX = (byte) 0xf0;
        final byte REPNE_PREFIX = (byte) 0xf2; // REPNE / REPNZ
        final byte REP_PREFIX = (byte) 0xf3; // REP / REPE / REPZ
        return prefix == LOCK_PREFIX || prefix == REPNE_PREFIX || prefix == REP_PREFIX;
    }

    private Optional<Byte> readLegacyPrefixGroup2(final ByteBuffer b) {
        final byte prefix2 = b.read1();
        if (isLegacyPrefixGroup2(prefix2)) {
            logger.debug("Found group 2 legacy prefix 0x%02x", prefix2);
            return Optional.of(prefix2);
        } else {
            b.goBack(1);
            return Optional.empty();
        }
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

    private Optional<Byte> readLegacyPrefixGroup3(final ByteBuffer b) {
        final byte prefix3 = b.read1();
        if (isLegacyPrefixGroup3(prefix3)) {
            logger.debug("Found group 3 legacy prefix 0x%02x", prefix3);
            return Optional.of(prefix3);
        } else {
            b.goBack(1);
            return Optional.empty();
        }
    }

    private boolean isLegacyPrefixGroup3(final byte prefix) {
        final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;
        return prefix == OPERAND_SIZE_OVERRIDE_PREFIX;
    }

    private Optional<Byte> readLegacyPrefixGroup4(final ByteBuffer b) {
        final byte prefix4 = b.read1();
        if (isLegacyPrefixGroup4(prefix4)) {
            logger.debug("Found group 4 legacy prefix 0x%02x", prefix4);
            return Optional.of(prefix4);
        } else {
            b.goBack(1);
            return Optional.empty();
        }
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
