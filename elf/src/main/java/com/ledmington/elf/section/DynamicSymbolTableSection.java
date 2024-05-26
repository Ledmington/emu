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
package com.ledmington.elf.section;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class DynamicSymbolTableSection implements LoadableSection, SymbolTable {

    private final String name;
    private final SectionHeader header;
    private final boolean is32Bit;
    private final SymbolTableEntry[] symbolTable;

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
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public SymbolTableEntry[] getSymbolTable() {
        return Arrays.copyOf(symbolTable, symbolTable.length);
    }

    @Override
    public byte[] getContent() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(symbolTable.length * (is32Bit ? 16 : 24));
        for (final SymbolTableEntry ste : symbolTable) {
            if (is32Bit) {
                bb.write(ste.getNameOffset());
                bb.write(BitUtils.asInt(ste.getValue()));
                bb.write(BitUtils.asInt(ste.getSize()));
                bb.write(ste.getInfo().toByte());
                bb.write(ste.getVisibility().getCode());
                bb.write(ste.getSectionTableIndex());
            } else {
                bb.write(ste.getNameOffset());
                bb.write(ste.getInfo().toByte());
                bb.write(ste.getVisibility().getCode());
                bb.write(ste.getSectionTableIndex());
                bb.write(ste.getValue());
                bb.write(ste.getSize());
            }
        }
        return bb.array();
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + HashUtils.hash(is32Bit);
        h = 31 * h + Arrays.hashCode(symbolTable);
        return h;
    }

    @Override
    public String toString() {
        return "DynamicSection(name=" + name + ";header=" + header + ";is32Bit=" + is32Bit + ";symbolTable="
                + symbolTable + ")";
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final DynamicSymbolTableSection dsts = (DynamicSymbolTableSection) other;
        return this.name.equals(dsts.name)
                && this.header.equals(dsts.header)
                && this.is32Bit == dsts.is32Bit
                && Arrays.equals(this.symbolTable, dsts.symbolTable);
    }
}
