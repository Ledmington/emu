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

public final class RelocationAddendSection extends LoadableSection {

    private final boolean is32Bit;
    private final RelocationAddendEntry[] relocationAddendTable;

    public RelocationAddendSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        this.is32Bit = is32Bit;
        b.setPosition((int) sectionHeader.getFileOffset());
        final int nEntries = (int) (sectionHeader.getSectionSize() / sectionHeader.getEntrySize());
        this.relocationAddendTable = new RelocationAddendEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            final long offset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            final long info = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            final long addend = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            this.relocationAddendTable[i] = new RelocationAddendEntry(offset, info, addend);
        }
    }

    @Override
    public byte[] getContent() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(relocationAddendTable.length * (is32Bit ? 12 : 24));
        for (final RelocationAddendEntry relocationAddendEntry : relocationAddendTable) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(relocationAddendEntry.offset()));
                bb.write(BitUtils.asInt(relocationAddendEntry.info()));
                bb.write(BitUtils.asInt(relocationAddendEntry.addend()));
            } else {
                bb.write(relocationAddendEntry.offset());
                bb.write(relocationAddendEntry.info());
                bb.write(relocationAddendEntry.addend());
            }
        }
        return bb.array();
    }
}
