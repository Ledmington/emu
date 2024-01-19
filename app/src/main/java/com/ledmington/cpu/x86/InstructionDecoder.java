package com.ledmington.cpu.x86;

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

    public InstructionDecoder() {}

    public void decode(final byte[] code, final boolean isLittleEndian) {
        final ByteBuffer b = new ByteBuffer(code, isLittleEndian);
        final int length = code.length;
        logger.info("The code is %,d bytes long", length);

        int i = 0;
        for (; i < length; i++) {
            final Instruction inst = new Instruction(b);
            logger.info("%08x: %s", b.position(), inst.toString());
        }
    }
}
