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

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class StringTableSection extends LoadableSection {

    private final char[] table;

    public StringTableSection(final String name, final SectionHeader entry, final ReadOnlyByteBuffer b) {
        super(name, entry);

        final int start = (int) entry.getFileOffset();
        final int size = (int) entry.getSectionSize();
        b.setPosition(start);
        this.table = new char[size];
        for (int i = 0; i < size; i++) {
            table[i] = (char) b.read1();
        }
    }

    @Override
    public byte[] getContent() {
        final byte[] v = new byte[table.length];
        for (int i = 0; i < table.length; i++) {
            v[i] = (byte) table[i];
        }
        return v;
    }
}
