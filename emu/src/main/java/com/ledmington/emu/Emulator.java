package com.ledmington.emu;

import java.util.Objects;

import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RelativeOffset;
import com.ledmington.elf.ELF;
import com.ledmington.elf.FileType;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.section.ProgBitsSection;
import com.ledmington.elf.section.Section;
import com.ledmington.emu.mem.MemoryController;
import com.ledmington.emu.mem.MemoryInitializer;
import com.ledmington.emu.mem.RandomAccessMemory;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

public final class Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private final ELF elf;
    private final InstructionDecoder dec;
    private final MemoryController mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()));
    private final X86RegisterFile regFile = new X86RegisterFile();
    private long rip;

    public Emulator(final ELF elf) {
        this.elf = Objects.requireNonNull(elf);

        if (elf.getFileHeader().getType() != FileType.ET_EXEC) {
            throw new IllegalArgumentException(String.format(
                    "Invalid ELF file type: expected ET_EXEC but was %s",
                    elf.getFileHeader().getType()));
        }

        final byte[] code = ((ProgBitsSection) elf.getFirstSectionByName(".text")).content();
        this.dec = new InstructionDecoder(code);

        final ProgBitsSection dataSection = (ProgBitsSection) elf.getFirstSectionByName(".data");
    }

    public void run() {
        loadELF();

        rip = 0L;

        while (true) {
            dec.goTo(rip);
            final Instruction inst = dec.decodeOne();
            rip = dec.position();

            logger.debug(inst.toIntelSyntax());
            switch (inst.opcode()) {
                case XOR -> {
                    switch (((Register) inst.op(0)).bits()) {
                        case 8 -> {
                            final byte r1 = regFile.get((Register8) inst.op(0));
                            final byte r2 = regFile.get((Register8) inst.op(1));
                            regFile.set((Register8) inst.op(0), BitUtils.xor(r1, r2));
                        }
                    }
                }
                case JMP -> {
                    rip += ((RelativeOffset) inst.op(0)).amount();
                }
                case LEA -> {
                    final Register64 dest = (Register64) inst.op(0);
                    final IndirectOperand src = (IndirectOperand) inst.op(1);
                    final long addr = (src.r1() == null ? 0L : regFile.get((Register64) src.r1()));
                    regFile.set(dest, addr);
                }
                case MOV -> {
                    final Register64 dest = (Register64) inst.op(0);
                    final Register64 src = (Register64) inst.op(1);
                    regFile.set(dest, regFile.get(dest));
                }
                default -> throw new IllegalStateException(String.format("Unknwon opcode %s", inst.opcode()));
            }
        }
    }

    private void loadELF() {
        logger.debug("Loading ELF segments into memory");
        for (final PHTEntry phte : elf.programHeader()) {
            if (phte.type() != PHTEntryType.PT_LOAD) {
                // This segment is not loadable
                continue;
            }
            mem.setPermissions(
                    phte.segmentVirtualAddress(),
                    phte.segmentVirtualAddress() + phte.segmentMemorySize(),
                    phte.isReadable(),
                    phte.isWriteable(),
                    phte.isExecutable());
        }

        logger.debug("Loading ELF sections into memory");
        for (final Section sec : elf.sections()) {
            if (sec.header().size() != 0) {
                mem.loadSection(sec);
            }
        }
    }
}
