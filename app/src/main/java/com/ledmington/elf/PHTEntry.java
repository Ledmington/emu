package com.ledmington.elf;

/**
 * This class is just a data holder.
 * No check is performed in the constructor on the given data.
 */
public final class PHTEntry {

    private final PHTEntryType type;
    private final int flags;
    private final long segmentOffset;
    private final long segmentVirtualAddress;
    private final long segmentPhysicalAddress;
    private final long segmentFileSize;
    private final long segmentMemorySize;
    private final long alignment;

    public PHTEntry(
            PHTEntryType type,
            int flags,
            long segmentOffset,
            long segmentVirtualAddress,
            long segmentPhysicalAddress,
            long segmentFileSize,
            long segmentMemorySize,
            long alignment) {
        this.type = type;
        this.flags = flags;
        this.segmentOffset = segmentOffset;
        this.segmentVirtualAddress = segmentVirtualAddress;
        this.segmentPhysicalAddress = segmentPhysicalAddress;
        this.segmentFileSize = segmentFileSize;
        this.segmentMemorySize = segmentMemorySize;
        this.alignment = alignment;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Segment type           : ");
        sb.append(type);
        sb.append('\n');
        sb.append("Flags                  : ");
        sb.append(String.format("0x%08x", flags));
        if (flags != 0) {
            sb.append(" (");
            if ((flags & PHTEntryFlags.PF_R.code()) != 0) {
                sb.append(PHTEntryFlags.PF_R.id());
            } else {
                sb.append(' ');
            }
            if ((flags & PHTEntryFlags.PF_W.code()) != 0) {
                sb.append(PHTEntryFlags.PF_W.id());
            } else {
                sb.append(' ');
            }
            if ((flags & PHTEntryFlags.PF_X.code()) != 0) {
                sb.append(PHTEntryFlags.PF_X.id());
            } else {
                sb.append(' ');
            }
            sb.append(')');
        }
        sb.append('\n');
        sb.append("Offset                 : ");
        sb.append(String.format("0x%016x\n", segmentOffset));
        sb.append("Virtual address        : ");
        sb.append(String.format("0x%016x\n", segmentVirtualAddress));
        sb.append("Physical address       : ");
        sb.append(String.format("0x%016x\n", segmentPhysicalAddress));
        sb.append("Segment size on file   : ");
        sb.append(String.format("%,d bytes\n", segmentFileSize));
        sb.append("Segment size in memory : ");
        sb.append(String.format("%,d bytes\n", segmentMemorySize));
        sb.append("Alignment              : ");
        sb.append(alignment);
        if (alignment == 0 || alignment == 1) {
            sb.append(" (no alignment)");
        }
        sb.append('\n');
        return sb.toString();
    }
}
