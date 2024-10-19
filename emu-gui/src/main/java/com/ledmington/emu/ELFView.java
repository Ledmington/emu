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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
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

public final class ELFView extends BorderPane {

	private record Range(int offset, int length) {

		private static final int MINIMUM_ALLOWED_LENGTH = 1;

		public Range {
			if (length < MINIMUM_ALLOWED_LENGTH) {
				throw new IllegalArgumentException(String.format("Invalid range [%d; %d+%d]", offset, offset, length));
			}
		}

		public Range(final int singleByte) {
			this(singleByte, MINIMUM_ALLOWED_LENGTH);
		}
	}

	private final Stage parent;
	private final TextArea textArea = new TextArea();
	private final TreeView<Label> tree;
	private static final int nBytesPerBlock = 4;
	private static final int nBlocksPerRow = 4;
	private static final int nBytesPerRow = nBytesPerBlock * nBlocksPerRow;

	private static int computeTextPosition(final int byteIndex) {
		/*
		 * Here we can compute start and end of the range with a "closed" formula
		 * because we assume that the output is strictly formatted.
		 * If this is not the case, the formula needs to be rewritten.
		 */

		// counting all characters (space + '0x' + hex_address + ' : ')
		final int prefixLength = 1 + 2 + 8 + 3;
		// (' ' + actual characters + '\n')
		final int suffixLength = 3 + nBytesPerRow + 1;
		// (... + actual bytes + spaces between blocks + ...)
		final int totalRowLength = prefixLength + nBytesPerRow * 2 + (nBlocksPerRow - 1) + suffixLength;
		// final int byteIdInRow = byteIndex % nBytesPerRow;
		final int byteIdInBlock = byteIndex % nBytesPerBlock;
		final int myRow = byteIndex / nBytesPerRow;
		final int myBlock = byteIndex / nBytesPerBlock;
		final int blockIdInRow = myBlock % nBlocksPerRow;
		return myRow * totalRowLength // rows before
				+ prefixLength // prefix of my row
				+ blockIdInRow * (nBytesPerBlock * 2) // bytes in blocks before mine
				+ (blockIdInRow - 1) // spaces between blocks
				+ byteIdInBlock * 2 // bytes before me
		;
	}

	private final BiFunction<String, Range, TreeItem<Label>> factory = (name, range) -> {
		final Label lbl = new Label(name);
		lbl.setOnMouseClicked(e -> {
			if (range.length() == 1) {
				final int pos = computeTextPosition(range.offset());
				textArea.selectRange(pos, pos + 2);
				return;
			}

			textArea.selectRange(
					computeTextPosition(range.offset()), computeTextPosition(range.offset() + range.length()));
		});
		return new TreeItem<>(lbl);
	};

	public ELFView(final Stage parent) {
		this.parent = Objects.requireNonNull(parent);

		final TreeItem<Label> root = new TreeItem<>(new Label("<no file>"));
		tree = new TreeView<>(root);
		tree.setEditable(false);
		this.setLeft(tree);
		textArea.setEditable(false);
		this.setRight(textArea);
		this.setPadding(new Insets(5));
	}

	public void loadFile(final File elfFile) {
		tree.getRoot().getChildren().clear();
		tree.getTreeItem(0).setValue(new Label(elfFile.getName()));

		final byte[] fileBytes;
		try {
			fileBytes = Files.readAllBytes(elfFile.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final ELF elf = ELFReader.read(fileBytes);
		initializeTreeView(elf);
		initializeTextArea(fileBytes);
		this.parent.sizeToScene();
	}

	private void initializeTreeView(final ELF elf) {
		final TreeItem<Label> root = tree.getTreeItem(0);

		final TreeItem<Label> fileHeader = new TreeItem<>(new Label("File Header"));
		final FileHeader fh = elf.getFileHeader();
		final int wordSize = fh.is32Bit() ? 4 : 8;
		final List<TreeItem<Label>> fileHeaderElements = new ArrayList<>();
		{
			int i = 4;
			fileHeaderElements.add(factory.apply("Class = " + (fh.is32Bit() ? "32 bit" : "64 bit"), new Range(i)));
			i++;
			fileHeaderElements.add(factory.apply(
					"Endianness = " + (fh.isLittleEndian() ? "little-endian" : "big-endian"), new Range(i)));
			i += 2;
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
			fileHeaderElements.add(
					factory.apply("PHT entries = " + fh.getNumProgramHeaderTableEntries(), new Range(i, 2)));
			i += 2;
			fileHeaderElements.add(
					factory.apply("SHT entry size = " + fh.getSectionHeaderTableEntrySize(), new Range(i, 2)));
			i += 2;
			fileHeaderElements.add(
					factory.apply("SHT entries = " + fh.getNumSectionHeaderTableEntries(), new Range(i, 2)));
			i += 2;
			fileHeaderElements.add(
					factory.apply("shstrndx = " + fh.getSectionHeaderStringTableIndex(), new Range(i, 2)));
		}
		fileHeader.getChildren().addAll(fileHeaderElements);

		final TreeItem<Label> PHTRoot = new TreeItem<>(new Label("Program Header Table"));
		final ProgramHeaderTable pht = elf;
		final List<TreeItem<Label>> PHTElements = new ArrayList<>();
		{
			final long PHTOffset = fh.getProgramHeaderTableOffset();
			final long PHTEntrySize = fh.getProgramHeaderTableEntrySize();
			for (int i = 0; i < pht.getProgramHeaderTableLength(); i++) {
				final TreeItem<Label> x = factory.apply(
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
				x.getChildren()
						.add(factory.apply("Segment offset = " + phte.getSegmentOffset(), new Range(k, wordSize)));
				k += wordSize;
				x.getChildren()
						.add(factory.apply(
								"Segment vaddr = " + phte.getSegmentVirtualAddress(), new Range(k, wordSize)));
				k += wordSize;
				x.getChildren()
						.add(factory.apply(
								"Segment paddr = " + phte.getSegmentPhysicalAddress(), new Range(k, wordSize)));
				k += wordSize;
				x.getChildren()
						.add(factory.apply("Segment file size = " + phte.getSegmentFileSize(), new Range(k, wordSize)));
				k += wordSize;
				x.getChildren()
						.add(factory.apply(
								"Segment memory size = " + phte.getSegmentMemorySize(), new Range(k, wordSize)));
				k += wordSize;
				x.getChildren()
						.add(factory.apply("Segment alignment = " + phte.getAlignment(), new Range(k, wordSize)));
			}
		}
		PHTRoot.getChildren().addAll(PHTElements);

		final TreeItem<Label> SHTRoot = new TreeItem<>(new Label("Section Table"));
		final SectionTable st = elf;
		final List<TreeItem<Label>> STElements = new ArrayList<>();
		{
			for (int i = 0; i < st.getSectionTableLength(); i++) {
				final Section s = st.getSection(i);
				final SectionHeader sh = s.getHeader();
				final TreeItem<Label> x = new TreeItem<>(new Label(s.getName().isEmpty() ? "(null)" : s.getName()));
				STElements.add(x);

				final long sectionHeaderOffset =
						fh.getSectionHeaderTableOffset() + (long) i * fh.getSectionHeaderTableEntrySize();
				final TreeItem<Label> shRoot = factory.apply(
						"Section Header", new Range((int) sectionHeaderOffset, fh.getSectionHeaderTableEntrySize()));
				x.getChildren().add(shRoot);
				{
					int k = (int) sectionHeaderOffset;
					shRoot.getChildren().add(factory.apply("Name offset = " + sh.getNameOffset(), new Range(k, 4)));
					k += 4;
					shRoot.getChildren()
							.add(factory.apply("Type = " + sh.getType().getName(), new Range(k, 4)));
					k += 4;
					shRoot.getChildren()
							.add(factory.apply(
									"Flags = "
											+ sh.getFlags().stream()
													.map(SectionHeaderFlags::getName)
													.collect(Collectors.joining(", ")),
									new Range(k, wordSize)));
					k += wordSize;
					shRoot.getChildren()
							.add(factory.apply("Vaddr = " + sh.getVirtualAddress(), new Range(k, wordSize)));
					k += wordSize;
					shRoot.getChildren()
							.add(factory.apply("File offset = " + sh.getFileOffset(), new Range(k, wordSize)));
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
					shRoot.getChildren()
							.add(factory.apply("Entry size = " + sh.getEntrySize(), new Range(k, wordSize)));
				}

				if (sh.getSectionSize() == 0L) {
					x.getChildren().add(new TreeItem<>(new Label("Section Content")));
				} else {
					x.getChildren().add(factory.apply("Section Content", new Range((int) sh.getFileOffset(), (int)
							sh.getSectionSize())));
				}
			}
		}
		SHTRoot.getChildren().addAll(STElements);

		root.getChildren().addAll(List.of(fileHeader, PHTRoot, SHTRoot));
	}

	private void initializeTextArea(final byte[] fileBytes) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fileBytes.length; i++) {
			if (i % nBytesPerRow == 0) {
				// start of the row
				sb.append(" 0x").append(String.format("%08x", i)).append(" : ");
			}
			sb.append(String.format("%02x", fileBytes[i]));
			if (i % nBytesPerBlock == nBytesPerBlock - 1) {
				sb.append(' ');
			}
			if (i % nBytesPerRow == nBytesPerRow - 1) {
				sb.append("   ");
				final int startOfTheRow = i - (i % nBytesPerRow);
				for (int j = 0; j < nBytesPerRow; j++) {
					final byte x = fileBytes[startOfTheRow + j];
					if (isAsciiPrintable(x)) {
						sb.append((char) x);
					} else {
						sb.append('.');
					}
				}
				sb.append('\n');
			}
		}

		textArea.setFont(new Font(AppConstants.getDefaultMonospaceFont(), AppConstants.getDefaultFontSize()));
		this.textArea.setText(sb.toString());
	}

	private boolean isAsciiPrintable(final byte x) {
		return x >= 32 && x < 127;
	}
}
