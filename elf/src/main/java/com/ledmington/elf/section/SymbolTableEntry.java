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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** An entry of an ELF symbol table. */
public final class SymbolTableEntry {

    private final int nameOffset; // relative to the start of the symbol string table
    private final short sectionTableIndex;
    private final long value;
    private final long size;
    private final SymbolTableEntryInfo info;
    private final SymbolTableEntryVisibility visibility;

    /**
     * Creates a symbol table entry with the given data.
     *
     * @param b The ReadOnlyByteBuffer to read data from.
     * @param is32Bit Used for correct parsing.
     */
    public SymbolTableEntry(final ReadOnlyByteBuffer b, final boolean is32Bit) {
        if (is32Bit) {
            this.nameOffset = b.read4();
            this.value = BitUtils.asLong(b.read4());
            this.size = BitUtils.asLong(b.read4());
            this.info = SymbolTableEntryInfo.fromByte(b.read1());
            this.visibility = SymbolTableEntryVisibility.fromByte(b.read1());
            this.sectionTableIndex = b.read2();
        } else {
            this.nameOffset = b.read4();
            this.info = SymbolTableEntryInfo.fromByte(b.read1());
            this.visibility = SymbolTableEntryVisibility.fromByte(b.read1());
            this.sectionTableIndex = b.read2();
            this.value = b.read8();
            this.size = b.read8();
        }
    }

    /**
     * Returns the index in the ELF file's string table where the name of this symbol starts.
     *
     * @return An index for the string table.
     */
    public int getNameOffset() {
        return nameOffset;
    }

    /**
     * Returns the index of the section related to this symbol.
     *
     * @return The index of the related section.
     */
    public short getSectionTableIndex() {
        return sectionTableIndex;
    }

    /**
     * Returns the 64-bit value of this symbol: it has different meanings depending on the type of symbol.
     *
     * @return The 64-bit value of this symbol.
     */
    public long getValue() {
        return value;
    }

    /**
     * The size in bytes of the associated data object.
     *
     * @return The size in bytes of the associated data object.
     */
    public long getSize() {
        return size;
    }

    /**
     * The info of a symbol specifies the type and binding attributes.
     *
     * @return The info object of this symbol.
     */
    public SymbolTableEntryInfo getInfo() {
        return info;
    }

    /**
     * Returns the visibility object of this symbol.
     *
     * @return The visibility object of this symbol.
     */
    public SymbolTableEntryVisibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "SymbolTableEntry(nameoffset=" + nameOffset + ";sectiontableIndex=" + sectionTableIndex + ";value="
                + value + ";size=" + size + ";info=" + info + ";visibility=" + visibility + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + nameOffset;
        h = 31 * h + HashUtils.hash(sectionTableIndex);
        h = 31 * h + HashUtils.hash(value);
        h = 31 * h + HashUtils.hash(size);
        h = 31 * h + info.hashCode();
        h = 31 * h + visibility.hashCode();
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
        final SymbolTableEntry ste = (SymbolTableEntry) other;
        return this.nameOffset == ste.nameOffset
                && this.sectionTableIndex == ste.sectionTableIndex
                && this.value == ste.value
                && this.size == ste.size
                && this.info.equals(ste.info)
                && this.visibility.equals(ste.visibility);
    }
}
