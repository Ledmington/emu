package com.ledmington.cpu.x86;

import java.util.Optional;

public final class IndirectOperandBuilder {

    private Optional<Register> reg1 = Optional.empty();
    private Optional<Integer> c = Optional.empty();
    private Optional<Register> reg2 = Optional.empty();
    private Optional<Long> displacement = Optional.empty();
    private DisplacementType displacementType = DisplacementType.LONG;
    private int ptrSize = 0;
    private boolean alreadyBuilt = false;

    public IndirectOperandBuilder() {}

    public IndirectOperandBuilder reg1(final Register r) {
        if (this.reg1.isPresent()) {
            throw new IllegalStateException("Cannot define reg1 twice");
        }
        this.reg1 = (r == null) ? Optional.empty() : Optional.of(r);
        return this;
    }

    public IndirectOperandBuilder constant(final int c) {
        if (this.c.isPresent()) {
            throw new IllegalStateException("Cannot define constant twice");
        }
        this.c = Optional.of(c);
        return this;
    }

    public IndirectOperandBuilder reg2(final Register r) {
        if (this.reg2.isPresent()) {
            throw new IllegalStateException("Cannot define reg2 twice");
        }
        this.reg2 = (r == null) ? Optional.empty() : Optional.of(r);
        return this;
    }

    public IndirectOperandBuilder displacement(final byte disp) {
        return displacement((long) disp, DisplacementType.BYTE);
    }

    public IndirectOperandBuilder displacement(final short disp) {
        return displacement((long) disp, DisplacementType.SHORT);
    }

    public IndirectOperandBuilder displacement(final int disp) {
        return displacement((long) disp, DisplacementType.INT);
    }

    public IndirectOperandBuilder displacement(final long disp) {
        return displacement(disp, DisplacementType.LONG);
    }

    private IndirectOperandBuilder displacement(final long disp, final DisplacementType displacementType) {
        if (this.displacement.isPresent()) {
            throw new IllegalStateException("Cannot define displacement twice");
        }
        this.displacement = Optional.of(disp);
        this.displacementType = displacementType;
        return this;
    }

    public IndirectOperandBuilder ptrSize(final int ptrSize) {
        if (this.ptrSize != 0) {
            throw new IllegalStateException("Cannot define PTR size twice");
        }

        if (ptrSize != 8 && ptrSize != 16 && ptrSize != 32 && ptrSize != 64 && ptrSize != 128) {
            throw new IllegalArgumentException(String.format("Invalid argument for ptrSize: %,d", ptrSize));
        }

        this.ptrSize = ptrSize;
        return this;
    }

    public IndirectOperand build() {
        if (alreadyBuilt) {
            throw new IllegalStateException("Cannot build the same IndirectOperandBuilder twice");
        }
        alreadyBuilt = true;

        // [reg2]
        if (reg1.isEmpty() && reg2.isPresent() && c.isEmpty() && displacement.isEmpty()) {
            return new IndirectOperand(null, reg2.orElseThrow(), 0, displacement, displacementType, ptrSize);
        } else
        // [reg2 + displacement]
        if (reg1.isEmpty() && reg2.isPresent() && c.isEmpty() && displacement.isPresent()) {
            return new IndirectOperand(null, reg2.orElseThrow(), 0, displacement, displacementType, ptrSize);
        } else
        // [reg2 * constant]
        if (reg1.isEmpty() && reg2.isPresent() && c.isPresent() && displacement.isEmpty()) {
            return new IndirectOperand(
                    null, reg2.orElseThrow(), c.orElseThrow(), displacement, displacementType, ptrSize);
        } else
        // [reg2 * constant + displacement]
        if (reg1.isEmpty() && reg2.isPresent() && c.isPresent() && displacement.isPresent()) {
            return new IndirectOperand(
                    null, reg2.orElseThrow(), c.orElseThrow(), displacement, displacementType, ptrSize);
        } else
        // [displacement]
        if (reg1.isEmpty() && reg2.isEmpty() && c.isEmpty() && displacement.isPresent()) {
            return new IndirectOperand(null, null, 0, displacement, displacementType, ptrSize);
        } else
        // [reg1 + reg2 * constant]
        if (reg1.isPresent() && reg2.isPresent() && c.isPresent() && displacement.isEmpty()) {
            return new IndirectOperand(
                    reg1.orElseThrow(), reg2.orElseThrow(), c.orElseThrow(), displacement, displacementType, ptrSize);
        } else
        // [reg1 + reg2 * constant + displacement]
        if (reg1.isPresent() && reg2.isPresent() && c.isPresent() && displacement.isPresent()) {
            return new IndirectOperand(
                    reg1.orElseThrow(), reg2.orElseThrow(), c.orElseThrow(), displacement, displacementType, ptrSize);
        }

        throw new IllegalStateException("Cannot build an IndirectOperand with "
                + (reg1.isEmpty() ? "no reg1" : "reg1=" + reg1.orElseThrow()) + ", "
                + (reg2.isEmpty() ? "no reg2" : "reg2=" + reg2.orElseThrow()) + ", "
                + (c.isEmpty() ? "no constant" : "constant=" + c.orElseThrow()) + ", "
                + (displacement.isEmpty() ? "no displacement" : "displacement=" + displacement.orElseThrow()));
    }

    @Override
    public String toString() {
        return "IndirectOperandBuilder(" + (reg1.isEmpty() ? "no reg1" : "reg1=" + reg1.orElseThrow()) + ", "
                + (reg2.isEmpty() ? "no reg2" : "reg2=" + reg2.orElseThrow()) + ", "
                + (c.isEmpty() ? "no constant" : "constant=" + c.orElseThrow()) + ", "
                + (displacement.isEmpty() ? "no displacement" : "displacement=" + displacement.orElseThrow()) + ")";
    }
}
