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
    private static final byte ADD_OPCODE = (byte) 0x01;
    private static final byte XOR_Ev_Gv_OPCODE = (byte) 0x31;
    private static final byte CMP_OPCODE = (byte) 0x81;
    private static final byte TEST_OPCODE = (byte) 0x85;
    private static final byte MOV_R2R_OPCODE = (byte) 0x89; // register to register
    private static final byte MOV_R2R_64_OPCODE = (byte) 0x8b; // register to register (64 bit)
    private static final byte LEA_OPCODE = (byte) 0x8d;
    private static final byte NOP_OPCODE = (byte) 0x90;
    private static final byte MOV_TO_EAX_OPCODE = (byte) 0xb8; // mov eax,0xXXXXXXXX
    private static final byte MOV_TO_ECX_OPCODE = (byte) 0xb9; // mov ecx,0xXXXXXXXX
    private static final byte MOV_TO_EDX_OPCODE = (byte) 0xba; // mov edx,0xXXXXXXXX
    private static final byte MOV_TO_EBX_OPCODE = (byte) 0xbb; // mov ebx,0xXXXXXXXX
    private static final byte MOV_TO_ESP_OPCODE = (byte) 0xbc; // mov esp,0xXXXXXXXX
    private static final byte MOV_TO_EBP_OPCODE = (byte) 0xbd; // mov ebp,0xXXXXXXXX
    private static final byte MOV_TO_ESI_OPCODE = (byte) 0xbe; // mov esi,0xXXXXXXXX
    private static final byte MOV_TO_EDI_OPCODE = (byte) 0xbf; // mov edi,0xXXXXXXXX
    private static final byte CALL_OPCODE = (byte) 0xe8;
    private static final byte JMP_nearf64_OPCODE = (byte) 0xe9;

    // multibyte opcodes
    private static final byte JE_OPCODE = (byte) 0x84;
    private static final byte CMOVE_OPCODE = (byte) 0x44;

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
                case LEA_OPCODE -> // page 1191
                parseLEA(b, p4.isPresent(), rexPrefix);
                default -> throw new IllegalArgumentException(String.format("Unknown opcode %02x", opcodeFirstByte));
            };
        }

        throw new IllegalArgumentException("Could not decode any instruction");
    }

    private Instruction parseLEA(
            final ByteBuffer b, final boolean hasAddressSizeOverridePrefix, final RexPrefix rexPrefix) {
        final byte _modrm = b.read1();
        final ModRM modrm = new ModRM(_modrm);
        logger.debug("Read ModR/M byte: 0x%02x -> %s", _modrm, modrm);
        final byte rm = modrm.rm();
        final Register operand1 =
                registerFromCode(modrm.reg(), rexPrefix.isOperand64Bit(), rexPrefix.ModRMRegExtension());
        final Register operand2 =
                registerFromCode(rm, !hasAddressSizeOverridePrefix || rexPrefix.isOperand64Bit(), rexPrefix.b());

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

                    iob.reg1(registerFromCode(sib.base(), rexPrefix.isOperand64Bit(), rexPrefix.extension()))
                            .reg2(
                                    (sib.index() == (byte) 0x04 /* 100 */
                                            ? null
                                            : registerFromCode(
                                                    sib.index(),
                                                    rexPrefix.isOperand64Bit(),
                                                    rexPrefix.SIBIndexExtension())))
                            .constant(1 << BitUtils.asInt(sib.scale()));
                } else if (rm == (byte) 0x05 /* 101 */) {
                    // just a 32-bit displacement (not sign extended) added to the index
                    final int disp32 = b.read4LittleEndian();
                    iob.reg1(Register64.RIP).displacement(disp32);
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

                    iob.reg1(registerFromCode(sib.base(), rexPrefix.isOperand64Bit(), rexPrefix.extension()))
                            .reg2(
                                    (sib.index() == (byte) 0x04 /* 100 */
                                            ? null
                                            : registerFromCode(
                                                    sib.index(),
                                                    rexPrefix.isOperand64Bit(),
                                                    rexPrefix.SIBIndexExtension())))
                            .constant(1 << BitUtils.asInt(sib.scale()));
                } else {
                    iob.reg1(operand2);
                }
                final byte disp8 = b.read1();
                yield new Instruction(
                        Opcode.LEA, operand1, iob.displacement(disp8).build());
            }
            case (byte) 0x02 -> { // 10
                final IndirectOperand.IndirectOperandBuilder iob = IndirectOperand.builder();
                if (rm == (byte) 0x04 /* 100 */) {
                    // SIB byte
                    final byte _sib = b.read1();
                    final SIB sib = new SIB(_sib);
                    logger.debug("Read SIB byte: 0x%02x -> %s", _sib, sib);
                    iob.reg1(
                                    (sib.index() == (byte) 0x04 /* 100 */
                                            ? null
                                            : registerFromCode(
                                                    sib.index(),
                                                    rexPrefix.isOperand64Bit(),
                                                    rexPrefix.SIBIndexExtension())))
                            .constant(1 << BitUtils.asInt(sib.scale()))
                            .reg2(registerFromCode(sib.base(), rexPrefix.isOperand64Bit(), rexPrefix.extension()));
                } else {
                    iob.reg1(operand2);
                }
                final int disp32 = b.read4LittleEndian();
                yield new Instruction(
                        Opcode.LEA, operand1, iob.displacement(disp32).build());
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
