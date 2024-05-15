package com.ledmington.emu.mem;

/**
 * A common interface for emulated RAMs, caches and hard drives.
 * <p>
 * It is designed to map 64-bit addresses to 8-bit values/words.
 */
public interface Memory {

    /**
     * Reads a single byte from the given address.
     *
     * @param address
     *      The address to read from.
     * @return
     *      The byte word contained at the given address.
     */
    byte read(final long address);

    /**
     * Writes the given byte word at the given address, overwriting any value previously stored at that location.
     *
     * @param address
     *      The address to write at.
     * @param value
     *      The value to write.
     */
    void write(final long address, final byte value);
}
