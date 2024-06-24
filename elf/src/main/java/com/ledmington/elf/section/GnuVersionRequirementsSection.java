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
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * The .gnu.version_r ELF section.
 *
 * <p>Reference <a href= "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.4.
 */
public final class GnuVersionRequirementsSection implements LoadableSection {

    private static final String standardName = ".gnu.version_r";

    /**
     * Returns the standard name of this special section.
     *
     * @return The string ".gnu.version_r".
     */
    public static String getStandardName() {
        return standardName;
    }

    private final SectionHeader header;
    private final GnuVersionRequirementEntry[] entries;

    /**
     * Creates the GNU version requirements section with the given header.
     *
     * @param sectionHeader The header for this section.
     */
    public GnuVersionRequirementsSection(
            final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final DynamicSection dynamicSection) {
        this.header = Objects.requireNonNull(sectionHeader);

        int versionRequirementsEntryNum = 0;
        {
            for (int i = 0; i < dynamicSection.getTableLength(); i++) {
                if (dynamicSection.getEntry(i).getTag() == DynamicTableEntryTag.DT_VERNEEDNUM) {
                    versionRequirementsEntryNum =
                            (int) dynamicSection.getEntry(i).getContent();
                    break;
                }
            }
        }

        this.entries = new GnuVersionRequirementEntry[versionRequirementsEntryNum];
        for (int i = 0; i < this.entries.length; i++) {
            this.entries[i] = new GnuVersionRequirementEntry(b.read2(), b.read2(), b.read4(), b.read4(), b.read4());
        }
    }

    public int getRequirementsLength() {
        return entries.length;
    }

    public GnuVersionRequirementEntry getEntry(final int idx) {
        return entries[idx];
    }

    @Override
    public String getName() {
        return standardName;
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getLoadableContent() {
        final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1((2 + 2 + 4 + 4 + 4) * entries.length);
        for (final GnuVersionRequirementEntry gvre : entries) {
            wb.write(gvre.version());
            wb.write(gvre.count());
            wb.write(gvre.fileOffset());
            wb.write(gvre.auxOffset());
            wb.write(gvre.nextOffset());
        }
        return wb.array();
    }

    @Override
    public String toString() {
        return "GnuVersionRequirementsSection(header=" + header + ";entries=" + Arrays.toString(entries) + ")";
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
        final GnuVersionRequirementsSection gvrs = (GnuVersionRequirementsSection) other;
        return this.header.equals(gvrs.header) && Arrays.equals(this.entries, gvrs.entries);
    }
}
