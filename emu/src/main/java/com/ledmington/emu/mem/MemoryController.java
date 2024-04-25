package com.ledmington.emu.mem;

import java.util.Objects;

/**
 * This is the part of the memory which checks permissions
 */
public final class MemoryController implements Memory {

    private final Memory mem;

    public MemoryController(final Memory mem) {
        this.mem = Objects.requireNonNull(mem);
    }

    /**
     * Sets the given permissions of the contiguous block of memory starting at {@code startBlockAddress} (inclusive) and ending at {@code endBlockAddress} (exclusive).
     *
     * @param startBLockAddress
     * 		The start (inclusive) of the memory block.
     * @param endBlockAddress
     * 		The end (exclusive) of the memory block.
     * @param readable
     * 		If this block should be readable.
     * @param writeable
     * 		If this block should be writeable.
     * @param executable
     * 		If this block should be executable (i.e. containing code).
     */
    public void setPermissions(
            final long startBLockAddress,
            final long endBlockAddress,
            final boolean readable,
            final boolean writeable,
            final boolean executable) {
        if (endBlockAddress < startBLockAddress) {
            throw new IllegalArgumentException(String.format(
                    "Invalid endBlockAddress (0x%016x) was less than startBlockAddress (0x%016x)",
                    startBLockAddress, endBlockAddress));
        }
        // TODO
    }

    @Override
    public byte read(final long address) {
        return this.mem.read(address);
    }

    @Override
    public void write(final long address, final byte value) {
        mem.write(address, value);
    }
}
