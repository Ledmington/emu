package com.ledmington.emu;

import java.util.Objects;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.elf.ELF;
import com.ledmington.utils.MiniLogger;

public final class Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private final ELF elf;
    private final InstructionDecoder dec;

    private int rip;

    public Emulator(final ELF elf, final InstructionDecoder dec) {
        this.elf = Objects.requireNonNull(elf);
        this.dec = Objects.requireNonNull(dec);
    }

    public void run() {
        rip = 0;

        while (true) {
            dec.goTo(rip);
            final Instruction inst = dec.decodeOne();
            rip = dec.position();
            logger.info(inst.toIntelSyntax());
        }
    }
}
