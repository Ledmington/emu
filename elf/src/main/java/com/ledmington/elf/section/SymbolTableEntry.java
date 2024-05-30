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

public final class SymbolTableEntry {

    private final int nameOffset; // relative to the start of the symbol string table
    private final short sectionTableIndex;
    private final long value;
    private final long size;
    private final SymbolTableEntryInfo info;
    private final SymbolTableEntryVisibility visibility;

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

    public int getNameOffset() {
        return nameOffset;
    }

    public short getSectionTableIndex() {
        return sectionTableIndex;
    }

    public long getValue() {
        return value;
    }

    public long getSize() {
        return size;
    }

    public SymbolTableEntryInfo getInfo() {
        return info;
    }

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
