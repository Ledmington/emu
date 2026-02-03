/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import com.ledmington.cpu.InstructionEncoder;
import com.ledmington.cpu.x86.GeneralInstruction;
import com.ledmington.cpu.x86.Immediate;
import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.mem.MemoryController;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.mem.PagedMemory;
import com.ledmington.utils.BitUtils;

final class TestExecutionWithMemory {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	/** Length of a CALL instruction with a 32-bit offset (5 bytes). */
	private static final int CALL_INSTRUCTION_LENGTH =
			InstructionEncoder.toHex(true, new GeneralInstruction(Opcode.CALL, new Immediate(0))).length;

	private RegisterFile rf = null;
	private MemoryController mem = null;

	@BeforeEach
	void setup() {
		rf = new DebuggingX86RegisterFile(rng);
		mem = new MemoryController(new PagedMemory(MemoryInitializer.random()), true, true);
	}

	@AfterEach
	void teardown() {
		rf = null;
		mem = null;
	}

	private static Stream<Arguments> pairs64() {
		return Arrays.stream(Register64.values())
				.filter(r -> r != Register64.RIP)
				.flatMap(r -> Arrays.stream(Register64.values())
						.filter(x -> x != r && x != Register64.RIP)
						.map(x -> Arguments.of(r, x)));
	}

	private void set(final X86Cpu cpu, final Register64 r, final long value) {
		cpu.executeOne(new GeneralInstruction(Opcode.MOVABS, r, new Immediate(value)));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void movMem64R64(final Register64 r1, final Register64 r2) {
		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.build();
		final long oldValue1 = rng.nextLong();
		final long oldValue2 = rng.nextLong();
		set(cpu, r1, oldValue1);
		set(cpu, r2, oldValue2);
		mem.initialize(oldValue1, 8L, (byte) 0x00);
		mem.setPermissions(oldValue1, 8L, false, true, false);
		cpu.executeOne(new GeneralInstruction(
				Opcode.MOV,
				IndirectOperand.builder()
						.pointer(PointerSize.QWORD_PTR)
						.base(r1)
						.build(),
				r2));
		mem.setPermissions(oldValue1, 8L, true, false, false);
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
		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.build();
		final long oldValue2 = rng.nextLong();
		set(cpu, r2, oldValue2);
		final long val = rng.nextLong();
		mem.initialize(oldValue2, val);
		mem.setPermissions(oldValue2, 8L, true, false, false);
		cpu.executeOne(new GeneralInstruction(
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
		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.build();

		final long base = rng.nextLong();
		mem.setPermissions(base - 8L, base, true, true, false);
		set(cpu, Register64.RSP, base);
		mem.write(base - 8L, 0L);
		final long val = rng.nextLong();
		set(cpu, Register64.RAX, val);

		cpu.executeOne(new GeneralInstruction(Opcode.PUSH, Register64.RAX));

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
	void pushOnFullStack() {
		final long stackTop = ELFLoader.alignAddress(rng.nextLong());
		final long stackSize = 1024L;
		final long stackBottom = stackTop - stackSize;

		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.stackTop(stackTop)
				.stackSize(stackSize)
				.build();

		set(cpu, Register64.RSP, stackTop);
		mem.setPermissions(stackBottom, stackSize, true, true, false);
		mem.initialize(stackBottom, stackSize, (byte) 0x00);

		final long val = rng.nextLong();
		set(cpu, Register64.RAX, val);
		final Instruction push = new GeneralInstruction(Opcode.PUSH, Register64.RAX);
		final long maxStackValues = stackSize / 8L;
		for (long i = 0L; i < maxStackValues; i++) {
			assertDoesNotThrow(
					() -> cpu.executeOne(push),
					() -> String.format(
							"Expected executing '%s' on a non-full stack to work fine but it did not.",
							InstructionEncoder.toIntelSyntax(push, false)));
		}
		assertThrows(
				StackOverflow.class,
				() -> cpu.executeOne(push),
				() -> String.format(
						"Expected executing '%s' on a full stack to produce a stack overflow but it did not.",
						InstructionEncoder.toIntelSyntax(push, false)));
	}

	@Test
	void pop() {
		final long stackTop = ELFLoader.alignAddress(rng.nextLong());
		final long stackSize = 8L;

		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.stackTop(stackTop)
				.stackSize(stackSize)
				.build();

		mem.setPermissions(stackTop, stackSize, true, true, false);
		set(cpu, Register64.RSP, stackTop);
		final long val = rng.nextLong();
		mem.write(stackTop, val);

		cpu.executeOne(new GeneralInstruction(Opcode.POP, Register64.RAX));

		final long rax = cpu.getRegisters().get(Register64.RAX);
		assertEquals(val, rax, () -> String.format("Expected RAX to be 0x%016x but was 0x%016x.", val, rax));

		final long expectedRSP = stackTop + 8L;
		final long actualRSP = cpu.getRegisters().get(Register64.RSP);
		assertEquals(
				expectedRSP,
				actualRSP,
				() -> String.format("Expected RSP to be 0x%016x but was 0x%016x.", expectedRSP, actualRSP));
	}

	@Test
	void popOnEmptyStack() {
		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.build();

		final long stackSize = 1024L;
		final long stackTop = ELFLoader.alignAddress(rng.nextLong());
		final long stackBottom = stackTop - stackSize;

		set(cpu, Register64.RSP, stackTop);
		mem.setPermissions(stackBottom, stackSize, true, true, false);
		mem.initialize(stackBottom, stackSize, (byte) 0x00);

		final Instruction pop = new GeneralInstruction(Opcode.POP, Register64.RAX);
		assertThrows(
				StackUnderflow.class,
				() -> cpu.executeOne(pop),
				() -> String.format(
						"Expected executing '%s' on an empty stack to produce a stack underflow but it did not.",
						InstructionEncoder.toIntelSyntax(pop, false)));
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
	void pushAndPop(final int n) {
		// Init random values
		final long[] values = new long[n];
		for (int i = 0; i < n; i++) {
			values[i] = rng.nextLong();
		}

		final long stackTop = ELFLoader.alignAddress(rng.nextLong());
		final long stackSize = n * 8L;

		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.stackTop(stackTop)
				.stackSize(stackSize)
				.build();

		// Setup stack at random location
		set(cpu, Register64.RSP, stackTop);

		// Doing n pushes of random values
		for (int i = 0; i < n; i++) {
			// Set memory to readable and writable
			mem.setPermissions(stackTop - 8L * (i + 1), 8L, true, true, false);

			set(cpu, Register64.RAX, values[i]);

			final int finalI = i;

			// Before each push, RSP must be equal to the old stackTop
			assertEquals(
					stackTop - 8L * i,
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"Before %,d-th PUSH, expected RSP to be 0x%016x but was 0x%016x.",
							finalI, stackTop - 8L * finalI, cpu.getRegisters().get(Register64.RSP)));

			// push RAX
			cpu.executeOne(new GeneralInstruction(Opcode.PUSH, Register64.RAX));

			// After each push, RSP must be equal to the new stackTop
			assertEquals(
					stackTop - 8L * (i + 1),
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"After %,d-th PUSH, expected RSP to be 0x%016x but was 0x%016x.",
							finalI,
							stackTop - 8L * (finalI + 1),
							cpu.getRegisters().get(Register64.RSP)));
		}

		// Doing n pops (in reverse) checking the random values
		for (int i = n - 1; i >= 0; i--) {
			final int finalI = i;

			// Before each pop, RSP must be equal to the new stackTop
			assertEquals(
					stackTop - 8L * (i + 1),
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"Before %,d-th POP, expected RSP to be 0x%016x but was 0x%016x.",
							finalI,
							stackTop - 8L * (finalI + 1),
							cpu.getRegisters().get(Register64.RSP)));

			// pop RAX
			cpu.executeOne(new GeneralInstruction(Opcode.POP, Register64.RAX));

			// After each pop, RSP must be equal to the old stackTop
			assertEquals(
					stackTop - 8L * i,
					cpu.getRegisters().get(Register64.RSP),
					() -> String.format(
							"After %,d-th POP, expected RSP to be 0x%016x but was 0x%016x.",
							finalI, stackTop - 8L * finalI, cpu.getRegisters().get(Register64.RSP)));

			assertEquals(
					values[i],
					cpu.getRegisters().get(Register64.RAX),
					() -> String.format(
							"After %,d-th POP, expected RAX to be 0x%016x but was 0x%016x.",
							finalI, values[finalI], cpu.getRegisters().get(Register64.RAX)));
		}

		// At the end RSP must be equal to the initial stackTop value
		assertEquals(
				stackTop,
				cpu.getRegisters().get(Register64.RSP),
				() -> String.format(
						"At the end, expected RSP to be 0x%016x but was 0x%016x.",
						stackTop, cpu.getRegisters().get(Register64.RSP)));
	}

	private void writeFunction(final long functionAddress, final Instruction... code) {
		final byte[] functionCode = InstructionEncoder.toHex(true, code);
		mem.initialize(functionAddress, functionCode);
		mem.setPermissions(functionAddress, functionCode.length, false, false, true);
	}

	@Test
	void callAndReturn() {
		final long stackTop = ELFLoader.alignAddress(rng.nextLong());
		final long stackSize = 8L;
		final long stackBottom = stackTop - stackSize;

		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.stackTop(stackTop)
				.stackSize(stackSize)
				.build();

		mem.setPermissions(stackBottom, stackSize, true, true, false);
		set(cpu, Register64.RSP, stackTop);

		// Setup RIP at random location
		final long rip = rng.nextLong();
		cpu.setInstructionPointer(rip);

		// Ensure that the offset between RIP and the function fits in a 32-bit immediate
		final int offset = rng.nextInt();
		final long functionAddress = rip + BitUtils.asLong(offset);
		// Write an empty function somewhere in memory (only RET instruction)
		writeFunction(functionAddress, new GeneralInstruction(Opcode.RET));

		// Write the code to be executed at RIP (just a CALL to the function and a HLT)
		writeFunction(
				rip,
				new GeneralInstruction(Opcode.CALL, new Immediate(offset - CALL_INSTRUCTION_LENGTH)),
				new GeneralInstruction(Opcode.HLT));

		// Start the CPU
		assertDoesNotThrow(cpu::execute);
	}

	@Test
	void callAllocateAndReturn() {
		final long stackTop = ELFLoader.alignAddress(rng.nextLong());
		final long stackSize = 20L;
		final long stackBottom = stackTop - stackSize;

		final X86Cpu cpu = X86Cpu.builder()
				.memory(mem)
				.registerFile(rf)
				.checkInstructions()
				.stackTop(stackTop)
				.stackSize(stackSize)
				.build();

		mem.initialize(stackBottom, stackSize, (byte) 0);
		mem.setPermissions(stackBottom, stackSize, true, true, false);
		set(cpu, Register64.RSP, stackTop);
		set(cpu, Register64.RBP, stackTop);

		// Setup RIP at random location
		final long rip = rng.nextLong();
		cpu.setInstructionPointer(rip);

		// Ensure that the offset between RIP and the function fits in a 32-bit immediate
		final int offset = Math.abs(rng.nextInt());
		final long functionAddress = rip + BitUtils.asLong(offset);

		// Write a function which just allocates a variable
		writeFunction(
				functionAddress,
				// code adapted from this one: https://godbolt.org/z/W8Kjj6Woz
				new GeneralInstruction(Opcode.PUSH, Register64.RBP),
				new GeneralInstruction(Opcode.MOV, Register64.RBP, Register64.RSP),
				new GeneralInstruction(
						Opcode.MOV,
						IndirectOperand.builder()
								.pointer(PointerSize.DWORD_PTR)
								.base(Register64.RBP)
								.displacement((byte) -4)
								.build(),
						new Immediate(rng.nextInt())),
				new GeneralInstruction(Opcode.NOP),
				new GeneralInstruction(Opcode.POP, Register64.RBP),
				new GeneralInstruction(Opcode.RET));

		// Write the code to be executed at RIP (just a CALL to the function and a HLT)
		writeFunction(
				rip,
				new GeneralInstruction(Opcode.CALL, new Immediate(offset - CALL_INSTRUCTION_LENGTH)),
				new GeneralInstruction(Opcode.HLT));

		// Start the CPU
		assertDoesNotThrow(cpu::execute);
	}
}
