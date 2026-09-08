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

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.gnu.GnuVersionRequirementsSection;
import com.ledmington.elf.section.gnu.GnuVersionSection;
import com.ledmington.elf.section.rel.RelocationAddendEntry;
import com.ledmington.elf.section.rel.RelocationAddendSection;
import com.ledmington.elf.section.sym.SymbolTable;
import com.ledmington.elf.section.sym.SymbolTableEntry;
import com.ledmington.elf.section.sym.SymbolTableEntryType;
import com.ledmington.elf.section.sym.SymbolTableSection;

/** Resolves the symbolic names (function labels, PLT stub labels, ...) used while disassembling an ELF file. */
@SuppressWarnings("PMD.UseConcurrentHashMap")
final class SymbolResolver {

	private SymbolResolver() {}

	/**
	 * The symbol names needed to label a disassembly listing.
	 *
	 * @param functionNames The names used to introduce a new function (e.g. {@code <foo>:}).
	 * @param allSymbols The names used to resolve any address (e.g. jump/call targets, RIP-relative operands) to the
	 *     nearest preceding symbol.
	 */
	/* default */ record SymbolInfo(Map<Long, String> functionNames, NavigableMap<Long, String> allSymbols) {}

	/**
	 * Builds the {@link SymbolInfo} used to label the disassembly of the given ELF file.
	 *
	 * @param st The ELF file's section table.
	 * @return The resolved symbol names.
	 */
	/* default */ static SymbolInfo resolveSymbols(final SectionTable st) {
		final Map<Long, RelocatedSymbol> relocatedSymbols = findRelocatedSymbols(st);
		final Map<Long, String> pltLabels = PltLabelResolver.findPltLabels(st, relocatedSymbols);

		final Map<Long, String> functionNames = new HashMap<>(findFunctionNames(st));
		functionNames.putAll(pltLabels);

		final NavigableMap<Long, String> allSymbols = new TreeMap<>(findAllSymbols(st));
		allSymbols.putAll(pltLabels);
		for (final Map.Entry<Long, RelocatedSymbol> e : relocatedSymbols.entrySet()) {
			allSymbols.put(e.getKey(), e.getValue().versionedName());
		}

		return new SymbolInfo(functionNames, allSymbols);
	}

	/**
	 * A symbol referenced through a relocation entry (e.g. a GOT slot), together with its bare name (used for
	 * '@plt'-style PLT stub labels) and its version-suffixed name (used for '# addr &lt;symbol&gt;' comments).
	 */
	/* default */ record RelocatedSymbol(String bareName, String versionedName) {}

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
				// A distinct RelocatedSymbol is unavoidably needed for every relocation entry found.
				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				final RelocatedSymbol relocatedSymbol = new RelocatedSymbol(bareName, bareName + suffix);
				result.put(rae.offset(), relocatedSymbol);
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

	private static NavigableMap<Long, String> findAllSymbols(final SectionTable st) {
		final NavigableMap<Long, String> symbols = new TreeMap<>();
		final Map<Long, Integer> bindingPriority = new HashMap<>();
		final Map<Long, Long> sizeAtAddress = new HashMap<>();
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
				// (GLOBAL over WEAK over LOCAL); among equally-typed, equally-bound symbols, the larger one wins
				// (e.g. '_r_debug_extended', which embeds a legacy 'r_debug' struct, over the plain '_r_debug'
				// alias at the same address); among equally-typed, equally-bound, equally-sized symbols, the
				// alphabetically first name wins.
				final int priority = symbolPriority(ste);
				final String candidateName = strtab.getString(ste.nameOffset());
				final Integer existingPriority = bindingPriority.get(ste.value());
				final String existingName = symbols.get(ste.value());
				final Long existingSize = sizeAtAddress.get(ste.value());
				if (existingPriority == null
						|| priority > existingPriority
						|| (priority == existingPriority && ste.size() > existingSize)
						|| (priority == existingPriority
								&& ste.size() == existingSize
								&& candidateName.compareTo(existingName) < 0)) {
					symbols.put(ste.value(), candidateName);
					sizeAtAddress.put(ste.value(), ste.size());
					bindingPriority.put(ste.value(), priority);
				}
			}
		}
		return symbols;
	}

	private static int symbolPriority(final SymbolTableEntry ste) {
		// GNU objdump prefers a concrete FUNC/OBJECT symbol over a GNU_IFUNC one at the same address (the
		// ifunc symbol names a resolver indirection, not the code actually at that address), and prefers
		// either over an untyped boundary marker (NOTYPE).
		final int typeRank =
				switch (ste.info().getType()) {
					case STT_NOTYPE -> 0;
					case STT_GNU_IFUNC -> 1;
					default -> 2;
				};
		final int bindingRank =
				switch (ste.info().getBinding()) {
					case STB_GLOBAL -> 2;
					case STB_WEAK -> 1;
					case STB_LOCAL -> 0;
				};
		return typeRank * 10 + bindingRank;
	}

	private static Map<Long, String> findFunctionNames(final SectionTable st) {
		final Map<Long, String> functionNames = new HashMap<>();
		final Map<Long, Integer> bindingPriority = new HashMap<>();
		final Map<Long, Long> sizeAtAddress = new HashMap<>();
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
				// Same tie-break as findAllSymbols: when multiple symbols share an address, prefer the
				// strongest binding, then the larger symbol, then the alphabetically first name.
				final int priority = symbolPriority(ste);
				final String candidateName = strtab.getString(ste.nameOffset());
				final Integer existingPriority = bindingPriority.get(ste.value());
				final String existingName = functionNames.get(ste.value());
				final Long existingSize = sizeAtAddress.get(ste.value());
				if (existingPriority == null
						|| priority > existingPriority
						|| (priority == existingPriority && ste.size() > existingSize)
						|| (priority == existingPriority
								&& ste.size() == existingSize
								&& candidateName.compareTo(existingName) < 0)) {
					functionNames.put(ste.value(), candidateName);
					sizeAtAddress.put(ste.value(), ste.size());
					bindingPriority.put(ste.value(), priority);
				}
			}
		}
		return functionNames;
	}
}
