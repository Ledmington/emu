/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.emu;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionDecoderV1;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.RelativeOffset;
import com.ledmington.elf.ELF;
import com.ledmington.elf.FileType;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.NoBitsSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

/**
 * Useful references <a href="https://linuxgazette.net/84/hawk.html">here</a> and <a
 * href="https://gist.github.com/x0nu11byt3/bcb35c3de461e5fb66173071a2379779" >here</a>.
 */
public final class X86Emulator implements Emulator {

    private static final MiniLogger logger = MiniLogger.getLogger("emu");

    private ELF elf;
    private MemoryController mem;
    private final X86RegisterFile regFile = new X86RegisterFile();
    private InstructionFetcher instructionFetcher;
    private InstructionDecoder dec;

    /** Creates an emulator to be used later. */
    public X86Emulator() {}

    private void setup(final ELF elf, final MemoryInitializer memInit) {
        this.mem = new MemoryController(memInit);
        this.elf = Objects.requireNonNull(elf);
        this.instructionFetcher = new InstructionFetcher(mem, regFile);
        this.dec = new InstructionDecoderV1(this.instructionFetcher);
    }

    @Override
    public void run(final ELF elf, final MemoryInitializer memInit, final String... commandLineArguments) {
        setup(elf, memInit);

        if (elf.fileHeader().getFileType() != FileType.ET_EXEC
                && elf.fileHeader().getFileType() != FileType.ET_DYN) {
            throw new IllegalArgumentException(String.format(
                    "Invalid ELF file type: expected ET_EXEC or ET_DYN but was %s",
                    elf.fileHeader().getFileType()));
        }

        this.instructionFetcher.setPosition(elf.fileHeader().getEntryPointVirtualAddress());
        logger.debug("Entry point virtual address : 0x%x", elf.fileHeader().getEntryPointVirtualAddress());

        loadELF();

        // TODO: load argc and argv (command-line arguments)
        logger.debug("Command-line arguments:");
        for (final String arg : commandLineArguments) {
            logger.debug("'%s'", arg);
        }

        // TODO: load environment variables
        logger.debug("Environment variables:");
        for (final Entry<String, String> env : System.getenv().entrySet()) {
            logger.debug("'%s' = '%s'", env.getKey(), env.getValue());
        }

        // setup stack
        final long allocatedMemory = 100_000_000L; // 100 MB
        final long highestAddress = Arrays.stream(elf.sectionTable())
                .map(sec ->
                        sec.getHeader().getVirtualAddress() + sec.getHeader().getSectionSize())
                .max(Long::compare)
                .orElseThrow();
        logger.debug(
                "Setting stack size to %,d bytes (%.3f MB) starting at 0x%x",
                allocatedMemory, (double) allocatedMemory / 1_000_000.0, highestAddress + allocatedMemory);
        mem.setPermissions(highestAddress, highestAddress + allocatedMemory, true, true, false);
        // we make RSP point at the last 8 bytes of allocated memory
        regFile.set(Register64.RSP, highestAddress + allocatedMemory - 8L);

        // run pre-constructors? (.preinit_array)

        // run constructors? (.init_array)

        while (true) {
            // dec.goTo(this.instructionFetcher.position());
            final Instruction inst = dec.decodeOne();
            // this.instructionFetcher.setPosition(dec.position());

            logger.debug(inst.toIntelSyntax());
            switch (inst.opcode()) {
                case XOR -> {
                    switch (((Register) inst.firstOperand()).bits()) {
                        case 8 -> {
                            final byte r1 = regFile.get((Register8) inst.firstOperand());
                            final byte r2 = regFile.get((Register8) inst.secondOperand());
                            regFile.set((Register8) inst.firstOperand(), BitUtils.xor(r1, r2));
                        }
                        case 32 -> {
                            final int r1 = regFile.get((Register32) inst.firstOperand());
                            final int r2 = regFile.get((Register32) inst.secondOperand());
                            regFile.set((Register32) inst.firstOperand(), r1 ^ r2);
                        }
                        default -> throw new IllegalArgumentException(String.format(
                                "Don't know what to do when XOR has %,d bits",
                                ((Register) inst.firstOperand()).bits()));
                    }
                }
                case AND -> {
                    switch (inst.firstOperand()) {
                        case IndirectOperand io -> {
                            // final long result = computeIndirectOperand(io);
                            throw new Error("Not implemented");
                        }
                        case Register r -> {
                            if (r instanceof Register64 r64) {
                                final long imm64 = ((Immediate) inst.secondOperand()).asLong();
                                regFile.set(r64, regFile.get(r64) & imm64);
                            } else {
                                throw new IllegalArgumentException(
                                        String.format("Don't know what to do when AND has %,d bits", r.bits()));
                            }
                        }
                        default -> throw new IllegalArgumentException(
                                String.format("Unknown type of first operand '%s'", inst.firstOperand()));
                    }
                }
                case JMP -> this.instructionFetcher.setPosition(
                        this.instructionFetcher.getPosition() + ((RelativeOffset) inst.firstOperand()).getValue());
                case MOV -> {
                    final Register64 dest = (Register64) inst.firstOperand();
                    final Register64 src = (Register64) inst.secondOperand();
                    regFile.set(dest, regFile.get(src));
                }
                case PUSH -> {
                    final Register64 src = (Register64) inst.firstOperand();
                    final long rsp = regFile.get(Register64.RSP);
                    mem.write(rsp, regFile.get(src));
                    // the stack "grows downward"
                    regFile.set(Register64.RSP, rsp - 8L);
                }
                case POP -> {
                    final Register64 dest = (Register64) inst.firstOperand();
                    final long rsp = regFile.get(Register64.RSP);
                    regFile.set(dest, mem.read8(rsp));
                    // the stack "grows downward"
                    regFile.set(Register64.RSP, rsp + 8L);
                }
                case LEA -> {
                    final Register64 dest = (Register64) inst.firstOperand();
                    final IndirectOperand src = (IndirectOperand) inst.secondOperand();
                    final long result = computeIndirectOperand(src);
                    regFile.set(dest, result);
                }
                case CALL -> {
                    // TODO: check this
                    final IndirectOperand src = (IndirectOperand) inst.firstOperand();
                    final long result = computeIndirectOperand(src);
                    regFile.set(Register64.RIP, result);
                }
                case ENDBR64 -> logger.warning("ENDBR64 not implemented");
                default -> throw new IllegalStateException(
                        String.format("Unknwon instruction %s", inst.toIntelSyntax()));
            }
        }

        // run desctructors? (.fini_array)
    }

    private long computeIndirectOperand(final IndirectOperand io) {
        return ((io.base() == null) ? 0L : regFile.get((Register64) io.base()))
                + ((io.index() == null) ? 0L : regFile.get((Register64) io.index())) * io.scale()
                + io.getDisplacement();
    }

    private void loadELF() {
        logger.debug("Loading ELF segments into memory");
        for (final PHTEntry phte : elf.programHeaderTable()) {
            if (phte.getType() != PHTEntryType.PT_LOAD) {
                // This segment is not loadable
                continue;
            }

            final long start = phte.getSegmentVirtualAddress();
            final long end = phte.getSegmentVirtualAddress() + phte.getSegmentMemorySize() - 1;
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
        for (final Section sec : elf.sectionTable()) {
            if (sec instanceof NoBitsSection || sec instanceof LoadableSection) {
                loadSection(sec);
            }
        }
    }

    /**
     * Loads the given ELF section into memory without modifying or checking the memory permissions.
     *
     * @param sec The ELF section to be loaded
     */
    public void loadSection(final Section sec) {
        Objects.requireNonNull(sec);

        if (sec instanceof NoBitsSection) {
            // allocate uninitialized data blocks
            final long startVirtualAddress = sec.getHeader().getVirtualAddress();
            final long size = sec.getHeader().getSectionSize();
            logger.debug(
                    "Loading section '%s' in memory range 0x%x-0x%x (%,d bytes)",
                    sec.getName(), startVirtualAddress, startVirtualAddress + size, size);
            mem.initialize(startVirtualAddress, size, (byte) 0x00);
        } else if (sec instanceof LoadableSection ls) {
            final long startVirtualAddress = sec.getHeader().getVirtualAddress();
            final byte[] content = ls.getLoadableContent();
            logger.debug(
                    "Loading section '%s' in memory range 0x%x-0x%x (%,d bytes)",
                    sec.getName(), startVirtualAddress, startVirtualAddress + content.length, content.length);
            mem.initialize(startVirtualAddress, content);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Don't know what to do with section '%s' of type %s and flags '%s'",
                    sec.getName(),
                    sec.getHeader().getType().getName(),
                    sec.getHeader().getFlags().stream()
                            .map(SectionHeaderFlags::getName)
                            .collect(Collectors.joining(", "))));
        }
    }

    @Override
    public String toString() {
        return "Emulator(elf=" + elf + ";mem="
                + mem + ";regFile="
                + regFile + ";instructionFetcher="
                + instructionFetcher + ";instructionDecoder="
                + dec + ')';
    }
}
