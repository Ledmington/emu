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

	default void write(final long address, final byte[] values) {
		for (int i = 0; i < values.length; i++) {
			write(address + i, values[i]);
		}
	}

	default void write(final long address, final long value) {
		write(address, BitUtils.asBEBytes(value));
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
