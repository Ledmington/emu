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
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.IndirectOperand;
import com.ledmington.cpu.x86.IndirectOperandBuilder;
import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.PointerSize;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.mem.Memory;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.BitUtils;

final class TestExecution {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);
	private static final List<Register8> r8 = Arrays.asList(Register8.values());
	private static final List<Register16> r16 = Arrays.asList(Register16.values());
	private static final List<Register32> r32 = Arrays.stream(Register32.values())
			.filter(x -> !x.equals(Register32.EIP))
			.toList();
	private static final List<Register64> r64 = Arrays.stream(Register64.values())
			.filter(x -> !x.equals(Register64.RIP))
			.toList();
	private static final List<Arguments> r8r8 =
			r8.stream().flatMap(a -> r8.stream().map(b -> Arguments.of(a, b))).toList();
	private static final List<Arguments> r16r16 =
			r16.stream().flatMap(a -> r16.stream().map(b -> Arguments.of(a, b))).toList();
	private static final List<Arguments> r32r32 =
			r32.stream().flatMap(a -> r32.stream().map(b -> Arguments.of(a, b))).toList();
	private static final List<Arguments> r64r64 =
			r64.stream().flatMap(a -> r64.stream().map(b -> Arguments.of(a, b))).toList();
	private static final List<Arguments> r64r32 =
			r64.stream().flatMap(a -> r32.stream().map(b -> Arguments.of(a, b))).toList();
	private static final List<Supplier<IndirectOperandBuilder>> indirectOperands = List.of(
			() -> IndirectOperand.builder().base(Register32.EAX),
			() -> IndirectOperand.builder().base(Register64.RAX),
			() -> IndirectOperand.builder().index(Register32.EAX).scale(2).displacement((byte) 0),
			() -> IndirectOperand.builder().index(Register64.RAX).scale(2).displacement((byte) 0),
			() -> IndirectOperand.builder().index(Register32.EAX).scale(4).displacement((byte) 0),
			() -> IndirectOperand.builder().index(Register64.RAX).scale(4).displacement((byte) 0),
			() -> IndirectOperand.builder().index(Register32.EAX).scale(8).displacement((byte) 0),
			() -> IndirectOperand.builder().index(Register64.RAX).scale(8).displacement((byte) 0),
			//
			() -> IndirectOperand.builder()
					.base(Register32.EBX)
					.index(Register32.EAX)
					.scale(1),
			() -> IndirectOperand.builder()
					.base(Register64.RBX)
					.index(Register64.RAX)
					.scale(1),
			() -> IndirectOperand.builder()
					.base(Register32.EBX)
					.index(Register32.EAX)
					.scale(2),
			() -> IndirectOperand.builder()
					.base(Register64.RBX)
					.index(Register64.RAX)
					.scale(2),
			() -> IndirectOperand.builder()
					.base(Register32.EBX)
					.index(Register32.EAX)
					.scale(4),
			() -> IndirectOperand.builder()
					.base(Register64.RBX)
					.index(Register64.RAX)
					.scale(4),
			() -> IndirectOperand.builder()
					.base(Register32.EBX)
					.index(Register32.EAX)
					.scale(8),
			() -> IndirectOperand.builder()
					.base(Register64.RBX)
					.index(Register64.RAX)
					.scale(8));
	private static final List<Arguments> r16m16 = r16.stream()
			.flatMap(a -> indirectOperands.stream()
					.map(b -> Arguments.of(
							a, b.get().pointer(PointerSize.WORD_PTR).build())))
			.toList();
	private static final List<Arguments> r32m32 = r32.stream()
			.flatMap(a -> indirectOperands.stream()
					.map(b -> Arguments.of(
							a, b.get().pointer(PointerSize.DWORD_PTR).build())))
			.toList();
	private static final List<Arguments> r64m64 = r64.stream()
			.flatMap(a -> indirectOperands.stream()
					.map(b -> Arguments.of(
							a, b.get().pointer(PointerSize.QWORD_PTR).build())))
			.toList();
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
		return r64r64.stream();
	}

	private static Stream<Arguments> pairs32() {
		return r32r32.stream();
	}

	private static Stream<Arguments> pairs16() {
		return r16r16.stream();
	}

	private static Stream<Arguments> pairs8() {
		return r8r8.stream();
	}

	private static Stream<Arguments> r64r32() {
		return r64r32.stream();
	}

	private static Stream<Arguments> r16m16() {
		return r16m16.stream();
	}

	private static Stream<Arguments> r32m32() {
		return r32m32.stream();
	}

	private static Stream<Arguments> r64m64() {
		return r64m64.stream();
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
		expected.set(RFlags.ZERO, result == 0);
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
	void xor64(final Register64 r1, final Register64 r2) {
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final long result = oldValue1 ^ oldValue2;
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.XOR, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs32")
	void xor32(final Register32 r1, final Register32 r2) {
		final int oldValue1 = cpu.getRegisters().get(r1);
		final int oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final int result = oldValue1 ^ oldValue2;
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.XOR, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs16")
	void xor16(final Register16 r1, final Register16 r2) {
		final short oldValue1 = cpu.getRegisters().get(r1);
		final short oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final short result = BitUtils.xor(oldValue1, oldValue2);
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.XOR, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs8")
	void xor8(final Register8 r1, final Register8 r2) {
		final byte oldValue1 = cpu.getRegisters().get(r1);
		final byte oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final byte result = BitUtils.xor(oldValue1, oldValue2);
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.XOR, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs64")
	void and64(final Register64 r1, final Register64 r2) {
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final long result = oldValue1 & oldValue2;
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0L);
		cpu.executeOne(new Instruction(Opcode.AND, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs32")
	void and32(final Register32 r1, final Register32 r2) {
		final int oldValue1 = cpu.getRegisters().get(r1);
		final int oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final int result = oldValue1 & oldValue2;
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.AND, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs16")
	void and16(final Register16 r1, final Register16 r2) {
		final short oldValue1 = cpu.getRegisters().get(r1);
		final short oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final short result = BitUtils.and(oldValue1, oldValue2);
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
		cpu.executeOne(new Instruction(Opcode.AND, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("pairs8")
	void and8(final Register8 r1, final Register8 r2) {
		final byte oldValue1 = cpu.getRegisters().get(r1);
		final byte oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		final byte result = BitUtils.and(oldValue1, oldValue2);
		expected.set(r1, result);
		expected.set(RFlags.ZERO, result == 0);
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

	@ParameterizedTest
	@MethodSource("r64r32")
	void movsxd(final Register64 r1, final Register32 r2) {
		final int oldValue2 = cpu.getRegisters().get(r2);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(r1, oldValue2);
		cpu.executeOne(new Instruction(Opcode.MOVSXD, r1, r2));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("r16m16")
	void lea16(final Register16 dest, final IndirectOperand src) {
		final short address = BitUtils.asShort(X86Cpu.computeIndirectOperand((RegisterFile) cpu.getRegisters(), src));
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(dest, address);
		cpu.executeOne(new Instruction(Opcode.LEA, dest, src));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("r32m32")
	void lea32(final Register32 dest, final IndirectOperand src) {
		final int address = BitUtils.asInt(X86Cpu.computeIndirectOperand((RegisterFile) cpu.getRegisters(), src));
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(dest, address);
		cpu.executeOne(new Instruction(Opcode.LEA, dest, src));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}

	@ParameterizedTest
	@MethodSource("r64m64")
	void lea64(final Register64 dest, final IndirectOperand src) {
		final long address = X86Cpu.computeIndirectOperand((RegisterFile) cpu.getRegisters(), src);
		final X86RegisterFile expected = new X86RegisterFile(cpu.getRegisters());
		expected.set(dest, address);
		cpu.executeOne(new Instruction(Opcode.LEA, dest, src));
		assertEquals(
				expected,
				cpu.getRegisters(),
				() -> String.format("Expected register file to be '%s' but was '%s'.", expected, cpu.getRegisters()));
	}
}
