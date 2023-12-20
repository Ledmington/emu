package com.ledmington.elf;

/**
 * This class is just a data holder.
 * No check is performed in the constructor on the given data.
 */
public final class SectionHeaderEntry {

    private final int nameOffset;
    private final SectionHeaderEntryType type;
    private final long flags;
    private final long virtualAddress;
    private final long size;
    private final int sh_link;
    private final int sh_info;
    private final long alignment;
    private final long entrySize;

    public SectionHeaderEntry(
            int nameOffset,
            SectionHeaderEntryType type,
            long flags,
            long virtualAddress,
            long size,
            int sh_link,
            int sh_info,
            long alignment,
            long entrySize) {
        this.nameOffset = nameOffset;
        this.type = type;
        this.flags = flags;
        this.virtualAddress = virtualAddress;
        this.size = size;
        this.sh_link = sh_link;
        this.sh_info = sh_info;
        this.alignment = alignment;
        this.entrySize = entrySize;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name offset     : ");
        sb.append(nameOffset);
        sb.append('\n');
        sb.append("Type            : ");
        sb.append(type.description());
        sb.append('\n');
        sb.append("Flags           : ");
        {
            boolean first = true;
            for (final SectionHeaderEntryFlags f : SectionHeaderEntryFlags.values()) {
                if ((flags & f.code()) != 0L) {
                    if (!first) {
                        sb.append('|');
                    }
                    sb.append(f.name());
                }
            }
        }
        sb.append('\n');
        sb.append("Virtual address : ");
        sb.append(String.format("0x%016x\n", virtualAddress));
        sb.append("Size            : ");
        sb.append(size);
        sb.append('\n');
        sb.append("sh_link         : ");
        sb.append(String.format("%,d (0x%08x)\n", sh_link, sh_link));
        sb.append("sh_info         : ");
        sb.append(String.format("%,d (0x%08x)\n", sh_info, sh_info));
        sb.append("Alignment       : ");
        sb.append(alignment);
        sb.append('\n');
        sb.append("Entry size      : ");
        sb.append(entrySize);
        sb.append('\n');
        return sb.toString();
    }
}
