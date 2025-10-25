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

/** X86 RFlags values. */
public enum RFlags {

	/** ID Flag (ID). System flag. */
	ID(21, "ID"),

	/** Virtual Interrupt Pending (VIP). System flag. */
	VIRTUAL_INTERRUPT_PENDING(20, "VIP"),

	/** Virtual Interrupt Flag (VIF). System flag. */
	VIRTUAL_INTERRUPT(19, "VIF"),

	/** Alignment Check / Access Control (AC). System flag. */
	ALIGNMENT_CHECK(18, "AC"),

	/** Virtual-8068 Mode (VM). System flag. */
	VIRTUAL_8086_MODE(17, "VM"),

	/** Resume Flag (RF). System flag. */
	RESUME(16, "RF"),

	/** Nested Task (NT). System flag. */
	NESTED_TASK(14, "NT"),

	/** I/O Privilege Level (IOPL). System flag. */
	IO_PRIVILEGE_LEVEL(12, "IOPL"),

	/** Overflow Flag (OF). Status flag. */
	OVERFLOW(11, "OF"),

	/** Direction Flag (DF). Control flag. */
	DIRECTION(10, "DF"),

	/** Interrupt Enable Flag (IF). System flag. */
	INTERRUPT_ENABLE(9, "IF"),

	/** Trap Flag (TF). System flag. */
	TRAP(8, "TF"),

	/** Sign Flag (SF). Status flag. */
	SIGN(7, "SF"),

	/** Zero Flag (ZF). Status flag. */
	ZERO(6, "ZF"),

	/** Auxiliary Carry Flag (AF). Status flag. */
	AUXILIARY_CARRY(4, "AF"),

	/** Parity Flag (PF). Status flag. */
	PARITY(2, "PF"),

	// Bit 1 is reserved and always 1

	/** Carry Flag (CF). Status flag. */
	CARRY(0, "CF");

	private final int bitIndex;
	private final String symbol;

	RFlags(final int bit, final String symbol) {
		this.bitIndex = bit;
		this.symbol = symbol;
	}

	/**
	 * Returns the bit index of the flag.
	 *
	 * @return The bit index of the flag.
	 */
	public int bit() {
		return bitIndex;
	}

	/**
	 * Returns a String representing the flag.
	 *
	 * @return The unique String representing the flag.
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Returns the default value of RFlags, meaning the value that RFlags holds when all flags are cleared.
	 *
	 * @return The default value.
	 */
	public static long defaultValue() {
		return 0x2L;
	}
}
