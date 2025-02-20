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
package com.ledmington.id;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.ledmington.cpu.x86.InstructionDecoder;
import com.ledmington.cpu.x86.InstructionDecoderV1;
import com.ledmington.utils.MiniLogger;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Timeout(time = 5)
public class Decoding {

	static {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
	}

	// Bytes of the instruction 'cmp WORD PTR [r9+rcx*4+0x12345678],0xbeef'
	private final byte[] instructionBytes = {
		(byte) 0x66,
		(byte) 0x41,
		(byte) 0x81,
		(byte) 0xbc,
		(byte) 0x89,
		(byte) 0x78,
		(byte) 0x56,
		(byte) 0x34,
		(byte) 0x12,
		(byte) 0xef,
		(byte) 0xbe
	};
	private InstructionDecoder id;

	@Setup(Level.Invocation)
	public void setup() {
		id = new InstructionDecoderV1(instructionBytes);
	}

	@Benchmark
	public void parse(final Blackhole bh) {
		bh.consume(id.decode());
	}
}
