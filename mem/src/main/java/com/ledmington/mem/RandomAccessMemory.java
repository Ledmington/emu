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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** A class to mimic the behavior of a real-world RAM. */
public final class RandomAccessMemory implements Memory {

    private final MemoryInitializer init;
    private final Map<Long, Byte> m = new HashMap<>();

    /**
     * Creates a RAM with the given memory initializer.
     *
     * @param init The memory initializer to be used when accessing uninitialized memory locations.
     */
    public RandomAccessMemory(final MemoryInitializer init) {
        this.init = Objects.requireNonNull(init);
    }

    @Override
    public byte read(final long address) {
        return m.containsKey(address) ? m.get(address) : init.get();
    }

    @Override
    public void write(final long address, final byte value) {
        m.put(address, value);
    }

    @Override
    public String toString() {
        return "RandomAccessMemory(initializer=" + init + "m=" + m + ')';
    }
}
