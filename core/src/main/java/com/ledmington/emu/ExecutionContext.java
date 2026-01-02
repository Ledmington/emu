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

import com.ledmington.mem.Memory;
import com.ledmington.utils.SuppressFBWarnings;

/**
 * This class contains all the information needed for the emulator to execute a file, except the file itself.
 *
 * @param cpu The emulated CPU to be used.
 * @param memory The emulated memory to be used.
 */
// FIXME: this is ugly
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@SuppressFBWarnings(
		value = "EI_EXPOSE_REP",
		justification = "This object is meant to be a collection of mutable objects.")
@SuppressFBWarnings(
		value = "EI_EXPOSE_REP2",
		justification = "This object is meant to be a collection of mutable objects.")
public record ExecutionContext(X86Emulator cpu, Memory memory) {

	/**
	 * Creates a new {@link ExecutionContext} with the given emulated CPU and emulated Memory.
	 *
	 * @param cpu The CPU to be used to execute instructions.
	 * @param memory The memory to be used as main memory.
	 */
	@SuppressFBWarnings(
			value = "EI_EXPOSE_REP2",
			justification = "This object is meant to be a collection of mutable objects.")
	public ExecutionContext {
		Objects.requireNonNull(cpu, "Null cpu.");
		Objects.requireNonNull(memory, "Null memory.");
	}

	/**
	 * Returns the contained X86Emulator.
	 *
	 * @return The contained X86Emulator.
	 */
	@SuppressFBWarnings(
			value = "EI_EXPOSE_REP",
			justification = "This object is meant to be a collection of mutable objects.")
	public X86Emulator cpu() {
		return cpu;
	}

	/**
	 * Returns the contained Memory.
	 *
	 * @return The contained Memory.
	 */
	@SuppressFBWarnings(
			value = "EI_EXPOSE_REP",
			justification = "This object is meant to be a collection of mutable objects.")
	public Memory memory() {
		return memory;
	}
}
