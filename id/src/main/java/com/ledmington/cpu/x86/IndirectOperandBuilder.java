package com.ledmington.cpu.x86;

public final class IndirectOperandBuilder {

    private Register reg1 = null;
    private Integer c = null;
    private Register reg2 = null;
    private Long displacement = null;
    private DisplacementType displacementType = DisplacementType.LONG;
    private int ptrSize = 0;
    private boolean alreadyBuilt = false;

    public IndirectOperandBuilder() {}

    public IndirectOperandBuilder reg1(final Register r) {
        if (this.reg1 != null) {
            throw new IllegalStateException("Cannot define reg1 twice");
        }
        this.reg1 = r;
        return this;
    }

    public IndirectOperandBuilder constant(final int c) {
        if (this.c != null) {
            throw new IllegalStateException("Cannot define constant twice");
        }
        this.c = c;
        return this;
    }

    public IndirectOperandBuilder reg2(final Register r) {
        if (this.reg2 != null) {
            throw new IllegalStateException("Cannot define reg2 twice");
        }
        this.reg2 = r;
        return this;
    }

    public IndirectOperandBuilder displacement(final byte disp) {
        return displacement(disp, DisplacementType.BYTE);
    }

    public IndirectOperandBuilder displacement(final short disp) {
        return displacement(disp, DisplacementType.SHORT);
    }

    public IndirectOperandBuilder displacement(final int disp) {
        return displacement(disp, DisplacementType.INT);
    }

    public IndirectOperandBuilder displacement(final long disp) {
        return displacement(disp, DisplacementType.LONG);
    }

    private IndirectOperandBuilder displacement(final long disp, final DisplacementType displacementType) {
        if (this.displacement != null) {
            throw new IllegalStateException("Cannot define displacement twice");
        }
        this.displacement = disp;
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

        if (reg1 != null) {
            if (reg2 == null || c == null) {
                throw new IllegalStateException("Cannot build an IndirectOperand with reg1=" + reg1 + ", "
                        + (reg2 == null ? "no reg2" : "reg2=" + reg2) + ", "
                        + (c == null ? "no constant" : "constant=" + c) + ", "
                        + (displacement == null ? "no displacement" : "displacement=" + displacement));
            }
            return new IndirectOperand(reg1, reg2, c, displacement, displacementType, ptrSize);

        } else {
            if (c != null) {
                if (reg2 == null) {
                    throw new IllegalStateException("Cannot build an IndirectOperand with no reg1, no reg2, constant="
                            + c + ", " + (displacement == null ? "no displacement" : "displacement=" + displacement));
                }

                return new IndirectOperand(null, reg2, c, displacement, displacementType, ptrSize);
            } else {
                if (reg2 == null && displacement == null) {
                    throw new IllegalStateException(
                            "Cannot build an IndirectOperand with no reg1, no reg2, no constant, no displacement");
                }

                if (reg2 != null) {
                    return new IndirectOperand(null, reg2, 0, displacement, displacementType, ptrSize);
                } else {
                    // [displacement]
                    return new IndirectOperand(null, null, 0, displacement, displacementType, ptrSize);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "IndirectOperandBuilder(" + (reg1 == null ? "no reg1" : "reg1=" + reg1) + ", "
                + (reg2 == null ? "no reg2" : "reg2=" + reg2) + ", "
                + (c == null ? "no constant" : "constant=" + c) + ", "
                + (displacement == null ? "no displacement" : "displacement=" + displacement) + ")";
    }
}
