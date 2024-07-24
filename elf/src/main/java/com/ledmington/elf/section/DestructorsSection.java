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

/** An ELF .fini_array section. */
public final class DestructorsSection implements LoadableSection {

    private final String name;
    private final SectionHeader header;
    private final int[] destructors;

    /**
     * Creates a DestructorsSection with the given name and the given header.
     *
     * @param name The name of this section.
     * @param sectionHeader The header of this section.
     * @param b The {@link ReadOnlyByteBuffer} to read data from.
     * @param dynamicSection The Dynamic section of the ELF file to retrieve the value of DT_FINI_ARRAYSZ from.
     */
    public DestructorsSection(
            final String name,
            final SectionHeader sectionHeader,
            final ReadOnlyByteBuffer b,
            final DynamicSection dynamicSection) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);

        if (dynamicSection == null) {
            this.destructors = new int[0];
            return;
        }

        int destructorsSizeInBytes = 0; // bytes
        {
            for (int i = 0; i < dynamicSection.getTableLength(); i++) {
                if (dynamicSection.getEntry(i).getTag() == DynamicTableEntryTag.DT_FINI_ARRAYSZ) {
                    destructorsSizeInBytes = (int) dynamicSection.getEntry(i).getContent();
                    break;
                }
            }
        }

        if (destructorsSizeInBytes % 4 != 0) {
            throw new IllegalArgumentException(String.format(
                    "Expected size of .fini_array section to be a multiple of 4 bytes but was %d (0x%x)",
                    destructorsSizeInBytes, destructorsSizeInBytes));
        }

        b.setPosition(sectionHeader.getFileOffset());
        b.setAlignment(sectionHeader.getAlignment());

        this.destructors = new int[destructorsSizeInBytes / 4];
        for (int i = 0; i < destructors.length; i++) {
            this.destructors[i] = b.read4();
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
        final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(destructors.length * 4);
        wb.write(destructors);
        return wb.array();
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + Arrays.hashCode(destructors);
        return h;
    }

    @Override
    public String toString() {
        return "DestructorsSection(name=" + name + ";header=" + header + ";destructors=" + Arrays.toString(destructors)
                + ")";
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
        final DestructorsSection ds = (DestructorsSection) other;
        return this.name.equals(ds.name)
                && this.header.equals(ds.header)
                && Arrays.equals(this.destructors, ds.destructors);
    }
}
