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

public final class DynamicSymbolTableSection extends LoadableSection {

    private final boolean is32Bit;
    private final SymbolTableEntry[] symbolTable;

    public DynamicSymbolTableSection(
            final String name, final SectionHeader entry, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, entry);
        this.is32Bit = is32Bit;

        final int start = (int) entry.getFileOffset();
        final int size = (int) entry.getSectionSize();
        b.setPosition(start);
        final int symtabEntrySize = (int) entry.getEntrySize(); // 16 bytes for 32-bits, 24 bytes for 64-bits

        final int nEntries = size / symtabEntrySize;
        this.symbolTable = new SymbolTableEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
        }
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
}
