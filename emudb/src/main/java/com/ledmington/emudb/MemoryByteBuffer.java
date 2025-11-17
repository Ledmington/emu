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
package com.ledmington.emudb;

import java.util.Objects;

import com.ledmington.mem.Memory;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.SuppressFBWarnings;

public final class MemoryByteBuffer implements ReadOnlyByteBuffer {

	private long position;
	private final Memory memory;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "At the moment we need this object as it is.")
	public MemoryByteBuffer(final long initialPosition, final Memory memory) {
		this.position = initialPosition;
		this.memory = Objects.requireNonNull(memory);
	}

	@Override
	public boolean isLittleEndian() {
		return true;
	}

	@Override
	public void setEndianness(final boolean isLittleEndian) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAlignment(final long newAlignment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getAlignment() {
		return 1L;
	}

	@Override
	public void setPosition(final long newPosition) {
		this.position = newPosition;
	}

	@Override
	public long getPosition() {
		return this.position;
	}

	@Override
	public byte read() {
		return memory.read(position);
	}
}
