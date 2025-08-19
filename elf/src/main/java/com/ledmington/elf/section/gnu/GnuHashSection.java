/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.elf.section.gnu;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * A GNU-style hash table ELF section. A useful reference can be found <a href=
 * "https://sourceware.org/legacy-ml/binutils/2006-10/msg00377.html">here</a>.
 */
public final class GnuHashSection implements LoadableSection {

	private final String name;
	private final SectionHeader header;
	private final boolean is32Bit;
	private final int symIndex;
	private final int bloomShift;
	private final long[] bloom;
	private final int[] buckets;
	private final int[] chains;

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

		if (sectionHeader.getSectionSize() % 4 != 0) {
			throw new IllegalArgumentException(String.format(
					"Expected section size to be a multiple of 4 but was %,d (0x%08x).",
					sectionHeader.getSectionSize(), sectionHeader.getSectionSize()));
		}

		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);

		b.setPosition(sectionHeader.getFileOffset());

		final int nBuckets = b.read4();
		this.symIndex = b.read4();
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

		this.chains = new int[BitUtils.asInt(b.getPosition() - sectionHeader.getFileOffset()) / 4];
		for (int i = 0; i < chains.length; i++) {
			this.chains[i] = b.read4();
		}

		b.setAlignment(oldAlignment);
	}

	/**
	 * Computes the hash of the given byte-array as defined <a href=
	 * "https://sourceware.org/legacy-ml/binutils/2006-10/msg00377.html">here</a>.
	 *
	 * @param bytes The byte array to be hashed.
	 * @return The hash of the array.
	 */
	public static int hash(final byte[] bytes) {
		int h = 5381;
		int i = 0;
		while (i < bytes.length && bytes[i] != 0) {
			h = h * 33 + BitUtils.asInt(bytes[i]);
			i++;
		}
		return h;
	}

	/**
	 * Returns the number of symbols which cannot be looked up using {@code .gnu.hash}.
	 *
	 * @return The number of symbols which cannot be looked up using this section.
	 */
	public int getSymbolTableIndex() {
		return symIndex;
	}

	/**
	 * Returns the number of elements in the bloom filter.
	 *
	 * @return The number of elements in the bloom filter.
	 */
	public int getBloomFilterLength() {
		return bloom.length;
	}

	/**
	 * Returns the i-th element of the bloom filter.
	 *
	 * @param idx The index of the element to retrieve.
	 * @return The i-th element in the bloom filter.
	 */
	public long getBloomFilter(final int idx) {
		return bloom[idx];
	}

	/**
	 * Returns the number of buckets.
	 *
	 * @return The number of buckets.
	 */
	public int getBucketsLength() {
		return buckets.length;
	}

	/**
	 * Returns the i-th bucket.
	 *
	 * @param idx The index of the bucket ot retrieve.
	 * @return The i-th bucket
	 */
	public int getBucket(final int idx) {
		return buckets[idx];
	}

	/**
	 * Returns the shift to be performed to compute the second hash value.
	 *
	 * @return The amount of bits to shift.
	 */
	public int getBloomShift() {
		return bloomShift;
	}

	/**
	 * Returns the number of chains.
	 *
	 * @return The number of chains.
	 */
	public int getChainsLength() {
		return chains.length;
	}

	/**
	 * Returns the i-th chain.
	 *
	 * @param idx The index of the chain.
	 * @return The i-th chain.
	 */
	public int getChain(final int idx) {
		return chains[idx];
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SectionHeader header() {
		return header;
	}

	@Override
	public byte[] getLoadableContent() {
		final WriteOnlyByteBuffer bb =
				new WriteOnlyByteBufferV1(4 + 4 + 4 + 4 + bloom.length * (is32Bit ? 4 : 8) + buckets.length * 4);
		bb.write(buckets.length);
		bb.write(symIndex);
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
		bb.write(chains);
		return bb.array();
	}

	@Override
	public String toString() {
		return "GnuHashSection(name=" + name + ";header="
				+ header + ";is32Bit="
				+ is32Bit + ";symOffset="
				+ symIndex + ";bloomShift="
				+ bloomShift + ";bloom="
				+ Arrays.toString(bloom) + ";buckets="
				+ Arrays.toString(buckets) + ";chains=" + Arrays.toString(chains) + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + Boolean.hashCode(is32Bit);
		h = 31 * h + symIndex;
		h = 31 * h + bloomShift;
		h = 31 * h + Arrays.hashCode(bloom);
		h = 31 * h + Arrays.hashCode(buckets);
		h = 31 * h + Arrays.hashCode(chains);
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
		if (!(other instanceof final GnuHashSection ghs)) {
			return false;
		}
		return this.name.equals(ghs.name)
				&& this.header.equals(ghs.header)
				&& this.is32Bit == ghs.is32Bit
				&& this.symIndex == ghs.symIndex
				&& this.bloomShift == ghs.bloomShift
				&& Arrays.equals(this.bloom, ghs.bloom)
				&& Arrays.equals(this.buckets, ghs.buckets)
				&& Arrays.equals(this.chains, ghs.chains);
	}
}
