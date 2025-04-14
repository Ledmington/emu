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
	private static long baseAddress = 0x00005a5a00000000L;

	/** The default stack size. */
	private static long stackSize = 8192L * 1024L;

	/** The value at the base of the stack. */
	private static long baseStackValue = 0x00fafa00deadbeefL;

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

	public static boolean getBreakOnWrongPermissions() {
		return breakOnWrongPermissions;
	}

	public static void setBreakOnWrongPermissions(final boolean b) {
		breakOnWrongPermissions = b;
	}

	public static boolean getBreakWhenReadingUninitializedMemory() {
		return breakWhenReadingUninitializedMemory;
	}

	public static void setBreakWhenReadingUninitializedMemory(final boolean b) {
		breakWhenReadingUninitializedMemory = b;
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

	public static boolean getCheckInstruction() {
		return checkInstructions;
	}

	public static void setCheckInstructions(final boolean b) {
		checkInstructions = b;
	}
}
