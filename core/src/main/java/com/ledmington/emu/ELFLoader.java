/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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
import java.util.Optional;
import java.util.stream.Collectors;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.elf.ELF;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.ProgramHeaderTable;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.BasicProgBitsSection;
import com.ledmington.elf.section.ConstructorsSection;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.NoBitsSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.sym.SymbolTableEntry;
import com.ledmington.elf.section.sym.SymbolTableEntryType;
import com.ledmington.elf.section.sym.SymbolTableSection;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.Pair;

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
	 * @param cpu The CPU to be used to execute some instructions, if needed.
	 * @param mem The emulated memory where to load the file.
	 * @param commandLineArguments The arguments to pass to the program.
	 * @param baseAddress The address where to start loading the file.
	 * @param stackSize The size in bytes of the stack.
	 */
	public static void load(
			final ELF elf,
			final X86Emulator cpu,
			final MemoryController mem,
			final String[] commandLineArguments,
			final long baseAddress,
			final long stackSize) {
		loadSegments(elf, mem, baseAddress);
		loadSections(elf, mem, baseAddress);
		final long highestAddress = setupStack(elf, stackSize, mem);

		// We make RSP point at the last 8 bytes of allocated memory
		final long stackPointer = highestAddress + stackSize - 8L;

		// These are fake instructions to set up the stack
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RSP, new Immediate(stackPointer)));
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RBP, new Immediate(stackPointer)));

		// TODO: extract the zero into an EmulatorConstant value like 'baseStackValue'
		cpu.executeOne(new Instruction(Opcode.PUSH, new Immediate(0L)));

		// Set RDI to argc
		cpu.executeOne(new Instruction(
				Opcode.MOV, Register64.RDI, new Immediate(BitUtils.asLong(commandLineArguments.length))));

		final Pair<Long, Long> p = loadCommandLineArgumentsAndEnvironmentVariables(
				mem, highestAddress, elf.getFileHeader().is32Bit(), commandLineArguments);

		// set RSI to 'argv'
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RSI, new Immediate(p.first())));
		// set RDX to 'envp'
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RDX, new Immediate(p.second())));

		if (elf.getSectionByName(".preinit_array").isPresent()) {
			runPreInitArray();
		}

		final Optional<Section> symtab = elf.getSectionByName(".symtab");
		final Optional<Section> strtab = elf.getSectionByName(".strtab");

		final Optional<Section> initArray = elf.getSectionByName(".init_array");
		if (initArray.isPresent()) {
			runInitArray(
					(ConstructorsSection) initArray.orElseThrow(),
					cpu,
					baseAddress,
					symtab.isPresent() ? (SymbolTableSection) symtab.orElseThrow() : null,
					strtab.isPresent() ? (StringTableSection) strtab.orElseThrow() : null);
		}

		final Optional<Section> init = elf.getSectionByName(".init");
		if (init.isPresent()) {
			runInit(
					(BasicProgBitsSection) init.orElseThrow(),
					cpu,
					baseAddress,
					symtab.isPresent() ? (SymbolTableSection) symtab.orElseThrow() : null,
					strtab.isPresent() ? (StringTableSection) strtab.orElseThrow() : null);
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

	private static void runFrom(final X86Emulator cpu, final long startAddress) {
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RIP, new Immediate(startAddress)));
		cpu.execute();
	}

	private static void runPreInitArray() {
		notImplemented();
	}

	private static void runInitArray(
			final ConstructorsSection initArray,
			final X86Emulator cpu,
			final long entryPointVirtualAddress,
			final SymbolTableSection symtab,
			final StringTableSection strtab) {
		logger.debug("Running %,d constructor(s) from .init_array", initArray.getNumConstructors());
		for (int i = 0; i < initArray.getNumConstructors(); i++) {
			final long c = initArray.getConstructor(i);
			if (symtab != null && strtab != null) {
				final String ctorName =
						strtab.getString(symtab.getSymbolWithValue(c).nameOffset());
				logger.debug("Running .init_array[%d] = %,d (0x%016x) '%s'", i, c, c, ctorName);
			} else {
				logger.debug("Running .init_array[%d] = %,d (0x%016x)", i, c, c);
			}
			runFrom(cpu, entryPointVirtualAddress + c);
		}
	}

	private static void runInit(
			final BasicProgBitsSection init,
			final X86Emulator cpu,
			final long entryPointVirtualAddress,
			final SymbolTableSection symtab,
			final StringTableSection strtab) {
		final long sectionStart = entryPointVirtualAddress + init.getHeader().getFileOffset();
		final long sectionEnd = sectionStart + init.getHeader().getSectionSize();

		if (symtab != null) {
			for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
				final SymbolTableEntry ste = symtab.getSymbolTableEntry(i);
				final long start = sectionStart + ste.value();

				if (ste.info().getType() == SymbolTableEntryType.STT_FUNC
						&& start >= sectionStart
						&& start < sectionEnd) {
					if (strtab != null) {
						logger.debug("Running constructor '%s' from .init", strtab.getString(ste.nameOffset()));
					} else {
						logger.debug("Running constructor from .init");
					}
					runFrom(cpu, start);
				}
			}
		} else {
			// Can we just execute everything in .init?
			logger.warning("Ignoring contents of .init (for now)");
			// runFrom(cpu, sectionStart);
		}
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

	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
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

		// Make sure that the base address is 16-byte aligned
		highestAddress = (highestAddress + 0xf) & (~0xf);

		logger.debug(
				"Setting stack size to %,d bytes (%.3f MB) at 0x%016x-0x%016x",
				stackSize, stackSize / 1_000_000.0, highestAddress, highestAddress + stackSize - 1L);
		mem.setPermissions(highestAddress, highestAddress + stackSize - 1L, true, true, false);
		return highestAddress;
	}

	private static long getNumEnvBytes() {
		long count = 0L;
		for (final Entry<String, String> env : System.getenv().entrySet()) {
			count += env.getKey().length() + 1L + env.getValue().length() + 1L;
		}
		return count;
	}

	private static long align(final long value, final long alignment) {
		return ((value % alignment) == 0L) ? value : ((value & (alignment - 1L)) + alignment);
	}

	private static long getNumCliBytes(final String... args) {
		long count = 0L;
		for (final String arg : args) {
			count += arg.length() + 1L;
		}
		return count;
	}

	private static Pair<Long, Long> loadCommandLineArgumentsAndEnvironmentVariables(
			final MemoryController mem,
			final long stackBase,
			final boolean is32Bit,
			final String... commandLineArguments) {
		final long wordSize = is32Bit ? 4L : 8L;

		final long totalEnvBytes = getNumEnvBytes();
		final long totalEnvBytesAligned = align(totalEnvBytes, wordSize);

		final long totalCliBytes = getNumCliBytes(commandLineArguments);
		final long totalCliBytesAligned = align(totalCliBytes, wordSize);

		// TODO: implement this
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
		final long argv = currentArgPointer;
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
		final long envp = currentEnvPointer;
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

		return new Pair<>(argv, envp);
	}

	private static void loadSegments(final ProgramHeaderTable pht, final MemoryController mem, final long baseAddress) {
		logger.debug("Loading ELF segments into memory");
		for (int i = 0; i < pht.getProgramHeaderTableLength(); i++) {
			final PHTEntry phte = pht.getProgramHeader(i);
			if (phte.type() != PHTEntryType.PT_LOAD) {
				// This segment is not loadable
				continue;
			}

			final long start = baseAddress + phte.segmentVirtualAddress();
			final long end = start + phte.segmentMemorySize();
			logger.debug(
					"Setting permissions of range 0x%x-0x%x (%,d bytes) to %s",
					start,
					end - 1L,
					end - start,
					(phte.isReadable() ? "R" : "")
							+ (phte.isWriteable() ? "W" : "")
							+ (phte.isExecutable() ? "X" : ""));
			mem.setPermissions(start, end - 1L, phte.isReadable(), phte.isWriteable(), phte.isExecutable());
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
			default ->
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
