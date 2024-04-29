package com.ledmington.emu;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RelativeOffset;
import com.ledmington.elf.ELF;
import com.ledmington.elf.FileType;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.section.Section;
import com.ledmington.emu.mem.MemoryController;
import com.ledmington.emu.mem.MemoryInitializer;
import com.ledmington.emu.mem.RandomAccessMemory;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;

/**
 * A useful reference <a href="https://linuxgazette.net/84/hawk.html">here</a>.
 */
public final class Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private final ELF elf;
    private final MemoryController mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()));
    private final X86RegisterFile regFile = new X86RegisterFile();
    private final ReadOnlyByteBuffer instructionFetcher = new ReadOnlyByteBuffer(false) {

        private long instructionPointer = 0L;

        @Override
        public void setPosition(final long newPosition) {
            this.instructionPointer = newPosition;
        }

        @Override
        public long position() {
            return instructionPointer;
        }

        @Override
        protected byte read() {
            return mem.readCode(instructionPointer);
        }
    };
    private final InstructionDecoder dec = new InstructionDecoder(this.instructionFetcher);

    public Emulator(final ELF elf) {
        this.elf = Objects.requireNonNull(elf);
    }

    public void run() {
        if (elf.getFileHeader().getType() != FileType.ET_EXEC) {
            throw new IllegalArgumentException(String.format(
                    "Invalid ELF file type: expected ET_EXEC but was %s",
                    elf.getFileHeader().getType()));
        }

        this.instructionFetcher.setPosition(elf.getFileHeader().entryPointVirtualAddress());
        logger.debug("Entry point virtual address : 0x%x", elf.getFileHeader().entryPointVirtualAddress());

        loadELF();

        // TODO: load argc and argv (command-line arguments)

        // TODO: load environment variables

        // setup stack
        final long allocatedMemory = 1L << 30; // 1 GiB
        final long highestAddress = Arrays.stream(elf.sections())
                .map(sec -> sec.header().virtualAddress() + sec.header().sectionSize())
                .max(Long::compare)
                .orElseThrow();
        logger.debug(
                "Setting stack size to %,d bytes (%.3f MiB) starting at 0x%x",
                allocatedMemory, (double) allocatedMemory / 1_048_576.0, highestAddress + allocatedMemory);
        mem.setPermissions(highestAddress, highestAddress + allocatedMemory, true, true, false);
        regFile.set(Register64.RSP, highestAddress + allocatedMemory);

        while (true) {
            // dec.goTo(this.instructionFetcher.position());
            final Instruction inst = dec.decodeOne();
            // this.instructionFetcher.setPosition(dec.position());

            logger.debug(inst.toIntelSyntax());
            switch (inst.opcode()) {
                case XOR -> {
                    switch (((Register) inst.op(0)).bits()) {
                        case 8 -> {
                            final byte r1 = regFile.get((Register8) inst.op(0));
                            final byte r2 = regFile.get((Register8) inst.op(1));
                            regFile.set((Register8) inst.op(0), BitUtils.xor(r1, r2));
                        }
                        case 32 -> {
                            final int r1 = regFile.get((Register32) inst.op(0));
                            final int r2 = regFile.get((Register32) inst.op(1));
                            regFile.set((Register32) inst.op(0), r1 ^ r2);
                        }
                        default -> throw new IllegalArgumentException(String.format(
                                "Don't know what to do when XOR has %,d bits", ((Register) inst.op(0)).bits()));
                    }
                }
                case AND -> {
                    switch (((Register) inst.op(0)).bits()) {
                        case 64 -> {
                            final Register64 r = (Register64) inst.op(0);
                            final long imm64 = ((Immediate) inst.op(1)).asLong();
                            regFile.set(r, regFile.get(r) & imm64);
                        }
                        default -> throw new IllegalArgumentException(String.format(
                                "Don't know what to do when XOR has %,d bits", ((Register) inst.op(0)).bits()));
                    }
                }
                case JMP -> this.instructionFetcher.setPosition(
                        this.instructionFetcher.position() + ((RelativeOffset) inst.op(0)).amount());
                case MOV -> {
                    final Register64 dest = (Register64) inst.op(0);
                    final Register64 src = (Register64) inst.op(1);
                    regFile.set(dest, regFile.get(src));
                }
                case PUSH -> {
                    final Register64 src = (Register64) inst.op(0);
                    final long rsp = regFile.get(Register64.RSP);
                    mem.write(rsp, regFile.get(src));
                    // the stack "grows downward"
                    regFile.set(Register64.RSP, rsp - 8L);
                }
                case POP -> {
                    final Register64 dest = (Register64) inst.op(0);
                    final long rsp = regFile.get(Register64.RSP);
                    regFile.set(dest, mem.read8(rsp));
                    // the stack "grows downward"
                    regFile.set(Register64.RSP, rsp + 8L);
                }
                case ENDBR64 -> logger.warning("ENDBR64 not implemented");
                default -> throw new IllegalStateException(
                        String.format("Unknwon instruction %s", inst.toIntelSyntax()));
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

            final long start = phte.segmentVirtualAddress();
            final long end = phte.segmentVirtualAddress() + phte.segmentMemorySize() - 1;
            logger.debug(
                    "Setting permissions of range 0x%x-0x%x (%,d bytes) to %s",
                    start,
                    end,
                    end - start,
                    (phte.isReadable() ? "R" : "")
                            + (phte.isWriteable() ? "W" : "")
                            + (phte.isExecutable() ? "X" : ""));
            mem.setPermissions(start, end, phte.isReadable(), phte.isWriteable(), phte.isExecutable());
        }

        logger.debug("Loading ELF sections into memory");
        for (final Section sec : elf.sections()) {
            if (sec.header().sectionSize() != 0) {
                mem.loadSection(sec);
            }
        }
    }
}
