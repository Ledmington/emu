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

import com.ledmington.utils.HashUtils;

/** This class is just a data holder. No check is performed in the constructor on the given data. */
public final class SectionHeader {

    private final int nameOffset;
    private final SectionHeaderType type;
    private final SectionHeaderFlags[] flags;
    private final long virtualAddress;
    private final long fileOffset;
    private final long sectionSize;
    private final int linkedSectionIndex;
    private final int info;
    private final long alignment;
    private final long entrySize;

    public SectionHeader(
            int nameOffset,
            SectionHeaderType type,
            long flags,
            long virtualAddress,
            long fileOffset,
            long sectionSize,
            int linkedSectionIndex,
            int sh_info,
            long alignment,
            long entrySize) {
        this.nameOffset = nameOffset;
        this.type = type;
        this.flags = Objects.requireNonNull(SectionHeaderFlags.fromLong(flags));
        this.virtualAddress = virtualAddress;
        this.fileOffset = fileOffset;
        this.sectionSize = sectionSize;
        this.linkedSectionIndex = linkedSectionIndex;
        this.info = sh_info;
        this.alignment = alignment;
        this.entrySize = entrySize;
    }

    /**
     * Returns the name of the section. Its value is an index into the section header string table section, giving the
     * location of a null-terminated string.
     */
    public int getNameOffset() {
        return nameOffset;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    /**
     * Size in bytes of the section. This is the amount of space occupied in the file, except for SHT_NO_BITS sections.
     */
    public long getSectionSize() {
        return sectionSize;
    }

    public SectionHeaderType getType() {
        return type;
    }

    public SectionHeaderFlags[] getFlags() {
        return Arrays.copyOf(flags, flags.length);
    }

    /**
     * Returns the size in bytes of each entry. Returns 0 if the section does not hold a table of fixed-size entries.
     */
    public long getEntrySize() {
        return entrySize;
    }

    /**
     * Returns the virtual address of the beginning of the section in memory. If the section is not allocated to the
     * memory image of the program, this field should be zero.
     */
    public long getVirtualAddress() {
        return virtualAddress;
    }

    public long getAlignment() {
        return alignment;
    }

    public int getLinkedSectionIndex() {
        return linkedSectionIndex;
    }

    public int getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "SectionHeader(nameOffset=" + nameOffset + ";type=" + type + ";flags=" + Arrays.toString(flags)
                + ";virtualAddress="
                + virtualAddress + ";fileOffset=" + fileOffset + ";size="
                + sectionSize + ";linkedSectionIndex=" + linkedSectionIndex + ";info=" + info + ";alignment="
                + alignment + ";entrySize=" + entrySize + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + nameOffset;
        h = 31 * h + type.hashCode();
        h = 31 * h + Arrays.hashCode(flags);
        h = 31 * h + HashUtils.hash(virtualAddress);
        h = 31 * h + HashUtils.hash(fileOffset);
        h = 31 * h + HashUtils.hash(sectionSize);
        h = 31 * h + linkedSectionIndex;
        h = 31 * h + info;
        h = 31 * h + HashUtils.hash(alignment);
        h = 31 * h + HashUtils.hash(entrySize);
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
        final SectionHeader sh = (SectionHeader) other;
        return this.nameOffset == sh.nameOffset
                && this.type.equals(sh.type)
                && Arrays.equals(this.flags, sh.flags)
                && this.virtualAddress == sh.virtualAddress
                && this.fileOffset == sh.fileOffset
                && this.sectionSize == sh.sectionSize
                && this.linkedSectionIndex == sh.linkedSectionIndex
                && this.info == sh.info
                && this.alignment == sh.alignment
                && this.entrySize == sh.entrySize;
    }
}
