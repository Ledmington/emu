package com.ledmington.emu.mem;

import java.util.Objects;

import com.ledmington.elf.section.NoteSection;
import com.ledmington.elf.section.ProgBitsSection;
import com.ledmington.elf.section.Section;
import com.ledmington.utils.IntervalArray;

/**
 * This is the part of the memory which checks permissions
 */
public final class MemoryController implements Memory {

    private final Memory mem;
    private final IntervalArray canRead = new IntervalArray();
    private final IntervalArray canWrite = new IntervalArray();
    private final IntervalArray canExecute = new IntervalArray();

    public MemoryController(final Memory mem) {
        this.mem = Objects.requireNonNull(mem);
    }

    /**
     * Sets the given permissions of the contiguous block of memory starting at
     * {@code startBlockAddress} (inclusive) and ending at {@code endBlockAddress}
     * (exclusive).
     *
     * @param startBLockAddress
     *                          The start (inclusive) of the memory block.
     * @param endBlockAddress
     *                          The end (exclusive) of the memory block.
     * @param readable
     *                          If this block should be readable.
     * @param writeable
     *                          If this block should be writeable.
     * @param executable
     *                          If this block should be executable (i.e. containing
     *                          code).
     */
    public void setPermissions(
            final long startBlockAddress,
            final long endBlockAddress,
            final boolean readable,
            final boolean writeable,
            final boolean executable) {
        if (endBlockAddress < startBlockAddress) {
            throw new IllegalArgumentException(String.format(
                    "Invalid endBlockAddress (0x%016x) was less than startBlockAddress (0x%016x)",
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
            throw new IllegalArgumentException(
                    String.format("Attempted read at non-readable address 0x%016x", address));
        }
        return this.mem.read(address);
    }

    @Override
    public void write(final long address, final byte value) {
        if (!canWrite.get(address)) {
            throw new IllegalArgumentException(
                    String.format("Attempted write of value 0x%02x at non-writeable address 0x%016x", value, address));
        }
        mem.write(address, value);
    }

    /**
     * Loads the given section into memory without checking write permissions.
     */
    public void loadSection(final Section sec) {
        Objects.requireNonNull(sec);

        // TODO: optimize into a kind of memcpy
        final long startVirtualAddress = sec.header().virtualAddress();

        switch (sec) {
            case ProgBitsSection pbs -> {
                final byte[] content = pbs.content();
                for (long i = 0L; i < sec.header().size(); i++) {
                    mem.write(startVirtualAddress + i, content[(int) i]);
                }
            }
            case NoteSection ns -> {
                final byte[] content = ns.content();
                for (long i = 0L; i < sec.header().size(); i++) {
                    mem.write(startVirtualAddress + i, content[(int) i]);
                }
            }
            default -> throw new IllegalArgumentException(String.format(
                    "Don't know what to do with section '%s' of type %s",
                    sec.name(), sec.header().type().name()));
        }
    }
}
