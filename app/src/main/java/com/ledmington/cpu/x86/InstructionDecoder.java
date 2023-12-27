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

    private ByteBuffer b;

    public InstructionDecoder() {}

    public void decode(final byte[] code) {
        this.b = new ByteBuffer(code);
        final int length = code.length;
        int i = 0;
        logger.debug("The code is %,d bytes long", length);

        for (; i < length; i++) {
            final Instruction inst = new Instruction(b);
            logger.debug(inst.toString());
        }
    }
}
