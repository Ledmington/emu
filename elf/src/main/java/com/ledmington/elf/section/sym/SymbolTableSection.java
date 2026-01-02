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
package com.ledmington.elf.section.sym;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** An ELF symbol table section. */
public final class SymbolTableSection implements SymbolTable {

	private final String name;
	private final SectionHeader header;
	private final SymbolTableEntry[] symbolTable;

	/**
	 * Creates a symbol table section with the given data.
	 *
	 * @param name The name of this section.
	 * @param sectionHeader The header of this section.
	 * @param b The ReadOnlyByteBuffer to read data from.
	 * @param is32Bit Used for byte alignment.
	 */
	public SymbolTableSection(
			final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);

		final long size = sectionHeader.getSectionSize();
		final long symtabEntrySize = sectionHeader.getEntrySize();

		if (size % symtabEntrySize != 0) {
			throw new IllegalArgumentException(String.format(
					"Expected section size to be multiple of entry size (%d bytes) but was %d bytes",
					symtabEntrySize, size));
		}

		final long nEntries = size / symtabEntrySize;
		this.symbolTable = new SymbolTableEntry[(int) nEntries];
		b.setPosition(sectionHeader.getFileOffset());
		b.setAlignment(1L);
		for (int i = 0; i < nEntries; i++) {
			symbolTable[i] = SymbolTableEntry.read(b, is32Bit);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SectionHeader header() {
		return header;
	}

	@Override
	public int getSymbolTableLength() {
		return symbolTable.length;
	}

	@Override
	public SymbolTableEntry getSymbolTableEntry(final int idx) {
		return symbolTable[idx];
	}

	@Override
	public String toString() {
		return "SymbolTableSection(name=" + name + ";header=" + header + ";symbolTable=" + Arrays.toString(symbolTable)
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + Arrays.hashCode(symbolTable);
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final SymbolTableSection sts)) {
			return false;
		}
		return this.name.equals(sts.name)
				&& this.header.equals(sts.header)
				&& Arrays.equals(this.symbolTable, sts.symbolTable);
	}
}
