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

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Opcode;

/** Common interface to represent an x86 Emulator. */
public interface X86Emulator {

	/** Allows instructions to be executed. Usually needed to start execution again after a halt. */
	void turnOn();

	/**
	 * Automatically fetches instruction from the emulated memory and executes them. Continues indefinitely until it
	 * encounters a {@link Opcode#HLT} instruction.
	 */
	void execute();

	/** Fetches next instruction and executes it. */
	void executeOne();

	/**
	 * Executes the given instruction without modifying the instruction pointer.
	 *
	 * @param inst The instruction to be executed.
	 */
	void executeOne(Instruction inst);

	/**
	 * Returns an immutable view of the registers in use.
	 *
	 * @return An immutable view of the registers in use.
	 */
	ImmutableRegisterFile getRegisters();

	/**
	 * Sets the instruction pointer which couldn't be accesses in any other way.
	 *
	 * @param ip The new instruction pointer.
	 */
	void setInstructionPointer(long ip);
}
