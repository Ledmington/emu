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

public final class IndirectOperandBuilder {

    private Register baseRegister;
    private Integer c;
    private Register indexRegister;
    private Long displacement;
    private DisplacementType displacementType = DisplacementType.LONG;
    private PointerSize ptrSize;
    private boolean alreadyBuilt;

    public IndirectOperandBuilder() {}

    public IndirectOperandBuilder reg1(final Register r) {
        if (this.baseRegister != null) {
            throw new IllegalStateException("Cannot define reg1 twice");
        }
        Objects.requireNonNull(r);
        if (r.bits() != 32 && r.bits() != 64) {
            throw new IllegalArgumentException(r + " is an invalid base register");
        }
        if (indexRegister != null && r.bits() != indexRegister.bits()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot mix %,d-bit and %,d-bit registers (%s, %s)",
                    r.bits(), indexRegister.bits(), r, indexRegister));
        }
        this.baseRegister = r;
        return this;
    }

    public IndirectOperandBuilder constant(final int c) {
        if (this.c != null) {
            throw new IllegalStateException("Cannot define constant twice");
        }
        if (c != 1 && c != 2 && c != 4 && c != 8) {
            throw new IllegalArgumentException(String.format("Invalid indirect operand index constant %,d", c));
        }
        this.c = c;
        return this;
    }

    public IndirectOperandBuilder reg2(final Register r) {
        if (this.indexRegister != null) {
            throw new IllegalStateException("Cannot define reg2 twice");
        }
        Objects.requireNonNull(r);
        if (r.bits() != 32 && r.bits() != 64) {
            throw new IllegalArgumentException(r + " is an invalid index register");
        }
        if (baseRegister != null && r.bits() != baseRegister.bits()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot mix %,d-bit and %,d-bit registers (%s, %s)",
                    baseRegister.bits(), r.bits(), baseRegister, r));
        }
        this.indexRegister = r;
        return this;
    }

    public IndirectOperandBuilder disp(final byte disp) {
        return disp(disp, DisplacementType.BYTE);
    }

    public IndirectOperandBuilder disp(final short disp) {
        return disp(disp, DisplacementType.SHORT);
    }

    public IndirectOperandBuilder disp(final int disp) {
        return disp(disp, DisplacementType.INT);
    }

    public IndirectOperandBuilder disp(final long disp) {
        return disp(disp, DisplacementType.LONG);
    }

    private IndirectOperandBuilder disp(final long disp, final DisplacementType displacementType) {
        if (this.displacement != null) {
            throw new IllegalStateException("Cannot define displacement twice");
        }
        this.displacement = disp;
        this.displacementType = displacementType;
        return this;
    }

    public IndirectOperandBuilder pointer(final PointerSize ptrSize) {
        if (this.ptrSize != null) {
            throw new IllegalStateException("Cannot define PTR size twice");
        }

        this.ptrSize = Objects.requireNonNull(ptrSize);
        return this;
    }

    public IndirectOperand build() {
        if (alreadyBuilt) {
            throw new IllegalStateException("Cannot build the same IndirectOperandBuilder twice");
        }
        alreadyBuilt = true;

        if (baseRegister != null) {
            if (indexRegister == null || c == null) {
                throw new IllegalStateException("Cannot build an IndirectOperand with reg1=" + baseRegister + ", "
                        + (indexRegister == null ? "no reg2" : "reg2=" + indexRegister) + ", "
                        + (c == null ? "no constant" : "constant=" + c) + ", "
                        + (displacement == null ? "no displacement" : "displacement=" + displacement));
            }
            return new IndirectOperand(baseRegister, indexRegister, c, displacement, displacementType, ptrSize);

        } else {
            if (c != null) {
                if (indexRegister == null) {
                    throw new IllegalStateException("Cannot build an IndirectOperand with no reg1, no reg2, constant="
                            + c + ", " + (displacement == null ? "no displacement" : "displacement=" + displacement));
                }

                return new IndirectOperand(null, indexRegister, c, displacement, displacementType, ptrSize);
            } else {
                if (indexRegister == null && displacement == null) {
                    throw new IllegalStateException(
                            "Cannot build an IndirectOperand with no reg1, no reg2, no constant, no displacement");
                }

                if (indexRegister != null) {
                    return new IndirectOperand(null, indexRegister, 0, displacement, displacementType, ptrSize);
                } else {
                    // [displacement]
                    return new IndirectOperand(null, null, 0, displacement, displacementType, ptrSize);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "IndirectOperandBuilder(" + (baseRegister == null ? "no reg1" : "reg1=" + baseRegister) + ", "
                + (indexRegister == null ? "no reg2" : "reg2=" + indexRegister) + ", "
                + (c == null ? "no constant" : "constant=" + c) + ", "
                + (displacement == null ? "no displacement" : "displacement=" + displacement) + ")";
    }
}
