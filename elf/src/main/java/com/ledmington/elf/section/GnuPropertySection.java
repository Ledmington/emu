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

public final class GnuPropertySection implements NoteSection {

    private final SectionHeader header;
    private final NoteSectionEntry[] entries;

    public GnuPropertySection(final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        this.header = Objects.requireNonNull(sectionHeader);

        if (header.getEntrySize() != 0) {
            throw new IllegalArgumentException(String.format(
                    "The .note.gnu.property section doesn't have fixed-size entries but its header says they should be %,d bytes each",
                    header.getEntrySize()));
        }

        b.setPosition(sectionHeader.getFileOffset());
        this.entries = NoteSection.loadNoteSectionEntries(is32Bit, b, sectionHeader.getSectionSize());

        if (entries.length != 1) {
            throw new IllegalArgumentException(String.format(
                    "Invalid .note.gnu.property section: expected 1 note entry but found %,d: %s",
                    entries.length, Arrays.toString(entries)));
        }

        if (entries[0].name().length() != 4 || !"GNU\0".equals(entries[0].name())) {
            throw new IllegalArgumentException(String.format(
                    "Invalid owner for .note.gnu.property section: expected 'GNU' but was '%s'", entries[0].name()));
        }
    }

    @Override
    public String getName() {
        return ".note.gnu.property";
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getContent() {
        // final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(4 + 4 + 4 +
        // ownerBytes.length + descriptionBytes.length);
        // bb.write(ownerBytes.length);
        // bb.write(descriptionBytes.length);
        // bb.write(type);
        // bb.write(ownerBytes);
        // bb.write(descriptionBytes);
        // return bb.array();
        throw new Error("Not implemented");
    }

    @Override
    public String toString() {
        return "GnuPropertySection(header=" + header + ";entries=" + Arrays.toString(entries) + ")";
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
        final GnuPropertySection gps = (GnuPropertySection) other;
        return this.header.equals(gps.header) && Arrays.equals(this.entries, gps.entries);
    }
}
