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
package com.ledmington.objdump;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.utils.MiniLogger;

/**
 * Copy of GNU's objdump utility. Original source code available <a href=
 * "https://github.com/bminor/binutils-gdb/blob/master/binutils/objdump.c">here</a>.
 */
public final class Main {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);

	private Main() {}

	/**
	 * Entry point.
	 *
	 * @param args The command-line arguments.
	 */
	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);

		Runtime.getRuntime().addShutdownHook(new Thread(out::flush));

		final CliOptions options = parseArgs(args);

		final ELF elf = ELFParser.parse(options.filename());

		out.println();
		out.printf("%s:     file format elf64-x86-64%n", options.filename());
		out.println();
		out.println();

		if (options.disassemble()) {
			disassembleExecutableSections(elf);
		}

		out.flush();
		System.exit(0);
	}

	/** The command-line options accepted by this program. */
	private record CliOptions(String filename, boolean disassemble) {}

	// FIXME: rewrite using package 'cmdline'
	private static CliOptions parseArgs(final String... args) {
		String filename = null;
		boolean disassemble = false;

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
					disassemble = true;
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

		return new CliOptions(filename, disassemble);
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private static void disassembleExecutableSections(final ELF elf) {
		final SymbolResolver.SymbolInfo symbols = SymbolResolver.resolveSymbols(elf);

		boolean isFirstSection = true;
		for (int i = 0; i < elf.getSectionTableLength(); i++) {
			final Section s = elf.getSection(i);
			if (!s.header().getFlags().contains(SectionHeaderFlags.SHT_EXECINSTR)) {
				continue;
			}

			if (!isFirstSection) {
				out.println();
			}

			try {
				Disassembler.disassembleSection(out, elf, i, symbols.functionNames(), symbols.allSymbols());
			} catch (final Throwable t) {
				// Ensure any output already produced for this section is flushed and visually terminated
				// before letting the failure propagate (and, eventually, crash the process).
				out.println();
				out.flush();
				throw t;
			}
			isFirstSection = false;
		}
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
