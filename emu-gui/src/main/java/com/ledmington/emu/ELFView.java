/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.ledmington.elf.ELF;
import com.ledmington.elf.ELFReader;
import com.ledmington.elf.FileHeader;
import com.ledmington.elf.PHTEntry;
import com.ledmington.elf.ProgramHeaderTable;
import com.ledmington.elf.SectionTable;
import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.elf.section.SectionHeaderFlags;
import com.ledmington.utils.MiniLogger;

public final class ELFView extends BorderPane {

	private static final MiniLogger logger = MiniLogger.getLogger("emu-gui");

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

		public Range(final int singleByte) {
			this(singleByte, MINIMUM_ALLOWED_LENGTH);
		}
	}

	private final Stage parent;
	private final TextArea addressArea = new TextArea();
	private final TextArea hexContentArea = new TextArea();
	private final TextArea asciiContentArea = new TextArea();
	private final TreeView<String> tree;
	private final Map<TreeItem<String>, Range> ranges = new HashMap<>();

	// The actual index of the first byte displayed
	private File file = null;
	private int startByte = 0;

	private final BiFunction<String, Range, TreeItem<String>> factory = (name, range) -> {
		final TreeItem<String> ti = new TreeItem<>(name);
		ranges.put(ti, range);
		return ti;
	};

	public ELFView(final Stage parent) {
		this.parent = Objects.requireNonNull(parent);

		final TreeItem<String> root = new TreeItem<>("<no file>");
		tree = new TreeView<>(root);
		tree.setEditable(false);
		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(
					final ObservableValue<? extends TreeItem<String>> observable,
					final TreeItem<String> oldValue,
					final TreeItem<String> selectedItem) {
				if (!ranges.containsKey(selectedItem)) {
					return;
				}

				final Range range = ranges.get(selectedItem);

				if (range.offset() > startByte + MAX_ROWS * MAX_BYTES_PER_ROW
						|| range.offset() + range.length() < startByte) {
					logger.debug(
							"Range [%,d; %,d] is outside the displayed range [%,d; %,d]",
							range.offset(),
							range.offset() + range.length(),
							startByte,
							startByte + MAX_ROWS * MAX_BYTES_PER_ROW);

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
			}
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
		parent.sizeToScene();
	}

	public void loadFile(final File elfFile) {
		ranges.clear();
		tree.getRoot().getChildren().clear();
		tree.getTreeItem(0).setValue(elfFile.getName());
		this.file = elfFile;

		final byte[] fileBytes;
		try {
			fileBytes = Files.readAllBytes(elfFile.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final ELF elf = ELFReader.read(fileBytes);
		initializeTreeView(elf);

		updateGrid(0);

		this.parent.sizeToScene();
	}

	private void initializeTreeView(final ELF elf) {
		final TreeItem<String> root = tree.getTreeItem(0);

		final TreeItem<String> fileHeader = new TreeItem<>("File Header");
		final FileHeader fh = elf.getFileHeader();
		fileHeader.getChildren().addAll(initializeFileHeader(fh));

		final TreeItem<String> PHTRoot = new TreeItem<>("Program Header Table");
		final ProgramHeaderTable pht = elf;
		PHTRoot.getChildren().addAll(initializeProgramHeaderTable(fh, pht));

		final TreeItem<String> SHTRoot = new TreeItem<>("Section Table");
		final SectionTable st = elf;
		SHTRoot.getChildren().addAll(initializeSectionTable(fh, st));

		root.getChildren().addAll(List.of(fileHeader, PHTRoot, SHTRoot));
	}

	private List<TreeItem<String>> initializeFileHeader(final FileHeader fh) {
		final List<TreeItem<String>> fileHeaderElements = new ArrayList<>();
		final int wordSize = fh.is32Bit() ? 4 : 8;

		int i = 0;
		fileHeaderElements.add(factory.apply("Signature = 0x7f454c46", new Range(i, 4)));
		i += 4;
		fileHeaderElements.add(factory.apply("Class = " + (fh.is32Bit() ? "32 bit" : "64 bit"), new Range(i)));
		i++;
		fileHeaderElements.add(
				factory.apply("Endianness = " + (fh.isLittleEndian() ? "little-endian" : "big-endian"), new Range(i)));
		i++;
		fileHeaderElements.add(factory.apply("Version = 1", new Range(i)));
		i++;
		fileHeaderElements.add(factory.apply("OS/ABI = " + fh.getOSABI().getName(), new Range(i)));
		i++;
		fileHeaderElements.add(factory.apply("ABI version = " + fh.getABIVersion(), new Range(i)));
		i++;
		fileHeaderElements.add(
				factory.apply("File type = " + fh.getFileType().name().replaceFirst("^ET_", ""), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(factory.apply("ISA = " + fh.getISA().getName(), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(factory.apply("Version = " + fh.getVersion(), new Range(i, 4)));
		i += 4;
		fileHeaderElements.add(
				factory.apply("Entrypoint = " + fh.getEntryPointVirtualAddress(), new Range(i, wordSize)));
		i += wordSize;
		fileHeaderElements.add(
				factory.apply("PHT offset = " + fh.getProgramHeaderTableOffset(), new Range(i, wordSize)));
		i += wordSize;
		fileHeaderElements.add(
				factory.apply("SHT offset = " + fh.getSectionHeaderTableOffset(), new Range(i, wordSize)));
		i += wordSize;
		fileHeaderElements.add(factory.apply("Flags = " + fh.getFlags(), new Range(i, 4)));
		i += 4;
		fileHeaderElements.add(factory.apply("Header size = " + fh.getHeaderSize(), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(
				factory.apply("PHT entry size = " + fh.getProgramHeaderTableEntrySize(), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(factory.apply("PHT entries = " + fh.getNumProgramHeaderTableEntries(), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(
				factory.apply("SHT entry size = " + fh.getSectionHeaderTableEntrySize(), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(factory.apply("SHT entries = " + fh.getNumSectionHeaderTableEntries(), new Range(i, 2)));
		i += 2;
		fileHeaderElements.add(factory.apply("shstrndx = " + fh.getSectionHeaderStringTableIndex(), new Range(i, 2)));

		return fileHeaderElements;
	}

	private List<TreeItem<String>> initializeProgramHeaderTable(final FileHeader fh, final ProgramHeaderTable pht) {
		final List<TreeItem<String>> PHTElements = new ArrayList<>();
		final int wordSize = fh.is32Bit() ? 4 : 8;

		final long PHTOffset = fh.getProgramHeaderTableOffset();
		final long PHTEntrySize = fh.getProgramHeaderTableEntrySize();
		for (int i = 0; i < pht.getProgramHeaderTableLength(); i++) {
			final TreeItem<String> x = factory.apply(
					String.valueOf(i), new Range((int) (PHTOffset + i * PHTEntrySize), (int) PHTEntrySize));
			PHTElements.add(x);

			final PHTEntry phte = pht.getProgramHeader(i);
			final long entryOffset = PHTOffset + i * PHTEntrySize;
			int k = (int) entryOffset;
			x.getChildren().add(factory.apply("Type = " + phte.getType().getName(), new Range(k, 4)));
			k += 4;
			x.getChildren()
					.add(factory.apply(
							"Flags = " + (phte.isReadable() ? "R" : " ") + (phte.isWriteable() ? "W" : " ")
									+ (phte.isExecutable() ? "X" : " "),
							new Range(k, 4)));
			k += 4;
			x.getChildren().add(factory.apply("Segment offset = " + phte.getSegmentOffset(), new Range(k, wordSize)));
			k += wordSize;
			x.getChildren()
					.add(factory.apply("Segment vaddr = " + phte.getSegmentVirtualAddress(), new Range(k, wordSize)));
			k += wordSize;
			x.getChildren()
					.add(factory.apply("Segment paddr = " + phte.getSegmentPhysicalAddress(), new Range(k, wordSize)));
			k += wordSize;
			x.getChildren()
					.add(factory.apply("Segment file size = " + phte.getSegmentFileSize(), new Range(k, wordSize)));
			k += wordSize;
			x.getChildren()
					.add(factory.apply("Segment memory size = " + phte.getSegmentMemorySize(), new Range(k, wordSize)));
			k += wordSize;
			x.getChildren().add(factory.apply("Segment alignment = " + phte.getAlignment(), new Range(k, wordSize)));
		}

		return PHTElements;
	}

	private List<TreeItem<String>> initializeSectionTable(final FileHeader fh, final SectionTable st) {
		final List<TreeItem<String>> STElements = new ArrayList<>();
		final int wordSize = fh.is32Bit() ? 4 : 8;

		for (int i = 0; i < st.getSectionTableLength(); i++) {
			final Section s = st.getSection(i);
			final SectionHeader sh = s.getHeader();
			final TreeItem<String> x = new TreeItem<>(s.getName().isEmpty() ? "(null)" : s.getName());
			STElements.add(x);

			final long sectionHeaderOffset =
					fh.getSectionHeaderTableOffset() + (long) i * fh.getSectionHeaderTableEntrySize();
			final TreeItem<String> shRoot = factory.apply(
					"Section Header", new Range((int) sectionHeaderOffset, fh.getSectionHeaderTableEntrySize()));
			x.getChildren().add(shRoot);
			{
				int k = (int) sectionHeaderOffset;
				shRoot.getChildren().add(factory.apply("Name offset = " + sh.getNameOffset(), new Range(k, 4)));
				k += 4;
				shRoot.getChildren().add(factory.apply("Type = " + sh.getType().getName(), new Range(k, 4)));
				k += 4;
				shRoot.getChildren()
						.add(factory.apply(
								"Flags = "
										+ (sh.getFlags().isEmpty()
												? "(none)"
												: sh.getFlags().stream()
														.map(SectionHeaderFlags::getName)
														.collect(Collectors.joining(", "))),
								new Range(k, wordSize)));
				k += wordSize;
				shRoot.getChildren().add(factory.apply("Vaddr = " + sh.getVirtualAddress(), new Range(k, wordSize)));
				k += wordSize;
				shRoot.getChildren().add(factory.apply("File offset = " + sh.getFileOffset(), new Range(k, wordSize)));
				k += wordSize;
				shRoot.getChildren()
						.add(factory.apply("Section size = " + sh.getSectionSize(), new Range(k, wordSize)));
				k += wordSize;
				shRoot.getChildren()
						.add(factory.apply("Linked section = " + sh.getLinkedSectionIndex(), new Range(k, 4)));
				k += 4;
				shRoot.getChildren().add(factory.apply("sh_info = " + sh.getInfo(), new Range(k, 4)));
				k += 4;
				shRoot.getChildren().add(factory.apply("Alignment = " + sh.getAlignment(), new Range(k, wordSize)));
				k += wordSize;
				shRoot.getChildren().add(factory.apply("Entry size = " + sh.getEntrySize(), new Range(k, wordSize)));
			}

			if (sh.getSectionSize() == 0L) {
				x.getChildren().add(new TreeItem<>("No Content"));
			} else {
				// TODO: add custom section content
				x.getChildren().add(factory.apply("Section Content", new Range((int) sh.getFileOffset(), (int)
						sh.getSectionSize())));
			}
		}

		return STElements;
	}

	private void updateGrid(final int startByte) {
		this.startByte = startByte;

		// we only care about the first MAX_ROWS * MAX_GROUPS_PER_ROW *
		// MAX_BYTES_PER_GROUP bytes
		final byte[] fileBytes = new byte[MAX_ROWS * MAX_BYTES_PER_ROW];
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
			sbAddress.append(String.format("0x%0" + (2 * ADDRESS_BYTES) + "x", (long)
					(startByte + row * MAX_GROUPS_PER_ROW * MAX_BYTES_PER_GROUP)));

			for (int group = 0; group < MAX_GROUPS_PER_ROW; group++) {
				for (int b = 0; b < MAX_BYTES_PER_GROUP; b++) {
					final int byteIndex =
							row * MAX_GROUPS_PER_ROW * MAX_BYTES_PER_GROUP + group * MAX_BYTES_PER_GROUP + b;
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
