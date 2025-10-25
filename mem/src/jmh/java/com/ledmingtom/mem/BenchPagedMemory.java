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

import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;

import com.ledmington.utils.MiniLogger;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Timeout(time = 5)
public class BenchPagedMemory {

	static {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
	}

	private static final long startAddress = 0x1234_5678L;
	private static final int numBytes = 1_000_000;
	private final Memory mem = new PagedMemory(MemoryInitializer.zero());
	private final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());

	public BenchPagedMemory() {
		for (int i = 0; i < numBytes; i++) {
			mem.write(startAddress + i, (byte) 0x99);
		}
	}

	@Benchmark
	public void readInitializedAddress() {
		mem.read(rng.nextLong(startAddress, startAddress + numBytes));
	}

	@Benchmark
	public void readUninitializedAddress() {
		mem.read(rng.nextLong(Long.MIN_VALUE, -startAddress));
	}
}
