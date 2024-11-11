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
package com.ledmington.emu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.mem.Memory;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.BitUtils;

final class TestExecution {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(System.nanoTime());
	private static final X86Cpu cpu = new X86Cpu(new MemoryController(new Memory() {
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

	@BeforeEach
	void setup() {
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

	@Test
	void add() {
		final Register64 r1 = Register64.values()[rng.nextInt(0, Register64.values().length)];
		final Register64 r2 = Register64.values()[rng.nextInt(0, Register64.values().length)];
		final long oldValue1 = cpu.getRegisters().get(r1);
		final long oldValue2 = cpu.getRegisters().get(r2);
		cpu.executeOne(new Instruction(Opcode.ADD, r1, r2));
		assertEquals(
				oldValue1 + oldValue2,
				cpu.getRegisters().get(r1),
				() -> String.format(
						"Expected register %s to have value 0x%016x but was 0x%016x",
						r1, oldValue1 + oldValue2, cpu.getRegisters().get(r1)));
		assertEquals(
				oldValue2,
				cpu.getRegisters().get(r2),
				() -> String.format(
						"Expected register %s to have value 0x%016x but was 0x%016x",
						r2, oldValue2, cpu.getRegisters().get(r2)));
	}
}
