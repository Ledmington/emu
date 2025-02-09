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
package com.ledmington.utils;

import java.util.Arrays;

/** A ByteBuffer which allows only write operations. */
public final class WriteOnlyByteBufferV1 implements WriteOnlyByteBuffer {

	private static final int DEFAULT_SIZE = 16;

	private byte[] v;
	private final boolean isLittleEndian;
	private int i;
	private int size;

	/**
	 * Creates an empty WriteOnlyByteBuffer with the given length and the given endianness.
	 *
	 * @param length The length of the underlying array.
	 * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
	 */
	public WriteOnlyByteBufferV1(final int length, final boolean isLittleEndian) {
		this.v = new byte[length];
		this.isLittleEndian = isLittleEndian;
	}

	/**
	 * Creates a big-endian WriteOnlyByteBuffer with the given length. It is equivalent to calling {@code new
	 * WriteOnlyByteBuffer(length, false)}.
	 *
	 * @param length The length of the underlying array.
	 */
	public WriteOnlyByteBufferV1(final int length) {
		this(length, false);
	}

	/** Creates a big-endian WriteOnlyByteBuffer with a default length. */
	public WriteOnlyByteBufferV1() {
		this(DEFAULT_SIZE, false);
	}

	private void ensureSize(final int newSize) {
		if (v.length >= newSize) {
			return;
		}
		byte[] v2;
		try {
			v2 = new byte[Integer.highestOneBit(newSize) << 1];
		} catch (final OutOfMemoryError oome) {
			v2 = new byte[newSize];
		}

		System.arraycopy(v, 0, v2, 0, v.length);
		v = v2;
	}

	@Override
	public void setPosition(final int newPosition) {
		ensureSize(newPosition);
		if (newPosition > size) {
			size = newPosition;
		}
		i = newPosition;
	}

	@Override
	public int getPosition() {
		return i;
	}

	private void writeDirect(final byte x) {
		v[i++] = x;
	}

	/**
	 * Writes the given byte at the current position.
	 *
	 * @param x The byte to be written.
	 */
	@Override
	public void write(final byte x) {
		ensureSize(size + 1);
		writeDirect(x);
		size++;
	}

	/**
	 * Writes the given short with the current endianness.
	 *
	 * @param x The short to be written.
	 */
	@Override
	public void write(final short x) {
		ensureSize(size + 2);
		if (isLittleEndian) {
			writeLE(x);
		} else {
			writeBE(x);
		}
		size += 2;
	}

	private void writeLE(final short x) {
		writeDirect((byte) (x & ((short) 0x00ff)));
		writeDirect((byte) (x >>> 8));
	}

	private void writeBE(final short x) {
		writeDirect((byte) (x >>> 8));
		writeDirect((byte) (x & ((short) 0x00ff)));
	}

	/**
	 * Writes the given int with the current endianness.
	 *
	 * @param x The int to be written.
	 */
	@Override
	public void write(final int x) {
		ensureSize(size + 4);
		if (isLittleEndian) {
			writeLE(x);
		} else {
			writeBE(x);
		}
		size += 4;
	}

	private void writeLE(final int x) {
		writeDirect((byte) (x & 0x000000ff));
		writeDirect((byte) ((x & 0x0000ff00) >>> 8));
		writeDirect((byte) ((x & 0x00ff0000) >>> 16));
		writeDirect((byte) (x >>> 24));
	}

	private void writeBE(final int x) {
		writeDirect((byte) (x >>> 24));
		writeDirect((byte) ((x & 0x00ff0000) >>> 16));
		writeDirect((byte) ((x & 0x0000ff00) >>> 8));
		writeDirect((byte) (x & 0x000000ff));
	}

	/**
	 * Writes the given long with the current endianness.
	 *
	 * @param x The long to be written.
	 */
	@Override
	public void write(final long x) {
		ensureSize(size + 8);
		if (isLittleEndian) {
			writeLE(x);
		} else {
			writeBE(x);
		}
		size += 8;
	}

	private void writeLE(final long x) {
		writeDirect((byte) (x & 0x00000000000000ffL));
		writeDirect((byte) ((x & 0x000000000000ff00L) >>> 8));
		writeDirect((byte) ((x & 0x0000000000ff0000L) >>> 16));
		writeDirect((byte) ((x & 0x00000000ff000000L) >>> 24));
		writeDirect((byte) ((x & 0x000000ff00000000L) >>> 32));
		writeDirect((byte) ((x & 0x0000ff0000000000L) >>> 40));
		writeDirect((byte) ((x & 0x00ff000000000000L) >>> 48));
		writeDirect((byte) (x >>> 56));
	}

	private void writeBE(final long x) {
		writeDirect((byte) (x >>> 56));
		writeDirect((byte) ((x & 0x00ff000000000000L) >>> 48));
		writeDirect((byte) ((x & 0x0000ff0000000000L) >>> 40));
		writeDirect((byte) ((x & 0x000000ff00000000L) >>> 32));
		writeDirect((byte) ((x & 0x00000000ff000000L) >>> 24));
		writeDirect((byte) ((x & 0x0000000000ff0000L) >>> 16));
		writeDirect((byte) ((x & 0x000000000000ff00L) >>> 8));
		writeDirect((byte) (x & 0x00000000000000ffL));
	}

	@Override
	public void write(final byte... arr) {
		ensureSize(size + arr.length);
		for (final byte b : arr) {
			writeDirect(b);
		}
		size += arr.length;
	}

	/**
	 * Writes the given array of ints with the current endianness. It is equivalent to doing <code>
	 *     for (int i=0; i&lt;arr.length; i++) {
	 *         write(arr[i]);
	 *     }
	 * </code>
	 *
	 * @param arr The array of ints to be written.
	 */
	@Override
	public void write(final int... arr) {
		ensureSize(size + 4 * arr.length);
		if (isLittleEndian) {
			for (final int x : arr) {
				writeLE(x);
			}
		} else {
			for (final int x : arr) {
				writeBE(x);
			}
		}
		size += 4 * arr.length;
	}

	@Override
	public void write(final long... arr) {
		ensureSize(size + 8 * arr.length);
		if (isLittleEndian) {
			for (final long x : arr) {
				writeLE(x);
			}
		} else {
			for (final long x : arr) {
				writeBE(x);
			}
		}
		size += 8 * arr.length;
	}

	/**
	 * Returns a copy of the underlying array.
	 *
	 * @return A copy of the underlying array.
	 */
	@Override
	public byte[] array() {
		final byte[] w = new byte[size];
		System.arraycopy(v, 0, w, 0, size);
		return w;
	}

	@Override
	public String toString() {
		return "WriteOnlyByteBufferV1(v=" + Arrays.toString(v) + ";isLE=" + isLittleEndian + ";size=" + size + ";i=" + i
				+ ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Arrays.hashCode(v);
		h = 31 * h + HashUtils.hash(isLittleEndian);
		h = 31 * h + size;
		h = 31 * h + i;
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return false;
		}
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final WriteOnlyByteBufferV1 wobb = (WriteOnlyByteBufferV1) other;
		return Arrays.equals(this.v, wobb.v)
				&& this.isLittleEndian == wobb.isLittleEndian
				&& this.size == wobb.size
				&& this.i == wobb.i;
	}
}
