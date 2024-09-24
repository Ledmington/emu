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
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

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

public final class ELFView extends BorderPane {

    private record Range(int start, int end) {
        public Range {
            if (start > end) {
                throw new IllegalArgumentException(String.format("Invalid range [%d; %d]", start, end));
            }
        }

        public Range(final int singleByte) {
            this(singleByte, singleByte);
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
        // ('   ' + actual characters + '\n')
        final int suffixLength = 3 + nBytesPerRow + 1;
        // (... + actual bytes + spaces between blocks + ...)
        final int totalRowLength = prefixLength + nBytesPerRow * 2 + (nBlocksPerRow - 1) + suffixLength;
        // final int byteIdInRow = byteIndex % nBytesPerRow;
        final int byteIdInBlock = byteIndex % nBytesPerBlock;
        final int myRow = byteIndex / nBytesPerRow;
        final int myBlock = byteIndex / nBytesPerBlock;
        return myRow * totalRowLength // rows before
                + prefixLength // prefix of my row
                + myBlock * (nBytesPerBlock * 2) // bytes in blocks before mine
                + (myBlock - 1) // spaces between blocks
                + byteIdInBlock * 2 // bytes before me
                + 1;
    }

    private final BiFunction<String, Range, TreeItem<Label>> factory = (name, range) -> {
        final Label lbl = new Label(name);
        lbl.setOnMouseClicked(e -> {
            if (range.start() == range.end()) {
                final int pos = computeTextPosition(range.start());
                textArea.selectRange(pos, pos + 2);
                return;
            }

            textArea.selectRange(computeTextPosition(range.start()), computeTextPosition(range.end()));
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
    }

    public void loadFile(final File elfFile) {
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
        fileHeader
                .getChildren()
                .addAll(List.of(
                        factory.apply("Class = " + (fh.is32Bit() ? "32 bit" : "64 bit"), new Range(4)),
                        factory.apply(
                                "Endianness = " + (fh.isLittleEndian() ? "little-endian" : "big-endian"), new Range(5)),
                        factory.apply("OS/ABI = " + fh.getOSABI().getName(), new Range(7)),
                        factory.apply("ABI version = " + fh.getABIVersion(), new Range(8)),
                        factory.apply(
                                "File type = " + fh.getFileType().name().replaceFirst("^ET_", ""), new Range(9, 11)),
                        factory.apply("ISA = " + fh.getISA().getName(), new Range(11, 13)),
                        factory.apply("Version = " + fh.getVersion(), new Range(13, 17))));

        root.getChildren()
                .addAll(List.of(
                        fileHeader,
                        new TreeItem<>(new Label("Program Header Table")),
                        new TreeItem<>(new Label("Section Header Table"))));
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
