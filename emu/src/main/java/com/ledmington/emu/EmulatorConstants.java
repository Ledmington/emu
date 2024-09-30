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
package com.ledmington.emu;

import java.util.Objects;
import java.util.function.Supplier;

import com.ledmington.mem.MemoryInitializer;

/** A class to hold constants for the emulator execution. */
public final class EmulatorConstants {

    /** The default memory initializer */
    private static Supplier<MemoryInitializer> memoryInitializer = MemoryInitializer::random;

    /** Base address where to load the executable file. */
    private static long baseAddress = 0x8000000000000000L;

    /** The default stack size. */
    private static long stackSize = 100_000_000L;

    private EmulatorConstants() {}

    /**
     * Allows to change the memory initializer of the emulator.
     *
     * @param memInit The new memory initializer.
     */
    public static void setMemoryInitializer(final Supplier<MemoryInitializer> memInit) {
        memoryInitializer = Objects.requireNonNull(memInit);
    }

    /**
     * Returns the default memory initializer.
     *
     * @return The default memory initializer.
     */
    public static MemoryInitializer getMemoryInitializer() {
        return memoryInitializer.get();
    }

    /**
     * Changes the default stack size.
     *
     * @param newStackSize The new default stack size.
     */
    public static void setStackSize(final long newStackSize) {
        final long minStackSize = 1L;
        if (newStackSize < minStackSize) {
            throw new IllegalArgumentException("Stack size must be at least 1");
        }
        stackSize = newStackSize;
    }

    /**
     * Returns the default stack size.
     *
     * @return The default stack size.
     */
    public static long getStackSize() {
        return stackSize;
    }

    /**
     * Changes the default base address.
     *
     * @param bAddr The new default base address.
     */
    public static void setBaseAddress(final long bAddr) {
        baseAddress = bAddr;
    }

    /**
     * Returns the default base address.
     *
     * @return The default base address.
     */
    public static long getbaseAddress() {
        return baseAddress;
    }
}
