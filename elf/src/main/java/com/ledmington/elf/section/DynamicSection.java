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
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class DynamicSection extends LoadableSection {

    private final boolean is32Bit;
    private final DynamicTableEntry[] dynamicTable;

    public DynamicSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        this.is32Bit = is32Bit;
        b.setPosition((int) sectionHeader.getFileOffset());
        final int entrySize = is32Bit ? 8 : 16;
        final int nEntries = (int) sectionHeader.getSectionSize() / entrySize;

        final DynamicTableEntry[] tmp = new DynamicTableEntry[nEntries];
        int i = 0;
        while (i < nEntries) {
            long tag = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            long content = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            tmp[i] = new DynamicTableEntry(tag, content);
            if (tmp[i].getTag().equals(DynamicTableEntryTag.DT_NULL)) {
                break;
            }
            i++;
        }

        // resize dynamic table
        dynamicTable = new DynamicTableEntry[i];
        System.arraycopy(tmp, 0, dynamicTable, 0, i);
    }

    @Override
    public byte[] getContent() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(dynamicTable.length * (is32Bit ? 8 : 16));
        for (final DynamicTableEntry dynamicTableEntry : dynamicTable) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(dynamicTableEntry.getTag().getCode()));
                bb.write(BitUtils.asInt(dynamicTableEntry.getContent()));
            } else {
                bb.write(dynamicTableEntry.getTag().getCode());
                bb.write(dynamicTableEntry.getContent());
            }
        }
        return bb.array();
    }
}
