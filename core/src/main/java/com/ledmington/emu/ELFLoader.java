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
import java.util.ArrayList;
import java.util.List;
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

	private final List<Range> memorySegments = new ArrayList<>();

	private record Range(long start, long end) {}

	public ELFLoader() {}

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
	public void load(
			final ELF elf,
			final X86Emulator cpu,
			final MemoryController mem,
			final String[] commandLineArguments,
			final long baseAddress,
			final long baseStackAddress,
			final long stackSize,
			final long baseStackValue) {
		loadSegments(elf, mem, baseAddress);
		loadSections(elf, mem, baseAddress);
		setupStack(baseStackAddress, stackSize, mem);

		// We make RSP point at the last 8 bytes of allocated memory
		final long stackPointer = baseStackAddress + stackSize - 8L;

		// These are fake instructions to set up the stack
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSP, new Immediate(stackPointer)));
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RBP, new Immediate(stackPointer)));

		// Since it is not possible to push a 64-bit immediate value, we need to use a register temporarily
		{
			final long oldValue = cpu.getRegisters().get(Register64.R15);
			cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.R15, new Immediate(baseStackValue)));
			cpu.executeOne(new Instruction(Opcode.PUSH, Register64.R15));
			cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.R15, new Immediate(oldValue)));
		}

		// Set RDI to argc
		cpu.executeOne(new Instruction(
				Opcode.MOVABS, Register64.RDI, new Immediate(BitUtils.asLong(commandLineArguments.length))));

		final Pair<Long, Long> p = loadCommandLineArgumentsAndEnvironmentVariables(
				mem,
				// highestAddress,
				stackPointer,
				elf.getFileHeader().is32Bit(),
				commandLineArguments);

		// set RSI to 'argv'
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSI, new Immediate(p.first())));
		// set RDX to 'envp'
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RDX, new Immediate(p.second())));

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
	public void unload(final ELF elf) {
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

	private void runFrom(final X86Emulator cpu, final long startAddress) {
		cpu.setInstructionPointer(startAddress);
		cpu.execute();
	}

	private void runPreInitArray() {
		notImplemented();
	}

	private void runInitArray(
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
			cpu.turnOn();
			runFrom(cpu, entryPointVirtualAddress + c);
		}
	}

	private void runInit(
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
					cpu.turnOn();
					runFrom(cpu, start);
				}
			}
		} else {
			// Can we just execute everything in .init?
			logger.warning("Ignoring contents of .init (for now)");
			// runFrom(cpu, sectionStart);
		}
	}

	private void runCtors() {
		notImplemented();
	}

	private void runFiniArray() {
		notImplemented();
	}

	private void runFini() {
		notImplemented();
	}

	private void runDtors() {
		notImplemented();
	}

	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	private void notImplemented() {
		throw new Error("Not implemented");
	}

	private void setupStack(long baseStackAddress, final long stackSize, final MemoryController mem) {
		if ((baseStackAddress & 0xfL) != 0L) {
			baseStackAddress = (baseStackAddress + 15L) & 0xfffffffffffffff0L;
			logger.debug("Aligning base stack address to 16-byte aligned value: 0x%x", baseStackAddress);
		}

		logger.debug(
				"Setting stack size to %,d bytes (%.3e B) at 0x%016x-0x%016x",
				stackSize, (double) stackSize, baseStackAddress, baseStackAddress + stackSize - 1L);
		mem.setPermissions(baseStackAddress, baseStackAddress + stackSize - 1L, true, true, false);
	}

	private long getNumEnvBytes() {
		long count = 0L;
		for (final Entry<String, String> env : System.getenv().entrySet()) {
			count += env.getKey().length() + 1L + env.getValue().length() + 1L;
		}
		return count;
	}

	private long align(final long value, final long alignment) {
		return ((value % alignment) == 0L) ? value : ((value & (alignment - 1L)) + alignment);
	}

	private long getNumCliBytes(final String... args) {
		long count = 0L;
		for (final String arg : args) {
			count += arg.length() + 1L;
		}
		return count;
	}

	private Pair<Long, Long> loadCommandLineArgumentsAndEnvironmentVariables(
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
		mem.initialize(p, wordSize, (byte) 0x00);

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

	private void loadSegments(final ProgramHeaderTable pht, final MemoryController mem, final long baseAddress) {
		logger.debug("Loading ELF segments into memory");

		// NOTE: this index is not the PHTE index, this index makes sense only during loading
		int segmentIndex = 0;
		for (int i = 0; i < pht.getProgramHeaderTableLength(); i++) {
			final PHTEntry phte = pht.getProgramHeader(i);
			if (phte.type() != PHTEntryType.PT_LOAD) {
				// This segment is not loadable
				continue;
			}

			final long start = baseAddress + phte.segmentVirtualAddress();
			final long end = start + phte.segmentMemorySize();
			logger.debug(
					"Setting permissions of memory segment %,d at range 0x%x-0x%x (%,d bytes) to %s",
					segmentIndex,
					start,
					end,
					end - start,
					(phte.isReadable() ? "R" : "")
							+ (phte.isWriteable() ? "W" : "")
							+ (phte.isExecutable() ? "X" : ""));
			mem.setPermissions(start, end, phte.isReadable(), phte.isWriteable(), phte.isExecutable());

			segmentIndex++;
			memorySegments.add(new Range(start, end));
		}
	}

	private void loadSections(final SectionTable st, final MemoryController mem, final long baseAddress) {
		logger.debug("Loading ELF sections into memory");
		for (int i = 0; i < st.getSectionTableLength(); i++) {
			final Section sec = st.getSection(i);
			if (!sec.getHeader().getFlags().contains(SectionHeaderFlags.SHF_ALLOC)) {
				continue;
			}
			if (sec instanceof NoBitsSection || sec instanceof LoadableSection) {
				initializeSection(sec, mem, baseAddress);
			}
		}
	}

	private void initializeSection(final Section sec, final MemoryController mem, final long baseAddress) {
		Objects.requireNonNull(sec);

		switch (sec) {
			case NoBitsSection ignored -> {
				// allocate uninitialized data blocks
				final long startVirtualAddress = baseAddress + sec.getHeader().getVirtualAddress();
				final long size = sec.getHeader().getSectionSize();
				final int segmentIndex = findSegmentIndex(startVirtualAddress, size);
				logger.debug(
						"Loading section %s in memory segment %,d at range 0x%x-0x%x (%,d bytes)",
						sec.getName(), segmentIndex, startVirtualAddress, startVirtualAddress + size, size);
				mem.initialize(startVirtualAddress, size, (byte) 0x00);
			}
			case LoadableSection ls -> {
				final long startVirtualAddress = baseAddress + sec.getHeader().getVirtualAddress();
				final byte[] content = ls.getLoadableContent();
				final int segmentIndex = findSegmentIndex(startVirtualAddress, content.length);
				logger.debug(
						"Loading section %s in memory segment %,d at range 0x%x-0x%x (%,d bytes)",
						sec.getName(),
						segmentIndex,
						startVirtualAddress,
						startVirtualAddress + content.length,
						content.length);
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

	private int findSegmentIndex(final long sectionStart, final long sectionSize) {
		for (int i = 0; i < memorySegments.size(); i++) {
			final Range segment = memorySegments.get(i);
			if (sectionStart >= segment.start() && sectionStart + sectionSize <= segment.end()) {
				return i;
			}
		}
		throw new IllegalArgumentException("No memory segment found.");
	}
}
