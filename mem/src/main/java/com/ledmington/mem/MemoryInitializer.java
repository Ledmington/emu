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

import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import com.ledmington.utils.BitUtils;

/** A procedure to initialize the memory. */
public interface MemoryInitializer extends Supplier<Byte> {

	/**
	 * Initializes the memory with random values, mimicking the "garbage" values you usually get with a real computer
	 * when reading uninitialized memory. This is useful for debugging since successive reads from the same
	 * uninitialized address will return different values.
	 *
	 * @return A memory initializer which returns always a random value.
	 */
	static MemoryInitializer random() {
		final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());
		return () -> BitUtils.asByte(rng.nextInt());
	}

	/**
	 * Initializes the memory to all zeroes.
	 *
	 * @return A memory initializer which return always the zero byte.
	 */
	static MemoryInitializer zero() {
		return () -> (byte) 0x00;
	}
}
