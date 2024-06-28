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

/** An ELF .init_array section. */
public final class ConstructorsSection implements LoadableSection {

    private final String name;
    private final SectionHeader header;
    private final long[] constructors; // function pointers

    /**
     * Creates a ConstructorsSection with the given name and header.
     *
     * @param name The name of this section.
     * @param sectionHeader The header of this section.
     * @param b The {@link ReadOnlyByteBuffer} to read data from.
     * @param dynamicSection The dynamic section of the current executable to retrieve the DT_INIT_ARRAYSZ entry.
     */
    public ConstructorsSection(
            final String name,
            final SectionHeader sectionHeader,
            final ReadOnlyByteBuffer b,
            final DynamicSection dynamicSection) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);

        int constructorsSizeInBytes = 0; // bytes
        {
            for (int i = 0; i < dynamicSection.getTableLength(); i++) {
                if (dynamicSection.getEntry(i).getTag() == DynamicTableEntryTag.DT_INIT_ARRAYSZ) {
                    constructorsSizeInBytes = (int) dynamicSection.getEntry(i).getContent();
                    break;
                }
            }
        }

        if (constructorsSizeInBytes % 8 != 0) {
            throw new IllegalArgumentException(String.format(
                    "Expected size of .init_array section to be a multiple of 8 bytes but was %d (0x%x)",
                    constructorsSizeInBytes, constructorsSizeInBytes));
        }

        b.setPosition(sectionHeader.getFileOffset());
        b.setAlignment(sectionHeader.getAlignment());

        this.constructors = new long[constructorsSizeInBytes / 8];
        for (int i = 0; i < constructors.length; i++) {
            this.constructors[i] = b.read8();
        }
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
    public byte[] getLoadableContent() {
        final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(constructors.length + 8);
        wb.write(constructors);
        return wb.array();
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + Arrays.hashCode(constructors);
        return h;
    }

    @Override
    public String toString() {
        return "ConstructorsSection(name=" + name + ";header=" + header + ";constructors="
                + Arrays.toString(constructors) + ")";
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
        final ConstructorsSection cs = (ConstructorsSection) other;
        return this.name.equals(cs.name)
                && this.header.equals(cs.header)
                && Arrays.equals(this.constructors, cs.constructors);
    }
}
