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

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.mem.MemoryController;

public final class X86EmulatorAdapter implements X86Emulator {

	private final X86Cpu cpu;
	private final Queue<Instruction> instructions = new ArrayDeque<>();

	public X86EmulatorAdapter(final X86RegisterFile regFile, final MemoryController mem) {
		this.cpu = new X86Cpu(Objects.requireNonNull(regFile), Objects.requireNonNull(mem));
	}

	@Override
	public void setEntryPoint(final long address) {
		cpu.setEntryPoint(address);
	}

	@Override
	public void execute() {
		throw new Error("Not implemented");
	}

	@Override
	public void executeOne() {
		throw new Error("Not implemented");
	}

	@Override
	public void executeOne(final Instruction inst) {
		instructions.add(inst);
	}
}
