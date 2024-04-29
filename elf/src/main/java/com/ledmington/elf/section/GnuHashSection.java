package com.ledmington.elf.section;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class GnuHashSection extends LoadableSection {

    private final boolean is32Bit;
    private final int symOffset;
    private final int bloomShift;
    private final long[] bloom;
    private final int[] buckets;

    public GnuHashSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        this.is32Bit = is32Bit;

        b.setPosition((int) sectionHeader.fileOffset());

        final int nBuckets = b.read4();
        this.symOffset = b.read4();
        final int bloomSize = b.read4();
        this.bloomShift = b.read4();
        this.bloom = new long[bloomSize];
        this.buckets = new int[nBuckets];

        for (int i = 0; i < bloomSize; i++) {
            bloom[i] = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
        }

        for (int i = 0; i < nBuckets; i++) {
            buckets[i] = b.read4();
        }
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb =
                new WriteOnlyByteBuffer(4 + 4 + 4 + 4 + bloom.length * (is32Bit ? 4 : 8) + buckets.length * 4);
        bb.write(buckets.length);
        bb.write(symOffset);
        bb.write(bloom.length);
        bb.write(bloomShift);
        for (int i = 0; i < bloom.length; i++) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(bloom[i]));
            } else {
                bb.write(bloom[i]);
            }
        }
        bb.write(buckets);
        return bb.array();
    }
}
