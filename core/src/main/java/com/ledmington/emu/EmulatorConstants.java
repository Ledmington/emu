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

import com.ledmington.mem.MemoryInitializer;

/** A class to hold constants for the emulator execution. */
public final class EmulatorConstants {

	/** The default memory initializer */
	private static MemoryInitializer memoryInitializer = MemoryInitializer.random();

	private static boolean breakOnWrongPermissions = true;
	private static boolean breakWhenReadingUninitializedMemory = true;

	/** Base address where to load the executable file. */
	private static long baseAddress = 0L; // This is the value gdb uses, why?

	/** The default stack address. */
	private static long baseStackAddress = 0x0000_7fff_ff7f_db80L; // This is the gdb uses, why?

	/** The default stack size. */
	private static long stackSize = 8L * 1024L * 1024L;

	/** The value at the base of the stack. */
	private static long baseStackValue = 0x00fa_fa00_dead_beefL;

	private static boolean checkInstructions = true;

	private EmulatorConstants() {}

	/**
	 * Allows changing the memory initializer of the emulator.
	 *
	 * @param memInit The new memory initializer.
	 */
	public static void setMemoryInitializer(final MemoryInitializer memInit) {
		memoryInitializer = Objects.requireNonNull(memInit);
	}

	/**
	 * Returns the default memory initializer.
	 *
	 * @return The default memory initializer.
	 */
	public static MemoryInitializer getMemoryInitializer() {
		return memoryInitializer;
	}

	public static boolean shouldBreakOnWrongPermissions() {
		return breakOnWrongPermissions;
	}

	public static void shouldBreakOnWrongPermissions(final boolean b) {
		breakOnWrongPermissions = b;
	}

	public static boolean shouldBreakWhenReadingUninitializedMemory() {
		return breakWhenReadingUninitializedMemory;
	}

	public static void shouldBreakWhenReadingUninitializedMemory(final boolean b) {
		breakWhenReadingUninitializedMemory = b;
	}

	/**
	 * Changes the default stack address.
	 *
	 * @param newBaseStackAddress The new base stack address.
	 */
	public static void setBaseStackAddress(final long newBaseStackAddress) {
		baseStackAddress = newBaseStackAddress;
	}

	/**
	 * Returns the default base stack address.
	 *
	 * @return The default base stack address.
	 */
	public static long getBaseStackAddress() {
		return baseStackAddress;
	}

	/**
	 * Changes the default stack size.
	 *
	 * @param newStackSize The new default stack size.
	 */
	public static void setStackSize(final long newStackSize) {
		final long minStackSize = 1L;
		if (newStackSize < minStackSize) {
			throw new IllegalArgumentException("Stack size must be at least 1");
		}
		stackSize = newStackSize;
	}

	/**
	 * Returns the default stack size.
	 *
	 * @return The default stack size.
	 */
	public static long getStackSize() {
		return stackSize;
	}

	/**
	 * Changes the default base address.
	 *
	 * @param newBaseAddress The new default base address.
	 */
	public static void setBaseAddress(final long newBaseAddress) {
		baseAddress = newBaseAddress;
	}

	/**
	 * Returns the default base address.
	 *
	 * @return The default base address.
	 */
	public static long getBaseAddress() {
		return baseAddress;
	}

	public static void setBaseStackValue(final long newBaseStackValue) {
		baseStackValue = newBaseStackValue;
	}

	public static long getBaseStackValue() {
		return baseStackValue;
	}

	public static boolean shouldCheckInstruction() {
		return checkInstructions;
	}

	public static void shouldCheckInstructions(final boolean b) {
		checkInstructions = b;
	}
}
