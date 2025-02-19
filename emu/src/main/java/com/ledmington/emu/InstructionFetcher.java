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

import java.util.Objects;

import com.ledmington.cpu.x86.Register64;
import com.ledmington.mem.MemoryController;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.SuppressFBWarnings;

/** A class which represents the part of the emulated CPU which reads instructions from memory during execution. */
public final class InstructionFetcher implements ReadOnlyByteBuffer {

	private final MemoryController mem;
	private final RegisterFile regFile;

	/**
	 * Creates an InstructionFetcher with the given MemoryController and register file.
	 *
	 * @param mem The memory controller to retrieve instructions from.
	 * @param regFile The register file to get and set the instruction pointer.
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "At the moment we need these objects as they are.")
	public InstructionFetcher(final MemoryController mem, final RegisterFile regFile) {
		this.mem = Objects.requireNonNull(mem);
		this.regFile = Objects.requireNonNull(regFile);
	}

	@Override
	public boolean isLittleEndian() {
		return false;
	}

	@Override
	public void setEndianness(final boolean isLittleEndian) {
		throw new UnsupportedOperationException("InstructionFetcher does not allow to change the endianness");
	}

	@Override
	public long getAlignment() {
		return 1L;
	}

	@Override
	public void setAlignment(final long newAlignment) {
		throw new UnsupportedOperationException("InstructionFetcher does not allow to change the byte alignment");
	}

	@Override
	public void setPosition(final long newPosition) {
		regFile.set(Register64.RIP, newPosition);
	}

	@Override
	public long getPosition() {
		return regFile.get(Register64.RIP);
	}

	@Override
	public byte read() {
		return mem.readCode(getPosition());
	}

	@Override
	public String toString() {
		return "InstructionFetcher(rip=" + getPosition() + ')';
	}
}
