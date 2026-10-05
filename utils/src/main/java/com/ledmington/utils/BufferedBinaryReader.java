/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link BinaryReader} backed by a file which is read lazily: only a fixed-size buffer of the file is kept in memory
 * at any time, and it is refilled from disk only when a byte outside of it is requested. This allows reading files of
 * arbitrary size with a constant memory footprint.
 */
public final class BufferedBinaryReader implements AutoCloseable, BinaryReader {

	/** The default size of the in-memory buffer, in bytes. */
	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

	private static final InMemoryArrayReader EMPTY_BUFFER = new InMemoryArrayReader(new byte[0]);

	private final Path path;
	private final RandomAccessFile file;
	private final long fileSize;
	// Scratch array the file is read into before being wrapped by the buffer.
	private final byte[] scratch;

	// The currently loaded portion of the file.
	private InMemoryArrayReader buffer = EMPTY_BUFFER;
	// File offset of the first byte of the buffer.
	private long bufferStart;
	// Number of bytes in the buffer (0 means nothing has been loaded yet).
	private int bufferLength;

	private long position;
	private boolean isLE;
	private long alignment;

	/**
	 * Creates a big-endian {@link BufferedBinaryReader} on the given file with alignment 1 and the default buffer size.
	 *
	 * @param path The file to be read.
	 * @throws UncheckedIOException If the file cannot be opened.
	 */
	public BufferedBinaryReader(final Path path) {
		this(path, false, 1L, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Creates a {@link BufferedBinaryReader} on the given file with the given endianness, alignment 1 and the default
	 * buffer size.
	 *
	 * @param path The file to be read.
	 * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
	 * @throws UncheckedIOException If the file cannot be opened.
	 */
	public BufferedBinaryReader(final Path path, final boolean isLittleEndian) {
		this(path, isLittleEndian, 1L, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Creates a {@link BufferedBinaryReader} on the given file.
	 *
	 * @param path The file to be read.
	 * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
	 * @param alignment The byte alignment to be used while reading.
	 * @param bufferSize The size in bytes of the in-memory buffer.
	 * @throws UncheckedIOException If the file cannot be opened.
	 */
	public BufferedBinaryReader(
			final Path path, final boolean isLittleEndian, final long alignment, final int bufferSize) {
		Objects.requireNonNull(path);
		checkAlignment(alignment);
		if (bufferSize <= 0) {
			throw new IllegalArgumentException(
					String.format("Invalid buffer size: expected >0 but was %,d", bufferSize));
		}
		this.path = path.toAbsolutePath().normalize();
		this.isLE = isLittleEndian;
		this.alignment = alignment;
		this.scratch = new byte[bufferSize];
		this.file = open(this.path);
		try {
			this.fileSize = file.length();
		} catch (final IOException e) {
			closeQuietly(file, e);
			throw new UncheckedIOException(e);
		}
	}

	private static RandomAccessFile open(final Path path) {
		try {
			return new RandomAccessFile(path.toFile(), "r");
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void closeQuietly(final RandomAccessFile f, final IOException cause) {
		try {
			f.close();
		} catch (final IOException e) {
			cause.addSuppressed(e);
		}
	}

	private void checkAlignment(final long alignment) {
		if (alignment <= 0L || Long.bitCount(alignment) != 1) {
			throw new IllegalArgumentException(
					String.format("Invalid alignment: expected a power of two >0 but was %,d", alignment));
		}
	}

	/**
	 * Returns the size of the underlying file.
	 *
	 * @return The size of the file, in bytes.
	 */
	public long size() {
		return fileSize;
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
	public long getAlignment() {
		return alignment;
	}

	@Override
	public void setAlignment(final long newAlignment) {
		checkAlignment(newAlignment);
		this.alignment = newAlignment;
	}

	@Override
	public void setPosition(final long newPosition) {
		// Validity is checked lazily on read, since BinaryReader moves the cursor one past the last byte read.
		this.position = newPosition;
	}

	@Override
	public long getPosition() {
		return position;
	}

	@Override
	public byte read() {
		if (position < 0L || position >= fileSize) {
			throw new IndexOutOfBoundsException(
					String.format("Position %,d is outside of file of %,d bytes", position, fileSize));
		}
		if (position < bufferStart || position >= bufferStart + bufferLength) {
			fill(position);
		}
		buffer.setPosition(position - bufferStart);
		return buffer.read();
	}

	/**
	 * Prepares the buffer for reading {@code n} bytes at the current position.
	 *
	 * @return True if all {@code n} bytes are in the buffer, false if the caller must fall back to byte-by-byte reads.
	 */
	private boolean preparebuffer(final int n) {
		if (position < 0L || position + n > fileSize) {
			return false;
		}
		if (position < bufferStart || position >= bufferStart + bufferLength) {
			fill(position);
		}
		if (position + n > bufferStart + bufferLength) {
			return false;
		}
		buffer.setPosition(position - bufferStart);
		return true;
	}

	private void move(final int n) {
		final long next = position + n;
		position = ((next % alignment) == 0L) ? next : (((next / alignment) + 1L) * alignment);
	}

	@Override
	public short read2LE() {
		if (!preparebuffer(2)) {
			return BinaryReader.super.read2LE();
		}
		final short x = buffer.read2LE();
		move(2);
		return x;
	}

	@Override
	public short read2BE() {
		if (!preparebuffer(2)) {
			return BinaryReader.super.read2BE();
		}
		final short x = buffer.read2BE();
		move(2);
		return x;
	}

	@Override
	public int read4LE() {
		if (!preparebuffer(4)) {
			return BinaryReader.super.read4LE();
		}
		final int x = buffer.read4LE();
		move(4);
		return x;
	}

	@Override
	public int read4BE() {
		if (!preparebuffer(4)) {
			return BinaryReader.super.read4BE();
		}
		final int x = buffer.read4BE();
		move(4);
		return x;
	}

	@Override
	public long read8LE() {
		if (!preparebuffer(8)) {
			return BinaryReader.super.read8LE();
		}
		final long x = buffer.read8LE();
		move(8);
		return x;
	}

	@Override
	public long read8BE() {
		if (!preparebuffer(8)) {
			return BinaryReader.super.read8BE();
		}
		final long x = buffer.read8BE();
		move(8);
		return x;
	}

	private void fill(final long pos) {
		final long start = pos - (pos % scratch.length);
		final int toRead = BitUtils.asInt(Math.min(scratch.length, fileSize - start));
		try {
			file.seek(start);
			file.readFully(scratch, 0, toRead);
		} catch (final IOException e) {
			// Invalidate the buffer so that partial data cannot be used.
			buffer = EMPTY_BUFFER;
			bufferLength = 0;
			throw new UncheckedIOException(e);
		}
		buffer = new InMemoryArrayReader(toRead == scratch.length ? scratch : Arrays.copyOf(scratch, toRead));
		bufferStart = start;
		bufferLength = toRead;
	}

	/**
	 * Closes the underlying file.
	 *
	 * @throws UncheckedIOException If an I/O error occurs while closing the file.
	 */
	@Override
	public void close() {
		try {
			file.close();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public String toString() {
		return "BufferedBinaryReader(path=" + path + ";fileSize=" + fileSize + ";bufferSize=" + scratch.length
				+ ";position=" + position + ";isLittleEndian=" + isLE + ";alignment=" + alignment + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + path.hashCode();
		h = 31 * h + Long.hashCode(fileSize);
		h = 31 * h + scratch.length;
		h = 31 * h + Long.hashCode(position);
		h = 31 * h + Boolean.hashCode(isLE);
		h = 31 * h + Long.hashCode(alignment);
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
		if (!(other instanceof final BufferedBinaryReader br)) {
			return false;
		}
		return this.path.equals(br.path)
				&& this.fileSize == br.fileSize
				&& this.scratch.length == br.scratch.length
				&& this.position == br.position
				&& this.isLE == br.isLE
				&& this.alignment == br.alignment;
	}
}
