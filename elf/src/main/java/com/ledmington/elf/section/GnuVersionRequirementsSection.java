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

import java.util.Objects;

/**
 * Reference <a href= "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.4.
 */
public final class GnuVersionRequirementsSection implements LoadableSection {

    private static final String standardName = ".gnu.version_r";

    public static String getStandardName() {
        return standardName;
    }

    private final SectionHeader header;

    public GnuVersionRequirementsSection(final SectionHeader sectionHeader) {
        this.header = Objects.requireNonNull(sectionHeader);
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
    public byte[] getContent() {
        throw new Error("Not implemented");
    }

    @Override
    public String toString() {
        return "GnuVersionRequirementsSection(header=" + header + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + header.hashCode();
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
        return this.header.equals(gvrs.header);
    }
}
