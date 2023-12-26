package com.ledmington.elf;

public final class GnuHashSection extends Section {

    private final int symOffset;
    private final int bloomShift;
    private final long[] bloom;
    private final int[] buckets;

    public GnuHashSection(
            final String name, final SectionHeader sectionHeader, final ByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());

        final int nBuckets = b.read4();
        this.symOffset = b.read4();
        final int bloomSize = b.read4();
        this.bloomShift = b.read4();
        this.bloom = new long[bloomSize];
        this.buckets = new int[nBuckets];

        for (int i = 0; i < bloomSize; i++) {
            bloom[i] = is32Bit ? b.read4AsLong() : b.read8();
        }

        for (int i = 0; i < nBuckets; i++) {
            buckets[i] = b.read4();
        }
    }
}
