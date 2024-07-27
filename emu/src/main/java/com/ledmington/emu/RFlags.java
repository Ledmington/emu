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

public enum RFlags {

    /** ID Flag (ID). System flag. */
    ID(21),

    /** Virtual Interrupt Pending (VIP). System flag. */
    VirtualInterruptPending(20),

    /** Virtual Interrupt Flag (VIF). System flag. */
    VirtualInterrupt(19),

    /** Alignment Check (AC). Equivalent to AccessControl. System flag. */
    AlignmentCheck(18),

    /** Access Control (AC). Equivalent to AlignmentCheck. System flag. */
    AccessControl(18),

    /** Virtual-8068 Mode (VM). System flag. */
    Virtual8086Mode(17),

    /** Resume Flag (RF). System flag. */
    Resume(16),

    /** Nested Task (NT). System flag. */
    NestedTask(14),

    /** I/O Privilege Level. System flag. */
    IOPrivilegeLevel(12),

    /** Overflow Flag (OF). Status flag. */
    Overflow(11),

    /** Direction Flag (DF). Control flag. */
    Direction(10),

    /** Interrupt Enable Flag (IF). System flag. */
    InterruptEnable(9),

    /** Trap Flag (TF). System flag. */
    Trap(8),

    /** Sign Flag (SF). Status flag. */
    Sign(7),

    /** Zero Flag (ZF). Status flag. */
    Zero(6),

    /** Auxiliary Carry Flag (AF). Status flag. */
    AuxiliaryCarry(4),

    /** Parity Flag (PF). Status flag. */
    Parity(2),

    /** Carry Flag (CF). Status flag. */
    Carry(0);

    private final int bitIndex;

    RFlags(final int bit) {
        this.bitIndex = bit;
    }

    public int bit() {
        return bitIndex;
    }
}
