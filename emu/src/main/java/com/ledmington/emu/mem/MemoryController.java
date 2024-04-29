package com.ledmington.emu.mem;

import java.util.Objects;

import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.NoBitsSection;
import com.ledmington.elf.section.Section;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.IntervalArray;
import com.ledmington.utils.MiniLogger;

/**
 * This is the part of the memory which checks permissions
 */
public final class MemoryController implements Memory {

    private static final MiniLogger logger = MiniLogger.getLogger("mem");

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
     * @param startBlockAddress
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
            throw new IllegalArgumentException(String.format("Attempted read at non-readable address 0x%x", address));
        }
        return this.mem.read(address);
    }

    /**
     * This behaves exactly like a normal read but check execute permissions instead
     * of read permissions.
     */
    public byte readCode(final long address) {
        if (!canExecute.get(address)) {
            throw new IllegalArgumentException(
                    String.format("Attempted execute at non-executable address 0x%x", address));
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
     * Loads the given section into memory without checking write permissions.
     */
    public void loadSection(final Section sec) {
        Objects.requireNonNull(sec);

        if (sec instanceof NoBitsSection) {
            // allocate uninitialized data blocks
            final long startVirtualAddress = sec.header().virtualAddress();
            final long size = sec.header().sectionSize();
            logger.debug(
                    "Loading section '%s' in memory range 0x%x-0x%x (%,d bytes)",
                    sec.name(), startVirtualAddress, startVirtualAddress + size, size);
            for (long i = 0L; i < size; i++) {
                mem.write(startVirtualAddress + i, (byte) 0x00);
            }
        } else if (sec instanceof LoadableSection ls) {
            final long startVirtualAddress = sec.header().virtualAddress();
            final byte[] content = ls.content();
            logger.debug(
                    "Loading section '%s' in memory range 0x%x-0x%x (%,d bytes)",
                    sec.name(), startVirtualAddress, startVirtualAddress + content.length, content.length);
            for (int i = 0; i < content.length; i++) {
                mem.write(startVirtualAddress + BitUtils.asLong(i), content[i]);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "Don't know what to do with section '%s' of type %s",
                    sec.name(), sec.header().type().name()));
        }
    }
}
