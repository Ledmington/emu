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

        boolean isModRMRequired = false;
        boolean isSIBRequired = false;
        boolean isDisplacementRequired = false;
        int displacementBytes = 0;
        Optional<Long> displacement = Optional.empty();
        boolean operand1RequiresDisplacement = false;
        boolean operand2RequiresDisplacement = false;
        boolean isImmediateRequired = false;

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

        Opcode opcode;
        final byte opcodeFirstByte = isREX ? b.read1() : rex;
        Operand operand1 = null;
        Operand operand2 = null;

        if (isMultibyteOpcode(opcodeFirstByte)) {
            // more than 1 bytes opcode
            final byte opcodeSecondByte = b.read1();
            switch (opcodeSecondByte) {
                case JE_OPCODE:
                    // page 1145
                    opcode = Opcode.JE;
                    operand1 = RelativeOffset.of32(b.read4());
                    break;
                case CMOVE_OPCODE:
                    // page 771
                    opcode = Opcode.CMOVE;
                    isModRMRequired = true;
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown multibyte opcode 0x%02x%02x", opcodeFirstByte, opcodeSecondByte));
            }
        } else {
            // 1 byte opcode
            switch (opcodeFirstByte) {
                case ADD_OPCODE:
                    // page 628
                    opcode = Opcode.ADD;
                    isModRMRequired = true;
                    break;
                case CMP_OPCODE:
                    // page 775
                    opcode = Opcode.CMP;
                    isModRMRequired = true;
                    isImmediateRequired = true;
                    break;
                case TEST_OPCODE:
                    {
                        // page 1923
                        opcode = Opcode.TEST;
                        final byte modrm = b.read1();
                        operand1 = registerFromCode(combine(ModRMRegExtension, ModRMExtractReg(modrm)), operand64Bit);
                        operand2 = registerFromCode(combine(extension, ModRMExtractRM(modrm)), operand64Bit);
                    }
                    break;
                case MOV_R2R_OPCODE:
                case MOV_R2R_64_OPCODE:
                    {
                        // page 1255
                        opcode = Opcode.MOV;
                        final byte modrm = b.read1();
                        operand1 = registerFromCode(combine(ModRMRegExtension, ModRMExtractReg(modrm)), operand64Bit);
                        operand2 = registerFromCode(combine(extension, ModRMExtractRM(modrm)), operand64Bit);
                    }
                    break;
                case LEA_OPCODE:
                    {
                        // page 1191
                        opcode = Opcode.LEA;
                        final byte modrm = b.read1();
                        final byte sib = b.read1();
                        // final byte disp = b.read1();
                        operand1 = IndirectOperand.builder()
                                .reg1(registerFromCode(SIBExtractBase(sib), operand64Bit))
                                .constant(2 * SIBExtractSS(sib))
                                .reg2(registerFromCode(SIBExtractIndex(sib), operand64Bit))
                                // .displacement(disp)
                                .build();
                        operand2 = registerFromCode(ModRMExtractReg(modrm), operand64Bit);
                    }
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
                    operand1 = MemoryLocation.of32(b.read4());
                    operand2 = Register32.fromByte(BitUtils.asByte(opcodeFirstByte & 0x07));
                    break;
                case JMP_nearf64_OPCODE:
                    // page 735
                    opcode = Opcode.JMP;
                    operand1 = RelativeOffset.of32(b.read4());
                    break;
                case CALL_OPCODE:
                    opcode = Opcode.CALL;
                    // page 598, section 3.1.1.1
                    operand1 = RelativeOffset.of32(b.read4());
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

        // ModR/M byte (1 byte, if required)
        if (isModRMRequired) {
            final byte m = b.read1();
            logger.debug("Read ModRM byte 0b%s", BitUtils.toBinaryString(m));
            final byte mod = ModRMExtractMod(m);
            final byte rm = ModRMExtractRM(m);
            final byte reg = ModRMExtractReg(m);
            byte op1;
            byte op2;
            switch (mod) {
                    // pages 529-530
                case (byte) 0x00: // 00
                    throw new IllegalArgumentException("Don't know what to do when first 2 bits of ModRM are 00");
                case (byte) 0x80: // 10
                    // register + displacement 32
                    op1 = combine(extension, rm);
                    if (op1 == (byte) 0x04 /* 100 */) {
                        operand1 = null;
                    } else {
                        operand1 = registerFromCode(op1, operand64Bit);
                    }
                    op2 = combine(ModRMRegExtension, reg);
                    operand2 = registerFromCode(op2, operand64Bit);
                    isSIBRequired = true;
                    isDisplacementRequired = true;
                    operand1RequiresDisplacement = true;
                    displacementBytes = 4;
                    break;
                case (byte) 0x40: // 01
                    // register + displacement 8
                    op1 = combine(extension, rm);
                    if (op1 == (byte) 0x04 /* 100 */) {
                        operand1 = null;
                    } else {
                        operand1 = registerFromCode(op1, operand64Bit);
                    }
                    op2 = combine(ModRMRegExtension, reg);
                    operand2 = registerFromCode(op2, operand64Bit);
                    isSIBRequired = true;
                    isDisplacementRequired = true;
                    operand1RequiresDisplacement = true;
                    displacementBytes = 1;
                    break;
                case (byte) 0xc0: // 11
                    op1 = combine(extension, rm);
                    operand1 = registerFromCode(op1, operand64Bit);
                    op2 = combine(ModRMRegExtension, reg);
                    operand2 = registerFromCode(op2, operand64Bit);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Invalid bit pattern in ModRM byte: it was 0x%02x", m));
            }
        }

        // SIB byte (1 byte, if required)
        Register baseRegister = null;
        Register indexRegister = null;
        if (isSIBRequired) {
            final byte s = b.read1();
            logger.debug("Read SIB byte 0b%s", BitUtils.toBinaryString(s));
            final byte ss = SIBExtractSS(s);
            final byte index = combine(SIBIndexExtension, SIBExtractIndex(s));
            final byte base = combine(extension, SIBExtractBase(s));

            baseRegister = (base == (byte) 0x05 /* 101 */) ? null : registerFromCode(base, operand64Bit);
            indexRegister = (index == (byte) 0x04 /* 100 */) ? null : registerFromCode(index, operand64Bit);
            operand2 = (indexRegister == null)
                    ? IndirectOperand.builder()
                            .reg1(baseRegister)
                            .constant(BitUtils.asInt(ss * 2))
                            .build()
                    : IndirectOperand.builder()
                            .reg1(baseRegister)
                            .constant(BitUtils.asInt(ss * 2))
                            .reg2(indexRegister)
                            .build();
        }

        // Displacement (1, 2, 4 or 8 bytes, if required)
        if (isDisplacementRequired) {
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
        }

        if (displacement.isPresent()) {
            logger.debug("displacement: 0x%016x", displacement.orElseThrow());
            // if (operand1RequiresDisplacement) {
            // operand1 = (operand1 != null)
            // ? IndirectOperand.of((Register) operand1, displacement.orElseThrow())
            // : IndirectOperand.of(displacement.orElseThrow());
            // }
            if (operand2RequiresDisplacement) {
                operand2 = (operand2 != null)
                        ? IndirectOperand.builder()
                                .reg1((Register) operand2)
                                .displacement(displacement.orElseThrow())
                                .build()
                        : IndirectOperand.builder()
                                .displacement(displacement.orElseThrow())
                                .build();
            }
        }

        // Immediate (1, 2, 4 or 8 bytes, if required)
        if (isImmediateRequired) {
            // TODO
        }

        return new Instruction(
                opcode,
                (operand1 == null) ? Optional.empty() : Optional.of(operand1),
                (operand2 == null) ? Optional.empty() : Optional.of(operand2));
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
