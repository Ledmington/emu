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

import java.util.Objects;

import com.ledmington.mem.MemoryController;

/** A builder class for the {@link X86Cpu}. */
public final class X86CpuBuilder {

	private boolean alreadyBuilt = false;
	private MemoryController mem = null;
	private RegisterFile rf = null;
	private boolean checkInstructions = false;
	private long stackTop = -1L;
	private long stackSize = -1L;

	/** Creates a new X86CpuBuilder with default parameters. */
	public X86CpuBuilder() {}

	private void assertNotAlreadyBuilt() {
		if (alreadyBuilt) {
			throw new IllegalStateException("This instance X86CpuBuilder has already been used.");
		}
	}

	/**
	 * Sets the given MemoryController for the X86Cpu.
	 *
	 * @param mem The MemoryController to be used.
	 * @return This instance of X86CpuBuilder.
	 */
	public X86CpuBuilder memory(final MemoryController mem) {
		assertNotAlreadyBuilt();
		this.mem = Objects.requireNonNull(mem);
		return this;
	}

	/**
	 * Sets the given RegisterFile for the X86Cpu.
	 *
	 * @param rf The RegisterFile to be used.
	 * @return This instance of X86CpuBuilder.
	 */
	public X86CpuBuilder registerFile(final RegisterFile rf) {
		assertNotAlreadyBuilt();
		this.rf = Objects.requireNonNull(rf);
		return this;
	}

	/**
	 * Configures the X86Cpu to check instructions before executing them.
	 *
	 * @return This instance of X86CpuBuilder.
	 */
	public X86CpuBuilder checkInstructions() {
		assertNotAlreadyBuilt();
		this.checkInstructions = true;
		return this;
	}

	/**
	 * Configures the X86Cpu to check instructions before executing them if true.
	 *
	 * @param checkInstructions If true, configures the resulting X86Cpu to check instructions before executing.
	 * @return This instance of X86CpuBuilder.
	 */
	public X86CpuBuilder checkInstructions(final boolean checkInstructions) {
		assertNotAlreadyBuilt();
		this.checkInstructions = checkInstructions;
		return this;
	}

	/**
	 * Sets the given value as top of the stack for the resulting X86Cpu.
	 *
	 * @param stackTop The top of the stack.
	 * @return This instance of X86CpuBuilder.
	 */
	public X86CpuBuilder stackTop(final long stackTop) {
		assertNotAlreadyBuilt();
		this.stackTop = stackTop;
		return this;
	}

	/**
	 * Sets the given value as maximum size of the stack (in bytes) for the resulting X86Cpu.
	 *
	 * @param stackSize The size of the stack in bytes.
	 * @return This instance of X86CpuBuilder.
	 */
	public X86CpuBuilder stackSize(final long stackSize) {
		assertNotAlreadyBuilt();
		final long minAllowedStackSize = 1L;
		if (stackSize < minAllowedStackSize) {
			throw new IllegalArgumentException(String.format("Invalid stack size: %,d B.", stackSize));
		}
		this.stackSize = stackSize;
		return this;
	}

	/**
	 * Creates a new X86Cpu by passing the proper parameters.
	 *
	 * @return A new X86Cpu with the given parameters.
	 */
	public X86Cpu build() {
		if (alreadyBuilt) {
			throw new IllegalStateException("Cannot build the same X86CpuBuilder twice.");
		}

		if (this.rf == null) {
			this.rf = new X86RegisterFile();
		}

		if (this.stackTop == -1L) {
			this.stackTop = EmulatorConstants.getBaseStackAddress();
		}

		if (this.stackSize == -1L) {
			this.stackSize = EmulatorConstants.getStackSize();
		}

		this.alreadyBuilt = true;
		return new X86Cpu(
				this.mem, this.rf, this.checkInstructions, ELFLoader.alignAddress(this.stackTop), this.stackSize);
	}
}
