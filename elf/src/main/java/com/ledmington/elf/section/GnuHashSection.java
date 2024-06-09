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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** A gnu-style hash table ELF section. */
public final class GnuHashSection implements LoadableSection {

    private final String name;
    private final SectionHeader header;
    private final boolean is32Bit;
    private final int symOffset;
    private final int bloomShift;
    private final long[] bloom;
    private final int[] buckets;

    /**
     * Creates the GNU hash section with the given data.
     *
     * @param name The name of this section.
     * @param sectionHeader The header of this section.
     * @param b The ReadOnlyByteBuffer to read data from.
     * @param is32Bit Used for alignment.
     */
    public GnuHashSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);
        this.is32Bit = is32Bit;

        b.setPosition(sectionHeader.getFileOffset());

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
    public String getName() {
        return name;
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getContent() {
        final WriteOnlyByteBuffer bb =
                new WriteOnlyByteBufferV1(4 + 4 + 4 + 4 + bloom.length * (is32Bit ? 4 : 8) + buckets.length * 4);
        bb.write(buckets.length);
        bb.write(symOffset);
        bb.write(bloom.length);
        bb.write(bloomShift);
        for (final long l : bloom) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(l));
            } else {
                bb.write(l);
            }
        }
        bb.write(buckets);
        return bb.array();
    }

    @Override
    public String toString() {
        return "GnuHashSection(name=" + name + ";header="
                + header + ";is32Bit="
                + is32Bit + ";symOffset="
                + symOffset + ";bloomShift="
                + bloomShift + ";bloom="
                + Arrays.toString(bloom) + ";buckets="
                + Arrays.toString(buckets) + ')';
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + HashUtils.hash(is32Bit);
        h = 31 * h + symOffset;
        h = 31 * h + bloomShift;
        h = 31 * h + Arrays.hashCode(bloom);
        h = 31 * h + Arrays.hashCode(buckets);
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
        final GnuHashSection ghs = (GnuHashSection) other;
        return this.name.equals(ghs.name)
                && this.header.equals(ghs.header)
                && this.is32Bit == ghs.is32Bit
                && this.symOffset == ghs.symOffset
                && this.bloomShift == ghs.bloomShift
                && Arrays.equals(this.bloom, ghs.bloom)
                && Arrays.equals(this.buckets, ghs.buckets);
    }
}
