/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.FileHeader;
import com.ledmington.elf.FileType;
import com.ledmington.elf.ISA;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.sym.SymbolTableEntryType;
import com.ledmington.elf.section.sym.SymbolTableSection;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.PagedMemory;
import com.ledmington.utils.MiniLogger;

/** The emulator. */
public final class Emu {

	private static final MiniLogger logger = MiniLogger.getLogger("emu");

	private final ExecutionContext context;
	private ELF elf = null;
	private long entryPointVirtualAddress = 0L;
	private final ELFLoader loader; // TODO: should we place this inside ExecutionContext, too?

	/**
	 * Creates a new Emu instance with the given context and the given {@link ELFLoader}.
	 *
	 * @param context The execution context to be used for emulation.
	 * @param loader The loader to be used to initialize ELF files.
	 */
	public Emu(final ExecutionContext context, final ELFLoader loader) {
		this.context = Objects.requireNonNull(context, "Null context.");
		this.loader = Objects.requireNonNull(loader, "Null loader.");
	}

	/**
	 * Creates a new Emu instance with the given context and a default {@link ELFLoader}.
	 *
	 * @param context The execution context to be used for emulation.
	 */
	public Emu(final ExecutionContext context) {
		this(context, new ELFLoader(context.cpu(), (MemoryController) context.memory()));
	}

	/**
	 * Creates an ExecutionContext with the default values for checks and memory initialization taken from
	 * {@link EmulatorConstants}.
	 *
	 * @return A new ExecutionContext.
	 */
	public static ExecutionContext getDefaultExecutionContext() {
		final MemoryController mem = new MemoryController(
				new PagedMemory(EmulatorConstants.getMemoryInitializer()),
				EmulatorConstants.shouldBreakOnWrongPermissions(),
				EmulatorConstants.shouldBreakWhenReadingUninitializedMemory());
		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.checkInstructions(EmulatorConstants.shouldCheckInstruction())
				.build();
		return new ExecutionContext(cpu, mem);
	}

	/**
	 * The safest-but-slowest execution configuration, useful for debugging. Checks are performed on memory access
	 * permissions, on accessing uninitialized memory and on execution of invalid instructions.
	 *
	 * @return A new ExecutionContext.
	 */
	public static ExecutionContext getSafeExecutionContext() {
		final MemoryController mem =
				new MemoryController(new PagedMemory(EmulatorConstants.getMemoryInitializer()), true, true);
		final X86Cpu cpu = X86Cpu.builder().memory(mem).checkInstructions().build();
		return new ExecutionContext(cpu, mem);
	}

	/**
	 * The fast-but-unsafe execution configuration, useful for reducing emulation time. No checks are performed.
	 *
	 * @return A new ExecutionContext.
	 */
	public static ExecutionContext getFastExecutionContext() {
		final MemoryController mem =
				new MemoryController(new PagedMemory(EmulatorConstants.getMemoryInitializer()), false, false);
		final X86Cpu cpu = X86Cpu.builder().memory(mem).build();
		return new ExecutionContext(cpu, mem);
	}

	/**
	 * Loads, runs and unloads the given filename with the given arguments. This is the end-to-end workflow for
	 * emulation.
	 *
	 * @param filename The name of the executable file to emulate.
	 * @param commandLineArguments The arguments to be passed to the executable as if they were passed on the
	 *     command-line.
	 */
	public void loadRunAndUnload(final String filename, final String... commandLineArguments) {
		load(filename, commandLineArguments);
		run();
		unload();
	}

	/**
	 * Loads the given file into memory with the given command-line arguments.
	 *
	 * @param filename The name of the file to be loaded.
	 * @param commandLineArguments The arguments to be loaded as if they were passed on the command-line.
	 */
	public void load(final String filename, final String... commandLineArguments) {
		this.elf = ELFParser.parse(filename);
		logger.info("ELF file parsed successfully");

		final FileHeader fh = elf.getFileHeader();
		this.entryPointVirtualAddress = fh.entryPointVirtualAddress();

		final FileType type = fh.fileType();
		if (type != FileType.ET_EXEC && type != FileType.ET_DYN) {
			throw new IllegalArgumentException(
					String.format("Invalid ELF file type: expected ET_EXEC or ET_DYN but was %s.", type));
		}

		final ISA isa = fh.isa();
		if (isa != ISA.AMD_X86_64) {
			throw new IllegalArgumentException(
					String.format("This file requires ISA %s, which is not implemented.", isa.getName()));
		}

		// Add again the filename as first command-line argument
		final String[] args = Stream.concat(Stream.of(filename), Arrays.stream(commandLineArguments))
				.toList()
				.toArray(new String[0]);

		loader.load(
				elf,
				args,
				EmulatorConstants.getBaseAddress(),
				EmulatorConstants.getBaseStackAddress(),
				EmulatorConstants.getStackSize());

		this.context.cpu().setInstructionPointer(EmulatorConstants.getBaseAddress() + entryPointVirtualAddress);
	}

	/** Executes the already-loaded ELF file. */
	public void run() {
		this.context.cpu().turnOn();

		logger.info(" ### Execution start ### ");
		{
			final Optional<Section> sym = elf.getSectionByName(".symtab");
			final Optional<Section> str = elf.getSectionByName(".strtab");
			if (sym.isPresent() && str.isPresent()) {
				final SymbolTableSection symtab = (SymbolTableSection) sym.orElseThrow();
				final StringTableSection strtab = (StringTableSection) str.orElseThrow();
				final Optional<String> entryPointFunction = IntStream.range(0, symtab.getSymbolTableLength())
						.mapToObj(symtab::getSymbolTableEntry)
						.filter(ste -> ste.info().getType() == SymbolTableEntryType.STT_FUNC
								&& ste.value() == entryPointVirtualAddress)
						.map(ste -> strtab.getString(ste.nameOffset()))
						.findFirst();
				if (entryPointFunction.isPresent()) {
					logger.debug("Executing '%s'", entryPointFunction.orElseThrow());
				} else {
					logger.debug("No name for entrypoint function");
				}
			}
		}
		this.context.cpu().execute();
		logger.info(" ### Execution end ### ");
	}

	/** Unloads the file from memory, deallocating all the memory it used. */
	public void unload() {
		loader.unload(elf, EmulatorConstants.getBaseAddress());
	}
}
