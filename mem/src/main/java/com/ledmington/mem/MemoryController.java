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
import java.util.function.Consumer;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.IntervalArray;

/** This is the part of the memory which implements read-write-execute permissions. */
public final class MemoryController implements Memory {

    private final Memory mem;
    private final IntervalArray canRead = new IntervalArray();
    private final IntervalArray canWrite = new IntervalArray();
    private final IntervalArray canExecute = new IntervalArray();
    private final boolean breakOnWrongPermissions;

    /**
     * Creates a MemoryController with the given initializer.
     *
     * @param memInit The {@link MemoryInitializer} object to be used.
     * @param breakOnWrongPermissions Decides whether this controller should throw an exception when accessing memory
     *     with the wrong permissions.
     */
    public MemoryController(final MemoryInitializer memInit, final boolean breakOnWrongPermissions) {
        this.mem = new RandomAccessMemory(Objects.requireNonNull(memInit));
        this.breakOnWrongPermissions = breakOnWrongPermissions;
    }

    public MemoryController(final MemoryInitializer memInit) {
        this(memInit, true);
    }

    private void reportIllegalRead(final long address) {
        reportIllegalAccess(
                String.format(
                        "Attempted read at%s non-readable address 0x%x",
                        isInitialized(address) ? "" : " uninitialized", address),
                address);
    }

    private void reportIllegalExecution(final long address) {
        reportIllegalAccess(
                String.format(
                        "Attempted execution at%s non-executable address 0x%x",
                        isInitialized(address) ? "" : " uninitialized", address),
                address);
    }

    private void reportIllegalWrite(final long address) {
        reportIllegalAccess(
                String.format(
                        "Attempted write at%s non-writable address 0x%x",
                        isInitialized(address) ? "" : " uninitialized", address),
                address);
    }

    private void reportIllegalAccess(final String message, final long address) {
        final String reset = "\u001B[0m";
        final String bold = "\u001B[1m";
        final String red = "\u001b[31m";
        final String green = "\u001b[32m";
        final String yellow = "\u001b[33m";
        final String blue = "\u001b[34m";
        final String magenta = "\u001b[35m";
        final String cyan = "\u001b[36m";
        final String white = "\u001b[37m";

        final long linesAround = 5;
        final long bytesPerLine = 16;

        // The given address must be at the center of the displayed memory portion
        final long startAddress = address - (bytesPerLine / 2 - 1) - (bytesPerLine * linesAround);

        final StringBuilder sb = new StringBuilder(128);

        sb.append("\nLegend:\n Uninitialized=xx ")
                .append(red)
                .append("Readable")
                .append(reset)
                .append(' ')
                .append(green)
                .append("Writable")
                .append(reset)
                .append(' ')
                .append(blue)
                .append("Executable")
                .append(reset)
                .append(' ')
                .append(yellow)
                .append("Read-Write")
                .append(reset)
                .append(' ')
                .append(magenta)
                .append("Read-Execute")
                .append(reset)
                .append(' ')
                .append(cyan)
                .append("Write-Execute")
                .append(reset)
                .append(' ')
                .append(white)
                .append("Read-Write-Execute")
                .append(reset)
                .append("\n\n");

        final Consumer<Long> printer = x -> {
            final String s = isInitialized(x) ? String.format("%02x", mem.read(x)) : "xx";
            sb.append(x == address ? '[' : ' ');
            if (x == address) {
                sb.append(bold);
            }
            final boolean r = canRead.get(x);
            final boolean w = canWrite.get(x);
            final boolean e = canExecute.get(x);
            if (r && !w && !e) {
                sb.append(red);
            }
            if (!r && w && !e) {
                sb.append(green);
            }
            if (!r && !w && e) {
                sb.append(blue);
            }
            if (r && w && !e) {
                sb.append(yellow);
            }
            if (r && !w && e) {
                sb.append(magenta);
            }
            if (!r && w && e) {
                sb.append(cyan);
            }
            if (r && w && e) {
                sb.append(white);
            }
            sb.append(s).append(reset).append(x == address ? ']' : ' ');
        };

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

        throw new MemoryException(sb.toString());
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
        if (breakOnWrongPermissions && !canRead.get(address)) {
            reportIllegalRead(address);
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
        if (breakOnWrongPermissions && !canExecute.get(address)) {
            reportIllegalExecution(address);
        }
        return this.mem.read(address);
    }

    @Override
    public void write(final long address, final byte value) {
        if (breakOnWrongPermissions && !canWrite.get(address)) {
            reportIllegalWrite(address);
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
