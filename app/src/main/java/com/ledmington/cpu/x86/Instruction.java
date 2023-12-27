package com.ledmington.cpu.x86;

import java.util.Objects;
import java.util.Optional;

import com.ledmington.elf.ByteBuffer;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    // legacy prefixes, group 1
    private static final byte LOCK_PREFIX = (byte) 0xf0;
    private static final byte REPNE_PREFIX = (byte) 0xf2; // REPNE / REPNZ
    private static final byte REP_PREFIX = (byte) 0xf3; // REP / REPE / REPZ

    // legacy prefixes, group 2
    private static final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
    private static final byte SS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x36;
    private static final byte DS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x3e;
    private static final byte ES_SEGMENT_OVERRIDE_PREFIX = (byte) 0x26;
    private static final byte FS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x64;
    private static final byte GS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x65;
    private static final byte BRANCH_NOT_TAKEN_PREFIX = (byte) 0x2e;
    private static final byte BRANCH_TAKEN_PREFIX = (byte) 0x3e;

    // legacy prefixes, group 3
    private static final byte OPERAND_SIZE_OVERRIDE_PREFIX = (byte) 0x66;

    // legacy prefixes, group 4
    private static final byte ADDRESS_SIZE_OVERRIDE_PREFIX = (byte) 0x67;

    private static final byte MULTIBYTE_OPCODE_PREFIX = (byte) 0x0f;
    private static final byte REX_PREFIX = (byte) 0x40;
    private static final byte REX_W_MASK = (byte) 0x08;
    private static final byte REX_R_MASK = (byte) 0x04;
    private static final byte REX_X_MASK = (byte) 0x02;
    private static final byte REX_B_MASK = (byte) 0x01;
    private static final byte XOR_OPCODE = (byte) 0x31;

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

    public Instruction(final ByteBuffer b) {
        Objects.requireNonNull(b);

        // reading legacy prefixes (1 to 4 bytes, optional)
        {
            // group 1
            {
                final byte prefix1 = b.read1();
                if (prefix1 == LOCK_PREFIX || prefix1 == REPNE_PREFIX || prefix1 == REP_PREFIX) {
                    p1 = Optional.of(prefix1);
                } else {
                    p1 = Optional.empty();
                    b.setPosition(b.position() - 1);
                }
            }

            // group 2
            {
                final byte prefix2 = b.read1();
                if (prefix2 == CS_SEGMENT_OVERRIDE_PREFIX
                        || prefix2 == SS_SEGMENT_OVERRIDE_PREFIX
                        || prefix2 == DS_SEGMENT_OVERRIDE_PREFIX
                        || prefix2 == ES_SEGMENT_OVERRIDE_PREFIX
                        || prefix2 == FS_SEGMENT_OVERRIDE_PREFIX
                        || prefix2 == GS_SEGMENT_OVERRIDE_PREFIX
                        || prefix2 == BRANCH_NOT_TAKEN_PREFIX
                        || prefix2 == BRANCH_TAKEN_PREFIX) {
                    p2 = Optional.of(prefix2);
                } else {
                    p2 = Optional.empty();
                    b.setPosition(b.position() - 1);
                }
            }

            // group 3
            {
                final byte prefix3 = b.read1();
                if (prefix3 == OPERAND_SIZE_OVERRIDE_PREFIX) {
                    p3 = Optional.of(prefix3);
                } else {
                    p3 = Optional.empty();
                    b.setPosition(b.position() - 1);
                }
            }

            // group 4
            {
                final byte prefix4 = b.read1();
                if (prefix4 == ADDRESS_SIZE_OVERRIDE_PREFIX) {
                    p4 = Optional.of(prefix4);
                } else {
                    p4 = Optional.empty();
                    b.setPosition(b.position() - 1);
                }
            }
        }

        // read opcode (1 to 4 bytes, required)
        {
            byte opcode1 = b.read1();

            if ((opcode1 & (byte) 0xf0) == REX_PREFIX) {
                final boolean operand64Bit = (opcode1 & REX_W_MASK) != 0;
                final boolean ModRMRegExtension = (opcode1 & REX_R_MASK) != 0;
                final boolean SIBIndexExtension = (opcode1 & REX_X_MASK) != 0;
                final boolean extension = (opcode1 & REX_B_MASK) != 0;
                rexPrefix = Optional.of(opcode1);
                opcode1 = b.read1();
            } else {
                rexPrefix = Optional.empty();
            }

            if (opcode1 == MULTIBYTE_OPCODE_PREFIX) {
                // more than 1 bytes opcode
                final byte opcode2 = b.read1();
                throw new IllegalArgumentException(String.format("Unknown opcode 0x%02x%02x", opcode1, opcode2));
            } else {
                // 1 byte opcode
                if (opcode1 == XOR_OPCODE) {
                    // XOR Ev, Gv
                } else {
                    throw new IllegalArgumentException(String.format("Unknown opcode 0x%02x", opcode1));
                }
            }
        }

        // ModR/M byte (1 byte, if required)
        {
            final byte modrm = b.read1();

            final byte reg = (byte) (modrm & MODRM_REG_MASK);
            final byte rm = (byte) (modrm & MODRM_RM_MASK);
        }

        // SIB byte (1 byte, if required)
        {
            final byte sib = b.read1();

            final byte scale = (byte) (sib & SIB_SCALE_MASK);
            final byte index = (byte) (sib & SIB_INDEX_MASK);
            final byte base = (byte) (sib & SIB_BASE_MASK);
        }

        // Displacement (1, 2, 4 or 8 bytes, if required)
        {
        }

        // Immediate (1, 2, 4 or 8 bytes, if required)
        {
        }
    }
}
