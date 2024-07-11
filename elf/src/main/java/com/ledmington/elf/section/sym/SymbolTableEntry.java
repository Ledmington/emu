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
package com.ledmington.elf.section.sym;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

/**
 * An entry of an ELF symbol table.
 *
 * @param nameOffset The index in the ELF file's string table where the name of this symbol starts.
 * @param sectionTableIndex The index of the section related to this symbol.
 * @param value The 64-bit value of this symbol: it has different meanings depending on the type of symbol.
 * @param size The size in bytes of the associated data object.
 * @param info The info of a symbol specifies the type and binding attributes.
 * @param visibility The visibility object of this symbol.
 */
public record SymbolTableEntry(
        int nameOffset,
        short sectionTableIndex,
        long value,
        long size,
        SymbolTableEntryInfo info,
        SymbolTableEntryVisibility visibility) {

    /**
     * Reads a symbol table entry from the given buffer.
     *
     * @param b The {@link ReadOnlyByteBuffer} to read data from.
     * @param is32Bit Used for correct parsing.
     * @return A symbol table entry.
     */
    public static SymbolTableEntry read(final ReadOnlyByteBuffer b, final boolean is32Bit) {
        int name;
        long val;
        long sz;
        SymbolTableEntryVisibility vis;
        SymbolTableEntryInfo inf;
        short sti;
        if (is32Bit) {
            name = b.read4();
            val = BitUtils.asLong(b.read4());
            sz = BitUtils.asLong(b.read4());
            inf = SymbolTableEntryInfo.fromByte(b.read1());
            vis = SymbolTableEntryVisibility.fromByte(b.read1());
            sti = b.read2();
        } else {
            name = b.read4();
            inf = SymbolTableEntryInfo.fromByte(b.read1());
            vis = SymbolTableEntryVisibility.fromByte(b.read1());
            sti = b.read2();
            val = b.read8();
            sz = b.read8();
        }

        return new SymbolTableEntry(name, sti, val, sz, inf, vis);
    }
}
