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
package com.ledmington.emu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.mem.RandomAccessMemory;
import com.ledmington.utils.BitUtils;

final class TestExecutionWithMemory {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(System.nanoTime());
	private MemoryController mem = null;
	private X86Cpu cpu = null;

	@BeforeEach
	void setup() {
		mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		cpu = new X86Cpu(mem);

		final RegisterFile rf = (RegisterFile) cpu.getRegisters();

		// set registers to random values
		for (final Register64 r : Register64.values()) {
			rf.set(r, rng.nextLong());
		}
		for (final Register16 r : new Register16[] {
			Register16.CS, Register16.DS, Register16.ES, Register16.FS, Register16.GS, Register16.SS
		}) {
			rf.set(r, BitUtils.asShort(rng.nextInt()));
		}
	}

	@AfterEach
	void teardown() {
		mem = null;
		cpu = null;
	}

	private static Stream<Arguments> pairs64() {
		return Arrays.stream(Register64.values())
				.flatMap(r -> Arrays.stream(Register64.values()).map(x -> Arguments.of(r, x)));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void movMem64R64(final Register64 r1, final Register64 r2) {
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		mem.initialize(oldValue1, 8L, (byte) 0x00);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		mem.setPermissions(oldValue1, oldValue1 + 7L, false, true, false);
		cpu.executeOne(new Instruction(
				Opcode.MOV,
				IndirectOperand.builder()
						.index(r1)
						.pointer(PointerSize.QWORD_PTR)
						.build(),
				r2));
		mem.setPermissions(oldValue1, oldValue1 + 7L, true, false, false);
		final long x = mem.read8(oldValue1);
		assertEquals(
				oldValue2,
				x,
				() -> String.format(
						"Expected memory at 0x%016x to be 0x%016x but was 0x%016x.", oldValue1, oldValue2, x));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void movR64Mem64(final Register64 r1, final Register64 r2) {
		final long oldValue2 = cpu.getRegisters().get(r2);
		final long val = rng.nextLong();
		mem.initialize(oldValue2, new byte[] {
			BitUtils.asByte(val),
			BitUtils.asByte(val >>> 8),
			BitUtils.asByte(val >>> 16),
			BitUtils.asByte(val >>> 24),
			BitUtils.asByte(val >>> 32),
			BitUtils.asByte(val >>> 40),
			BitUtils.asByte(val >>> 48),
			BitUtils.asByte(val >>> 56),
		});
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, val);
		mem.setPermissions(oldValue2, oldValue2 + 7L, true, false, false);
		cpu.executeOne(new Instruction(
				Opcode.MOV,
				r1,
				IndirectOperand.builder()
						.index(r2)
						.pointer(PointerSize.QWORD_PTR)
						.build()));
		final long x = mem.read8(oldValue2);
		assertEquals(
				val,
				x,
				() -> String.format("Expected memory at 0x%016x to be 0x%016x but was 0x%016x.", oldValue2, val, x));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}
}
