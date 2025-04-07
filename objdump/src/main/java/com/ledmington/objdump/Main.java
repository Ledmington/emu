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
package com.ledmington.objdump;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionEncoder;
import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.sym.SymbolTableEntry;
import com.ledmington.elf.section.sym.SymbolTableEntryBinding;
import com.ledmington.elf.section.sym.SymbolTableEntryType;
import com.ledmington.elf.section.sym.SymbolTableEntryVisibility;
import com.ledmington.elf.section.sym.SymbolTableSection;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

/**
 * Copy of GNU's objdump utility. Original source code available <a href=
 * "https://github.com/bminor/binutils-gdb/blob/master/binutils/objdump.c">here</a>.
 */
public final class Main {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);

	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);

		Runtime.getRuntime().addShutdownHook(new Thread(out::flush));

		String filename = null;
		boolean disassembleExecutableSections = false;

		for (final String arg : args) {
			switch (arg) {
				case "-H", "--help":
					printHelp();
					out.flush();
					System.exit(0);
					break;
				case "-v", "--version":
					out.println("objdump v0.1.0");
					out.flush();
					System.exit(0);
					break;
				case "-d", "--disassemble":
					disassembleExecutableSections = true;
					break;
				default:
					if (arg.startsWith("-")) {
						printHelp();
						out.flush();
						System.exit(0);
					} else {
						filename = arg;
					}
					break;
			}
		}

		if (filename == null) {
			printHelp();
			out.flush();
			System.exit(0);
		}

		final ELF elf = ELFParser.parse(filename);

		out.println();
		out.printf("%s:     file format elf64-x86-64%n", filename);
		out.println();
		out.println();

		if (disassembleExecutableSections) {
			for (int i = 0; i < elf.getSectionTableLength(); i++) {
				final Section s = elf.getSection(i);
				if (!s.getHeader().getFlags().contains(SectionHeaderFlags.SHT_EXECINSTR)) {
					continue;
				}

				disassembleSection(elf, i);
			}
		}

		out.flush();
		System.exit(0);
	}

	private static void disassembleSection(final SectionTable st, final int sectionIndex) {
		final Section s = st.getSection(sectionIndex);
		out.printf("Disassembly of section %s:%n", s.getName());
		out.println();

		final long startOfSection = s.getHeader().getVirtualAddress();

		final Map<Long, String> functionNames = new HashMap<>();
		final Optional<Section> symbolTable = st.getSectionByName(".symtab");
		if (symbolTable.isPresent()) {
			final SymbolTableSection symtab = (SymbolTableSection) symbolTable.orElseThrow();
			final StringTableSection strtab =
					(StringTableSection) st.getSection(symtab.getHeader().getLinkedSectionIndex());

			for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
				final SymbolTableEntry ste = symtab.getSymbolTableEntry(i);
				final boolean isFunction = ste.info().getType() == SymbolTableEntryType.STT_FUNC;
				final boolean isGlobal = ste.info().getBinding() == SymbolTableEntryBinding.STB_GLOBAL;
				final boolean isHidden = ste.visibility() == SymbolTableEntryVisibility.STV_HIDDEN;
				final boolean isInThisSection = ste.sectionTableIndex() == sectionIndex;
				if (!(isFunction && (isGlobal || isHidden) && isInThisSection)) {
					continue;
				}
				functionNames.put(ste.value(), strtab.getString(ste.nameOffset()));

				if (strtab.getString(ste.nameOffset()).equals("__assert_fail_base")) {
					out.println(ste);
				}
			}
		}

		final boolean hasNoFunctions = functionNames.isEmpty();
		if (hasNoFunctions) {
			out.printf("%016x <%s>:%n", startOfSection, s.getName());
		}

		final byte[] content = ((LoadableSection) s).getLoadableContent();
		final ReadOnlyByteBuffer b = new ReadOnlyByteBufferV1(content, true, 1L);
		while (b.getPosition() < content.length) {
			if (!hasNoFunctions) {
				final long currentPosition = startOfSection + b.getPosition();
				if (functionNames.containsKey(currentPosition)) {
					if (b.getPosition() > 0L) {
						out.println();
					}
					out.printf("%016x <%s>:%n", currentPosition, functionNames.get(currentPosition));
				}
			}

			final long startOfInstruction = b.getPosition();
			final Instruction inst;
			try {
				inst = InstructionDecoder.fromHex(b);
			} catch (final Throwable t) {
				out.println();
				out.flush();
				throw t;
			}
			final long endOfInstruction = b.getPosition();
			final long lengthOfInstruction = endOfInstruction - startOfInstruction;
			out.printf("%8x:\t", startOfSection + startOfInstruction);
			for (int i = 0; i < 7; i++) {
				if (i < lengthOfInstruction) {
					out.printf("%02x ", content[BitUtils.asInt(startOfInstruction + i)]);
				} else {
					out.print("   ");
				}
			}
			out.printf("\t%s%n", InstructionEncoder.toIntelSyntax(inst));

			if (lengthOfInstruction >= 8L) {
				out.printf("%8x:\t", startOfSection + startOfInstruction + 7L);
				for (int i = 7; i < 14; i++) {
					if (i < lengthOfInstruction) {
						out.printf("%02x ", content[BitUtils.asInt(startOfInstruction + i)]);
					} else {
						break;
					}
				}
				out.println();
			}
		}

		out.println();
	}

	private static void printHelp() {
		out.print(String.join(
				"\n",
				"Usage: objdump <option(s)> <file(s)>",
				" Display information from object <file(s)>.",
				" At least one of the following switches must be given:",
				"  -d, --disassemble        Display assembler contents of executable sections",
				"  -v, --version            Display this program's version number",
				"  -H, --help               Display this information"));
	}
}
