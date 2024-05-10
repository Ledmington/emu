package com.ledmington.cpu.x86;

public final class IndirectOperandBuilder {

    private Register baseRegister = null;
    private Integer c = null;
    private Register indexRegister = null;
    private Long displacement = null;
    private DisplacementType displacementType = DisplacementType.LONG;
    private int ptrSize = 0;
    private boolean alreadyBuilt = false;

    public IndirectOperandBuilder() {}

    public IndirectOperandBuilder reg1(final Register r) {
        if (this.baseRegister != null) {
            throw new IllegalStateException("Cannot define reg1 twice");
        }
        this.baseRegister = r;
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
        if (this.indexRegister != null) {
            throw new IllegalStateException("Cannot define reg2 twice");
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
