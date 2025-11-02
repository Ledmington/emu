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
package com.ledmington.cpu.x86;

import java.util.Objects;

/**
 * Represents a segmented memory address in the x86 architecture, consisting of a {@link SegmentRegister} and an
 * {@link Immediate} offset.
 *
 * @param segment The {@link SegmentRegister} working as base offset.
 * @param immediate The offset to be added to the segment.
 */
public record SegmentedAddress(SegmentRegister segment, Immediate immediate) implements Operand {

	/**
	 * Creates a new {@code SegmentedAddress} with the given segment and offset.
	 *
	 * @param segment The segment register component.
	 * @param immediate The immediate offset within the segment.
	 */
	public SegmentedAddress {
		Objects.requireNonNull(segment);
		Objects.requireNonNull(immediate);
	}

	@Override
	public String toIntelSyntax() {
		return segment.toIntelSyntax() + ":" + immediate.toIntelSyntax();
	}

	@Override
	public int bits() {
		throw new UnsupportedOperationException("This is a segmented address.");
	}
}
