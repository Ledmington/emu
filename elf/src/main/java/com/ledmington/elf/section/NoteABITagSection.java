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

public final class NoteABITagSection implements NoteSection {

    private final SectionHeader header;
    private final NoteSectionEntry[] entries;

    public NoteABITagSection(final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        this.header = Objects.requireNonNull(sectionHeader);

        if (header.getEntrySize() != 0) {
            throw new IllegalArgumentException(String.format(
                    "The .note.ABI-tag section doesn't have fixed-size entries but its header says they should be %,d bytes each",
                    header.getEntrySize()));
        }

        b.setPosition(sectionHeader.getFileOffset());
        b.setAlignment(sectionHeader.getAlignment());
        this.entries = NoteSection.loadNoteSectionEntries(is32Bit, b, sectionHeader.getSectionSize());

        final int expectedEntries = 1;
        if (entries.length != expectedEntries) {
            throw new IllegalArgumentException(String.format(
                    "Invalid .note.ABI-tag section: expected %,d note entry but found %,d: %s",
                    expectedEntries, entries.length, Arrays.toString(entries)));
        }

        if (!"GNU\0".equals(entries[0].name())) {
            throw new IllegalArgumentException(String.format(
                    "Invalid owner for .note.ABI-tag section: expected 'GNU' but was '%s'", entries[0].name()));
        }
    }

    @Override
    public String getName() {
        return ".note.ABI-tag";
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public NoteSectionEntry[] getEntries() {
        return Arrays.copyOf(entries, entries.length);
    }

    @Override
    public String toString() {
        return "NoteABITagSection(header=" + header + ";entries=" + Arrays.toString(entries) + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
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
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final NoteABITagSection nats = (NoteABITagSection) other;
        return this.header.equals(nats.header) && Arrays.equals(this.entries, nats.entries);
    }
}
