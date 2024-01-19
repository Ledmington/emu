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

    private static final byte REX_W_MASK = (byte) 0x08;
    private static final byte REX_R_MASK = (byte) 0x04;
    private static final byte REX_X_MASK = (byte) 0x02;
    private static final byte REX_B_MASK = (byte) 0x01;

    private static final byte XOR_Ev_Gv_OPCODE = (byte) 0x31;
    private static final byte JE_OPCODE = (byte) 0x84;
    private static final byte TEST_OPCODE = (byte) 0x85;
    private static final byte MOV_R2R_OPCODE = (byte) 0x89; // register to register
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

    private static final byte MODRM_MOD_MASK = (byte) 0xc0; // 11000000
    private static final byte MODRM_REG_MASK = (byte) 0x38; // 00111000
    private static final byte MODRM_RM_MASK = (byte) 0x07; // 00000111

    private static final byte SIB_SCALE_MASK = (byte) 0xc0; // 11000000
    private static final byte SIB_INDEX_MASK = (byte) 0x38; // 00111000
    private static final byte SIB_BASE_MASK = (byte) 0x07; // 00000111

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
            final Instruction inst = decodeInstruction(b);
            logger.info("%08x: %s", b.position(), inst.toString());
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

        boolean isModRMRequired = false;
        boolean isSIBRequired = false;
        boolean isDisplacementRequired = false;
        int displacementBytes = 0;
        Optional<Long> displacement;
        boolean isImmediateRequired = false;

        final byte rex = b.read1();
        boolean operand64Bit = false;
        boolean ModRMRegExtension = false;
        boolean SIBIndexExtension = false;
        boolean extension = false;
        final boolean isREX = isREXPrefix(rex);
        Optional<Byte> rexPrefix;
        if (isREX) {
            operand64Bit = (rex & REX_W_MASK) != 0;
            ModRMRegExtension = (rex & REX_R_MASK) != 0;
            SIBIndexExtension = (rex & REX_X_MASK) != 0;
            extension = (rex & REX_B_MASK) != 0;
            logger.debug(
                    "Found REX prefix: 0x%02x -> %s",
                    rex,
                    (operand64Bit ? ".W" : "")
                            + (ModRMRegExtension ? ".R" : "")
                            + (SIBIndexExtension ? ".X" : "")
                            + (extension ? ".B" : ""));
            rexPrefix = Optional.of(rex);
        } else {
            rexPrefix = Optional.empty();
        }

        Opcode opcode;
        final byte opcodeFirstByte = isREX ? b.read1() : rex;
        Optional<Operand> operand1 = null;
        Optional<Operand> operand2 = null;

        if (isMultibyteOpcode(opcodeFirstByte)) {
            // more than 1 bytes opcode
            final byte opcode2 = b.read1();
            switch (opcode2) {
                case JE_OPCODE:
                    // page 1145
                    opcode = Opcode.JE;
                    operand1 = Optional.of(RelativeOffset.of32(b.read4()));
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte, opcode2));
            }
        } else {
            // 1 byte opcode
            switch (opcodeFirstByte) {
                case TEST_OPCODE:
                    // page 1923
                    opcode = Opcode.TEST;
                    isModRMRequired = true;
                    break;
                case MOV_R2R_OPCODE:
                    // page 1255
                    opcode = Opcode.MOV;
                    isModRMRequired = true;
                    break;
                case NOP_OPCODE:
                    opcode = Opcode.NOP;
                    break;
                case MOV_TO_EAX_OPCODE:
                case MOV_TO_EBX_OPCODE:
                case MOV_TO_ECX_OPCODE:
                case MOV_TO_EDX_OPCODE:
                case MOV_TO_ESI_OPCODE:
                case MOV_TO_EDI_OPCODE:
                case MOV_TO_ESP_OPCODE:
                case MOV_TO_EBP_OPCODE:
                    opcode = Opcode.MOV;
                    operand1 = Optional.of(Register32.fromByte(BitUtils.asByte(opcodeFirstByte & 0x07)));
                    operand2 = Optional.of(MemoryLocation.of32(b.read4()));
                    break;
                case JMP_nearf64_OPCODE:
                    // page 735
                    opcode = Opcode.JMP;
                    operand1 = Optional.of(RelativeOffset.of32(b.read4()));
                    break;
                case CALL_OPCODE:
                    opcode = Opcode.CALL;
                    // page 598, section 3.1.1.1
                    operand1 = Optional.of(RelativeOffset.of32(b.read4()));
                    break;
                case XOR_Ev_Gv_OPCODE:
                    // page 2722
                    opcode = Opcode.XOR;
                    isModRMRequired = true;
                    break;
                default:
                    throw new IllegalArgumentException(String.format(
                            "Unknown opcode %02x %02x %02x %02x %02x %02x %02x",
                            opcodeFirstByte, b.read1(), b.read1(), b.read1(), b.read1(), b.read1(), b.read1()));
            }
        }

        Optional<Byte> modrm;

        // ModR/M byte (1 byte, if required)
        if (isModRMRequired) {
            final byte m = b.read1();
            logger.debug("Read ModRM byte 0b%s", BitUtils.toBinaryString(m));
            modrm = Optional.of(m);
            final byte tmp = BitUtils.asByte(m & MODRM_MOD_MASK);
            byte op1;
            switch (tmp) {
                case (byte) 0x00: // 00
                    throw new IllegalArgumentException("Don't know what to do when first 2 bits of ModRM are 00");
                case (byte) 0x80: // 10
                    // register + displacement 32
                    op1 = BitUtils.asByte((m & MODRM_RM_MASK) | (extension ? ((byte) 0x08) : 0));
                    if (op1 == (byte) 0x04 /* 100 */) {
                        operand1 = Optional.empty();
                    } else {
                        operand1 = registerFromCode(op1, operand64Bit);
                    }
                    isSIBRequired = true;
                    isDisplacementRequired = true;
                    displacementBytes = 4;
                    break;
                case (byte) 0x40: // 01
                    throw new IllegalArgumentException("Don't know what to do when first 2 bits of ModRM are 01");
                case MODRM_MOD_MASK: // 11
                    final byte op2 =
                            BitUtils.asByte(((m & MODRM_REG_MASK) >>> 3) | (ModRMRegExtension ? ((byte) 0x08) : 0));
                    operand2 = registerFromCode(op2, operand64Bit);
                    op1 = BitUtils.asByte((m & MODRM_RM_MASK) | (extension ? ((byte) 0x08) : 0));
                    operand1 = registerFromCode(op1, operand64Bit);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Invalid bit pattern in ModRM byte: it was 0x%02x", m));
            }
        } else {
            modrm = Optional.empty();
            if (operand1 == null) {
                operand1 = Optional.empty();
            }
            if (operand2 == null) {
                operand2 = Optional.empty();
            }
        }

        Optional<Byte> sib;

        // SIB byte (1 byte, if required)
        if (isSIBRequired) {
            final byte s = b.read1();
            logger.debug("Read SIB byte 0b%s", BitUtils.toBinaryString(s));
            sib = Optional.of(s);
            final byte ss = BitUtils.asByte((s & SIB_SCALE_MASK) >>> 6);
            final byte index = BitUtils.asByte(((s & SIB_INDEX_MASK) >>> 3) | (SIBIndexExtension ? ((byte) 0x08) : 0));
            final byte base = BitUtils.asByte((s & SIB_BASE_MASK) | (extension ? ((byte) 0x08) : 0));
        } else {
            sib = Optional.empty();
        }

        // Displacement (1, 2, 4 or 8 bytes, if required)
        if (isDisplacementRequired) {
            logger.debug("Reading %d bytes of displacement", displacementBytes);
            displacement = Optional.of(
                    switch (displacementBytes) {
                        case 0 -> 0L;
                        case 1 -> BitUtils.asLong(b.read1());
                        case 2 -> BitUtils.asLong(b.read2());
                        case 4 -> BitUtils.asLong(b.read4());
                        case 8 -> b.read8();
                        default -> throw new IllegalArgumentException(String.format(
                                "Invalid displacement bytes value: expected 1, 2, 4 or 8 but was %,d (0x%08x)",
                                displacementBytes, displacementBytes));
                    });
        } else {
            displacement = Optional.empty();
        }

        if (displacement.isPresent()) {}

        // Immediate (1, 2, 4 or 8 bytes, if required)
        if (isImmediateRequired) {}

        return new Instruction(opcode, operand1, operand2);
    }

    private Optional<Operand> registerFromCode(final byte operandCode, final boolean isOperand64Bit) {
        return Optional.of(isOperand64Bit ? Register64.fromByte(operandCode) : Register32.fromByte(operandCode));
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

    private boolean isREXPrefix(final byte opcode) {
        final byte REX_PREFIX_MASK = (byte) 0xf0;
        final byte REX_PREFIX = (byte) 0x40;
        return (opcode & REX_PREFIX_MASK) == REX_PREFIX;
    }

    private boolean isMultibyteOpcode(final byte opcode) {
        final byte MULTIBYTE_OPCODE_PREFIX = (byte) 0x0f;
        return opcode == MULTIBYTE_OPCODE_PREFIX;
    }
}
