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
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import com.ledmington.cpu.InstructionDecoder;
import com.ledmington.cpu.InstructionEncoder;
import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.gnu.GnuVersionRequirementsSection;
import com.ledmington.elf.section.gnu.GnuVersionSection;
import com.ledmington.elf.section.rel.RelocationAddendEntry;
import com.ledmington.elf.section.rel.RelocationAddendSection;
import com.ledmington.elf.section.sym.SymbolTable;
import com.ledmington.elf.section.sym.SymbolTableEntry;
import com.ledmington.elf.section.sym.SymbolTableEntryType;
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

	private Main() {}

	@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UseConcurrentHashMap"})
	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);

		Runtime.getRuntime().addShutdownHook(new Thread(out::flush));

		String filename = null;
		boolean disassembleExecutableSections = false;

		// FIXME: rewrite using package 'cmdline'
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
			final Map<Long, RelocatedSymbol> relocatedSymbols = findRelocatedSymbols(elf);
			final Map<Long, String> pltLabels = findPltLabels(elf, relocatedSymbols);

			final Map<Long, String> functionNames = new HashMap<>(findFunctionNames(elf));
			functionNames.putAll(pltLabels);

			final NavigableMap<Long, String> allSymbols = new TreeMap<>(findAllSymbols(elf));
			allSymbols.putAll(pltLabels);
			for (final Map.Entry<Long, RelocatedSymbol> e : relocatedSymbols.entrySet()) {
				allSymbols.put(e.getKey(), e.getValue().versionedName());
			}

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
					disassembleSection(elf, i, functionNames, allSymbols);
				} catch (final Throwable t) {
					out.println();
					out.flush();
					throw t;
				}
				isFirstSection = false;
			}
		}

		out.flush();
		System.exit(0);
	}

	@SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.NPathComplexity"})
	private static void disassembleSection(
			final SectionTable st,
			final int sectionIndex,
			final Map<Long, String> functionNames,
			final NavigableMap<Long, String> allSymbols) {
		final Section s = st.getSection(sectionIndex);
		out.printf("Disassembly of section %s:%n", s.getName());
		out.println();

		final long startOfSection = s.header().getVirtualAddress();

		if (!functionNames.containsKey(startOfSection)) {
			out.printf("%016x <%s>:%n", startOfSection, s.getName());
		}

		final byte[] content = ((LoadableSection) s).getLoadableContent();
		final ReadOnlyByteBuffer b = new ReadOnlyByteBufferV1(content, true, 1L);
		while (b.getPosition() < content.length) {
			final long currentPosition = startOfSection + b.getPosition();

			if (functionNames.containsKey(currentPosition)) {
				if (b.getPosition() > 0L) {
					out.println();
				}
				out.printf("%016x <%s>:%n", currentPosition, functionNames.get(currentPosition));
			}

			final long startOfInstruction = b.getPosition();
			final Instruction inst = InstructionDecoder.fromHex(b);
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

			out.print("\t");

			if (inst.opcode() == Opcode.BND_JMP) {
				// bnd jmps and LEAs need to print the address they point to
				final IndirectOperand io = (IndirectOperand) inst.firstOperand();
				final long displacement = io.getDisplacement();
				final long computedOffset = startOfSection + endOfInstruction + displacement;
				final long gotSectionAddress =
						st.getSectionByName(".got.plt").orElseThrow().header().getVirtualAddress();
				out.printf(
						"%s        # %x <_GLOBAL_OFFSET_TABLE_+0x%x>%n",
						InstructionEncoder.toIntelSyntax(inst, true, 6, true),
						computedOffset,
						computedOffset - gotSectionAddress);
			} else if (isJumpWithImmediate(inst)) {
				// conditional jumps and 'call' instructions need to be printed differently: instead of just the
				// immediate, we need to add it to the current IP and display the name of the symbol it points to.
				// An address-size-override prefix has no effect on these (there is no memory operand to address),
				// so GNU objdump shows it explicitly as a leading pseudo-prefix rather than silently dropping it.
				final String addressSizeOverride =
						content[BitUtils.asInt(startOfInstruction)] == (byte) 0x67 ? "addr32 " : "";
				final long jumpOffset = getAsLong((Immediate) inst.firstOperand());
				final long actualPointedAddress = startOfSection + endOfInstruction + jumpOffset;
				final String label = resolveAddressLabel(actualPointedAddress, allSymbols);
				final String mnemonicFormat = addressSizeOverride.isEmpty() ? "%-6s" : "%s";
				if (label == null) {
					out.printf(
							"%s" + mnemonicFormat + " %x%n",
							addressSizeOverride,
							inst.opcode().mnemonic(),
							actualPointedAddress);
				} else {
					out.printf(
							"%s" + mnemonicFormat + " %x <%s>%n",
							addressSizeOverride,
							inst.opcode().mnemonic(),
							actualPointedAddress,
							label);
				}
			} else if (isPaddingNopWithCsPrefix(inst)) {
				// GNU objdump displays no-op prefixes used purely for instruction-length padding (a CS segment
				// override, and any operand-size-override byte beyond the first one, which is the only one that
				// actually affects the operand size) as leading pseudo-prefix words instead of folding them into the
				// memory operand.
				int redundantOperandSizePrefixes = -1;
				for (long i = startOfInstruction;
						i < content.length && content[BitUtils.asInt(i)] == (byte) 0x66;
						i++) {
					redundantOperandSizePrefixes++;
				}
				out.print("data16 ".repeat(Math.max(0, redundantOperandSizePrefixes)));
				out.printf(
						"cs %s%n",
						InstructionEncoder.toIntelSyntax(inst, true, 0, true).replace("cs:", ""));
			} else if (hasNotrackPrefix(inst, content, startOfInstruction)) {
				// A DS segment override on an indirect jmp/call is the CET 'notrack' hint, not an actual segment
				// override (there is no memory operand to apply it to).
				out.printf("notrack %s%n", InstructionEncoder.toIntelSyntax(inst, true, 0, true));
			} else {
				// GNU objdump does not pad the mnemonic column when a legacy prefix (e.g. 'rep') is shown before it.
				final String base = InstructionEncoder.toIntelSyntax(inst, true, inst.hasPrefix() ? 0 : 6, true);
				final String ripComment = ripRelativeComment(inst, startOfSection, endOfInstruction, allSymbols);
				out.printf("%s%s%n", base, ripComment == null ? "" : ripComment);
			}

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
	}

	private static boolean isPaddingNopWithCsPrefix(final Instruction inst) {
		return inst.opcode() == Opcode.NOP
				&& inst.hasFirstOperand()
				&& inst.firstOperand() instanceof final IndirectOperand io
				&& io.hasSegment()
				&& io.getSegment() == SegmentRegister.CS;
	}

	private static boolean hasNotrackPrefix(
			final Instruction inst, final byte[] content, final long startOfInstruction) {
		return (inst.opcode() == Opcode.JMP || inst.opcode() == Opcode.CALL)
				&& !(inst.firstOperand() instanceof Immediate)
				&& content[BitUtils.asInt(startOfInstruction)] == (byte) 0x3e;
	}

	private static String ripRelativeComment(
			final Instruction inst,
			final long startOfSection,
			final long endOfInstruction,
			final NavigableMap<Long, String> allSymbols) {
		final IndirectOperand io = findRipRelativeOperand(inst);
		if (io == null) {
			return null;
		}
		final long target = startOfSection + endOfInstruction + io.getDisplacement();
		final String label = resolveAddressLabel(target, allSymbols);
		return label == null ? null : String.format("        # %x <%s>", target, label);
	}

	private static IndirectOperand findRipRelativeOperand(final Instruction inst) {
		if (inst.hasFirstOperand() && inst.firstOperand() instanceof final IndirectOperand io && isRipBase(io)) {
			return io;
		}
		if (inst.hasSecondOperand() && inst.secondOperand() instanceof final IndirectOperand io && isRipBase(io)) {
			return io;
		}
		if (inst.hasThirdOperand() && inst.thirdOperand() instanceof final IndirectOperand io && isRipBase(io)) {
			return io;
		}
		if (inst.hasFourthOperand() && inst.fourthOperand() instanceof final IndirectOperand io && isRipBase(io)) {
			return io;
		}
		return null;
	}

	private static boolean isRipBase(final IndirectOperand io) {
		return io.hasBase() && io.getBase() == Register64.RIP;
	}

	/**
	 * Resolves an address to a symbolic label the way GNU objdump does: an exact match is printed bare, otherwise the
	 * nearest preceding symbol is printed with a "+0xN" offset. Returns {@code null} if no preceding symbol exists.
	 */
	private static String resolveAddressLabel(final long address, final NavigableMap<Long, String> allSymbols) {
		final Map.Entry<Long, String> floor = allSymbols.floorEntry(address);
		if (floor == null) {
			return null;
		}
		final long offset = address - floor.getKey();
		return offset == 0L ? floor.getValue() : String.format("%s+0x%x", floor.getValue(), offset);
	}

	/**
	 * A symbol referenced through a relocation entry (e.g. a GOT slot), together with its bare name (used for
	 * '@plt'-style PLT stub labels) and its version-suffixed name (used for '# addr &lt;symbol&gt;' comments).
	 */
	private record RelocatedSymbol(String bareName, String versionedName) {}

	@SuppressWarnings("PMD.UseConcurrentHashMap")
	private static Map<Long, RelocatedSymbol> findRelocatedSymbols(final SectionTable st) {
		final Map<Long, RelocatedSymbol> result = new HashMap<>();
		final GnuVersionSection gvs = st.getSectionByName(GnuVersionSection.getStandardName())
				.map(GnuVersionSection.class::cast)
				.orElse(null);
		final GnuVersionRequirementsSection gvrs = st.getSectionByName(GnuVersionRequirementsSection.getStandardName())
				.map(GnuVersionRequirementsSection.class::cast)
				.orElse(null);

		for (int i = 0; i < st.getSectionTableLength(); i++) {
			if (!(st.getSection(i) instanceof final RelocationAddendSection ras)) {
				continue;
			}
			final int symtabIndex = ras.header().getLinkedSectionIndex();
			if (symtabIndex == 0) {
				continue;
			}
			final SymbolTable symtab = (SymbolTable) st.getSection(symtabIndex);
			final StringTableSection strtab =
					(StringTableSection) st.getSection(symtab.header().getLinkedSectionIndex());

			for (int j = 0; j < ras.getRelocationAddendTableLength(); j++) {
				final RelocationAddendEntry rae = ras.getRelocationAddendEntry(j);
				if (rae.symbolTableIndex() == 0) {
					continue;
				}
				final SymbolTableEntry ste = symtab.getSymbolTableEntry(rae.symbolTableIndex());
				if (ste.nameOffset() == 0) {
					continue;
				}
				final String bareName = strtab.getString(ste.nameOffset());
				final String suffix = versionSuffix(gvs, gvrs, strtab, rae.symbolTableIndex());
				result.put(rae.offset(), new RelocatedSymbol(bareName, bareName + suffix));
			}
		}
		return result;
	}

	private static String versionSuffix(
			final GnuVersionSection gvs,
			final GnuVersionRequirementsSection gvrs,
			final StringTableSection dynstr,
			final int dynsymIndex) {
		if (gvs == null) {
			return "";
		}
		final int masked = gvs.getVersion(dynsymIndex) & 0x7fff;
		if (masked <= 1 || gvrs == null) {
			return "@Base";
		}
		final int nameOffset = gvrs.getVersionNameOffset((short) masked);
		return nameOffset == -1 ? "@Base" : "@" + dynstr.getString(nameOffset);
	}

	/**
	 * Finds the '&lt;symbol@plt&gt;'-style labels of PLT-like sections (e.g. '.plt', '.plt.got', '.plt.sec') by
	 * correlating each stub's RIP-relative jump/call/push target with the GOT slot addresses touched by relocations.
	 */
	@SuppressWarnings("PMD.UseConcurrentHashMap")
	private static Map<Long, String> findPltLabels(
			final SectionTable st, final Map<Long, RelocatedSymbol> relocatedSymbols) {
		final Map<Long, String> labels = new HashMap<>();
		for (int i = 0; i < st.getSectionTableLength(); i++) {
			final Section s = st.getSection(i);
			if (!s.getName().startsWith(".plt") || !(s instanceof final LoadableSection ls)) {
				continue;
			}
			final long entrySize = s.header().getEntrySize();
			if (entrySize <= 0L) {
				continue;
			}
			final long sectionStart = s.header().getVirtualAddress();
			final byte[] content = ls.getLoadableContent();
			final ReadOnlyByteBuffer b = new ReadOnlyByteBufferV1(content, true, 1L);
			while (b.getPosition() < content.length) {
				final long instructionStart = b.getPosition();
				final Instruction inst;
				try {
					inst = InstructionDecoder.fromHex(b);
				} catch (final RuntimeException e) {
					break;
				}
				final long instructionEnd = b.getPosition();
				final IndirectOperand io = findRipRelativeOperand(inst);
				if (io == null) {
					continue;
				}
				final long target = sectionStart + instructionEnd + io.getDisplacement();
				final RelocatedSymbol rs = relocatedSymbols.get(target);
				if (rs != null) {
					final long stubStart = sectionStart + (instructionStart / entrySize) * entrySize;
					labels.put(stubStart, rs.bareName() + "@plt");
				}
			}
		}
		return labels;
	}

	@SuppressWarnings("PMD.UseConcurrentHashMap")
	private static NavigableMap<Long, String> findAllSymbols(final SectionTable st) {
		final NavigableMap<Long, String> symbols = new TreeMap<>();
		final Map<Long, Integer> bindingPriority = new HashMap<>();
		final Optional<Section> symbolTable = st.getSectionByName(".symtab");
		if (symbolTable.isPresent()) {
			final SymbolTableSection symtab = (SymbolTableSection) symbolTable.orElseThrow();
			final StringTableSection strtab =
					(StringTableSection) st.getSection(symtab.header().getLinkedSectionIndex());

			for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
				final SymbolTableEntry ste = symtab.getSymbolTableEntry(i);
				final SymbolTableEntryType type = ste.info().getType();
				final boolean isMeaningful = type != SymbolTableEntryType.STT_FILE
						&& type != SymbolTableEntryType.STT_SECTION
						&& ste.sectionTableIndex() != 0;
				if (!isMeaningful) {
					continue;
				}
				// When multiple symbols share the same address, GNU objdump prefers a typed symbol (OBJECT/FUNC/...)
				// over an untyped boundary marker (NOTYPE); among equally-typed symbols, the strongest binding wins
				// (GLOBAL over WEAK over LOCAL); among equally-typed, equally-bound symbols, the alphabetically
				// first name wins.
				final int priority = symbolPriority(ste);
				final String candidateName = strtab.getString(ste.nameOffset());
				final Integer existingPriority = bindingPriority.get(ste.value());
				final String existingName = symbols.get(ste.value());
				if (existingPriority == null
						|| priority > existingPriority
						|| (priority == existingPriority && candidateName.compareTo(existingName) < 0)) {
					symbols.put(ste.value(), candidateName);
					bindingPriority.put(ste.value(), priority);
				}
			}
		}
		return symbols;
	}

	private static int symbolPriority(final SymbolTableEntry ste) {
		final int typeRank = ste.info().getType() == SymbolTableEntryType.STT_NOTYPE ? 0 : 1;
		final int bindingRank =
				switch (ste.info().getBinding()) {
					case STB_GLOBAL -> 2;
					case STB_WEAK -> 1;
					case STB_LOCAL -> 0;
				};
		return typeRank * 10 + bindingRank;
	}

	private static long getAsLong(final Immediate imm) {
		return switch (imm.bits()) {
			case 8 -> imm.asByte();
			case 16 -> imm.asShort();
			case 32 -> imm.asInt();
			case 64 -> imm.asLong();
			default -> throw new IllegalArgumentException("Invalid immediate.");
		};
	}

	private static boolean isJumpWithImmediate(final Instruction inst) {
		return inst.hasFirstOperand()
				&& !inst.hasSecondOperand()
				&& (inst.opcode() == Opcode.JMP
						|| inst.opcode() == Opcode.JA
						|| inst.opcode() == Opcode.JAE
						|| inst.opcode() == Opcode.JB
						|| inst.opcode() == Opcode.JBE
						|| inst.opcode() == Opcode.JE
						|| inst.opcode() == Opcode.JG
						|| inst.opcode() == Opcode.JGE
						|| inst.opcode() == Opcode.JL
						|| inst.opcode() == Opcode.JLE
						|| inst.opcode() == Opcode.JNE
						|| inst.opcode() == Opcode.JNS
						|| inst.opcode() == Opcode.JO
						|| inst.opcode() == Opcode.JNO
						|| inst.opcode() == Opcode.JNP
						|| inst.opcode() == Opcode.JP
						|| inst.opcode() == Opcode.JRCXZ
						|| inst.opcode() == Opcode.JS
						|| inst.opcode() == Opcode.LOOP
						|| inst.opcode() == Opcode.LOOPE
						|| inst.opcode() == Opcode.LOOPNE
						|| inst.opcode() == Opcode.CALL)
				&& inst.firstOperand() instanceof Immediate;
	}

	@SuppressWarnings("PMD.UseConcurrentHashMap")
	private static Map<Long, String> findFunctionNames(final SectionTable st) {
		final Map<Long, String> functionNames = new HashMap<>();
		final Optional<Section> symbolTable = st.getSectionByName(".symtab");
		if (symbolTable.isPresent()) {
			final SymbolTableSection symtab = (SymbolTableSection) symbolTable.orElseThrow();
			final StringTableSection strtab =
					(StringTableSection) st.getSection(symtab.header().getLinkedSectionIndex());

			for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
				final SymbolTableEntry ste = symtab.getSymbolTableEntry(i);
				final SymbolTableEntryType type = ste.info().getType();
				final boolean isLabelWorthy =
						type == SymbolTableEntryType.STT_FUNC || type == SymbolTableEntryType.STT_NOTYPE;
				if (!isLabelWorthy) {
					continue;
				}
				functionNames.put(ste.value(), strtab.getString(ste.nameOffset()));
			}
		}
		return functionNames;
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
