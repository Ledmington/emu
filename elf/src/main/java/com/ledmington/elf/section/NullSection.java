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

/** An ELF Null section. */
public final class NullSection implements Section {

    private final SectionHeader header;

    /**
     * Creates a Null section with the given section header entry.
     *
     * @param entry The section header entry corresponding to this section.
     */
    public NullSection(final SectionHeader sectionHeader) {
        this.header = Objects.requireNonNull(sectionHeader);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "NullSection(header=" + header + ")";
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
        return this.header.equals(((NullSection) other).header);
    }
}
