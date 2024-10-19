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
package com.ledmington.utils;

import java.util.Arrays;
import java.util.Objects;

/** A buffer which allows reading with endianness. This implementation uses a byte array. */
public final class ReadOnlyByteBufferV1 implements ReadOnlyByteBuffer {

	private final byte[] b;
	private long position;
	private boolean isLE;
	private long alignment;

	/**
	 * Creates a big-endian ReadOnlyByteBufferV1 with the given array. It is equivalent to calling {@code new
	 * ReadOnlyByteBufferV1(b, false, 1)}.
	 *
	 * @param b The byte array ot be used.
	 */
	public ReadOnlyByteBufferV1(final byte[] b) {
		this(b, false, 1L);
	}

	/**
	 * Creates a ReadOnlyByteBufferV1 with the given array and the given endianness. Equivalent to calling {@code new
	 * ReadOnlyByteBufferV1(b, e, 1)}.
	 *
	 * @param b The byte array ot be used.
	 * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
	 */
	public ReadOnlyByteBufferV1(final byte[] b, final boolean isLittleEndian) {
		this(b, isLittleEndian, 1L);
	}

	/**
	 * Creates a ReadOnlyByteBufferV1 with the given array, the given endianness and the given alignment.
	 *
	 * @param bytes The byte array ot be used.
	 * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
	 * @param alignment The byte alignment to be used while reading.
	 */
	public ReadOnlyByteBufferV1(final byte[] bytes, final boolean isLittleEndian, final long alignment) {
		this.isLE = isLittleEndian;
		checkAlignment(alignment);
		this.alignment = alignment;
		Objects.requireNonNull(bytes);
		this.b = new byte[bytes.length];
		System.arraycopy(bytes, 0, this.b, 0, bytes.length);
		this.position = 0L;
	}

	private void checkAlignment(final long alignment) {
		if (alignment <= 0 || Long.bitCount(alignment) != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid alignment: expected a power of two >0 but was %,d", alignment));
		}
	}

	@Override
	public boolean isLittleEndian() {
		return isLE;
	}

	@Override
	public void setEndianness(final boolean isLittleEndian) {
		this.isLE = isLittleEndian;
	}

	@Override
	public void setAlignment(final long newAlignment) {
		checkAlignment(newAlignment);
		this.alignment = newAlignment;
	}

	@Override
	public long getAlignment() {
		return alignment;
	}

	@Override
	public long getPosition() {
		return position;
	}

	@Override
	public void setPosition(final long newPosition) {
		position = newPosition;
	}

	@Override
	public byte read() {
		return b[BitUtils.asInt(position)];
	}

	@Override
	public String toString() {
		return "ReadOnlyByteBufferV1(b=" + Arrays.toString(b) + ";i=" + position + ";isLittleEndian=" + isLE
				+ ";alignment=" + alignment + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Arrays.hashCode(b);
		h = 31 * h + HashUtils.hash(position);
		h = 31 * h + HashUtils.hash(isLE);
		h = 31 * h + HashUtils.hash(alignment);
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
		final ReadOnlyByteBufferV1 bb = (ReadOnlyByteBufferV1) other;
		return Arrays.equals(this.b, bb.b)
				&& this.position == bb.position
				&& this.isLE == bb.isLE
				&& this.alignment == bb.alignment;
	}
}
