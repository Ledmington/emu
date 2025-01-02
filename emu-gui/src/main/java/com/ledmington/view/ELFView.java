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
package com.ledmington.view;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFParser;
import com.ledmington.elf.FileHeader;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.ProgramHeaderTable;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.BasicProgBitsSection;
import com.ledmington.elf.section.ConstructorsSection;
import com.ledmington.elf.section.DestructorsSection;
import com.ledmington.elf.section.DynamicSection;
import com.ledmington.elf.section.DynamicTableEntry;
import com.ledmington.elf.section.HashTableSection;
import com.ledmington.elf.section.InterpreterPathSection;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.elf.section.SectionHeaderType;
import com.ledmington.elf.section.StringTableSection;
import com.ledmington.elf.section.gnu.GnuHashSection;
import com.ledmington.elf.section.gnu.GnuVersionRequirementEntry;
import com.ledmington.elf.section.gnu.GnuVersionRequirementsSection;
import com.ledmington.elf.section.gnu.GnuVersionSection;
import com.ledmington.elf.section.note.NoteSection;
import com.ledmington.elf.section.note.NoteSectionEntry;
import com.ledmington.elf.section.rel.RelocationAddendSection;
import com.ledmington.elf.section.rel.RelocationSection;
import com.ledmington.elf.section.sym.SymbolTable;

public final class ELFView extends BorderPane {

	// TODO: move these into settings
	private static final int MAX_ROWS = 32;
	private static final int MAX_GROUPS_PER_ROW = 4;
	private static final int MAX_BYTES_PER_GROUP = 4;
	private static final int MAX_BYTES_PER_ROW = MAX_GROUPS_PER_ROW * MAX_BYTES_PER_GROUP;
	private static final int ADDRESS_BYTES = 4;

	private record Range(int offset, int length) {

		private static final int MINIMUM_ALLOWED_LENGTH = 1;

		public Range {
			if (offset < 0 || length < MINIMUM_ALLOWED_LENGTH) {
				throw new IllegalArgumentException(String.format("Invalid range [%d; %d+%d]", offset, offset, length));
			}
		}
	}

	private final TextArea addressArea = new TextArea();
	private final TextArea hexContentArea = new TextArea();
	private final TextArea asciiContentArea = new TextArea();
	private final TreeView<String> tree;
	private final Map<TreeItem<String>, Range> ranges = new HashMap<>();

	// The actual index of the first byte displayed
	private File file = null;
	private int startByte = 0;

	public ELFView() {
		final TreeItem<String> root = new TreeItem<>("<no file>");
		tree = new TreeView<>(root);
		tree.setEditable(false);
		tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedItem) -> {
			if (!ranges.containsKey(selectedItem)) {
				return;
			}

			final Range range = ranges.get(selectedItem);

			if (range.offset() > startByte + MAX_ROWS * MAX_BYTES_PER_ROW
					|| range.offset() + range.length() < startByte) {
				// If we cannot display the given range, we load another chunk from the file
				updateGrid(range.offset());
			}

			final int start = range.offset() - startByte;
			final int end = range.offset() + range.length() - startByte;

			final int startRowIndex = start / MAX_BYTES_PER_ROW;
			final int endRowIndex = end / MAX_BYTES_PER_ROW;

			addressArea.selectRange(
					startRowIndex * (2 + 2 * ADDRESS_BYTES + 1), (endRowIndex + 1) * (2 + 2 * ADDRESS_BYTES + 1));

			asciiContentArea.selectRange(
					startRowIndex * (MAX_BYTES_PER_ROW + 1) + (start % MAX_BYTES_PER_ROW),
					endRowIndex * (MAX_BYTES_PER_ROW + 1) + (end % MAX_BYTES_PER_ROW));

			final int rowLength = 2 * MAX_BYTES_PER_ROW + (MAX_GROUPS_PER_ROW - 1) + 1;
			final int groupLength = 2 * MAX_BYTES_PER_GROUP;
			final int startGroupIndexInRow = (start % MAX_BYTES_PER_ROW) / MAX_GROUPS_PER_ROW;
			final int startByteIndexInGroup = (start % MAX_BYTES_PER_ROW) % MAX_BYTES_PER_GROUP;
			final int endGroupIndexInRow = (end % MAX_BYTES_PER_ROW) / MAX_GROUPS_PER_ROW;
			final int endByteIndexInGroup = (end % MAX_BYTES_PER_ROW) % MAX_BYTES_PER_GROUP;
			final int startActualIndex = startRowIndex * rowLength
					+ startGroupIndexInRow * groupLength
					+ startGroupIndexInRow
					+ startByteIndexInGroup * 2;
			final int endActualIndex = endRowIndex * rowLength
					+ endGroupIndexInRow * groupLength
					+ endGroupIndexInRow
					+ endByteIndexInGroup * 2
					+ (endByteIndexInGroup == 0 ? -1 : 0);

			hexContentArea.selectRange(startActualIndex, endActualIndex);
		});
		this.setLeft(tree);

		final Font defaultFont = new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize());

		final GridPane grid = new GridPane(10, 2);

		addressArea.setEditable(false);
		addressArea.setFont(defaultFont);
		addressArea.setText(String.join("\n", Collections.nCopies(MAX_ROWS, "0x" + "0".repeat(2 * ADDRESS_BYTES))));
		grid.add(addressArea, 0, 0);

		hexContentArea.setEditable(false);
		hexContentArea.setFont(defaultFont);
		hexContentArea.setText(String.join(
				"\n",
				Collections.nCopies(
						MAX_ROWS,
						String.join(" ", Collections.nCopies(MAX_GROUPS_PER_ROW, "xx".repeat(MAX_BYTES_PER_GROUP))))));
		grid.add(hexContentArea, 1, 0);

		asciiContentArea.setEditable(false);
		asciiContentArea.setFont(defaultFont);
		asciiContentArea.setText(String.join("\n", Collections.nCopies(MAX_ROWS, ".".repeat(MAX_BYTES_PER_ROW))));
		grid.add(asciiContentArea, 2, 0);

		this.setRight(grid);
		this.setPadding(new Insets(5));
	}

	public void loadFile(final File elfFile) {
		ranges.clear();
		tree.getRoot().getChildren().clear();
		tree.getTreeItem(0).setValue(elfFile.getName());
		this.file = elfFile;

		final ELF elf = ELFParser.parse(elfFile.toPath().toString());
		initializeTreeView(elf);

		updateGrid(0);
	}

	private void initializeTreeView(final ELF elf) {
		final TreeItem<String> root = tree.getTreeItem(0);

		final TreeItem<String> fileHeader = new TreeItem<>("File Header");
		final FileHeader fh = elf.getFileHeader();
		fileHeader.getChildren().addAll(initializeFileHeader(fh));

		final TreeItem<String> PHTRoot = new TreeItem<>("Program Header Table");
		PHTRoot.getChildren().addAll(initializeProgramHeaderTable(fh, elf));

		final TreeItem<String> SHTRoot = new TreeItem<>("Section Table");
		SHTRoot.getChildren().addAll(initializeSectionTable(fh, elf));

		root.getChildren().addAll(List.of(fileHeader, PHTRoot, SHTRoot));
	}

	private TreeItem<String> getTreeItem(final String name, final int offset, final int length) {
		final Range range = new Range(offset, length);
		final TreeItem<String> ti = new TreeItem<>(name);
		ranges.put(ti, range);
		return ti;
	}

	private TreeItem<String> getTreeItem(final String name, final int offset) {
		return getTreeItem(name, offset, 1);
	}

	private List<TreeItem<String>> initializeFileHeader(final FileHeader fh) {
		final List<TreeItem<String>> fileHeaderElements = new ArrayList<>();
		final int wordSize = fh.is32Bit() ? 4 : 8;

		int i = 0;
		fileHeaderElements.add(getTreeItem("Signature = 0x7f454c46", i, 4));
		i += 4;
		fileHeaderElements.add(getTreeItem("Class = " + (fh.is32Bit() ? "32 bit" : "64 bit"), i));
		i++;
		fileHeaderElements.add(
				getTreeItem("Endianness = " + (fh.isLittleEndian() ? "little-endian" : "big-endian"), i));
		i++;
		fileHeaderElements.add(getTreeItem("Version = 1", i));
		i++;
		fileHeaderElements.add(getTreeItem("OS/ABI = " + fh.getOSABI().getName(), i));
		i++;
		fileHeaderElements.add(getTreeItem("ABI version = " + fh.getABIVersion(), i));
		i++;
		fileHeaderElements.add(
				getTreeItem("File type = " + fh.getFileType().name().replaceFirst("^ET_", ""), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("ISA = " + fh.getISA().getName(), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("Version = " + fh.getVersion(), i, 4));
		i += 4;
		fileHeaderElements.add(getTreeItem("Entrypoint = " + fh.getEntryPointVirtualAddress(), i, wordSize));
		i += wordSize;
		fileHeaderElements.add(getTreeItem("PHT offset = " + fh.getProgramHeaderTableOffset(), i, wordSize));
		i += wordSize;
		fileHeaderElements.add(getTreeItem("SHT offset = " + fh.getSectionHeaderTableOffset(), i, wordSize));
		i += wordSize;
		fileHeaderElements.add(getTreeItem("Flags = " + fh.getFlags(), i, 4));
		i += 4;
		fileHeaderElements.add(getTreeItem("Header size = " + fh.getHeaderSize(), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("PHT entry size = " + fh.getProgramHeaderTableEntrySize(), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("PHT entries = " + fh.getNumProgramHeaderTableEntries(), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("SHT entry size = " + fh.getSectionHeaderTableEntrySize(), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("SHT entries = " + fh.getNumSectionHeaderTableEntries(), i, 2));
		i += 2;
		fileHeaderElements.add(getTreeItem("shstrndx = " + fh.getSectionHeaderStringTableIndex(), i, 2));

		return fileHeaderElements;
	}

	private List<TreeItem<String>> initializeProgramHeaderTable(final FileHeader fh, final ProgramHeaderTable pht) {
		final List<TreeItem<String>> PHTElements = new ArrayList<>();
		final int wordSize = fh.is32Bit() ? 4 : 8;

		final long PHTOffset = fh.getProgramHeaderTableOffset();
		final long PHTEntrySize = fh.getProgramHeaderTableEntrySize();
		for (int i = 0; i < pht.getProgramHeaderTableLength(); i++) {
			final TreeItem<String> x =
					getTreeItem(String.valueOf(i), (int) (PHTOffset + i * PHTEntrySize), (int) PHTEntrySize);
			PHTElements.add(x);

			final PHTEntry phte = pht.getProgramHeader(i);
			final long entryOffset = PHTOffset + i * PHTEntrySize;
			int k = (int) entryOffset;
			x.getChildren().add(getTreeItem("Type = " + phte.getType().getName(), k, 4));
			k += 4;
			x.getChildren()
					.add(getTreeItem(
							"Flags = " + (phte.isReadable() ? "R" : " ") + (phte.isWriteable() ? "W" : " ")
									+ (phte.isExecutable() ? "X" : " "),
							k,
							4));
			k += 4;
			x.getChildren().add(getTreeItem("Segment offset = " + phte.getSegmentOffset(), k, wordSize));
			k += wordSize;
			x.getChildren().add(getTreeItem("Segment vaddr = " + phte.getSegmentVirtualAddress(), k, wordSize));
			k += wordSize;
			x.getChildren().add(getTreeItem("Segment paddr = " + phte.getSegmentPhysicalAddress(), k, wordSize));
			k += wordSize;
			x.getChildren().add(getTreeItem("Segment file size = " + phte.getSegmentFileSize(), k, wordSize));
			k += wordSize;
			x.getChildren().add(getTreeItem("Segment memory size = " + phte.getSegmentMemorySize(), k, wordSize));
			k += wordSize;
			x.getChildren().add(getTreeItem("Segment alignment = " + phte.getAlignment(), k, wordSize));
		}

		return PHTElements;
	}

	private List<TreeItem<String>> initializeSectionTable(final FileHeader fh, final SectionTable st) {
		final List<TreeItem<String>> STElements = new ArrayList<>();

		for (int i = 0; i < st.getSectionTableLength(); i++) {
			final Section s = st.getSection(i);
			final long sectionHeaderOffset =
					fh.getSectionHeaderTableOffset() + (long) i * fh.getSectionHeaderTableEntrySize();
			STElements.add(
					initializeSection(s, sectionHeaderOffset, fh.getSectionHeaderTableEntrySize(), fh.is32Bit()));
		}

		return STElements;
	}

	private TreeItem<String> initializeSection(
			final Section s,
			final long sectionHeaderOffset,
			final long sectionHeaderTableEntrySize,
			final boolean is32Bit) {
		final SectionHeader sh = s.getHeader();
		final int wordSize = is32Bit ? 4 : 8;
		final TreeItem<String> x = new TreeItem<>(s.getName().isEmpty() ? "(null)" : s.getName());
		final TreeItem<String> shRoot =
				getTreeItem("Section Header", (int) sectionHeaderOffset, (int) sectionHeaderTableEntrySize);
		x.getChildren().add(shRoot);

		int k = (int) sectionHeaderOffset;
		shRoot.getChildren().add(getTreeItem("Name offset = " + sh.getNameOffset(), k, 4));
		k += 4;
		shRoot.getChildren().add(getTreeItem("Type = " + sh.getType().getName(), k, 4));
		k += 4;
		shRoot.getChildren()
				.add(getTreeItem(
						"Flags = "
								+ (sh.getFlags().isEmpty()
										? "(none)"
										: sh.getFlags().stream()
												.map(SectionHeaderFlags::getName)
												.collect(Collectors.joining(", "))),
						k,
						wordSize));
		k += wordSize;
		shRoot.getChildren().add(getTreeItem("Vaddr = " + sh.getVirtualAddress(), k, wordSize));
		k += wordSize;
		shRoot.getChildren().add(getTreeItem("File offset = " + sh.getFileOffset(), k, wordSize));
		k += wordSize;
		shRoot.getChildren().add(getTreeItem("Section size = " + sh.getSectionSize(), k, wordSize));
		k += wordSize;
		shRoot.getChildren().add(getTreeItem("Linked section = " + sh.getLinkedSectionIndex(), k, 4));
		k += 4;
		shRoot.getChildren().add(getTreeItem("sh_info = " + sh.getInfo(), k, 4));
		k += 4;
		shRoot.getChildren().add(getTreeItem("Alignment = " + sh.getAlignment(), k, wordSize));
		k += wordSize;
		shRoot.getChildren().add(getTreeItem("Entry size = " + sh.getEntrySize(), k, wordSize));

		if (sh.getType() == SectionHeaderType.SHT_NULL
				|| sh.getType() == SectionHeaderType.SHT_NOBITS
				|| sh.getSectionSize() == 0L) {
			x.getChildren().add(new TreeItem<>("No Content"));
		} else {
			final TreeItem<String> root =
					getTreeItem("Section Content", (int) sh.getFileOffset(), (int) sh.getSectionSize());

			switch (s) {
				case InterpreterPathSection interp -> initializeInterpreterPathSection(root, interp);
				case BasicProgBitsSection pbs -> initializeProgBitsSection(root, pbs);
				case DynamicSection dyn -> initializeDynamicSection(root, dyn, wordSize);
				case NoteSection ns -> initializeNoteSection(root, ns);
				case SymbolTable symtab -> initializeSymbolTable(root, symtab, is32Bit);
				case StringTableSection strtab -> initializeStringTable(root, strtab);
				case RelocationSection rs -> initializeRelocationSection(root, rs, wordSize);
				case RelocationAddendSection ras -> initializeRelocationAddendSection(root, ras, wordSize);
				case ConstructorsSection cs -> initializeConstructorsSection(root, cs, wordSize);
				case DestructorsSection ds -> initializeDestructorsSection(root, ds, wordSize);
				case HashTableSection hts -> initializeHashTableSection(root, hts);
				case GnuVersionSection gvs -> initializeGnuVersionSection(root, gvs);
				case GnuVersionRequirementsSection gvrs -> initializeGnuVersionRequirementsSection(root, gvrs);
				case GnuHashSection ghs -> initializeGnuHashSection(root, ghs, wordSize);
				default -> throw new IllegalArgumentException(String.format(
						"Unknown section with type '%s' and name '%s'",
						sh.getType().getName(), s.getName()));
			}

			x.getChildren().add(root);
		}

		return x;
	}

	private void initializeInterpreterPathSection(final TreeItem<String> root, final InterpreterPathSection interp) {
		root.getChildren()
				.add(getTreeItem(
						"Interpreter Path",
						(int) interp.getHeader().getFileOffset(),
						interp.getInterpreterFilePath().length()));
	}

	private void initializeProgBitsSection(final TreeItem<String> root, final BasicProgBitsSection pbs) {
		root.getChildren()
				.add(getTreeItem(
						// TODO: change name
						"Content", (int) pbs.getHeader().getFileOffset(), (int)
								pbs.getHeader().getSectionSize()));
	}

	private void initializeDynamicSection(final TreeItem<String> root, final DynamicSection dyn, final int wordSize) {
		final int entrySize = 2 * wordSize;
		for (int i = 0; i < dyn.getTableLength(); i++) {
			final DynamicTableEntry dte = dyn.getEntry(i);
			final int entryStart = (int) dyn.getHeader().getFileOffset() + entrySize * i;
			final TreeItem<String> x = getTreeItem("#" + i + ": " + dte.getTag().getName(), entryStart, entrySize);
			x.getChildren().add(getTreeItem("tag = " + dte.getTag().getName(), entryStart, wordSize));
			x.getChildren().add(getTreeItem("content", entryStart + wordSize, wordSize));
			root.getChildren().add(x);
		}
	}

	private void initializeNoteSection(final TreeItem<String> root, final NoteSection ns) {
		int start = (int) ns.getHeader().getFileOffset();
		for (int i = 0; i < ns.getNumEntries(); i++) {
			final NoteSectionEntry nse = ns.getEntry(i);
			final int nameLength = nse.getName().length() + 1;
			final TreeItem<String> entry = getTreeItem("Entry " + i, start, nse.getAlignedSize());
			entry.getChildren().add(getTreeItem("namesz = " + nameLength, start, 4));
			entry.getChildren().add(getTreeItem("descsz = " + nse.getDescriptionLength(), start + 4, 4));
			entry.getChildren().add(getTreeItem("type = " + nse.getType().getDescription(), start + 4 + 4, 4));
			entry.getChildren().add(getTreeItem("name = " + nse.getName(), start + 4 + 4 + 4, nameLength));
			entry.getChildren().add(getTreeItem("desc", start + 4 + 4 + 4 + nameLength, nse.getDescriptionLength()));
			root.getChildren().add(entry);

			start += nse.getAlignedSize();
		}
	}

	private void initializeGnuHashSection(final TreeItem<String> root, final GnuHashSection ghs, final int wordSize) {
		final int start = (int) ghs.getHeader().getFileOffset();
		root.getChildren().add(getTreeItem("nbuckets = " + ghs.getBucketsLength(), start, 4));
		root.getChildren().add(getTreeItem("symndx = " + ghs.getSymbolTableIndex(), start + 4, 4));
		root.getChildren().add(getTreeItem("maskwords = " + ghs.getBloomFilterLength(), start + 4 + 4, 4));
		root.getChildren().add(getTreeItem("shift2 = " + ghs.getBloomShift(), start + 4 + 4 + 4, 4));

		final int startBloom = start + 4 + 4 + 4 + 4;
		final TreeItem<String> bloom = getTreeItem("Bloom Filter", startBloom, wordSize * ghs.getBloomFilterLength());
		for (int i = 0; i < ghs.getBloomFilterLength(); i++) {
			bloom.getChildren().add(getTreeItem("#" + i, startBloom + wordSize * i, wordSize));
		}
		root.getChildren().add(bloom);

		final int startBuckets = startBloom + wordSize * ghs.getBloomFilterLength();
		final TreeItem<String> buckets = getTreeItem("Buckets", startBuckets, 4 * ghs.getBucketsLength());
		for (int i = 0; i < ghs.getBucketsLength(); i++) {
			buckets.getChildren().add(getTreeItem("#" + i, startBuckets + 4 * i, 4));
		}
		root.getChildren().add(buckets);

		final int startChains = startBuckets + 4 * ghs.getChainsLength();
		final TreeItem<String> chains = getTreeItem("Chains", startChains, 4 * ghs.getChainsLength());
		for (int i = 0; i < ghs.getChainsLength(); i++) {
			chains.getChildren().add(getTreeItem("#" + i, startChains + 4 * i, 4));
		}
		root.getChildren().add(chains);
	}

	private void initializeHashTableSection(final TreeItem<String> root, final HashTableSection hts) {
		final int start = (int) hts.getHeader().getFileOffset();
		root.getChildren().add(getTreeItem("nbuckets = " + hts.getNumBuckets(), start, 4));
		root.getChildren().add(getTreeItem("nchain = " + hts.getNumChains(), start + 4, 4));

		final int startBuckets = start + 4 + 4;
		final TreeItem<String> buckets = getTreeItem("Buckets", startBuckets, 4 * hts.getNumBuckets());
		for (int i = 0; i < hts.getNumBuckets(); i++) {
			buckets.getChildren().add(getTreeItem("#" + i, startBuckets + 4 * i, 4));
		}
		root.getChildren().add(buckets);

		final int startChains = start + 4 + 4 + 4 * hts.getNumBuckets();
		final TreeItem<String> chains = getTreeItem("Chains", startChains, 4 * hts.getNumChains());
		for (int i = 0; i < hts.getNumChains(); i++) {
			chains.getChildren().add(getTreeItem("#" + i, startChains + 4 * i, 4));
		}
		root.getChildren().add(chains);
	}

	private void initializeSymbolTable(final TreeItem<String> root, final SymbolTable symtab, final boolean is32Bit) {
		final int start = (int) symtab.getHeader().getFileOffset();
		final int wordSize = is32Bit ? 4 : 8;
		final int entrySize = is32Bit ? (4 + 4 + 4 + 1 + 1 + 2) : (4 + 1 + 1 + 2 + 8 + 8);
		for (int i = 0; i < symtab.getSymbolTableLength(); i++) {
			final int entryStart = start + entrySize * i;
			final TreeItem<String> x = getTreeItem("#" + i, entryStart, entrySize);

			x.getChildren().add(getTreeItem("name", entryStart, 4));
			x.getChildren()
					.add(getTreeItem("value", is32Bit ? (entryStart + 4) : (entryStart + 4 + 1 + 1 + 2), wordSize));
			x.getChildren()
					.add(getTreeItem(
							"size", is32Bit ? (entryStart + 4 + 4) : (entryStart + 4 + 1 + 1 + 2 + 8), wordSize));
			x.getChildren().add(getTreeItem("info", is32Bit ? (entryStart + 4 + 4 + 4) : (entryStart + 4)));
			x.getChildren()
					.add(getTreeItem("visibility", is32Bit ? (entryStart + 4 + 4 + 4 + 1) : (entryStart + 4 + 1)));
			x.getChildren()
					.add(getTreeItem(
							"stidx", is32Bit ? (entryStart + 4 + 4 + 4 + 1 + 1) : (entryStart + 4 + 1 + 1), 2));

			root.getChildren().add(x);
		}
	}

	private void initializeStringTable(final TreeItem<String> root, final StringTableSection strtab) {
		final int start = (int) strtab.getHeader().getFileOffset();
		int i = 0;
		while (i < strtab.getHeader().getSectionSize()) {
			final String s = strtab.getString(i);
			if (!s.isEmpty()) {
				root.getChildren().add(getTreeItem(s, start + i, s.length()));
			}
			i += s.length() + 1;
		}
	}

	private void initializeConstructorsSection(
			final TreeItem<String> root, final ConstructorsSection cs, final int wordSize) {
		if (cs.getNumConstructors() == 0) {
			root.getChildren().add(new TreeItem<>("(no constructors)"));
			return;
		}

		for (int i = 0; i < cs.getNumConstructors(); i++) {
			root.getChildren().add(getTreeItem("#" + i, (int) cs.getHeader().getFileOffset() + wordSize * i, wordSize));
		}
	}

	private void initializeDestructorsSection(
			final TreeItem<String> root, final DestructorsSection ds, final int wordSize) {
		if (ds.getNumDestructors() == 0) {
			root.getChildren().add(new TreeItem<>("(no destructors)"));
			return;
		}

		for (int i = 0; i < ds.getNumDestructors(); i++) {
			root.getChildren().add(getTreeItem("#" + i, (int) ds.getHeader().getFileOffset() + wordSize * i, wordSize));
		}
	}

	private void initializeGnuVersionSection(final TreeItem<String> root, final GnuVersionSection gvs) {
		if (gvs.getVersionsLength() == 0) {
			root.getChildren().add(new TreeItem<>("(no versions)"));
			return;
		}

		for (int i = 0; i < gvs.getVersionsLength(); i++) {
			root.getChildren().add(getTreeItem("#" + i, (int) gvs.getHeader().getFileOffset() + 2 * i, 2));
		}
	}

	private void initializeGnuVersionRequirementsSection(
			final TreeItem<String> root, final GnuVersionRequirementsSection gvrs) {
		if (gvrs.getRequirementsLength() == 0) {
			root.getChildren().add(new TreeItem<>("(no version requirements)"));
			return;
		}

		int entryStart = (int) gvrs.getHeader().getFileOffset();
		for (int i = 0; i < gvrs.getRequirementsLength(); i++) {
			final GnuVersionRequirementEntry gvre = gvrs.getEntry(i);
			root.getChildren().add(initializeGnuVersionRequirementEntry(gvre, "#" + i, entryStart));
			entryStart += gvrs.getEntry(i).getNextOffset();
		}
	}

	private TreeItem<String> initializeGnuVersionRequirementEntry(
			final GnuVersionRequirementEntry gvre, final String name, final int entryStart) {
		final TreeItem<String> x = new TreeItem<>(name);

		x.getChildren().add(getTreeItem("version", entryStart, 2));
		x.getChildren().add(getTreeItem("count", entryStart + 2, 2));
		x.getChildren().add(getTreeItem("file offset", entryStart + 2 + 2, 4));
		x.getChildren().add(getTreeItem("aux offset", entryStart + 2 + 2 + 4, 4));
		x.getChildren().add(getTreeItem("next offset", entryStart + 2 + 2 + 4 + 4, 4));

		final TreeItem<String> aux = new TreeItem<>("Auxiliary");
		int auxStart = entryStart + gvre.getAuxOffset();
		for (int j = 0; j < gvre.getAuxiliaryLength(); j++) {
			final TreeItem<String> y = getTreeItem("#" + j, auxStart, 4 + 2 + 2 + 4 + 4);
			y.getChildren().add(getTreeItem("vna_hash", auxStart, 4));
			y.getChildren().add(getTreeItem("vna_flags", auxStart + 4, 2));
			y.getChildren().add(getTreeItem("vna_other", auxStart + 4 + 2, 2));
			y.getChildren().add(getTreeItem("vna_name", auxStart + 4 + 2 + 2, 4));
			y.getChildren().add(getTreeItem("vna_next", auxStart + 4 + 2 + 2 + 4, 4));
			aux.getChildren().add(y);

			auxStart += gvre.getAuxiliary(j).nextOffset();
		}
		x.getChildren().add(aux);

		return x;
	}

	private void initializeRelocationSection(
			final TreeItem<String> root, final RelocationSection rs, final int wordSize) {
		if (rs.getNumRelocationEntries() == 0) {
			root.getChildren().add(new TreeItem<>("(no relocations)"));
			return;
		}

		final int start = (int) rs.getHeader().getFileOffset();
		final int entrySize = 2 * wordSize;
		for (int i = 0; i < rs.getNumRelocationEntries(); i++) {
			final int entryStart = start + entrySize * i;

			final TreeItem<String> x = getTreeItem(" #" + i, entryStart, entrySize);
			x.getChildren().add(getTreeItem("offset", entryStart, wordSize));
			x.getChildren().add(getTreeItem("info", entryStart + wordSize, wordSize));
			root.getChildren().add(x);
		}
	}

	private void initializeRelocationAddendSection(
			final TreeItem<String> root, final RelocationAddendSection ras, final int wordSize) {
		if (ras.getRelocationAddendTableLength() == 0) {
			root.getChildren().add(new TreeItem<>("(no relocations)"));
			return;
		}

		final int start = (int) ras.getHeader().getFileOffset();
		final int entrySize = 3 * wordSize;
		for (int i = 0; i < ras.getRelocationAddendTableLength(); i++) {
			final int entryStart = start + entrySize * i;
			final TreeItem<String> x = getTreeItem("#" + i, entryStart, entrySize);

			x.getChildren().add(getTreeItem("offset", entryStart, wordSize));
			x.getChildren().add(getTreeItem("info", entryStart + wordSize, wordSize));
			x.getChildren().add(getTreeItem("addend", entryStart + wordSize + wordSize, wordSize));

			root.getChildren().add(x);
		}
	}

	private void updateGrid(final int startByte) {
		if (startByte < 0) {
			throw new IllegalArgumentException(
					String.format("Negative start byte: %,d (0x%08x)", startByte, startByte));
		}

		this.startByte = startByte;

		// we only care about the first MAX_ROWS * MAX_GROUPS_PER_ROW *
		// MAX_BYTES_PER_GROUP bytes starting from 'startByte'
		final int nBytes = Math.min(MAX_ROWS * MAX_BYTES_PER_ROW, ((int) this.file.length()) - startByte);

		final byte[] fileBytes = new byte[nBytes];
		try (final RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
			raf.skipBytes(startByte);
			raf.readFully(fileBytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final StringBuilder sbAddress = new StringBuilder();
		final StringBuilder sbHex = new StringBuilder();
		final StringBuilder sbAscii = new StringBuilder();

		for (int row = 0; row < MAX_ROWS; row++) {
			if (row * MAX_BYTES_PER_ROW >= fileBytes.length) {
				break;
			}

			sbAddress.append(
					String.format("0x%0" + (2 * ADDRESS_BYTES) + "x", (long) (startByte + row * MAX_BYTES_PER_ROW)));

			for (int group = 0; group < MAX_GROUPS_PER_ROW; group++) {
				for (int b = 0; b < MAX_BYTES_PER_GROUP; b++) {
					final int byteIndex = row * MAX_BYTES_PER_ROW + group * MAX_BYTES_PER_GROUP + b;

					if (byteIndex >= fileBytes.length) {
						break;
					}

					sbHex.append(String.format("%02x", fileBytes[byteIndex]));
					if (isAsciiPrintable(fileBytes[byteIndex])) {
						sbAscii.append((char) fileBytes[byteIndex]);
					} else {
						sbAscii.append('.');
					}
				}

				if (group < MAX_GROUPS_PER_ROW - 1) {
					sbHex.append(' ');
				}
			}

			if (row < MAX_ROWS - 1) {
				sbAddress.append('\n');
				sbHex.append('\n');
				sbAscii.append('\n');
			}
		}

		addressArea.setText(sbAddress.toString());
		hexContentArea.setText(sbHex.toString());
		asciiContentArea.setText(sbAscii.toString());
	}

	private boolean isAsciiPrintable(final byte x) {
		return x >= 32 && x < 127;
	}
}
