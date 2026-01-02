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
package com.ledmington.mem;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.ledmington.mem.exc.AccessToUninitializedMemoryException;
import com.ledmington.mem.exc.IllegalExecutionException;
import com.ledmington.mem.exc.IllegalReadException;
import com.ledmington.mem.exc.IllegalWriteException;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.IntervalArray;
import com.ledmington.utils.SuppressFBWarnings;
import com.ledmington.utils.TerminalUtils;

/** This is the part of the memory which implements read-write-execute permissions. */
public final class MemoryController implements Memory {

	private final Memory mem;
	private final IntervalArray readableAddresses;
	private final IntervalArray writableAddresses;
	private final IntervalArray executableAddresses;

	// FIXME: this seems like a poor design choice
	private final boolean breakOnWrongPermissions;
	private final boolean breakWhenReadingUninitializedMemory;

	/**
	 * Creates a MemoryController with the given initializer.
	 *
	 * @param memory The Memory object to wrap with permission checking.
	 * @param breakOnWrongPermissions Decides whether this controller should throw an exception when accessing memory
	 *     with the wrong permissions.
	 * @param breakWhenReadingUninitializedMemory Decides whether this controller should throw an exception when reading
	 *     uninitialized memory.
	 * @param defaultReadable Default read permissions for the whole memory: true for readable, false otherwise.
	 * @param defaultWritable Default write permissions for the whole memory: true for writable, false otherwise.
	 * @param defaultExecutable Default execution permissions for the whole memory: true for executable, false
	 *     otherwise.
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "At the moment we need this object as it is.")
	public MemoryController(
			final Memory memory,
			final boolean breakOnWrongPermissions,
			final boolean breakWhenReadingUninitializedMemory,
			final boolean defaultReadable,
			final boolean defaultWritable,
			final boolean defaultExecutable) {
		this.mem = Objects.requireNonNull(memory);
		this.breakOnWrongPermissions = breakOnWrongPermissions;
		this.breakWhenReadingUninitializedMemory = breakWhenReadingUninitializedMemory;
		this.readableAddresses = new IntervalArray(defaultReadable);
		this.writableAddresses = new IntervalArray(defaultWritable);
		this.executableAddresses = new IntervalArray(defaultExecutable);
	}

	/**
	 * Creates a MemoryController with the given initializer.
	 *
	 * @param memory The Memory object to wrap with permission checking.
	 * @param breakOnWrongPermissions Decides whether this controller should throw an exception when accessing memory
	 *     with the wrong permissions.
	 * @param breakWhenReadingUninitializedMemory Decides whether this controller should throw an exception when reading
	 *     uninitialized memory.
	 */
	public MemoryController(
			final Memory memory,
			final boolean breakOnWrongPermissions,
			final boolean breakWhenReadingUninitializedMemory) {
		this(memory, breakOnWrongPermissions, breakWhenReadingUninitializedMemory, false, false, false);
	}

	/**
	 * Creates a MemoryController with the given initializer and the default behavior of breaking when accessing memory
	 * with the wrong permissions.
	 *
	 * @param memory The Memory object to wrap with permission checking.
	 */
	public MemoryController(final Memory memory) {
		this(memory, true, true, false, false, false);
	}

	private boolean canRead(final long address) {
		return readableAddresses.get(address);
	}

	private boolean canWrite(final long address) {
		return writableAddresses.get(address);
	}

	private boolean canExecute(final long address) {
		return executableAddresses.get(address);
	}

	private String reportIllegalAccess(
			final String message,
			final long address,
			final int length,
			final String propertyName,
			final Predicate<Long> allowed) {
		final long linesAround = 5;
		final long bytesPerLine = 16;

		// The start of the given range must be in the middle 16-bytes aligned line
		final long startAddress = address / bytesPerLine * bytesPerLine - bytesPerLine * linesAround;

		final StringBuilder sb = new StringBuilder(256);

		sb.append("\nLegend:\n ")
				.append(TerminalUtils.ANSI_WHITE)
				.append("Uninitialized=xx")
				.append(TerminalUtils.ANSI_RESET)
				.append(' ')
				.append(TerminalUtils.ANSI_RED)
				.append("Not-")
				.append(propertyName)
				.append(TerminalUtils.ANSI_RESET)
				.append(' ')
				.append(TerminalUtils.ANSI_WHITE)
				.append(propertyName)
				.append(TerminalUtils.ANSI_RESET)
				.append("\n\n");

		final Consumer<Long> printer = x -> {
			final String s = isInitialized(x) ? String.format("%02x", mem.read(x)) : "xx";
			sb.append(x == address ? '[' : ' ');

			// Print the bytes accessed in bold
			if (x >= address && x < address + length) {
				sb.append(TerminalUtils.ANSI_BOLD);
			}
			if (allowed.test(x)) {
				sb.append(TerminalUtils.ANSI_WHITE);
			} else {
				sb.append(TerminalUtils.ANSI_RED);
			}
			sb.append(s).append(TerminalUtils.ANSI_RESET).append(x == (address + length - 1) ? ']' : ' ');
		};

		// print column headers
		sb.append("                      00  01  02  03  04  05  06  07  08  09  0a  0b  0c  0d  0e  0f\n");

		// print lines before
		for (long r = 0L; r < linesAround; r++) {
			sb.append(String.format(" 0x%016x: ", startAddress + (r * bytesPerLine)));
			for (long i = 0L; i < bytesPerLine; i++) {
				final long x = startAddress + (r * bytesPerLine) + i;
				printer.accept(x);
			}
			sb.append('\n');
		}

		// print line with the given address
		sb.append(String.format(" 0x%016x: ", startAddress + (linesAround * bytesPerLine)));
		for (long i = 0L; i < bytesPerLine; i++) {
			final long x = startAddress + (linesAround * bytesPerLine) + i;
			printer.accept(x);
		}
		sb.append('\n');

		// print lines after
		for (long r = 0L; r < linesAround; r++) {
			sb.append(String.format(" 0x%016x: ", startAddress + ((linesAround + 1 + r) * bytesPerLine)));
			for (long i = 0L; i < bytesPerLine; i++) {
				final long x = startAddress + ((linesAround + 1 + r) * bytesPerLine) + i;
				printer.accept(x);
			}
			sb.append('\n');
		}

		sb.append('\n').append(message);

		return sb.toString();
	}

	private void checkLength(final int length) {
		if (length != 1 && length != 2 && length != 4 && length != 8) {
			throw new IllegalArgumentException(
					String.format("Invalid length: expected 1, 2, 4 or 8 but was %,d.", length));
		}
	}

	private void reportIllegalRead(final long address, final int length) {
		checkLength(length);
		throw new IllegalReadException(reportIllegalAccess(
				String.format(
						"Attempted %d-byte read at%s non-readable address 0x%x",
						length, isInitialized(address) ? "" : " uninitialized", address),
				address,
				length,
				"Readable",
				readableAddresses::get));
	}

	private void reportIllegalExecution(final long address) {
		checkLength(1);
		throw new IllegalExecutionException(reportIllegalAccess(
				String.format(
						"Attempted 1-byte execution at%s non-executable address 0x%x",
						isInitialized(address) ? "" : " uninitialized", address),
				address,
				1,
				"Executable",
				executableAddresses::get));
	}

	private void reportIllegalWrite(final long address, final int length) {
		checkLength(length);
		throw new IllegalWriteException(reportIllegalAccess(
				String.format(
						"Attempted %d-byte write at%s non-writable address 0x%x",
						length, isInitialized(address) ? "" : " uninitialized", address),
				address,
				length,
				"Writable",
				writableAddresses::get));
	}

	private void reportAccessToUninitialized(final long address, final int length) {
		checkLength(length);
		throw new AccessToUninitializedMemoryException(reportIllegalAccess(
				String.format("Attempted %d-byte access at uninitialized address 0x%x", length, address),
				address,
				length,
				"Initialized",
				mem::isInitialized));
	}

	/**
	 * Sets the given permissions of the contiguous block of memory starting at {@code start} long {@code numBytes}
	 * bytes.
	 *
	 * @param start The starting address of the memory block.
	 * @param numBytes The number of bytes of the memory block.
	 * @param readable If this block should be readable.
	 * @param writeable If this block should be writeable.
	 * @param executable If this block should be executable (i.e. containing code).
	 */
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	public void setPermissions(
			final long start,
			final long numBytes,
			final boolean readable,
			final boolean writeable,
			final boolean executable) {
		if (numBytes < 0L) {
			throw new IllegalArgumentException("Negative number of bytes.");
		}
		if (numBytes == 0L) {
			return;
		}

		readableAddresses.set(start, numBytes, readable);
		writableAddresses.set(start, numBytes, writeable);
		executableAddresses.set(start, numBytes, executable);
	}

	private void checkRead(final long address, final int length) {
		if (!breakOnWrongPermissions) {
			return;
		}
		for (int i = 0; i < length; i++) {
			if (!canRead(address + i)) {
				reportIllegalRead(address, length);
			}
		}
	}

	private void checkInitialized(final long address, final int length) {
		if (!breakWhenReadingUninitializedMemory) {
			return;
		}
		for (int i = 0; i < length; i++) {
			if (!isInitialized(address + i)) {
				reportAccessToUninitialized(address, length);
			}
		}
	}

	@Override
	public byte read(final long address) {
		checkRead(address, 1);
		checkInitialized(address, 1);
		return this.mem.read(address);
	}

	/**
	 * Reads 8 contiguous bytes starting from the given address (little-endian).
	 *
	 * @param address The address to start reading from.
	 * @return A 64-bit value read.
	 */
	@Override
	public long read8(final long address) {
		checkRead(address, 8);
		checkInitialized(address, 8);

		// Little-endian
		long x = 0x0000000000000000L;
		x |= BitUtils.asLong(mem.read(address));
		x |= (BitUtils.asLong(mem.read(address + 1L)) << 8);
		x |= (BitUtils.asLong(mem.read(address + 2L)) << 16);
		x |= (BitUtils.asLong(mem.read(address + 3L)) << 24);
		x |= (BitUtils.asLong(mem.read(address + 4L)) << 32);
		x |= (BitUtils.asLong(mem.read(address + 5L)) << 40);
		x |= (BitUtils.asLong(mem.read(address + 6L)) << 48);
		x |= (BitUtils.asLong(mem.read(address + 7L)) << 56);
		return x;
	}

	private void checkExecute(final long address) {
		if (!breakOnWrongPermissions) {
			return;
		}
		for (int i = 0; i < 1; i++) {
			if (!canExecute(address + i)) {
				reportIllegalExecution(address);
			}
		}
	}

	/**
	 * This behaves exactly like a normal read, but it checks execute permissions instead of read permissions.
	 *
	 * @param address The 64-bit address to read the instructions from.
	 * @return The instruction byte at the given address.
	 */
	public byte readCode(final long address) {
		checkExecute(address);
		checkInitialized(address, 1);
		return mem.read(address);
	}

	private void checkWrite(final long address, final int length) {
		if (!breakOnWrongPermissions) {
			return;
		}
		for (int i = 0; i < length; i++) {
			if (!canWrite(address + i)) {
				reportIllegalWrite(address, length);
			}
		}
	}

	@Override
	public void write(final long address, final byte value) {
		checkWrite(address, 1);
		mem.write(address, value);
	}

	/**
	 * Writes 8 contiguous bytes at the given address (little-endian).
	 *
	 * @param address The memory location where to write.
	 * @param value The 64-bit value to write.
	 */
	@Override
	public void write(final long address, final long value) {
		checkWrite(address, 8);
		initialize(address, BitUtils.asLEBytes(value));
	}

	@Override
	public boolean isInitialized(final long address) {
		return mem.isInitialized(address);
	}

	/**
	 * Writes the given value in the memory without checking nor modifying permissions.
	 *
	 * @param start The start of the address range.
	 * @param numBytes The length of the address range.
	 * @param value The 8-bit value to be written in each byte.
	 */
	public void initialize(final long start, final long numBytes, final byte value) {
		for (long i = 0L; i < numBytes; i++) {
			mem.write(start + i, value);
		}
	}

	/**
	 * Writes the given short in the memory, without checking nor modifying permissions, in little-endian.
	 *
	 * @param address The address where to write the value.
	 * @param value The value to be written.
	 */
	public void initialize(final long address, final short value) {
		initialize(address, BitUtils.asLEBytes(value));
	}

	/**
	 * Writes the given int in the memory, without checking nor modifying permissions, in little-endian.
	 *
	 * @param address The address where to write the value.
	 * @param value The value to be written.
	 */
	public void initialize(final long address, final int value) {
		initialize(address, BitUtils.asLEBytes(value));
	}

	/**
	 * Writes the given long in the memory, without checking nor modifying permissions, in little-endian.
	 *
	 * @param address The address where to write the value.
	 * @param value The value to be written.
	 */
	public void initialize(final long address, final long value) {
		initialize(address, BitUtils.asLEBytes(value));
	}

	/**
	 * Writes the given bytes in the memory without checking nor modifying permissions.
	 *
	 * @param start The start of the address range.
	 * @param values The non-null array of 8-bit values to be written.
	 */
	public void initialize(final long start, final byte[] values) {
		for (int i = 0; i < values.length; i++) {
			mem.write(start + i, values[i]);
		}
	}

	/**
	 * Writes the given byte in the memory without checking nor modifying permissions.
	 *
	 * @param address The address where to write the value.
	 * @param value The value to be written.
	 */
	public void initialize(final long address, final byte value) {
		mem.write(address, value);
	}

	@Override
	public String toString() {
		return "MemoryController(mem=" + mem + ";canRead="
				+ readableAddresses + ";canWrite="
				+ writableAddresses + ";canExecute="
				+ executableAddresses + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + mem.hashCode();
		h = 31 * h + readableAddresses.hashCode();
		h = 31 * h + writableAddresses.hashCode();
		h = 31 * h + executableAddresses.hashCode();
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
		if (!(other instanceof final MemoryController m)) {
			return false;
		}
		return this.mem.equals(m)
				&& this.readableAddresses.equals(m.readableAddresses)
				&& this.writableAddresses.equals(m.writableAddresses)
				&& this.executableAddresses.equals(m.executableAddresses);
	}
}
