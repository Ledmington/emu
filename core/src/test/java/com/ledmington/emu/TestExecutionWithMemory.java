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
import org.junit.jupiter.params.provider.ValueSource;

import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.InstructionEncoder;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.mem.RandomAccessMemory;
import com.ledmington.utils.BitUtils;

final class TestExecutionWithMemory {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);
	private MemoryController mem = null;
	private X86Cpu cpu = null;

	@BeforeEach
	void setup() {
		final DebuggingX86RegisterFile rf = new DebuggingX86RegisterFile(rng);
		mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		cpu = new X86Cpu(mem, rf, true);
	}

	@AfterEach
	void teardown() {
		mem = null;
		cpu = null;
	}

	private static Stream<Arguments> pairs64() {
		return Arrays.stream(Register64.values())
				.filter(r -> !r.equals(Register64.RIP))
				.flatMap(r -> Arrays.stream(Register64.values())
						.filter(x -> !x.equals(r) && !x.equals(Register64.RIP))
						.map(x -> Arguments.of(r, x)));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void movMem64R64(final Register64 r1, final Register64 r2) {
		final long oldValue1 = rng.nextLong();
		final long oldValue2 = rng.nextLong();
		cpu.executeOne(new Instruction(Opcode.MOVABS, r1, new Immediate(oldValue1)));
		cpu.executeOne(new Instruction(Opcode.MOVABS, r2, new Immediate(oldValue2)));
		mem.initialize(oldValue1, 8L, (byte) 0x00);
		mem.setPermissions(oldValue1, oldValue1 + 7L, false, true, false);
		cpu.executeOne(new Instruction(
				Opcode.MOV,
				IndirectOperand.builder()
						.base(r1)
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
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void movR64Mem64(final Register64 r1, final Register64 r2) {
		final long oldValue2 = rng.nextLong();
		cpu.executeOne(new Instruction(Opcode.MOVABS, r2, new Immediate(oldValue2)));
		final long val = rng.nextLong();
		mem.initialize(oldValue2, BitUtils.asBEBytes(val));
		mem.setPermissions(oldValue2, oldValue2 + 7L, true, false, false);
		cpu.executeOne(new Instruction(
				Opcode.MOV,
				r1,
				IndirectOperand.builder()
						.base(r2)
						.pointer(PointerSize.QWORD_PTR)
						.build()));
		final long x = mem.read8(oldValue2);
		assertEquals(
				val,
				x,
				() -> String.format("Expected memory at 0x%016x to be 0x%016x but was 0x%016x.", oldValue2, val, x));
	}

	@Test
	void push() {
		final long base = rng.nextLong();
		mem.setPermissions(base - 8L, base, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSP, new Immediate(base)));
		mem.write(base - 8L, 0L);
		final long val = rng.nextLong();
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RAX, new Immediate(val)));

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
		final long base = rng.nextLong();
		mem.setPermissions(base, base + 7L, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSP, new Immediate(base)));
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

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
	void pushAndPop(final int n) {

		// Init random values
		final long[] values = new long[n];
		for (int i = 0; i < n; i++) {
			values[i] = rng.nextLong();
		}

		// Setup stack at random location
		final long base = rng.nextLong();
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSP, new Immediate(base)));

		// Doing n pushes of random values
		for (int i = 0; i < n; i++) {
			// Set memory to readable and writable
			mem.setPermissions(base - 8L * (i + 1), base - 8L * i, true, true, false);

			// mov RAX,val
			cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RAX, new Immediate(values[i])));

			final int finalI = i;

			// Before each push, RSP must be equal to the old base
			assertEquals(
					base - 8L * i,
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"Before %,d-th PUSH, expected RSP to be 0x%016x but was 0x%016x.",
							finalI, base - 8L * finalI, cpu.getRegisters().get(Register64.RSP)));

			// push RAX
			cpu.executeOne(new Instruction(Opcode.PUSH, Register64.RAX));

			// After each push, RSP must be equal to the new base
			assertEquals(
					base - 8L * (i + 1),
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"After %,d-th PUSH, expected RSP to be 0x%016x but was 0x%016x.",
							finalI, base - 8L * (finalI + 1), cpu.getRegisters().get(Register64.RSP)));
		}

		// Doing n pops (in reverse) checking the random values
		for (int i = n - 1; i >= 0; i--) {
			final int finalI = i;

			// Before each pop, RSP must be equal to the new base
			assertEquals(
					base - 8L * (i + 1),
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"Before %,d-th POP, expected RSP to be 0x%016x but was 0x%016x.",
							finalI, base - 8L * (finalI + 1), cpu.getRegisters().get(Register64.RSP)));

			// pop RAX
			cpu.executeOne(new Instruction(Opcode.POP, Register64.RAX));

			// After each pop, RSP must be equal to the old base
			assertEquals(
					base - 8L * i,
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"After %,d-th POP, expected RSP to be 0x%016x but was 0x%016x.",
							finalI, base - 8L * finalI, cpu.getRegisters().get(Register64.RSP)));

			assertEquals(
					values[i],
					cpu.getRegisters().get(Register64.RAX),
					() -> String.format(
							"After %,d-th POP, expected RAX to be 0x%016x but was 0x%016x.",
							finalI, values[finalI], cpu.getRegisters().get(Register64.RAX)));
		}

		// At the end RSP must be equal to the initial base value
		assertEquals(
				base,
				cpu.getRegisters().get(Register64.RSP),
				() -> String.format(
						"At the end, expected RSP to be 0x%016x but was 0x%016x.",
						base, cpu.getRegisters().get(Register64.RSP)));
	}

	@Test
	void callAndReturn() {
		// Setup stack at random location (8-byte aligned)
		final long stackBase = rng.nextLong() & 0xfffffffffffffff0L;
		mem.setPermissions(stackBase, stackBase, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSP, new Immediate(stackBase)));

		// Setup RIP at random location
		final long rip = rng.nextLong();
		cpu.setInstructionPointer(rip);

		// Write an empty function somewhere in memory (only RET instruction)
		// Ensure that the offset between RIP and the function fits in a 32-bit immediate
		final int offset = rng.nextInt();
		final long functionAddress = rip + BitUtils.asLong(offset);
		final byte[] functionCode = InstructionEncoder.toHex(new Instruction(Opcode.RET));
		mem.initialize(functionAddress, functionCode);
		mem.setPermissions(functionAddress, functionAddress + (long) functionCode.length - 1L, false, false, true);

		// Write the code to be executed at RIP (just a CALL to the function and a HLT)
		final int callInstructionLength =
				InstructionEncoder.toHex(new Instruction(Opcode.CALL, new Immediate(0))).length;
		final Instruction callInstruction = new Instruction(Opcode.CALL, new Immediate(offset - callInstructionLength));
		final byte[] mainCode = InstructionEncoder.toHex(callInstruction, new Instruction(Opcode.HLT));
		mem.initialize(rip, mainCode);
		mem.setPermissions(rip, rip + (long) mainCode.length - 1L, false, false, true);

		// Start the CPU
		cpu.execute();
	}

	@Test
	void callAllocateAndReturn() {
		// Setup stack at random location (8-byte aligned)
		final long stackBase = rng.nextLong() & 0xfffffffffffffff0L;
		mem.initialize(stackBase - 16L - 4L, stackBase, (byte) 0);
		mem.setPermissions(stackBase - 16L - 4L, stackBase, true, true, false);
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RSP, new Immediate(stackBase)));
		cpu.executeOne(new Instruction(Opcode.MOVABS, Register64.RBP, new Immediate(stackBase)));

		// Setup RIP at random location
		final long rip = rng.nextLong();
		cpu.setInstructionPointer(rip);

		// Write a function which just allocates a variable
		// Ensure that the offset between RIP and the function fits in a 32-bit immediate
		final int offset = Math.abs(rng.nextInt());
		final long functionAddress = rip + BitUtils.asLong(offset);
		final byte[] functionCode = InstructionEncoder.toHex(
				// code adapted from this one: https://godbolt.org/z/W8Kjj6Woz
				new Instruction(Opcode.PUSH, Register64.RBP),
				new Instruction(Opcode.MOV, Register64.RBP, Register64.RSP),
				new Instruction(
						Opcode.MOV,
						IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.base(Register64.RBP)
								.displacement((byte) -4)
								.build(),
						new Immediate(rng.nextInt())),
				new Instruction(Opcode.POP, Register64.RBP),
				new Instruction(Opcode.RET));
		mem.initialize(functionAddress, functionCode);
		mem.setPermissions(functionAddress, functionAddress + functionCode.length - 1L, false, false, true);

		// Write the code to be executed at RIP (just a CALL to the function and a HLT)
		final int callInstructionLength =
				InstructionEncoder.toHex(new Instruction(Opcode.CALL, new Immediate(0))).length;
		final Instruction callInstruction = new Instruction(Opcode.CALL, new Immediate(offset - callInstructionLength));
		final byte[] mainCode = InstructionEncoder.toHex(callInstruction, new Instruction(Opcode.HLT));
		mem.initialize(rip, mainCode);
		mem.setPermissions(rip, rip + (long) mainCode.length - 1L, false, false, true);

		// Start the CPU
		cpu.execute();
	}
}
