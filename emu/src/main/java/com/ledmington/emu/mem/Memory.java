package com.ledmington.emu.mem;

/**
 * A common interface for emulated RAMs, caches and hard drives.
 */
public interface Memory {
    byte read(final long address);

    void write(final long address, final byte value);
}
