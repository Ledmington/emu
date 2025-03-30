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

import java.util.Optional;
import java.util.stream.IntStream;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register64;
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
import com.ledmington.mem.RandomAccessMemory;
import com.ledmington.utils.MiniLogger;

public final class Emu {

	private static final MiniLogger logger = MiniLogger.getLogger("emu");

	private Emu() {}

	public static void run(final String filename, final String... commandLineArguments) {
		final ELF elf = ELFParser.parse(filename);
		logger.info("ELF file parsed successfully");

		final FileHeader fh = elf.getFileHeader();

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

		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(EmulatorConstants.getMemoryInitializer()));
		final X86Emulator cpu = new X86Cpu(mem);

		ELFLoader.load(
				elf,
				cpu,
				mem,
				commandLineArguments,
				EmulatorConstants.getBaseAddress(),
				EmulatorConstants.getStackSize(),
				EmulatorConstants.getBaseStackValue());

		cpu.turnOn();
		cpu.executeOne(new Instruction(
				Opcode.MOVABS,
				Register64.RIP,
				new Immediate(EmulatorConstants.getBaseAddress() + fh.entryPointVirtualAddress())));

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
								&& ste.value() == fh.entryPointVirtualAddress())
						.map(ste -> strtab.getString(ste.nameOffset()))
						.findFirst();
				if (entryPointFunction.isPresent()) {
					logger.debug("Executing '%s'", entryPointFunction.orElseThrow());
				} else {
					logger.debug("No name for entrypoint function");
				}
			}
		}
		cpu.execute();
		logger.info(" ### Execution end ### ");
		ELFLoader.unload(elf);
	}
}
