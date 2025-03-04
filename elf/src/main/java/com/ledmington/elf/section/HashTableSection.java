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
package com.ledmington.elf.section;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * A .hash ELF section.
 *
 * <p>Useful reference <a href=
 * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/chapter6-48031/index.html">here</a>.
 */
public final class HashTableSection implements LoadableSection {

	private final String name;
	private final SectionHeader header;
	private final int[] buckets;
	private final int[] chains;

	/**
	 * Creates an hash table section with the given data.
	 *
	 * @param name The name of the section.
	 * @param sectionHeader The header of the section.
	 * @param b The buffer to read data from.
	 */
	public HashTableSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);

		if (sectionHeader.getSectionSize() % 4 != 0) {
			throw new IllegalArgumentException(String.format(
					"Expected section size to be a multiple of 4 but was %,d (0x%08x).",
					sectionHeader.getSectionSize(), sectionHeader.getSectionSize()));
		}

		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);

		b.setPosition(sectionHeader.getFileOffset());

		final int nBuckets = b.read4();
		final int nChains = b.read4();
		this.buckets = new int[nBuckets];
		this.chains = new int[nChains];

		for (int i = 0; i < nBuckets; i++) {
			buckets[i] = b.read4();
		}

		for (int i = 0; i < nChains; i++) {
			this.chains[i] = b.read4();
		}

		b.setAlignment(oldAlignment);
	}

	/**
	 * Returns the number of buckets.
	 *
	 * @return The number of buckets.
	 */
	public int getNumBuckets() {
		return buckets.length;
	}

	/**
	 * Returns the i-th bucket.
	 *
	 * @param idx The index of the bucket.
	 * @return The i-th bucket.
	 */
	public int getBucket(final int idx) {
		return buckets[idx];
	}

	/**
	 * Returns the number of chains.
	 *
	 * @return The number of chains.
	 */
	public int getNumChains() {
		return chains.length;
	}

	/**
	 * Returns the i-th chain value.
	 *
	 * @param idx The index of the chain.
	 * @return The i-th chain.
	 */
	public int getChain(final int idx) {
		return chains[idx];
	}

	/**
	 * Computes the hash of the given byte-array as defined <a href=
	 * "https://www.sco.com/developers/gabi/latest/ch5.dynamic.html#hash">here</a> (Figure 5-13).
	 *
	 * @param bytes The byte array to be hashed.
	 * @return The hash of the array.
	 */
	public static int hash(final byte[] bytes) {
		int h = 0;
		for (int i = 0; i < bytes.length && bytes[i] != 0; i++) {
			h = (h << 4) + BitUtils.asInt(bytes[i]);
			final int g = h & 0xf0000000;
			if (g != 0) {
				h ^= g >>> 24;
			}
			h &= (~g);
		}
		return h & 0x0fffffff;
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
		final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(4 + 4 + (buckets.length * 4) + (chains.length * 4));
		bb.write(buckets.length);
		bb.write(chains.length);
		bb.write(buckets);
		bb.write(chains);
		return bb.array();
	}

	@Override
	public String toString() {
		return "HashTableSection(name=" + name + ";header=" + header + ";nBuckets=" + buckets.length + ";nChains="
				+ chains.length + ";buckets=" + Arrays.toString(buckets) + ";chains=" + Arrays.toString(chains) + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
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
		if (!(other instanceof HashTableSection hts)) {
			return false;
		}
		return this.name.equals(hts.name)
				&& this.header.equals(hts.header)
				&& Arrays.equals(this.buckets, hts.buckets)
				&& Arrays.equals(this.chains, hts.chains);
	}
}
