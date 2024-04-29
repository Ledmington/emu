package com.ledmington.elf.section;

/**
 * This class is just a data holder.
 * No check is performed in the constructor on the given data.
 */
public final class SectionHeader {

    private final int nameOffset;
    private final SectionHeaderType type;
    private final long flags;

    /**
     * Contains the virtual address of the beginning of the section in
     * memory. If the section is not allocated to the memory image of the
     * program, this field should be zero.
     */
    private final long virtualAddress;

    private final long fileOffset;

    /**
     * Size in bytes of the section.
     * This is the amount of space occupied in the file, except for SHT_NO_BITS
     * sections.
     */
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

    public int nameOffset() {
        return nameOffset;
    }

    public long fileOffset() {
        return fileOffset;
    }

    public long sectionSize() {
        return sectionSize;
    }

    public SectionHeaderType type() {
        return type;
    }

    public long alignment() {
        return alignment;
    }

    public long entrySize() {
        return entrySize;
    }

    public long virtualAddress() {
        return virtualAddress;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name offset     : ");
        sb.append(String.format("%,d (0x%08x)\n", nameOffset, nameOffset));
        sb.append("Type            : ");
        sb.append(String.format("%s (%s)\n", type.name(), type.description()));
        sb.append("Flags           : ");
        sb.append(String.format("0x%016x ", flags));
        {
            for (final SectionHeaderFlags f : SectionHeaderFlags.values()) {
                if ((flags & f.code()) != 0L) {
                    sb.append(f.id());
                }
            }
        }
        sb.append('\n');
        sb.append("Virtual address : ");
        sb.append(String.format("0x%016x\n", virtualAddress));
        sb.append("Offset on file  : ");
        sb.append(String.format("%,d (0x%016x)\n", fileOffset, fileOffset));
        sb.append("Size on file    : ");
        sb.append(String.format("%,d bytes\n", sectionSize));
        sb.append("linkedSectionIndex         : ");
        sb.append(String.format("%,d (0x%08x)\n", linkedSectionIndex, linkedSectionIndex));
        sb.append("sh_info         : ");
        sb.append(String.format("%,d (0x%08x)\n", sh_info, sh_info));
        sb.append("Alignment       : ");
        sb.append(alignment);
        if (alignment == 0 || alignment == 1) {
            sb.append(" (no alignment)");
        }
        sb.append('\n');
        sb.append("Entry size      : ");
        sb.append(String.format("%,d bytes\n", entrySize));
        return sb.toString();
    }
}
