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

/** A 64-bit memory address. */
public record MemoryAddress(long address) implements Comparable<MemoryAddress> {
	public MemoryAddress plus(final long offset) {
		return new MemoryAddress(address + offset);
	}

	@Override
	public int compareTo(final MemoryAddress other) {
		return Long.compareUnsigned(address, other.address);
	}

	@Override
	public String toString() {
		return String.format("MemoryAddress(address=0x%016x)", address);
	}
}
