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

    public long entrySize() {
        return entrySize;
    }

    public long virtualAddress() {
        return virtualAddress;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
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
        sb.append('\n')
                .append("Virtual address : ")
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
        sb.append('\n').append("Entry size      : ").append(String.format("%,d bytes\n", entrySize));
        return sb.toString();
    }
}
