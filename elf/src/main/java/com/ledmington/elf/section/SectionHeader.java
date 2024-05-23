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

/** This class is just a data holder. No check is performed in the constructor on the given data. */
public final class SectionHeader {

    private final int nameOffset;
    private final SectionHeaderType type;
    private final long flags;
    private final long virtualAddress;
    private final long fileOffset;
    private final long sectionSize;
    private final int linkedSectionIndex;
    private final int sh_info;
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
        this.flags = flags;
        this.virtualAddress = virtualAddress;
        this.fileOffset = fileOffset;
        this.sectionSize = sectionSize;
        this.linkedSectionIndex = linkedSectionIndex;
        this.sh_info = sh_info;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(1_000);
        sb.append("Name offset     : ")
                .append(String.format("%,d (0x%08x)\n", nameOffset, nameOffset))
                .append("Type            : ")
                .append(String.format("%s (%s)\n", type.getName(), type.getDescription()))
                .append("Flags           : ")
                .append(String.format("0x%016x ", flags));
        {
            for (final SectionHeaderFlags f : SectionHeaderFlags.values()) {
                if ((flags & f.getCode()) != 0L) {
                    sb.append(f.getId());
                }
            }
        }
        sb.append("\nVirtual address : ")
                .append(String.format("0x%016x\n", virtualAddress))
                .append("Offset on file  : ")
                .append(String.format("%,d (0x%016x)\n", fileOffset, fileOffset))
                .append("Size on file    : ")
                .append(String.format("%,d bytes\n", sectionSize))
                .append("linkedSectionIndex         : ")
                .append(String.format("%,d (0x%08x)\n", linkedSectionIndex, linkedSectionIndex))
                .append("sh_info         : ")
                .append(String.format("%,d (0x%08x)\n", sh_info, sh_info))
                .append("Alignment       : ")
                .append(alignment);
        if (alignment == 0 || alignment == 1) {
            sb.append(" (no alignment)");
        }
        sb.append("\nEntry size      : ").append(String.format("%,d bytes\n", entrySize));
        return sb.toString();
    }
}
