package com.ledmington.cpu.x86;

import com.ledmington.elf.ByteBuffer;
import com.ledmington.utils.MiniLogger;

/**
 * Reference Intel® 64 and IA-32 Architectures Software Developer’s Manual volume 2.
 * Legacy prefixes : Paragraph 2.1.1.
 * Instruction opcodes : Appendix A.
 */
public final class InstructionDecoder {

    private static final MiniLogger logger = MiniLogger.getLogger("x86-asm");

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

    private ByteBuffer b;
    private int length;
    private int i = 0;

    public InstructionDecoder() {}

    // 1 to 4 bytes, optional
    private void readLegacyPrefixes(final StringBuilder sb) {
        // group 1
        {
            final byte prefix1 = b.read1();
            if (prefix1 == LOCK_PREFIX) {
                sb.append("LOCK ");
            } else if (prefix1 == REPNE_PREFIX) {
                sb.append("REPNE ");
            } else if (prefix1 == REP_PREFIX) {
                sb.append("REP ");
            } else {
                b.setPosition(b.position() - 1);
            }
        }

        // group 2
        {
            final byte prefix2 = b.read1();
            boolean found = false;
            if (prefix2 == CS_SEGMENT_OVERRIDE_PREFIX) {
                sb.append("CS ");
                found = true;
            }
            if (prefix2 == SS_SEGMENT_OVERRIDE_PREFIX) {
                sb.append("SS ");
                found = true;
            }
            if (prefix2 == DS_SEGMENT_OVERRIDE_PREFIX) {
                sb.append("DS ");
                found = true;
            }
            if (prefix2 == ES_SEGMENT_OVERRIDE_PREFIX) {
                sb.append("ES ");
                found = true;
            }
            if (prefix2 == FS_SEGMENT_OVERRIDE_PREFIX) {
                sb.append("FS ");
                found = true;
            }
            if (prefix2 == GS_SEGMENT_OVERRIDE_PREFIX) {
                sb.append("GS ");
                found = true;
            }
            if (prefix2 == BRANCH_NOT_TAKEN_PREFIX) {
                sb.append("Br-not-taken ");
            }
            if (prefix2 == BRANCH_TAKEN_PREFIX) {
                sb.append("Br-taken ");
            }
            if (!found) {
                b.setPosition(b.position() - 1);
            }
        }

        // group 3
        {
            final byte prefix3 = b.read1();
            if (prefix3 == OPERAND_SIZE_OVERRIDE_PREFIX) {
                sb.append("OPERAND ");
            } else {
                b.setPosition(b.position() - 1);
            }
        }

        // group 4
        {
            final byte prefix4 = b.read1();
            if (prefix4 == ADDRESS_SIZE_OVERRIDE_PREFIX) {
                sb.append("ADDRESS ");
            } else {
                b.setPosition(b.position() - 1);
            }
        }
    }

    // 1 to 4 bytes, required
    private void readOpcode(final StringBuilder sb) {
        byte opcode1 = b.read1();

        if ((opcode1 & (byte) 0xf0) == REX_PREFIX) {
            final boolean operand64Bit = (opcode1 & REX_W_MASK) != 0;
            final boolean ModRMRegExtension = (opcode1 & REX_R_MASK) != 0;
            final boolean SIBIndexExtension = (opcode1 & REX_X_MASK) != 0;
            final boolean extension = (opcode1 & REX_B_MASK) != 0;
            if (operand64Bit) {
                sb.append('W');
            }
            if (ModRMRegExtension) {
                sb.append('R');
            }
            if (SIBIndexExtension) {
                sb.append('X');
            }
            if (extension) {
                sb.append('B');
            }
            sb.append(' ');
            opcode1 = b.read1();
        }

        if (opcode1 == MULTIBYTE_OPCODE_PREFIX) {
            // more than 1 bytes opcode
            final byte opcode2 = b.read1();
            throw new IllegalArgumentException(String.format("Unknown opcode 0x%02x%02x", opcode1, opcode2));
        } else {
            // 1 byte opcode
            if (opcode1 == XOR_OPCODE) {
                sb.append("XOR "); // Ev, Gv
            } else {
                throw new IllegalArgumentException(String.format("Unknown opcode 0x%02x", opcode1));
            }
        }
    }

    // 1 byte, if required
    private void readModRM(final StringBuilder sb) {
        final byte modrm = b.read1();

        final byte reg = (byte) (modrm & MODRM_REG_MASK);
        final byte rm = (byte) (modrm & MODRM_RM_MASK);
    }

    // 1 byte, if required
    private void readSIB(final StringBuilder sb) {
        final byte sib = b.read1();

        final byte scale = (byte) (sib & SIB_SCALE_MASK);
        final byte index = (byte) (sib & SIB_INDEX_MASK);
        final byte base = (byte) (sib & SIB_BASE_MASK);
    }

    // 1, 2, 4 or 8 bytes, if required
    private void readDisplacement(final StringBuilder sb) {}

    // 1, 2, 4 or 8 bytes, if required
    private void readImmediate(final StringBuilder sb) {}

    public void decode(final byte[] code) {
        this.b = new ByteBuffer(code);
        this.length = code.length;
        this.i = 0;
        logger.debug("The code is %,d bytes long", length);

        for (; i < length; i++) {
            final StringBuilder sb = new StringBuilder();

            readLegacyPrefixes(sb);
            readOpcode(sb);
            readModRM(sb);
            readSIB(sb);
            readDisplacement(sb);
            readImmediate(sb);

            logger.debug(sb.toString());
        }
    }
}
