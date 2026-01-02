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

import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** An ELF .dynsym section. */
public final class DynamicSymbolTableSection implements LoadableSection, SymbolTable {

	private final String name;
	private final SectionHeader header;
	private final boolean is32Bit;
	private final SymbolTableEntry[] symbolTable;

	/**
	 * Creates a DynamicSymbolTableSection with the given name and header by parsing bytes read from the
	 * ReadOnlyByteBuffer.
	 *
	 * @param name The name of this section.
	 * @param sectionHeader The header of this section.
	 * @param b The buffer to read bytes from.
	 * @param is32Bit Used for alignment.
	 */
	public DynamicSymbolTableSection(
			final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);
		this.is32Bit = is32Bit;

		final long size = sectionHeader.getSectionSize();
		b.setPosition(sectionHeader.getFileOffset());
		final long symtabEntrySize = sectionHeader.getEntrySize(); // 16 bytes for 32-bits, 24 bytes for 64-bits

		if (symtabEntrySize != (is32Bit ? 16 : 24)) {
			throw new IllegalArgumentException(String.format(
					"Wrong dynamic symbol table entry size: expected %,d but was %,d",
					is32Bit ? 16 : 24, symtabEntrySize));
		}

		final long nEntries = size / symtabEntrySize;
		this.symbolTable = new SymbolTableEntry[(int) nEntries];
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
	public byte[] getLoadableContent() {
		final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(symbolTable.length * (is32Bit ? 16 : 24));
		for (final SymbolTableEntry ste : symbolTable) {
			if (is32Bit) {
				bb.write(ste.nameOffset());
				bb.write(BitUtils.asInt(ste.value()));
				bb.write(BitUtils.asInt(ste.size()));
				bb.write(ste.info().toByte());
				bb.write(ste.visibility().getCode());
				bb.write(ste.sectionTableIndex());
			} else {
				bb.write(ste.nameOffset());
				bb.write(ste.info().toByte());
				bb.write(ste.visibility().getCode());
				bb.write(ste.sectionTableIndex());
				bb.write(ste.value());
				bb.write(ste.size());
			}
		}
		return bb.array();
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + Boolean.hashCode(is32Bit);
		h = 31 * h + Arrays.hashCode(symbolTable);
		return h;
	}

	@Override
	public String toString() {
		return "DynamicSection(name=" + name + ";header=" + header + ";is32Bit=" + is32Bit + ";symbolTable="
				+ Arrays.toString(symbolTable) + ")";
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final DynamicSymbolTableSection dsts)) {
			return false;
		}
		return this.name.equals(dsts.name)
				&& this.header.equals(dsts.header)
				&& this.is32Bit == dsts.is32Bit
				&& Arrays.equals(this.symbolTable, dsts.symbolTable);
	}
}
