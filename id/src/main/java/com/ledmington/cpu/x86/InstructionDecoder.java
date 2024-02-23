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

    private static final byte OPCODE_REG_MASK = (byte) 0x07;

    // single byte opcodes
    private static final byte XOR_OPCODE = (byte) 0x31;
    private static final byte TEST_OPCODE = (byte) 0x85;
    private static final byte MOV_R2R_OPCODE = (byte) 0x89; // register to register
    private static final byte LEA_OPCODE = (byte) 0x8d;
    private static final byte NOP_OPCODE = (byte) 0x90;
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
    private static final byte RET_OPCODE = (byte) 0xc3;
    private static final byte LEAVE_OPCODE = (byte) 0xc9;
    private static final byte INT3_OPCODE = (byte) 0xcc;
    private static final byte CDQ_OPCODE = (byte) 0x99;

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
            logger.info("%08x: %s", pos, inst.toString());
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
            /*
             * final byte opcodeSecondByte = b.read1();
             * switch (opcodeSecondByte) {
             * case JE_OPCODE:
             * // page 1145
             * opcode = Opcode.JE;
             * operand1 = RelativeOffset.of32(b.read4());
             * break;
             * case CMOVE_OPCODE:
             * // page 771
             * opcode = Opcode.CMOVE;
             * break;
             * default:
             * throw new IllegalArgumentException(
             * String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte,
             * opcodeSecondByte));
             * }
             */
        } else {
            // 1 byte opcode
            return switch (opcodeFirstByte) {
                case NOP_OPCODE -> new Instruction(Opcode.NOP);
                case RET_OPCODE -> new Instruction(Opcode.RET);
                case LEAVE_OPCODE -> new Instruction(Opcode.LEAVE);
                case INT3_OPCODE -> new Instruction(Opcode.INT3);
                case CDQ_OPCODE -> new Instruction(Opcode.CDQ);
                case MOV_R2R_OPCODE -> parseSimple(b, rexPrefix, Opcode.MOV);
                case TEST_OPCODE -> parseSimple(b, rexPrefix, Opcode.TEST);
                case XOR_OPCODE -> parseSimple(b, rexPrefix, Opcode.XOR);
                case PUSH_EAX_OPCODE,
                        PUSH_EBX_OPCODE,
                        PUSH_ECX_OPCODE,
                        PUSH_EDX_OPCODE,
                        PUSH_ESP_OPCODE,
                        PUSH_EBP_OPCODE,
                        PUSH_ESI_OPCODE,
                        PUSH_EDI_OPCODE -> parseOpcode(rexPrefix, opcodeFirstByte, Opcode.PUSH);
                case POP_EAX_OPCODE,
                        POP_EBX_OPCODE,
                        POP_ECX_OPCODE,
                        POP_EDX_OPCODE,
                        POP_ESP_OPCODE,
                        POP_EBP_OPCODE,
                        POP_ESI_OPCODE,
                        POP_EDI_OPCODE -> parseOpcode(rexPrefix, opcodeFirstByte, Opcode.POP);
                case LEA_OPCODE -> // page 1191
                parseLEA(b, p4.isPresent(), rexPrefix);
                default -> throw new IllegalArgumentException(String.format("Unknown opcode %02x", opcodeFirstByte));
            };
        }

        throw new IllegalArgumentException("Could not decode any instruction");
    }

    private Instruction parseOpcode(final RexPrefix rexPrefix, final byte opcodeByte, final Opcode opcode) {
        final Register operand =
                Register64.fromByte(combine(rexPrefix.extension(), BitUtils.and(opcodeByte, OPCODE_REG_MASK)));
        return new Instruction(opcode, operand);
    }

    private Instruction parseSimple(final ByteBuffer b, final RexPrefix rexPrefix, final Opcode opcode) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final Register operand1 =
                registerFromCode(modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension());
        final Register operand2 = registerFromCode(modrm.rm(), rexPrefix.isOperand64Bit(), rexPrefix.b());
        return new Instruction(opcode, operand2, operand1);
    }

    private Instruction parseLEA(
            final ByteBuffer b, final boolean hasAddressSizeOverridePrefix, final RexPrefix rexPrefix) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final byte rm = modrm.rm();
        final Register operand1 =
                registerFromCode(modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension());
        final Register operand2 = registerFromCode(rm, !hasAddressSizeOverridePrefix, rexPrefix.ModRMRMExtension());

        // Table at page 530
        final byte mod = modrm.mod();
        return switch (mod) {
            case (byte) 0x00 -> { // 00
                final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
                if (rm == (byte) 0x04 /* 100 */) {
                    // just SIB byte
                    final byte _sib = b.read1();
                    final SIB sib = new SIB(_sib);
                    logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);

                    if (sib.index() != (byte) 0x04 /* 100 */) {
                        iob.reg2(registerFromCode(
                                        sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension()))
                                .constant(1 << BitUtils.asInt(sib.scale()));
                    } else {
                        iob.reg2(registerFromCode(rm, !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension()))
                                .constant(1);
                    }
                    if (sib.base() != (byte) 0x05 /* 101 */) {
                        iob.reg1(registerFromCode(
                                sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension()));
                    } else {
                        final int disp32 = b.read4LittleEndian();
                        iob.displacement(disp32);
                    }
                } else if (rm == (byte) 0x05 /* 101 */) {
                    // just a 32-bit displacement (not sign extended) added to the index
                    final int disp32 = b.read4LittleEndian();
                    iob.reg2(Register64.RIP).displacement((long) disp32);
                } else {
                    iob.reg1(operand2);
                }
                yield new Instruction(Opcode.LEA, operand1, iob.build());
            }
            case (byte) 0x01 -> { // 01
                final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
                if (rm == (byte) 0x04 /* 100 */) {
                    // SIB byte
                    final byte _sib = b.read1();
                    final SIB sib = new SIB(_sib);
                    logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);

                    iob.constant(1 << BitUtils.asInt(sib.scale()));
                    final Register _base =
                            registerFromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension());

                    if (sib.index() != (byte) 0x04 /* 100 */) {
                        iob.reg1(_base)
                                .reg2(registerFromCode(
                                        sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension()));
                    } else if (!operand2.toIntelSyntax().endsWith("sp")) { // cannot have [xxx+rsp+...]
                        iob.reg1(_base).reg2(operand2);
                    } else {
                        iob.reg2(_base);
                    }
                } else {
                    iob.reg2(operand2);
                }
                final int disp8 = b.read1();
                iob.displacement(disp8);
                yield new Instruction(Opcode.LEA, operand1, iob.build());
            }
            case (byte) 0x02 -> { // 10
                final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
                if (rm == (byte) 0x04 /* 100 */) {
                    // SIB byte
                    final byte _sib = b.read1();
                    final SIB sib = new SIB(_sib);
                    logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);

                    iob.constant(1 << BitUtils.asInt(sib.scale()));
                    final Register _base =
                            registerFromCode(sib.base(), !hasAddressSizeOverridePrefix, rexPrefix.SIBBaseExtension());

                    if (sib.index() != (byte) 0x04 /* 100 */) {
                        iob.reg1(_base)
                                .reg2(registerFromCode(
                                        sib.index(), !hasAddressSizeOverridePrefix, rexPrefix.SIBIndexExtension()));
                    } else if (!operand2.toIntelSyntax().endsWith("sp")) { // cannot have [xxx+rsp+...]
                        iob.reg1(_base).reg2(operand2);
                    } else {
                        iob.reg2(_base);
                    }
                } else {
                    iob.reg2(operand2);
                }
                final int disp32 = b.read4LittleEndian();
                iob.displacement(disp32);
                yield new Instruction(Opcode.LEA, operand1, iob.build());
            }
            case (byte) 0x03 -> // 11
            throw new Error("Not implemented");
            default -> throw new IllegalArgumentException(String.format("Unknown mod value: %d (0x%02x)", mod, mod));
        };
    }

    /**
     * Performs a bitwise OR with the given byte and a byte
     * with the given value in the third bit.
     * xxxxxxxx OR
     * 0000b000
     */
    private byte combine(final boolean b, final byte x) {
        return BitUtils.asByte(x | (b ? ((byte) 0x08) : 0));
    }

    private Register registerFromCode(final byte operandCode, final boolean isOperand64Bit, final boolean extension) {
        return isOperand64Bit
                ? Register64.fromByte(combine(extension, operandCode))
                : Register32.fromByte(combine(extension, operandCode));
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
