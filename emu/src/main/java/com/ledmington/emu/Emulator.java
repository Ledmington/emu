package com.ledmington.emu;

import java.util.Map;
import java.util.Objects;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.elf.ELF;
import com.ledmington.utils.ImmutableMap;
import com.ledmington.utils.MiniLogger;

public final class Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private static final Map<Register, Integer> regIndex =
            ImmutableMap.<Register, Integer>builder().put(Register32.EAX, 0).build();

    private final ELF elf;
    private final InstructionDecoder dec;

    private int rip;

    private long[] r = new long[16];

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

            logger.debug(inst.toIntelSyntax());
            switch (inst.opcode()) {
                case XOR -> {
                    final Register r1 = (Register) inst.op(0);
                    final Register r2 = (Register) inst.op(1);
                    final int r1i = regIndex.get(r1);
                }
                default -> throw new IllegalStateException(String.format("Unknwon opcode %s", inst.opcode()));
            }
        }
    }
}
