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

/** An interface for a ByteBuffer which allows only write operations. */
public interface WriteOnlyByteBuffer {

	/**
	 * Writes the given byte at the current address.
	 *
	 * @param b The byte to be written.
	 */
	void write(byte b);

	/**
	 * Writes the given short with the current endianness at the current position.
	 *
	 * @param s The 2-byte value to be written.
	 */
	void write(short s);

	/**
	 * Writes the given int with the current endianness at the current position.
	 *
	 * @param x The 4-byte value to be written.
	 */
	void write(int x);

	/**
	 * Writes the fiven long with the current endianness at the current position.
	 *
	 * @param x The 8-byte value to be written.
	 */
	void write(long x);

	/**
	 * Writes the array of bytes at the current position. It is equivalent to call write() on each element in order.
	 *
	 * @param bytes The byte array to be written.
	 */
	void write(byte... bytes);

	/**
	 * Writes the array of ints, each with the current endianness, at the current position. It is equivalent to call
	 * write() on each element in order.
	 *
	 * @param ints The int array to be written.
	 */
	void write(int... ints);

	/**
	 * Writes the array of longs, each with the current endianness, at the current position. It is equivalent to call
	 * write() on each element in order.
	 *
	 * @param longs The long array to be written.
	 */
	void write(long... longs);

	/**
	 * Returns the array that has been written.
	 *
	 * @return The backing array.
	 */
	byte[] array();

	/**
	 * Returns the current position in the buffer.
	 *
	 * @return The position in the buffer.
	 */
	int getPosition();

	/**
	 * Changes the current position to the given one.
	 *
	 * @param newPosition The new position in the buffer.
	 */
	void setPosition(int newPosition);

	/**
	 * Returns the number of bytes of the underlying array. NOTE: it may not correspond to the number of bytes actually
	 * written.
	 *
	 * @return The size of the underlying array.
	 */
	int getSize();
}
