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

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionEncoder;
import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.ReadOnlyByteBufferV1;

public final class Main {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);

	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);

		Runtime.getRuntime().addShutdownHook(new Thread(out::flush));

		String filename = null;
		boolean disassembleExecutableSections = false;

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			switch (arg) {
				case "-H", "--help":
					printHelp();
					out.flush();
					System.exit(0);
					break;
				case "-v", "--version":
					out.println("readelf v0.1.0");
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
		if (!disassembleExecutableSections) {
			printHelp();
			out.flush();
			System.exit(0);
		}

		final ELF elf = ELFParser.parse(filename);

		if (disassembleExecutableSections) {
			for (int i = 0; i < elf.getSectionTableLength(); i++) {
				final Section s = elf.getSection(i);
				if (!s.getHeader().getFlags().contains(SectionHeaderFlags.SHT_EXECINSTR)) {
					continue;
				}

				disassembleSection(s);
			}
		}

		out.flush();
		System.exit(0);
	}

	private static void disassembleSection(final Section s) {
		out.printf("Disassembly of section %s:%n", s.getName());
		out.println();

		final long startOfSection = s.getHeader().getFileOffset();
		out.printf("%016x <.init>:%n", startOfSection);

		final byte[] content = ((LoadableSection) s).getLoadableContent();
		final ReadOnlyByteBuffer b = new ReadOnlyByteBufferV1(content, true, 1L);
		while (b.getPosition() < content.length) {
			final long startOfInstruction = b.getPosition();
			final Instruction inst = InstructionDecoder.fromHex(b);
			final long endOfInstruction = b.getPosition();
			final long lengthOfInstruction = endOfInstruction - startOfInstruction;
			out.printf("%8x:      ", startOfSection + startOfInstruction);
			for (int i = 0; i < 8; i++) {
				if (i < lengthOfInstruction) {
					out.printf("%02x ", content[BitUtils.asInt(startOfInstruction + i)]);
				} else {
					out.print("   ");
				}
			}
			out.printf("%s%n", InstructionEncoder.toIntelSyntax(inst));
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
