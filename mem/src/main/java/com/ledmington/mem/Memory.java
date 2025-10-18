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
package com.ledmington.mem;

import com.ledmington.utils.BitUtils;

/**
 * A common interface for emulated RAMs, caches and hard drives.
 *
 * <p>It is designed to map 64-bit addresses to 8-bit values/words.
 */
public interface Memory {

	/**
	 * Reads a single byte from the given address.
	 *
	 * @param address The address to read from.
	 * @return The byte word contained at the given address.
	 */
	byte read(long address);

	/**
	 * Reads 2 contiguous bytes (little-endian) starting at the given address.
	 *
	 * @param address The address to read from.
	 * @return The 2-byte value that was stored at the given address.
	 */
	default short read2(final long address) {
		// Little-endian
		short x = 0;
		x = BitUtils.or(x, BitUtils.asShort(read(address)));
		x = BitUtils.or(x, BitUtils.shl(BitUtils.asShort(read(address + 1L)), 8));
		return x;
	}

	/**
	 * Reads 4 contiguous bytes (little-endian) starting at the given address.
	 *
	 * @param address The address to read from.
	 * @return The 4-byte value that was stored at the given address.
	 */
	default int read4(final long address) {
		// Little-endian
		int x = 0;
		x |= BitUtils.asInt(read(address));
		x |= (BitUtils.asInt(read(address + 1L)) << 8);
		x |= (BitUtils.asInt(read(address + 2L)) << 16);
		x |= (BitUtils.asInt(read(address + 3L)) << 24);
		return x;
	}

	/**
	 * Reads 8 contiguous bytes (little-endian) starting at the given address.
	 *
	 * @param address The address to read from.
	 * @return The 8-byte value that was stored at the given address.
	 */
	default long read8(final long address) {
		// Little-endian
		long x = 0x0000000000000000L;
		x |= BitUtils.asLong(read(address));
		x |= (BitUtils.asLong(read(address + 1L)) << 8);
		x |= (BitUtils.asLong(read(address + 2L)) << 16);
		x |= (BitUtils.asLong(read(address + 3L)) << 24);
		x |= (BitUtils.asLong(read(address + 4L)) << 32);
		x |= (BitUtils.asLong(read(address + 5L)) << 40);
		x |= (BitUtils.asLong(read(address + 6L)) << 48);
		x |= (BitUtils.asLong(read(address + 7L)) << 56);
		return x;
	}

	/**
	 * Writes the given byte word at the given address, overwriting any value previously stored at that location.
	 *
	 * @param address The address to write at.
	 * @param value The value to write.
	 */
	void write(long address, byte value);

	/**
	 * Writes an arbitrary number of single-byte values contiguously in memory.
	 *
	 * @param address The address to start writing from.
	 * @param values THe array of values to write contiguously.
	 */
	default void write(final long address, final byte[] values) {
		for (int i = 0; i < values.length; i++) {
			write(address + i, values[i]);
		}
	}

	/**
	 * Writes a 2-byte value at the given address (little-endian).
	 *
	 * @param address The address to write the value at.
	 * @param value The value to be written.
	 */
	default void write(final long address, final short value) {
		write(address, BitUtils.asLEBytes(value));
	}

	/**
	 * Writes a 4-byte value at the given address (little-endian).
	 *
	 * @param address The address to write the value at.
	 * @param value The value to be written.
	 */
	default void write(final long address, final int value) {
		write(address, BitUtils.asLEBytes(value));
	}

	/**
	 * Writes an 8-byte value at the given address (little-endian).
	 *
	 * @param address The address to write the value at.
	 * @param value The value to be written.
	 */
	default void write(final long address, final long value) {
		write(address, BitUtils.asLEBytes(value));
	}

	/**
	 * Checks whether the given address is initialized or not. A memory address is said to be initialized if it has been
	 * written to at least once.
	 *
	 * @param address The address to be checked.
	 * @return True if it is initialized, false otherwise.
	 */
	boolean isInitialized(long address);
}
