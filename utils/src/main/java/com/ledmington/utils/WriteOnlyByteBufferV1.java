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

	private static final int DEFAULT_CAPACITY = 16;

	private final boolean isLittleEndian;
	private byte[] v;
	private int position = 0;
	private int size = 0;

	/**
	 * Creates an empty WriteOnlyByteBuffer with the given capacity and the given endianness.
	 *
	 * @param capacity The initial capacity of the underlying array.
	 * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
	 */
	public WriteOnlyByteBufferV1(final int capacity, final boolean isLittleEndian) {
		this.v = new byte[capacity];
		this.isLittleEndian = isLittleEndian;
	}

	/**
	 * Creates a big-endian WriteOnlyByteBuffer with the given capacity. It is equivalent to calling {@code new
	 * WriteOnlyByteBuffer(capacity, false)}.
	 *
	 * @param capacity The initial capacity of the underlying array.
	 */
	public WriteOnlyByteBufferV1(final int capacity) {
		this(capacity, false);
	}

	/** Creates a big-endian WriteOnlyByteBuffer with a default capacity. */
	public WriteOnlyByteBufferV1() {
		this(DEFAULT_CAPACITY, false);
	}

	private void ensureCapacity(final int newCapacity) {
		if (v.length >= newCapacity) {
			return;
		}
		byte[] v2;
		try {
			v2 = new byte[Integer.highestOneBit(newCapacity) << 1];
		} catch (final OutOfMemoryError oome) {
			v2 = new byte[newCapacity];
		}

		System.arraycopy(v, 0, v2, 0, v.length);
		v = v2;
	}

	@Override
	public void setPosition(final int newPosition) {
		ensureCapacity(newPosition);
		size = Math.max(size, newPosition);
		position = newPosition;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getCapacity() {
		return v.length;
	}

	private void writeDirect(final byte x) {
		v[position] = x;
		setPosition(position + 1);
	}

	/**
	 * Writes the given byte at the current position.
	 *
	 * @param x The byte to be written.
	 */
	@Override
	public void write(final byte x) {
		ensureCapacity(position + 1);
		writeDirect(x);
	}

	/**
	 * Writes the given short with the current endianness.
	 *
	 * @param x The short to be written.
	 */
	@Override
	public void write(final short x) {
		ensureCapacity(position + 2);
		if (isLittleEndian) {
			writeLE(x);
		} else {
			writeBE(x);
		}
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
		ensureCapacity(position + 4);
		if (isLittleEndian) {
			writeLE(x);
		} else {
			writeBE(x);
		}
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
		ensureCapacity(position + 8);
		if (isLittleEndian) {
			writeLE(x);
		} else {
			writeBE(x);
		}
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
		ensureCapacity(size + arr.length);
		for (final byte b : arr) {
			writeDirect(b);
		}
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
		ensureCapacity(size + 4 * arr.length);
		if (isLittleEndian) {
			for (final int x : arr) {
				writeLE(x);
			}
		} else {
			for (final int x : arr) {
				writeBE(x);
			}
		}
	}

	@Override
	public void write(final long... arr) {
		ensureCapacity(size + 8 * arr.length);
		if (isLittleEndian) {
			for (final long x : arr) {
				writeLE(x);
			}
		} else {
			for (final long x : arr) {
				writeBE(x);
			}
		}
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
		return "WriteOnlyByteBufferV1(v=" + Arrays.toString(v) + ";isLE=" + isLittleEndian + ";size=" + size + ";i="
				+ position + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Arrays.hashCode(v);
		h = 31 * h + HashUtils.hash(isLittleEndian);
		h = 31 * h + size;
		h = 31 * h + position;
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
		if (!(other instanceof WriteOnlyByteBufferV1 wobb)) {
			return false;
		}
		return Arrays.equals(this.v, wobb.v)
				&& this.isLittleEndian == wobb.isLittleEndian
				&& this.size == wobb.size
				&& this.position == wobb.position;
	}
}
