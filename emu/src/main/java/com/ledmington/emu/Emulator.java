package com.ledmington.emu;

import java.util.Objects;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.elf.ELF;
import com.ledmington.elf.ProgBitsSection;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

public final class Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private final ELF elf;
    private final InstructionDecoder dec;
    private final X86RegisterFile regs = new X86RegisterFile();
    private int rip;

    private long[] r = new long[16];

    public Emulator(final ELF elf) {
        this.elf = Objects.requireNonNull(elf);
        final byte[] code = ((ProgBitsSection) elf.getFirstSectionByName(".text")).content();
        this.dec = new InstructionDecoder(code);
    }

    public void run() {
        rip = 0;

        while (true) {
            dec.goTo(rip);
            final Instruction inst = dec.decodeOne();
            rip = dec.position();

            logger.debug(inst.toIntelSyntax());
            switch (inst.opcode()) {
                case XOR -> {
                    switch (((Register) inst.op(0)).bits()) {
                        case 8 -> {
                            final byte r1 = regs.get((Register8) inst.op(0));
                            final byte r2 = regs.get((Register8) inst.op(1));
                            regs.write((Register8) inst.op(0), BitUtils.xor(r1, r2));
                        }
                    }
                }
                default -> throw new IllegalStateException(String.format("Unknwon opcode %s", inst.opcode()));
            }
        }
    }
}
