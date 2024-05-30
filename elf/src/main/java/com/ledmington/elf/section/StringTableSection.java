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

public final class StringTableSection implements LoadableSection {

    private final String name;
    private final SectionHeader header;
    private final char[] table;

    public StringTableSection(final String name, final SectionHeader header, final ReadOnlyByteBuffer b) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(header);

        final int start = (int) header.getFileOffset();
        final int size = (int) header.getSectionSize();
        b.setPosition(start);
        this.table = new char[size];
        for (int i = 0; i < size; i++) {
            table[i] = (char) b.read1();
        }
    }

    public String getString(final int stringStartIndex) {
        final char nullChar = '\0';
        final StringBuilder sb = new StringBuilder();
        for (int i = stringStartIndex; i < table.length; i++) {
            if (table[i] == nullChar) {
                break;
            }
            sb.append(table[i]);
        }
        return sb.toString();
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
    public byte[] getContent() {
        final byte[] v = new byte[table.length];
        for (int i = 0; i < table.length; i++) {
            v[i] = (byte) table[i];
        }
        return v;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + Arrays.hashCode(table);
        return h;
    }

    @Override
    public String toString() {
        return "StringTableSection(name=" + name + ";header=" + header + ";table=" + new String(table) + ")";
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
        final StringTableSection sts = (StringTableSection) other;
        return this.name.equals(sts.name) && this.header.equals(sts.header) && Arrays.equals(this.table, sts.table);
    }
}
