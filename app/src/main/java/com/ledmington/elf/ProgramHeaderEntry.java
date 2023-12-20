package com.ledmington.elf;

/**
 * This class is just a data holder.
 * No check is performed in the constructor on the given data.
 */
public final class ProgramHeaderEntry {

    private final ProgramHeaderEntryType type;
    private final int flags;
    private final long segmentOffset;
    private final long segmentVirtualAddress;
    private final long segmentPhysicalAddress;
    private final long segmentFileSize;
    private final long segmentMemorySize;
    private final long alignment;

    public ProgramHeaderEntry(
            ProgramHeaderEntryType type,
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
        sb.append(type.description());
        sb.append('\n');
        sb.append("Flags                  : ");
        sb.append(String.format("0x%08x", flags));
        if (flags != 0) {
            sb.append(" (");
            if ((flags & PHFlags.PF_R.code()) != 0) {
                sb.append('r');
            }
            if ((flags & PHFlags.PF_W.code()) != 0) {
                sb.append('w');
            }
            if ((flags & PHFlags.PF_X.code()) != 0) {
                sb.append('x');
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
        sb.append(segmentFileSize);
        sb.append('\n');
        sb.append("Segment size in memory : ");
        sb.append(segmentMemorySize);
        sb.append('\n');
        sb.append("Alignment              : ");
        sb.append(alignment);
        if (alignment == 0 || alignment == 1) {
            sb.append(" (none)");
        }
        sb.append('\n');
        return sb.toString();
    }
}
