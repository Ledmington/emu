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

import com.ledmington.elf.ISA;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** An ELF Relocation table with explicit addends. */
public final class RelocationAddendSection implements LoadableSection {

    private final String name;
    private final SectionHeader header;
    private final boolean is32Bit;
    private final RelocationAddendEntry[] relocationAddendTable;

    /**
     * Creates a RelocationAddendSection with the given data.
     *
     * @param name The name of this section.
     * @param sectionHeader The header of this section.
     * @param b The readOnlyByteBuffer to read data from.
     * @param is32Bit Used for alignment.
     */
    public RelocationAddendSection(
            final String name,
            final SectionHeader sectionHeader,
            final ReadOnlyByteBuffer b,
            final boolean is32Bit,
            final ISA isa) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);
        this.is32Bit = is32Bit;

        b.setPosition(sectionHeader.getFileOffset());
        final int nEntries = (int) (sectionHeader.getSectionSize() / sectionHeader.getEntrySize());
        this.relocationAddendTable = new RelocationAddendEntry[nEntries];

        for (int i = 0; i < nEntries; i++) {
            final long offset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            final long info = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            final int symbolTableIndex =
                    is32Bit ? BitUtils.asInt((info & 0x000000000000ff00L) >>> 8) : BitUtils.asInt(info >>> 32);
            final RelocationAddendEntryType type = RelocationAddendEntryType.fromCode(
                    isa, is32Bit ? BitUtils.asInt(BitUtils.asByte(info)) : BitUtils.asInt(info));
            final long addend = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            this.relocationAddendTable[i] = new RelocationAddendEntry(offset, symbolTableIndex, type, addend);
        }
    }

    public int getRelocationAddendTableLength() {
        return relocationAddendTable.length;
    }

    public RelocationAddendEntry getRelocationAddendEntry(final int idx) {
        return relocationAddendTable[idx];
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
    public byte[] getLoadableContent() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(relocationAddendTable.length * (is32Bit ? 12 : 24));
        for (final RelocationAddendEntry entry : relocationAddendTable) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(entry.offset()));
                bb.write(BitUtils.asInt((BitUtils.asByte(entry.symbolTableIndex()) << 8)
                        | (BitUtils.asByte(entry.type().getCode()))));
                bb.write(BitUtils.asInt(entry.addend()));
            } else {
                bb.write(entry.offset());
                bb.write((BitUtils.asLong(entry.symbolTableIndex()) << 32)
                        | (BitUtils.asLong(entry.type().getCode())));
                bb.write(entry.addend());
            }
        }
        return bb.array();
    }

    @Override
    public String toString() {
        return "RelocationAddendSection(name=" + name + ";header=" + header + ";is32Bit=" + is32Bit
                + ";relocationAddendTable=" + Arrays.toString(relocationAddendTable) + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + HashUtils.hash(is32Bit);
        h = 31 * h + Arrays.hashCode(relocationAddendTable);
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
        final RelocationAddendSection ras = (RelocationAddendSection) other;
        return this.name.equals(ras.name)
                && this.header.equals(ras.header)
                && this.is32Bit == ras.is32Bit
                && Arrays.equals(this.relocationAddendTable, ras.relocationAddendTable);
    }
}
