package com.ledmington.cpu.x86;

import java.util.Objects;
import java.util.Optional;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ByteBuffer;
import com.ledmington.utils.MiniLogger;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    private static final MiniLogger logger = MiniLogger.getLogger("inst");

    private static final byte REX_W_MASK = (byte) 0x08;
    private static final byte REX_R_MASK = (byte) 0x04;
    private static final byte REX_X_MASK = (byte) 0x02;
    private static final byte REX_B_MASK = (byte) 0x01;

    private static final byte XOR_Ev_Gv_OPCODE = (byte) 0x31;
    private static final byte JMP_nearf64_OPCODE = (byte) 0xe9;

    private static final byte MODRM_MOD_MASK = (byte) 0xc0; // 11000000
    private static final byte MODRM_REG_MASK = (byte) 0x38; // 00111000
    private static final byte MODRM_RM_MASK = (byte) 0x07; // 00000111

    private static final byte SIB_SCALE_MASK = (byte) 0xc0; // 11000000
    private static final byte SIB_INDEX_MASK = (byte) 0x38; // 00111000
    private static final byte SIB_BASE_MASK = (byte) 0x07; // 00000111

    // Legacy prefixes
    private final Optional<Byte> p1;
    private final Optional<Byte> p2;
    private final Optional<Byte> p3;
    private final Optional<Byte> p4;

    // REX prefix
    private final Optional<Byte> rexPrefix;

    private final Opcode opcode;
    private final Optional<Register> operand1;
    private final Optional<Register> operand2;

    // ModR/M
    private final Optional<Byte> modrm;

    // SIB
    private final Optional<Byte> sib;

    public Instruction(final ByteBuffer b) {
        Objects.requireNonNull(b);

        p1 = readLegacyPrefixGroup1(b);
        p2 = readLegacyPrefixGroup2(b);
        p3 = readLegacyPrefixGroup3(b);
        p4 = readLegacyPrefixGroup4(b);

        boolean isModRMRequired = false;
        boolean isSIBRequired = false;
        boolean isDisplacementRequired = false;
        boolean isImmediateRequired = false;

        byte opcode1 = b.read1();
        boolean operand64Bit = false;
        boolean ModRMRegExtension = false;
        boolean SIBIndexExtension = false;
        boolean extension = false;
        if (isREXPrefix(opcode1)) {
            operand64Bit = (opcode1 & REX_W_MASK) != 0;
            ModRMRegExtension = (opcode1 & REX_R_MASK) != 0;
            SIBIndexExtension = (opcode1 & REX_X_MASK) != 0;
            extension = (opcode1 & REX_B_MASK) != 0;
            logger.debug(
                    "Found REX prefix: 0x%02x -> %s",
                    opcode1,
                    (operand64Bit ? ".W" : "")
                            + (ModRMRegExtension ? ".R" : "")
                            + (SIBIndexExtension ? ".X" : "")
                            + (extension ? ".B" : ""));
            rexPrefix = Optional.of(opcode1);
            opcode1 = b.read1();
        } else {
            logger.debug("No REX prefix found");
            rexPrefix = Optional.empty();
        }

        if (isMultibyteOpcode(opcode1)) {
            // more than 1 bytes opcode
            final byte opcode2 = b.read1();
            throw new IllegalArgumentException(String.format("Unknown multibyte opcode 0x%02x%02x", opcode1, opcode2));
        } else {
            // 1 byte opcode
            switch (opcode1) {
                case XOR_Ev_Gv_OPCODE:
                    // page 2722
                    this.opcode = Opcode.XOR;
                    isModRMRequired = true;
                    break;
                case JMP_nearf64_OPCODE:
                    this.opcode = Opcode.JMP;
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown opcode %02x", opcode1));
            }
        }

        // ModR/M byte (1 byte, if required)
        if (isModRMRequired) {
            final byte m = b.read1();
            modrm = Optional.of(m);
            if ((m & MODRM_MOD_MASK) == MODRM_MOD_MASK) {
                final byte op2 =
                        BitUtils.asByte(((m & MODRM_REG_MASK) >>> 3) | (ModRMRegExtension ? ((byte) 0x08) : 0));
                operand2 = Optional.of(operand64Bit ? Register64.fromByte(op2) : Register32.fromByte(op2));
                final byte op1 = BitUtils.asByte((m & MODRM_RM_MASK) | (extension ? ((byte) 0x08) : 0));
                operand1 = Optional.of(operand64Bit ? Register64.fromByte(op1) : Register32.fromByte(op1));
            } else {
                throw new IllegalArgumentException("Don't know what to do when first 2 bits of ModRM ar not set");
            }
        } else {
            modrm = Optional.empty();
            operand1 = Optional.empty();
            operand2 = Optional.empty();
        }

        // SIB byte (1 byte, if required)
        sib = isSIBRequired ? Optional.of(b.read1()) : Optional.empty();

        // Displacement (1, 2, 4 or 8 bytes, if required)
        if (isDisplacementRequired) {}

        // Immediate (1, 2, 4 or 8 bytes, if required)
        if (isImmediateRequired) {}
    }

    private Optional<Byte> readLegacyPrefixGroup1(final ByteBuffer b) {
        final byte prefix1 = b.read1();
        if (isLegacyPrefixGroup1(prefix1)) {
            logger.debug("Found group 1 legacy prefix 0x%02x", prefix1);
            return Optional.of(prefix1);
        } else {
            logger.debug("No group 1 legacy prefix found");
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
            logger.debug("No group 2 legacy prefix found");
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
            logger.debug("No group 3 legacy prefix found");
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
            logger.debug("No group 4 legacy prefix found");
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

    public String toString() {
        if (operand1.isPresent()) {
            if (operand2.isPresent()) {
                return opcode.mnemonic() + " " + operand1.orElseThrow().getName() + ","
                        + operand2.orElseThrow().getName();
            }
            return opcode.mnemonic() + " " + operand1.orElseThrow().getName();
        }
        return opcode.mnemonic();
    }
}
