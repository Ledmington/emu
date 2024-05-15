package com.ledmington.elf;

public final class PHTEntry {

    private final PHTEntryType type;
    private final boolean readable;
    private final boolean writeable;
    private final boolean executable;
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
        this.readable = (flags & PHTEntryFlags.PF_R.code()) != 0;
        this.writeable = (flags & PHTEntryFlags.PF_W.code()) != 0;
        this.executable = (flags & PHTEntryFlags.PF_X.code()) != 0;

        if ((flags & ~(PHTEntryFlags.PF_R.code() | PHTEntryFlags.PF_W.code() | PHTEntryFlags.PF_X.code())) != 0) {
            throw new IllegalArgumentException(String.format("Invalid PHT Entry flags 0x%08x", flags));
        }

        this.segmentOffset = segmentOffset;
        this.segmentVirtualAddress = segmentVirtualAddress;
        this.segmentPhysicalAddress = segmentPhysicalAddress;
        this.segmentFileSize = segmentFileSize;
        this.segmentMemorySize = segmentMemorySize;
        this.alignment = alignment;
    }

    public PHTEntryType type() {
        return type;
    }

    public long segmentVirtualAddress() {
        return segmentVirtualAddress;
    }

    public long segmentMemorySize() {
        return segmentMemorySize;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public boolean isExecutable() {
        return executable;
    }

    @Override
    public String toString() {
        return "Segment type           : " + type
                + '\n'
                + "Flags                  : "
                + (readable ? 'R' : ' ') + (writeable ? 'W' : ' ') + (executable ? 'X' : ' ') + "\n"
                + "Offset                 : "
                + String.format("0x%016x\n", segmentOffset)
                + "Virtual address        : "
                + String.format("0x%016x\n", segmentVirtualAddress)
                + "Physical address       : "
                + String.format("0x%016x\n", segmentPhysicalAddress)
                + "Segment size on file   : "
                + String.format("%,d bytes\n", segmentFileSize)
                + "Segment size in memory : "
                + String.format("%,d bytes\n", segmentMemorySize)
                + "Alignment              : "
                + alignment
                + ((alignment == 0 || alignment == 1) ? " (no alignment)" : "")
                + '\n';
    }
}
