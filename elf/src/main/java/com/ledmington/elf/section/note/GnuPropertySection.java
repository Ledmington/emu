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
package com.ledmington.elf.section.note;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** The .note.gnu.property ELF section. */
public final class GnuPropertySection implements NoteSection {

    private final SectionHeader header;
    private final NoteSectionEntry[] entries;

    /**
     * Creates the .note.gnu.property ELF section with the given data.
     *
     * @param sectionHeader The header of this section.
     * @param b The ReadOnlyByteBuffer to read data from.
     * @param is32Bit Used for alignment.
     */
    public GnuPropertySection(final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        this.header = Objects.requireNonNull(sectionHeader);

        if (header.getEntrySize() != 0) {
            throw new IllegalArgumentException(String.format(
                    "The .note.gnu.property section doesn't have fixed-size entries but its header says they should be %,d bytes each",
                    header.getEntrySize()));
        }

        b.setPosition(sectionHeader.getFileOffset());
        b.setAlignment(sectionHeader.getAlignment());
        this.entries = NoteSection.loadNoteSectionEntries(is32Bit, b, sectionHeader.getSectionSize());

        final int expectedEntries = 1;
        if (entries.length != expectedEntries) {
            throw new IllegalArgumentException(String.format(
                    "Invalid .note.gnu.property section: expected %,d note entry but found %,d: %s",
                    expectedEntries, entries.length, Arrays.toString(entries)));
        }

        if (!"GNU".equals(entries[0].getName())) {
            throw new IllegalArgumentException(String.format(
                    "Invalid owner for .note.gnu.property section: expected 'GNU' but was '%s'", entries[0].getName()));
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
    public int getNumEntries() {
        return entries.length;
    }

    @Override
    public NoteSectionEntry getEntry(final int idx) {
        return entries[idx];
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
