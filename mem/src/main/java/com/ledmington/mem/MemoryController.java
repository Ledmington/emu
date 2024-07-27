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
package com.ledmington.mem;

import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.IntervalArray;

/** This is the part of the memory which implements read-write-execute permissions. */
public final class MemoryController implements Memory {

    private final Memory mem;
    private final IntervalArray canRead = new IntervalArray();
    private final IntervalArray canWrite = new IntervalArray();
    private final IntervalArray canExecute = new IntervalArray();

    /**
     * Creates a MemoryController with the given initializer.
     *
     * @param memInit The {@link MemoryInitializer} object to be used.
     */
    public MemoryController(final MemoryInitializer memInit) {
        this.mem = new RandomAccessMemory(Objects.requireNonNull(memInit));
    }

    /**
     * Sets the given permissions of the contiguous block of memory starting at {@code startBlockAddress} (inclusive)
     * and ending at {@code endBlockAddress} (exclusive).
     *
     * @param startBlockAddress The start (inclusive) of the memory block.
     * @param endBlockAddress The end (exclusive) of the memory block.
     * @param readable If this block should be readable.
     * @param writeable If this block should be writeable.
     * @param executable If this block should be executable (i.e. containing code).
     */
    public void setPermissions(
            final long startBlockAddress,
            final long endBlockAddress,
            final boolean readable,
            final boolean writeable,
            final boolean executable) {
        if (endBlockAddress < startBlockAddress) {
            throw new IllegalArgumentException(String.format(
                    "Invalid endBlockAddress (0x%x) was less than startBlockAddress (0x%x)",
                    startBlockAddress, endBlockAddress));
        }

        if (readable) {
            canRead.set(startBlockAddress, endBlockAddress);
        } else {
            canRead.reset(startBlockAddress, endBlockAddress);
        }

        if (writeable) {
            canWrite.set(startBlockAddress, endBlockAddress);
        } else {
            canWrite.reset(startBlockAddress, endBlockAddress);
        }

        if (executable) {
            canExecute.set(startBlockAddress, endBlockAddress);
        } else {
            canExecute.reset(startBlockAddress, endBlockAddress);
        }
    }

    @Override
    public byte read(final long address) {
        if (!canRead.get(address)) {
            throw new IllegalArgumentException(String.format(
                    "Attempted read at%s non-readable address 0x%x",
                    isInitialized(address) ? "" : " uninitialized", address));
        }
        return this.mem.read(address);
    }

    /**
     * Reads 8 contiguous byte starting from the given address.
     *
     * @param address The address to start reading from.
     * @return A 64-bit value read.
     */
    public long read8(final long address) {
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
     * This behaves exactly like a normal read but it checks execute permissions instead of read permissions.
     *
     * @param address The 64-bit address to read the instructions from.
     * @return The instruction byte at the given address.
     */
    public byte readCode(final long address) {
        if (!canExecute.get(address)) {
            throw new IllegalArgumentException(String.format(
                    "Attempted execute at%s non-executable address 0x%x",
                    isInitialized(address) ? "" : " uninitialized", address));
        }
        return this.mem.read(address);
    }

    @Override
    public void write(final long address, final byte value) {
        if (!canWrite.get(address)) {
            throw new IllegalArgumentException(
                    String.format("Attempted write of value 0x%02x at non-writeable address 0x%x", value, address));
        }
        mem.write(address, value);
    }

    /**
     * Writes 8 contiguous bytes at the given address.
     *
     * @param address The memory location where to write.
     * @param value The 64-bit value to write.
     */
    public void write(final long address, final long value) {
        write(address, BitUtils.asByte(value >> 56));
        write(address + 1L, BitUtils.asByte(value >> 48));
        write(address + 2L, BitUtils.asByte(value >> 40));
        write(address + 3L, BitUtils.asByte(value >> 32));
        write(address + 4L, BitUtils.asByte(value >> 24));
        write(address + 5L, BitUtils.asByte(value >> 16));
        write(address + 6L, BitUtils.asByte(value >> 8));
        write(address + 7L, BitUtils.asByte(value));
    }

    @Override
    public boolean isInitialized(final long address) {
        return mem.isInitialized(address);
    }

    /**
     * Writes the given value in the memory withuot checking nor modifying permissions.
     *
     * @param start The start of the address range.
     * @param bytes The length of the address range.
     * @param value The 8-bit value to be written in each byte.
     */
    public void initialize(final long start, final long bytes, final byte value) {
        for (long i = 0L; i < bytes; i++) {
            mem.write(start + i, value);
        }
    }

    /**
     * Writes the given bytes in the memory withuot checking nor modifying permissions.
     *
     * @param start The start of the address range.
     * @param values The non-null array of 8-bit values to be written.
     */
    public void initialize(final long start, final byte[] values) {
        for (long i = 0L; i < values.length; i++) {
            mem.write(start + i, values[BitUtils.asInt(i)]);
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
                + canRead + ";canWrite="
                + canWrite + ";canExecute="
                + canExecute + ')';
    }
}
