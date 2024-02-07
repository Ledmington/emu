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

        final byte rex = b.read1();
        boolean operand64Bit = false;
        boolean ModRMRegExtension = false;
        boolean SIBIndexExtension = false;
        boolean extension = false;
        final boolean isREX = isREXPrefix(rex);
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
        }

        final byte opcodeFirstByte = isREX ? b.read1() : rex;

        if (isMultibyteOpcode(opcodeFirstByte)) {
            // more than 1 bytes opcode
            /* final byte opcodeSecondByte = b.read1();
            switch (opcodeSecondByte) {
                case JE_OPCODE:
                    // page 1145
                    opcode = Opcode.JE;
                    operand1 = RelativeOffset.of32(b.read4());
                    break;
                case CMOVE_OPCODE:
                    // page 771
                    opcode = Opcode.CMOVE;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            }*/
        } else {
            // 1 byte opcode
            return switch (opcodeFirstByte) {
                case NOP_OPCODE -> new Instruction(Opcode.NOP);
                case LEA_OPCODE -> // page 1191
                parseLEA(b, ModRMRegExtension, extension);
                default -> throw new IllegalArgumentException(String.format("Unknown opcode %02x", opcodeFirstByte));
            };
        }

        throw new IllegalArgumentException("Could not decode any instruction");
    }

    private Instruction parseLEA(final ByteBuffer b, final boolean ModRMRegExtension, final boolean extension) {
        final byte modrm = b.read1();
        final byte rm = ModRMExtractRM(modrm);
        final Register operand1 = registerFromCode(ModRMExtractReg(modrm), ModRMRegExtension);
        final Register operand2 = registerFromCode(rm, extension);

        final byte mod = ModRMExtractMod(modrm);
        if (mod == (byte) 0x00 && rm == (byte) 0x04) {
            // SIB byte needed
            final byte sib = b.read1();
            // TODO
        } else if ((mod == (byte) 0x00 && rm == (byte) 0x05) || mod == (byte) 0x02) {
            // 32-bit displacement
            final int disp = b.read4();
            return new Instruction(
                    Opcode.LEA,
                    operand1,
                    IndirectOperand.builder()
                            .reg1(operand2)
                            .displacement(BitUtils.asLong(disp))
                            .build());
        } else if (mod == (byte) 0x01) {
            // 8-bit displacement
            final byte disp = b.read1();
            return new Instruction(
                    Opcode.LEA,
                    operand1,
                    IndirectOperand.builder()
                            .reg1(operand2)
                            .displacement(BitUtils.asLong(disp))
                            .build());
        }

        return new Instruction(Opcode.LEA, operand1, operand2);
    }

    /**
     * Returns the number of operands required by the given opcode.
     * <p>
     * As stated in section 2.2.1.1 of Intel's Software Developer Manual,
     * any x86 instruction can be of three formats:
     * - 1 operand: the reg field of the opcode is used
     * - 2 operands: the reg and r/m fields of the ModRM byte are used
     * - 3 operands: the reg field of the ModRM byte, the base and the index fields of the SIB byte
     * Zero-operand instructions like NOP are not mentioned since they are pretty rare.
     */
    private int operandsRequired(final byte opcode) {
        return switch (opcode) {
            case NOP_OPCODE -> 0;
            case MOV_TO_EAX_OPCODE,
                    MOV_TO_EBX_OPCODE,
                    MOV_TO_ECX_OPCODE,
                    MOV_TO_EDX_OPCODE,
                    MOV_TO_ESI_OPCODE,
                    MOV_TO_EDI_OPCODE,
                    MOV_TO_ESP_OPCODE,
                    MOV_TO_EBP_OPCODE -> 1;
            case MOV_R2R_OPCODE, MOV_R2R_64_OPCODE, LEA_OPCODE, XOR_Ev_Gv_OPCODE -> 2;
            default -> throw new IllegalArgumentException(String.format("Unknown opcode 0x%02x (%d)", opcode, opcode));
        };
    }

    private Opcode opcodeFromByte(final byte opcode) {
        return switch (opcode) {
            case NOP_OPCODE -> Opcode.NOP;
            case MOV_R2R_OPCODE,
                    MOV_R2R_64_OPCODE,
                    MOV_TO_EAX_OPCODE,
                    MOV_TO_EBX_OPCODE,
                    MOV_TO_ECX_OPCODE,
                    MOV_TO_EDX_OPCODE,
                    MOV_TO_ESI_OPCODE,
                    MOV_TO_EDI_OPCODE,
                    MOV_TO_ESP_OPCODE,
                    MOV_TO_EBP_OPCODE -> Opcode.MOV;
            case LEA_OPCODE -> Opcode.LEA;
            default -> throw new IllegalArgumentException(String.format("Unknown opcode 0x%02x (%d)", opcode, opcode));
        };
    }

    private byte SIBExtractSS(final byte sib) {
        final byte SIB_SCALE_MASK = (byte) 0xc0; // 11000000
        return BitUtils.shr(BitUtils.and(sib, SIB_SCALE_MASK), 6);
    }

    private byte SIBExtractIndex(final byte sib) {
        final byte SIB_INDEX_MASK = (byte) 0x38; // 00111000
        return BitUtils.shr(BitUtils.and(sib, SIB_INDEX_MASK), 3);
    }

    private byte SIBExtractBase(final byte sib) {
        final byte SIB_BASE_MASK = (byte) 0x07; // 00000111
        return BitUtils.and(sib, SIB_BASE_MASK);
    }

    private byte ModRMExtractMod(final byte m) {
        final byte MODRM_MOD_MASK = (byte) 0xc0; // 11000000
        return BitUtils.and(m, MODRM_MOD_MASK);
    }

    private byte ModRMExtractReg(final byte m) {
        final byte MODRM_REG_MASK = (byte) 0x38; // 00111000
        return BitUtils.shr(BitUtils.and(m, MODRM_REG_MASK), 3);
    }

    private byte ModRMExtractRM(final byte m) {
        final byte MODRM_RM_MASK = (byte) 0x07; // 00000111
        return BitUtils.and(m, MODRM_RM_MASK);
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

    private Register registerFromCode(final byte operandCode, final boolean isOperand64Bit) {
        return isOperand64Bit ? Register64.fromByte(operandCode) : Register32.fromByte(operandCode);
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
