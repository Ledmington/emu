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

	/** The default memory initializer. */
	private static MemoryInitializer memoryInitializer = MemoryInitializer.random();

	private static boolean breakOnWrongPermissions = true;
	private static boolean breakWhenReadingUninitializedMemory = true;

	/** Base address where to load the executable file. */
	private static long baseAddress = 0L; // This is the value gdb uses, why?

	/** The default stack address. */
	private static long baseStackAddress = 0x0000_7fff_ffff_dc50L; // This is the value gdb uses, why?

	/** The default stack size. */
	private static long stackSize = 8L * 1024L * 1024L;

	/** The value at the base of the stack. */
	// TODO: do we actually need this?
	private static long baseStackValue = 0L; // This is the value gdb uses, why?

	private static boolean checkInstructions = true;

	private EmulatorConstants() {
		// Prevent instantiation
	}

	/**
	 * Allows changing the memory initializer of the emulator.
	 *
	 * @param memInit The new memory initializer.
	 */
	public static void setMemoryInitializer(final MemoryInitializer memInit) {
		memoryInitializer = Objects.requireNonNull(memInit);
	}

	/**
	 * Returns the current memory initializer used by the emulator.
	 *
	 * @return The current {@link MemoryInitializer} instance.
	 */
	public static MemoryInitializer getMemoryInitializer() {
		return memoryInitializer;
	}

	/**
	 * Indicates whether the emulator should break when memory access permissions are violated.
	 *
	 * @return {@code true} if the emulator should break on wrong permissions, {@code false} otherwise.
	 */
	public static boolean shouldBreakOnWrongPermissions() {
		return breakOnWrongPermissions;
	}

	/**
	 * Sets whether the emulator should break when memory access permissions are violated.
	 *
	 * @param b {@code true} to enable breaking on permission violations; {@code false} to disable.
	 */
	public static void shouldBreakOnWrongPermissions(final boolean b) {
		breakOnWrongPermissions = b;
	}

	/**
	 * Indicates whether the emulator should break when attempting to read uninitialized memory.
	 *
	 * @return {@code true} if the emulator should break when reading uninitialized memory, {@code false} otherwise.
	 */
	public static boolean shouldBreakWhenReadingUninitializedMemory() {
		return breakWhenReadingUninitializedMemory;
	}

	/**
	 * Sets whether the emulator should break when attempting to read uninitialized memory.
	 *
	 * @param b {@code true} to enable breaking when reading uninitialized memory; {@code false} to disable.
	 */
	public static void shouldBreakWhenReadingUninitializedMemory(final boolean b) {
		breakWhenReadingUninitializedMemory = b;
	}

	/**
	 * Sets the base address where the emulator loads executable files.
	 *
	 * @param newBaseAddress The new base address.
	 */
	public static void setBaseAddress(final long newBaseAddress) {
		baseAddress = newBaseAddress;
	}

	/**
	 * Returns the base address where the emulator loads executable files.
	 *
	 * @return the current base address.
	 */
	public static long getBaseAddress() {
		return baseAddress;
	}

	/**
	 * Sets the base stack address used by the emulator.
	 *
	 * @param newBaseStackAddress The new base stack address.
	 */
	public static void setBaseStackAddress(final long newBaseStackAddress) {
		baseStackAddress = newBaseStackAddress;
	}

	/**
	 * Returns the base stack address used by the emulator.
	 *
	 * @return the current base stack address
	 */
	public static long getBaseStackAddress() {
		return baseStackAddress;
	}

	/**
	 * Sets the default stack size used by the emulator.
	 *
	 * @param newStackSize The new stack size in bytes.
	 */
	public static void setStackSize(final long newStackSize) {
		final long minStackSize = 1L;
		if (newStackSize < minStackSize) {
			throw new IllegalArgumentException("Stack size must be at least 1");
		}
		stackSize = newStackSize;
	}

	/**
	 * Returns the current default stack size used by the emulator.
	 *
	 * @return The current stack size in bytes.
	 */
	public static long getStackSize() {
		return stackSize;
	}

	/**
	 * Sets the value to be placed at the base of the stack used by the emulator.
	 *
	 * @param newBaseStackValue The new base stack value.
	 */
	public static void setBaseStackValue(final long newBaseStackValue) {
		baseStackValue = newBaseStackValue;
	}

	/**
	 * Returns the base stack value used by the emulator.
	 *
	 * @return The current base stack value.
	 */
	public static long getBaseStackValue() {
		return baseStackValue;
	}

	/**
	 * Indicates whether the emulator should perform instruction validation checks.
	 *
	 * @return {@code true} if instruction checks are enabled; {@code false} otherwise.
	 */
	public static boolean shouldCheckInstruction() {
		return checkInstructions;
	}

	/**
	 * Sets whether the emulator should perform instruction validation checks.
	 *
	 * @param b {@code true} to enable instruction checking; {@code false} to disable.
	 */
	public static void shouldCheckInstructions(final boolean b) {
		checkInstructions = b;
	}
}
