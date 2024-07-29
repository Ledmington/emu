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

import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ledmington.cpu.x86.Register64;
import com.ledmington.elf.ELF;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.ProgramHeaderTable;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.ConstructorsSection;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.NoBitsSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

/**
 * Loads an ELF into memory and sets it up for execution.
 *
 * <p>Useful references <a href="https://linuxgazette.net/84/hawk.html">here</a>, <a
 * href="https://gist.github.com/x0nu11byt3/bcb35c3de461e5fb66173071a2379779" >here</a> and <a
 * href="https://gitlab.com/x86-psABIs/x86-64-ABI">here</a>.
 */
public final class ELFLoader {

    private static final MiniLogger logger = MiniLogger.getLogger("elf-loader");

    private ELFLoader() {}

    /**
     * Loads the given ELF file in the emulated memory.
     *
     * @param elf The file to be loaded.
     * @param mem The emulated memory where to load the file.
     * @param commandLineArguments The arguments to pass to the program.
     * @param baseAddress The address where to start loading the file.
     * @param stackSize The size in bytes of the stack.
     * @param rf Register file of the CPU.
     */
    public static void load(
            final ELF elf,
            final MemoryController mem,
            final String[] commandLineArguments,
            final long baseAddress,
            final long stackSize,
            final X86RegisterFile rf) {
        loadSegments(elf, mem, baseAddress);
        loadSections(elf, mem, baseAddress);
        final long highestAddress = setupStack(elf, stackSize, mem);

        // we make RSP point at the last 8 bytes of allocated memory
        rf.set(Register64.RSP, highestAddress + stackSize - 8L);

        loadCommandLineArgumentsAndEnvironmentVariables(
                mem, highestAddress, elf.getFileHeader().is32Bit(), commandLineArguments);
        if (elf.getSectionByName(".preinit_array").isPresent()) {
            runPreInitArray();
        }
        if (elf.getSectionByName(".init_array").isPresent()) {
            runInitArray(
                    (ConstructorsSection) elf.getSectionByName(".init_array").orElseThrow(), mem, rf, baseAddress
                    // elf.getFileHeader().getEntryPointVirtualAddress()
                    );
        }
        if (elf.getSectionByName(".init").isPresent()) {
            runInit();
        }
        if (elf.getSectionByName(".ctors").isPresent()) {
            runCtors();
        }
    }

    /**
     * Unloads the ELF file from memory. No deallocation takes place, only termination/finalization routines are
     * executed.
     *
     * @param elf The file to be unloaded.
     */
    public static void unload(final ELF elf) {
        if (elf.getSectionByName(".fini_array").isPresent()) {
            runFiniArray();
        }
        if (elf.getSectionByName(".fini").isPresent()) {
            runFini();
        }
        if (elf.getSectionByName(".dtors").isPresent()) {
            runDtors();
        }
    }

    private static void runPreInitArray() {
        notImplemented();
    }

    private static void runInitArray(
            final ConstructorsSection initArray,
            final MemoryController mem,
            final X86RegisterFile rf,
            final long entryPointVirtualAddress) {
        for (int i = 0; i < initArray.getConstructorsLength(); i++) {
            logger.debug("Running .init_array[%d]", i);
            final long c = initArray.getConstructor(i);
            X86Emulator.run(mem, rf, entryPointVirtualAddress + c);
        }
    }

    private static void runInit() {
        notImplemented();
    }

    private static void runCtors() {
        notImplemented();
    }

    private static void runFiniArray() {
        notImplemented();
    }

    private static void runFini() {
        notImplemented();
    }

    private static void runDtors() {
        notImplemented();
    }

    private static void notImplemented() {
        throw new Error("Not implemented");
    }

    private static long setupStack(final SectionTable st, final long stackSize, final MemoryController mem) {
        long highestAddress = 0L;
        for (int i = 0; i < st.getSectionTableLength(); i++) {
            highestAddress = Math.max(
                    highestAddress,
                    st.getSection(i).getHeader().getVirtualAddress()
                            + st.getSection(i).getHeader().getSectionSize());
        }
        logger.debug(
                "Setting stack size to %,d bytes (%.3f MB) starting at 0x%x",
                stackSize, (double) stackSize / 1_000_000.0, highestAddress + stackSize);
        mem.setPermissions(highestAddress, highestAddress + stackSize, true, true, false);
        return highestAddress;
    }

    private static long loadCommandLineArgumentsAndEnvironmentVariables(
            final MemoryController mem,
            final long stackBase,
            final boolean is32Bit,
            final String... commandLineArguments) {
        final long wordSize = is32Bit ? 4L : 8L;

        long totalEnvBytes = 0L;
        for (final Entry<String, String> env : System.getenv().entrySet()) {
            totalEnvBytes += env.getKey().length() + 1L + env.getValue().length() + 1L;
        }
        // align size
        final long totalEnvBytesAligned =
                (totalEnvBytes % wordSize == 0) ? totalEnvBytes : ((totalEnvBytes & (wordSize - 1L)) + wordSize);

        long totalCliBytes = 0L;
        for (final String arg : commandLineArguments) {
            totalCliBytes += arg.length() + 1L;
        }
        // align size to 4-bytes
        final long totalCliBytesAligned =
                (totalCliBytes % wordSize == 0) ? totalCliBytes : ((totalCliBytes & (wordSize - 1L)) + wordSize);

        final long numAuxvEntries = 0L;

        final long numEnv = BitUtils.asLong(System.getenv().size());

        long p = stackBase
                - (
                // argc
                wordSize
                        // argv pointers
                        + wordSize * commandLineArguments.length
                        // null word
                        + wordSize
                        // envp pointers
                        + wordSize * numEnv
                        // null word
                        + wordSize
                        // auxv structures
                        + 2L * wordSize * numAuxvEntries
                        // null word
                        + wordSize
                        // argv contents
                        + totalCliBytesAligned
                        // envp contents
                        + totalEnvBytesAligned);

        // write argc
        mem.initialize(
                p,
                is32Bit
                        ? BitUtils.asBEBytes(commandLineArguments.length)
                        : BitUtils.asBEBytes(BitUtils.asLong(commandLineArguments.length)));
        p += wordSize;

        // write argv pointers and contents
        long currentArgPointer = stackBase - totalEnvBytesAligned - totalCliBytesAligned;
        for (final String arg : commandLineArguments) {
            mem.initialize(
                    p,
                    is32Bit
                            ? BitUtils.asBEBytes(BitUtils.asInt(currentArgPointer))
                            : BitUtils.asBEBytes(currentArgPointer));
            final byte[] argBytes = arg.getBytes(StandardCharsets.UTF_8);
            mem.initialize(currentArgPointer, argBytes);
            mem.initialize(currentArgPointer + BitUtils.asLong(argBytes.length), (byte) 0x00);
            currentArgPointer += BitUtils.asLong(argBytes.length) + 1L;
            p += wordSize;
        }

        // write null word
        mem.initialize(p, p + wordSize, (byte) 0x00);

        // write envp pointers and contents
        long currentEnvPointer = stackBase - totalEnvBytesAligned;
        for (final Entry<String, String> env : System.getenv().entrySet()) {
            final String envString = env.getKey() + "=" + env.getValue();
            final byte[] envBytes = envString.getBytes(StandardCharsets.UTF_8);
            mem.initialize(
                    p,
                    is32Bit
                            ? BitUtils.asBEBytes(BitUtils.asInt(currentEnvPointer))
                            : BitUtils.asBEBytes(currentEnvPointer));
            mem.initialize(currentEnvPointer, envBytes);
            mem.initialize(currentEnvPointer + BitUtils.asLong(envBytes.length), (byte) 0x00);
            currentEnvPointer += BitUtils.asLong(envBytes.length) + 1L;
            p += wordSize;
        }

        return p;
    }

    private static void loadSegments(final ProgramHeaderTable pht, final MemoryController mem, final long baseAddress) {
        logger.debug("Loading ELF segments into memory");
        for (int i = 0; i < pht.getProgramHeaderTableLength(); i++) {
            final PHTEntry phte = pht.getProgramHeader(i);
            if (phte.getType() != PHTEntryType.PT_LOAD) {
                // This segment is not loadable
                continue;
            }

            final long start = baseAddress + phte.getSegmentVirtualAddress();
            final long end = start + phte.getSegmentMemorySize();
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
    }

    private static void loadSections(final SectionTable st, final MemoryController mem, final long baseAddress) {
        logger.debug("Loading ELF sections into memory");
        for (int i = 0; i < st.getSectionTableLength(); i++) {
            final Section sec = st.getSection(i);
            if (sec instanceof NoBitsSection || sec instanceof LoadableSection) {
                initializeSection(sec, mem, baseAddress);
            }
        }
    }

    private static void initializeSection(final Section sec, final MemoryController mem, final long baseAddress) {
        Objects.requireNonNull(sec);

        switch (sec) {
            case NoBitsSection ignored -> {
                // allocate uninitialized data blocks
                final long startVirtualAddress = baseAddress + sec.getHeader().getVirtualAddress();
                final long size = sec.getHeader().getSectionSize();
                logger.debug(
                        "Loading section '%s' in memory range 0x%x-0x%x (%,d bytes)",
                        sec.getName(), startVirtualAddress, startVirtualAddress + size, size);
                mem.initialize(startVirtualAddress, size, (byte) 0x00);
            }
            case LoadableSection ls -> {
                final long startVirtualAddress = baseAddress + sec.getHeader().getVirtualAddress();
                final byte[] content = ls.getLoadableContent();
                logger.debug(
                        "Loading section '%s' in memory range 0x%x-0x%x (%,d bytes)",
                        sec.getName(), startVirtualAddress, startVirtualAddress + content.length, content.length);
                mem.initialize(startVirtualAddress, content);
            }
            default -> {
                throw new IllegalArgumentException(String.format(
                        "Don't know what to do with section '%s' of type %s and flags '%s'",
                        sec.getName(),
                        sec.getHeader().getType().getName(),
                        sec.getHeader().getFlags().stream()
                                .map(SectionHeaderFlags::getName)
                                .collect(Collectors.joining(", "))));
            }
        }
    }
}
