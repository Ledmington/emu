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

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class SymbolTableSection implements SymbolTable {

    private final String name;
    private final SectionHeader header;
    private final SymbolTableEntry[] symbolTable;

    public SymbolTableSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);

        final long size = sectionHeader.getSectionSize();
        b.setPosition(sectionHeader.getFileOffset());
        final int symtabEntrySize = is32Bit ? 16 : 24;

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
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final SymbolTableSection sts = (SymbolTableSection) other;
        return this.name.equals(sts.name)
                && this.header.equals(sts.header)
                && Arrays.equals(this.symbolTable, sts.symbolTable);
    }
}
