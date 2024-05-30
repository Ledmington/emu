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

/** Implementation of a "non-special" {@code .note*} ELF section. */
public final class BasicNoteSection implements NoteSection {

    private final String name;
    private final SectionHeader header;
    private final NoteSectionEntry[] entries;

    public BasicNoteSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);

        if (header.getEntrySize() != 0) {
            throw new IllegalArgumentException(String.format(
                    "The %s section doesn't have fixed-size entries but its header says they should be %,d bytes each",
                    name, header.getEntrySize()));
        }

        b.setPosition(sectionHeader.getFileOffset());

        final long expectedAlignment = is32Bit ? 4L : 8L;
        if (sectionHeader.getAlignment() != expectedAlignment) {
            throw new IllegalArgumentException(String.format(
                    "Invalid alignment: expected %,d but was %,d", expectedAlignment, sectionHeader.getAlignment()));
        }
        b.setAlignment(sectionHeader.getAlignment());

        this.entries = NoteSection.loadNoteSectionEntries(is32Bit, b, sectionHeader.getSectionSize());
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
        throw new Error("Not implemented");
    }

    @Override
    public NoteSectionEntry[] getEntries() {
        return Arrays.copyOf(entries, entries.length);
    }

    @Override
    public String toString() {
        return "BasicNoteSection(name=" + name + ";header=" + header + ";entries=" + Arrays.toString(entries) + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + Arrays.hashCode(entries);
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
        if (this.getClass().equals(other.getClass())) {
            return false;
        }
        final BasicNoteSection bns = (BasicNoteSection) other;
        return name.equals(bns.name) && header.equals(bns.header) && Arrays.equals(this.entries, bns.entries);
    }
}
