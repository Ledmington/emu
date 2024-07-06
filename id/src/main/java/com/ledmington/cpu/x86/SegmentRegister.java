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
package com.ledmington.cpu.x86;

import java.util.Objects;

/**
 * This class represents a pair of an x86 segment register and a general-purpose x86 register, used for representing
 * string operands inside x86 instructions.
 */
public final class SegmentRegister implements Register {

    private final Register16 seg;
    private final Register reg;

    /**
     * Creates a segment register with the given couple of registers.
     *
     * @param segment The 16-bit register.
     * @param register The general-purpose register.
     */
    public SegmentRegister(final Register16 segment, final Register register) {
        this.seg = Objects.requireNonNull(segment);
        this.reg = Objects.requireNonNull(register);
    }

    /**
     * Returns the segment.
     *
     * @return The segment.
     */
    public Register16 segment() {
        return seg;
    }

    /**
     * Returns the general-purpose register.
     *
     * @return The general-purpose register.
     */
    public Register register() {
        return reg;
    }

    @Override
    public int bits() {
        // TODO: check this
        return reg.bits();
    }

    @Override
    public String toIntelSyntax() {
        return reg.toIntelSyntax();
    }

    @Override
    public String toString() {
        return "SegmentRegister(seg=" + seg + ";reg=" + reg + ')';
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + seg.hashCode();
        h = 31 * h + reg.hashCode();
        return h;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final SegmentRegister sr = (SegmentRegister) other;
        return this.seg.equals(sr.seg) && this.reg.equals(sr.reg);
    }
}
