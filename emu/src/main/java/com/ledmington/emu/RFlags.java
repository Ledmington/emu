/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
	VirtualInterruptPending(20, "VIP"),

	/** Virtual Interrupt Flag (VIF). System flag. */
	VirtualInterrupt(19, "VIF"),

	/** Alignment Check / Access Control (AC). System flag. */
	AlignmentCheck(18, "AC"),

	/** Virtual-8068 Mode (VM). System flag. */
	Virtual8086Mode(17, "VM"),

	/** Resume Flag (RF). System flag. */
	Resume(16, "RF"),

	/** Nested Task (NT). System flag. */
	NestedTask(14, "NT"),

	/** I/O Privilege Level (IOPL). System flag. */
	IOPrivilegeLevel(12, "IOPL"),

	/** Overflow Flag (OF). Status flag. */
	Overflow(11, "OF"),

	/** Direction Flag (DF). Control flag. */
	Direction(10, "DF"),

	/** Interrupt Enable Flag (IF). System flag. */
	InterruptEnable(9, "IF"),

	/** Trap Flag (TF). System flag. */
	Trap(8, "TF"),

	/** Sign Flag (SF). Status flag. */
	Sign(7, "SF"),

	/** Zero Flag (ZF). Status flag. */
	Zero(6, "ZF"),

	/** Auxiliary Carry Flag (AF). Status flag. */
	AuxiliaryCarry(4, "AF"),

	/** Parity Flag (PF). Status flag. */
	Parity(2, "PF"),

	/** Carry Flag (CF). Status flag. */
	Carry(0, "CF");

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
}
