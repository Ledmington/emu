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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.mem.Memory;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.mem.RandomAccessMemory;
import com.ledmington.utils.BitUtils;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.JUnitTestContainsTooManyAsserts"})
final class TestExecution {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(System.nanoTime());
	private X86Cpu cpu = null;

	@BeforeEach
	void setup() {
		cpu = new X86Cpu(new MemoryController(new Memory() {
			@Override
			public byte read(final long address) {
				throw new UnsupportedOperationException(String.format("Attempted read at 0x%016x", address));
			}

			@Override
			public void write(final long address, final byte value) {
				throw new UnsupportedOperationException(
						String.format("Attempted write of 0x%02x at 0x%016x", value, address));
			}

			@Override
			public boolean isInitialized(final long address) {
				throw new UnsupportedOperationException(
						String.format("Attempted initialization check at 0x%016x", address));
			}
		}));

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
		cpu = null;
	}

	private static Stream<Arguments> pairs64() {
		return Arrays.stream(Register64.values())
				.flatMap(r -> Arrays.stream(Register64.values()).map(x -> Arguments.of(r, x)));
	}

	private static Stream<Arguments> pairs32() {
		return Arrays.stream(Register32.values())
				.flatMap(r -> Arrays.stream(Register32.values()).map(x -> Arguments.of(r, x)));
	}

	private static Stream<Arguments> pairs16() {
		return Arrays.stream(Register16.values())
				.flatMap(r -> Arrays.stream(Register16.values()).map(x -> Arguments.of(r, x)));
	}

	private static Stream<Arguments> pairs8() {
		return Arrays.stream(Register8.values())
				.flatMap(r -> Arrays.stream(Register8.values()).map(x -> Arguments.of(r, x)));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void add64(final Register64 a, final Register64 b) {
		final long oldValue1 = cpu.getRegisters().get(a);
		final long oldValue2 = cpu.getRegisters().get(b);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final long result = oldValue1 + oldValue2;
		expected.set(a, result);
		expected.set(RFlags.ZERO, result == 0L);
		cpu.executeOne(new Instruction(Opcode.ADD, a, b));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs32")
	void add32(final Register32 a, final Register32 b) {
		final int oldValue1 = cpu.getRegisters().get(a);
		final int oldValue2 = cpu.getRegisters().get(b);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final int result = oldValue1 + oldValue2;
		expected.set(a, result);
		expected.set(RFlags.ZERO, result == 0L);
		cpu.executeOne(new Instruction(Opcode.ADD, a, b));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs16")
	void add16(final Register16 a, final Register16 b) {
		final short oldValue1 = cpu.getRegisters().get(a);
		final short oldValue2 = cpu.getRegisters().get(b);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final short result = BitUtils.asShort(oldValue1 + oldValue2);
		expected.set(a, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.ADD, a, b));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs8")
	void add8(final Register8 a, final Register8 b) {
		final byte oldValue1 = cpu.getRegisters().get(a);
		final byte oldValue2 = cpu.getRegisters().get(b);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final byte result = BitUtils.asByte(oldValue1 + oldValue2);
		expected.set(a, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.ADD, a, b));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void sub64(final Register64 r1, final Register64 r2) {
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final long result = oldValue1 - oldValue2;
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0L);
		cpu.executeOne(new Instruction(Opcode.SUB, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs32")
	void sub32(final Register32 r1, final Register32 r2) {
		final int oldValue1 = cpu.getRegisters().get(r1);
		final int oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final int result = oldValue1 - oldValue2;
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.SUB, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs16")
	void sub16(final Register16 r1, final Register16 r2) {
		final short oldValue1 = cpu.getRegisters().get(r1);
		final short oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final short result = BitUtils.asShort(oldValue1 - oldValue2);
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.SUB, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs8")
	void sub8(final Register8 r1, final Register8 r2) {
		final byte oldValue1 = cpu.getRegisters().get(r1);
		final byte oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final byte result = BitUtils.asByte(oldValue1 - oldValue2);
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.SUB, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void xor(final Register64 r1, final Register64 r2) {
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue1 ^ oldValue2);
		cpu.executeOne(new Instruction(Opcode.XOR, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void and(final Register64 r1, final Register64 r2) {
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue1 & oldValue2);
		cpu.executeOne(new Instruction(Opcode.AND, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void mov64(final Register64 r1, final Register64 r2) {
		final long oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue2);
		cpu.executeOne(new Instruction(Opcode.MOV, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs32")
	void mov32(final Register32 r1, final Register32 r2) {
		final int oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue2);
		cpu.executeOne(new Instruction(Opcode.MOV, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs16")
	void mov16(final Register16 r1, final Register16 r2) {
		final short oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue2);
		cpu.executeOne(new Instruction(Opcode.MOV, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs8")
	void mov8(final Register8 r1, final Register8 r2) {
		final byte oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue2);
		cpu.executeOne(new Instruction(Opcode.MOV, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@Test
	void push() {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final X86Cpu cpu = new X86Cpu(mem);
		final long base = rng.nextLong();
		mem.setPermissions(base - 8L, base, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RSP, new Immediate(base)));
		mem.write(base - 8L, 0L);
		final long val = rng.nextLong();
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RAX, new Immediate(val)));

		cpu.executeOne(new Instruction(Opcode.PUSH, Register64.RAX));

		final long newRSP = base - 8L;
		assertEquals(
				newRSP,
				cpu.getRegisters().get(Register64.RSP),
				() -> String.format(
						"Expected 0x%016x but was 0x%016x.",
						newRSP, cpu.getRegisters().get(Register64.RSP)));
		assertEquals(
				val,
				mem.read8(newRSP),
				() -> String.format("Expected 0x%016x but was 0x%016x.", val, mem.read8(newRSP)));
	}

	@Test
	void pop() {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final X86Cpu cpu = new X86Cpu(mem);
		final long base = rng.nextLong();
		mem.setPermissions(base, base + 7L, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RSP, new Immediate(base)));
		final long val = rng.nextLong();
		mem.write(base, val);

		cpu.executeOne(new Instruction(Opcode.POP, Register64.RAX));

		final long newRSP = base + 8L;
		assertEquals(
				val,
				cpu.getRegisters().get(Register64.RAX),
				() -> String.format(
						"Expected 0x%016x but was 0x%016x.",
						val, cpu.getRegisters().get(Register64.RAX)));
		assertEquals(
				newRSP,
				cpu.getRegisters().get(Register64.RSP),
				() -> String.format(
						"Expected 0x%016x but was 0x%016x.",
						newRSP, cpu.getRegisters().get(Register64.RSP)));
	}

	@Test
	void pushAndPop() {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final X86Cpu cpu = new X86Cpu(mem);
		final long base = rng.nextLong();
		mem.setPermissions(base - 8L, base, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RSP, new Immediate(base)));
		final long val = rng.nextLong();
		cpu.executeOne(new Instruction(Opcode.MOV, Register64.RAX, new Immediate(val)));
		mem.write(base - 8L, 0L);

		cpu.executeOne(new Instruction(Opcode.PUSH, Register64.RAX));
		cpu.executeOne(new Instruction(Opcode.POP, Register64.RAX));

		assertEquals(
				base,
				cpu.getRegisters().get(Register64.RSP),
				() -> String.format(
						"Expected 0x%016x but was 0x%016x.",
						base, cpu.getRegisters().get(Register64.RSP)));
		assertEquals(
				val,
				cpu.getRegisters().get(Register64.RAX),
				() -> String.format(
						"Expected 0x%016x but was 0x%016x.",
						val, cpu.getRegisters().get(Register64.RAX)));
	}
}
