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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ledmington.cpu.x86.GeneralInstruction;
import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.elf.ELF;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.PHTEntryType;
import com.ledmington.elf.ProgramHeaderTable;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.BasicProgBitsSection;
import com.ledmington.elf.section.ConstructorsSection;
import com.ledmington.elf.section.DestructorsSection;
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
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;
import com.ledmington.utils.os.OSUtils;

/**
 * Loads an ELF into memory and sets it up for execution.
 *
 * <p>Useful references <a href="https://linuxgazette.net/84/hawk.html">here</a>, <a
 * href="https://gist.github.com/x0nu11byt3/bcb35c3de461e5fb66173071a2379779" >here</a> and <a
 * href="https://gitlab.com/x86-psABIs/x86-64-ABI">here</a>.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class ELFLoader {

	private static final MiniLogger logger = MiniLogger.getLogger("elf-loader");

	private final X86Emulator cpu;
	private final MemoryController mem;
	private final List<Range> memorySegments = new ArrayList<>();

	private record Range(long start, long end) {}

	/**
	 * Creates a new ELFLoader.
	 *
	 * @param cpu The CPU to be used to execute some instructions, if needed.
	 * @param mem The emulated memory where to load the file.
	 */
	public ELFLoader(final X86Emulator cpu, final MemoryController mem) {
		this.cpu = Objects.requireNonNull(cpu, "Null cpu.");
		this.mem = Objects.requireNonNull(mem, "Null memory.");
	}

	/**
	 * Loads the given ELF file in the emulated memory.
	 *
	 * @param elf The file to be loaded.
	 * @param commandLineArguments The arguments to pass to the program. Must include the name of the program as the
	 *     first argument.
	 * @param baseAddress The address where to start loading the file.
	 * @param baseStackAddress The address where to place the base of the stack.
	 * @param stackSize The size in bytes of the stack.
	 */
	public void load(
			final ELF elf,
			final String[] commandLineArguments,
			final long baseAddress,
			final long baseStackAddress,
			final long stackSize) {
		loadSegments(elf, baseAddress);
		loadSections(elf, baseAddress);

		final long stackTop = alignAddress(baseStackAddress); // highest address (initial RSP)
		final long stackBottom = stackTop - stackSize; // lowest address (stack limit)

		setupStack(stackTop, stackBottom);

		// These are fake instructions to set up the stack
		set(Register64.RSP, stackTop);

		// Do we *actually* need to do this?
		push(BitUtils.asLong(commandLineArguments.length));
		// push(baseStackValue);
		// push(baseStackValue + 1L); // why?

		final int argc = commandLineArguments.length;
		set(Register64.RDI, BitUtils.asLong(argc));

		loadCommandLineArgumentsAndEnvironmentVariables(
				elf, stackTop, elf.getFileHeader().is32Bit(), commandLineArguments);

		if (elf.getSectionByName(".preinit_array").isPresent()) {
			runPreInitArray();
		}

		final SymbolTableSection symtab =
				(SymbolTableSection) elf.getSectionByName(".symtab").orElse(null);
		final StringTableSection strtab =
				(StringTableSection) elf.getSectionByName(".strtab").orElse(null);

		final Optional<Section> initArray = elf.getSectionByName(".init_array");
		if (initArray.isPresent()) {
			runInitArray((ConstructorsSection) initArray.orElseThrow(), baseAddress, symtab, strtab);
		}

		final Optional<Section> init = elf.getSectionByName(".init");
		if (init.isPresent()) {
			runInit((BasicProgBitsSection) init.orElseThrow(), baseAddress, symtab, strtab);
		}
		if (elf.getSectionByName(".ctors").isPresent()) {
			runCtors();
		}
	}

	private void push(final long value) {
		// Since it is not possible to push a 64-bit immediate value, we need to use a register temporarily
		final long oldValue = cpu.getRegisters().get(Register64.R15);
		set(Register64.R15, value);
		cpu.executeOne(new GeneralInstruction(Opcode.PUSH, Register64.R15));
		set(Register64.R15, oldValue);
	}

	/**
	 * Aligns the given address to a 16-byte boundary.
	 *
	 * @param address The address to align.
	 * @return The same address aligned to a 16-byte boundary.
	 */
	public static long alignAddress(final long address) {
		final boolean isAligned = (address & 0xFL) == 0L;
		if (isAligned) {
			return address;
		} else {
			return (address + 15L) & 0xFFFFFFFFFFFFFFF0L;
		}
	}

	private void set(final Register64 r, final long value) {
		cpu.executeOne(new GeneralInstruction(Opcode.MOVABS, r, new Immediate(value)));
	}

	/**
	 * Unloads the ELF file from memory. No deallocation takes place, only termination/finalization routines are
	 * executed.
	 *
	 * @param elf The file to be unloaded.
	 * @param baseAddress The address to unload the ELF file from.
	 */
	public void unload(final ELF elf, final long baseAddress) {
		final SymbolTableSection symtab =
				(SymbolTableSection) elf.getSectionByName(".symtab").orElse(null);
		final StringTableSection strtab =
				(StringTableSection) elf.getSectionByName(".strtab").orElse(null);

		final Optional<Section> finiArray = elf.getSectionByName(".fini_array");
		if (finiArray.isPresent()) {
			runFiniArray((DestructorsSection) finiArray.orElseThrow(), baseAddress, symtab, strtab);
		}

		final Optional<Section> fini = elf.getSectionByName(".fini");
		if (fini.isPresent()) {
			runFini((BasicProgBitsSection) fini.orElseThrow(), baseAddress, symtab, strtab);
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
			final long entryPointVirtualAddress,
			final SymbolTableSection symtab,
			final StringTableSection strtab) {
		logger.debug("Running %,d constructor(s) from .init_array", initArray.getNumConstructors());
		for (int i = 0; i < initArray.getNumConstructors(); i++) {
			final long c = initArray.getConstructor(i);
			if (symtab != null && strtab != null) {
				final String ctorName =
						strtab.getString(symtab.getSymbolWithValue(c).nameOffset());
				logger.debug("Running .init_array[%d] = 0x%x '%s'", i, c, ctorName);
			} else {
				logger.debug("Running .init_array[%d] = 0x%x", i, c);
			}
			cpu.turnOn();
			runFrom(cpu, entryPointVirtualAddress + c);
		}
	}

	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	private void runInit(
			final BasicProgBitsSection init,
			final long entryPointVirtualAddress,
			final SymbolTableSection symtab,
			final StringTableSection strtab) {
		final long sectionStart = entryPointVirtualAddress + init.header().getFileOffset();
		final long sectionEnd = sectionStart + init.header().getSectionSize();

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

	private void runFiniArray(
			final DestructorsSection finiArray,
			final long entryPointVirtualAddress,
			final SymbolTableSection symtab,
			final StringTableSection strtab) {
		logger.debug("Running %,d destructor(s) from .fini_array", finiArray.getNumDestructors());
		for (int i = 0; i < finiArray.getNumDestructors(); i++) {
			final long c = finiArray.getDestructor(i);
			if (symtab != null && strtab != null) {
				final String ctorName =
						strtab.getString(symtab.getSymbolWithValue(c).nameOffset());
				logger.debug("Running .fini_array[%d] = 0x%x '%s'", i, c, ctorName);
			} else {
				logger.debug("Running .fini_array[%d] = 0x%x", i, c);
			}
			cpu.turnOn();
			runFrom(cpu, entryPointVirtualAddress + c);
		}
	}

	@SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
	private void runFini(
			final BasicProgBitsSection fini,
			final long entryPointVirtualAddress,
			final SymbolTableSection symtab,
			final StringTableSection strtab) {
		final long sectionStart = entryPointVirtualAddress + fini.header().getFileOffset();
		final long sectionEnd = sectionStart + fini.header().getSectionSize();

		if (symtab != null) {
			for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
				final SymbolTableEntry ste = symtab.getSymbolTableEntry(i);
				final long start = sectionStart + ste.value();

				if (ste.info().getType() == SymbolTableEntryType.STT_FUNC
						&& start >= sectionStart
						&& start < sectionEnd) {
					if (strtab != null) {
						logger.debug("Running destructor '%s' from .init", strtab.getString(ste.nameOffset()));
					} else {
						logger.debug("Running destructor from .init");
					}
					cpu.turnOn();
					runFrom(cpu, start);
				}
			}
		} else {
			// Can we just execute everything in .fini?
			logger.warning("Ignoring contents of .fini (for now)");
			// runFrom(cpu, sectionStart);
		}
	}

	private void runDtors() {
		notImplemented();
	}

	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	private void notImplemented() {
		throw new Error("Not implemented");
	}

	private void setupStack(final long stackTop, final long stackBottom) {
		final long stackSize = stackTop - stackBottom;
		logger.debug(
				"Setting stack size to %,d bytes (%.3f MiB) at 0x%016x-0x%016x",
				stackSize, stackSize / 1_048_576.0, stackBottom, stackTop - 1L);
		mem.setPermissions(stackBottom, stackSize, true, true, false);
		mem.initialize(stackBottom, stackSize, (byte) 0x00);
	}

	private void loadCommandLineArgumentsAndEnvironmentVariables(
			final ELF elf, final long stackBase, final boolean is32Bit, final String... commandLineArguments) {
		/*
		low address
		┌────────────────┐
		│      argc      │ ◄─── RSP
		├────────────────┤
		│     argv[0]    │ ───────────────┐
		├────────────────┤                │
		│     argv[1]    │ ───────────────┼─┐
		├────────────────┤                │ │
		│      ...       │                │ │
		├────────────────┤                │ │
		│      NULL      │ argv[argc]     │ │
		├────────────────┤                │ │
		│     envp[0]    │ ───────────────┼─┼─┐
		├────────────────┤                │ │ │
		│     envp[1]    │ ───────────────┼─┼─┼─┐
		├────────────────┤                │ │ │ │
		│      ...       │                │ │ │ │
		├────────────────┤                │ │ │ │
		│      NULL      │ envp[envc]     │ │ │ │
		├────────────────┤                │ │ │ │
		│ auxv[0].a_type │                │ │ │ │
		├────────────────┤                │ │ │ │
		│ auxv[0].a_val  │                │ │ │ │
		├────────────────┤                │ │ │ │
		│      ...       │                │ │ │ │
		├────────────────┤                │ │ │ │
		│    AT_NULL     │ auxv[n].a_type │ │ │ │
		├────────────────┤                │ │ │ │
		│"program_name\0"│ ◄──────────────┘ │ │ │
		├────────────────┤                  │ │ │
		│"arg1\0"        │ ◄────────────────┘ │ │
		├────────────────┤                    │ │
		│"VAR1=value1\0" │ ◄──────────────────┘ │
		├────────────────┤                      │
		│"VAR2=value2\0" │ ◄────────────────────┘
		├────────────────┤
		│...             │
		└────────────────┘
			high address

		Helper program: https://godbolt.org/z/z1ccPYrWd
		 */

		final long argc = commandLineArguments.length;

		if (argc == 0) {
			throw new AssertionError("Program name missing as first command-line argument.");
		}

		final Map<String, String> environmentVariables = System.getenv();
		final long envc = BitUtils.asLong(environmentVariables.size());

		final List<AuxiliaryEntry> auxv = getAuxiliaryVector(elf);
		final long numAuxvEntries = auxv.size();

		final long wordSize = is32Bit ? 4L : 8L;

		long stringsOffset = stackBase
				+ wordSize
						* (1 // argc
								+ commandLineArguments.length // argv
								+ 1 // NULL
								+ envc // envp
								+ 1 // NULL
								+ 2 * numAuxvEntries // auxv
								+ 2 // auxv[n]
						);

		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(true);
		final StringBuilder sb = new StringBuilder();

		if (is32Bit) {
			// TODO
			notImplemented();
		} else {
			wb.write(argc);
			for (final String arg : commandLineArguments) {
				wb.write(stringsOffset);
				sb.append(arg).append('\0');
				stringsOffset += (arg.length() + 1);
			}
			wb.write(0L); // argv[argc]

			for (final Map.Entry<String, String> env : environmentVariables.entrySet()) {
				wb.write(stringsOffset);
				sb.append(env.getKey()).append('=').append(env.getValue()).append('\0');
				stringsOffset += (env.getKey().length() + 1 + env.getValue().length() + 1);
			}
			wb.write(0L); // envp[envc]

			for (final AuxiliaryEntry ae : auxv) {
				wb.write(ae.type().getCode());
				wb.write(ae.value());
			}
			// auxv[n]
			wb.write(AuxiliaryEntryType.AT_NULL.getCode());
			wb.write(0L);

			// Align strings to wordSize
			while ((sb.length() % wordSize) != 0) {
				sb.append('\0');
			}

			// Dump strings at the end
			wb.write(sb.toString().getBytes(StandardCharsets.UTF_8));
		}

		final byte[] content = wb.array();

		if ((content.length % wordSize) != 0) {
			throw new AssertionError("Content on the stack is not word-aligned.");
		}

		mem.initialize(stackBase, content);

		// FIXME: setting the stack's permissions, again?
		mem.setPermissions(stackBase, content.length, true, true, false);
	}

	private List<AuxiliaryEntry> getAuxiliaryVector(final ELF elf) {
		final OSUtils os = OSUtils.INSTANCE;
		return List.of(
				new AuxiliaryEntry(AuxiliaryEntryType.AT_PHNUM, elf.getProgramHeaderTableLength()),
				new AuxiliaryEntry(
						AuxiliaryEntryType.AT_PHDR, elf.getFileHeader().programHeaderTableOffset()),
				new AuxiliaryEntry(
						AuxiliaryEntryType.AT_PHENT, elf.getFileHeader().programHeaderTableEntrySize()),
				new AuxiliaryEntry(AuxiliaryEntryType.AT_UID, os.getUserID()),
				new AuxiliaryEntry(AuxiliaryEntryType.AT_EUID, os.getEffectiveUserID()),
				new AuxiliaryEntry(AuxiliaryEntryType.AT_GID, os.getGroupID()),
				new AuxiliaryEntry(AuxiliaryEntryType.AT_EGID, os.getEffectiveGroupID()));
	}

	private void loadSegments(final ProgramHeaderTable pht, final long baseAddress) {
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

	private void loadSections(final SectionTable st, final long baseAddress) {
		logger.debug("Loading ELF sections into memory");
		for (int i = 0; i < st.getSectionTableLength(); i++) {
			final Section sec = st.getSection(i);
			if (!sec.header().getFlags().contains(SectionHeaderFlags.SHF_ALLOC)) {
				continue;
			}
			if (sec instanceof NoBitsSection || sec instanceof LoadableSection) {
				initializeSection(sec, baseAddress);
			}
		}
	}

	private void initializeSection(final Section sec, final long baseAddress) {
		Objects.requireNonNull(sec);

		switch (sec) {
			case NoBitsSection ignored -> {
				// allocate uninitialized data blocks
				final long startVirtualAddress = baseAddress + sec.header().getVirtualAddress();
				final long size = sec.header().getSectionSize();
				final int segmentIndex = findSegmentIndex(startVirtualAddress, size);
				logger.debug(
						"Loading section %s in memory segment %,d at range 0x%x-0x%x (%,d bytes)",
						sec.getName(), segmentIndex, startVirtualAddress, startVirtualAddress + size, size);
				mem.initialize(startVirtualAddress, size, (byte) 0x00);
			}
			case LoadableSection ls -> {
				final long startVirtualAddress = baseAddress + sec.header().getVirtualAddress();
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
						sec.header().getType().getName(),
						sec.header().getFlags().stream()
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
