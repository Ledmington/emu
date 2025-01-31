/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** A class to mimic the behaviour of a real-world RAM. */
public final class RandomAccessMemory implements Memory {

	private final MemoryInitializer init;
	private final Map<Long, Byte> m = new ConcurrentHashMap<>();

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
		return isInitialized(address) ? m.get(address) : init.get();
	}

	@Override
	public void write(final long address, final byte value) {
		m.put(address, value);
	}

	@Override
	public boolean isInitialized(final long address) {
		return m.containsKey(address);
	}

	@Override
	public String toString() {
		return "RandomAccessMemory(initializer=" + init + ";m=" + m + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + init.hashCode();
		h = 31 * h + m.hashCode();
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
		if (!(other instanceof final RandomAccessMemory ram)) {
			return false;
		}
		return this.init.equals(ram.init) && this.m.equals(ram.m);
	}
}
