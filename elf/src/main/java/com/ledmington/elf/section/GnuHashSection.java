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
}
