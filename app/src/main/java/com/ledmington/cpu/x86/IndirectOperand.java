package com.ledmington.cpu.x86;

import java.util.Objects;
import java.util.Optional;

import com.ledmington.utils.BitUtils;

/**
 * This class maps the following cases:
 * [reg1]
 * [reg1 + displacement]
 * [displacement]
 * [reg1 * constant + reg2]
 * [reg1 * constant + reg2 + displacement]
 */
public final class IndirectOperand implements Operand {

    private enum Type {
        BYTE,
        SHORT,
        INT,
        LONG;
    }

    private final Register reg1;
    private final int constant;
    private final Register reg2;
    private final Optional<Long> displacement;
    private final Type displacementType;

    public static IndirectOperandBuilder builder() {
        return new IndirectOperandBuilder();
    }

    public static final class IndirectOperandBuilder {

        private Optional<Register> reg1 = Optional.empty();
        private Optional<Integer> c = Optional.empty();
        private Optional<Register> reg2 = Optional.empty();
        private Optional<Long> displacement = Optional.empty();
        private Type displacementType = Type.LONG;
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
            return displacement((long) disp, Type.BYTE);
        }

        public IndirectOperandBuilder displacement(final short disp) {
            return displacement((long) disp, Type.SHORT);
        }

        public IndirectOperandBuilder displacement(final int disp) {
            return displacement((long) disp, Type.INT);
        }

        public IndirectOperandBuilder displacement(final long disp) {
            return displacement(disp, Type.LONG);
        }

        private IndirectOperandBuilder displacement(final long disp, final Type displacementType) {
            if (this.displacement.isPresent()) {
                throw new IllegalStateException("Cannot define displacement twice");
            }
            this.displacement = Optional.of(disp);
            this.displacementType = displacementType;
            return this;
        }

        public IndirectOperand build() {
            if (alreadyBuilt) {
                throw new IllegalStateException("Cannot build the same IndirectOperandBuilder twice");
            }
            alreadyBuilt = true;

            // swap reg1 with reg2 if reg2 is defined but reg1 is not
            if (reg1.isEmpty() && reg2.isPresent()) {
                reg1 = reg2;
                reg2 = Optional.empty();
            }

            // [reg1]
            if (reg1.isPresent() && c.isEmpty() && reg2.isEmpty() && displacement.isEmpty()) {
                return new IndirectOperand(reg1.orElseThrow(), 0, null, displacement, displacementType);
            } else
            // [reg1 + disp]
            if (reg1.isPresent() && c.isEmpty() && reg2.isEmpty() && displacement.isPresent()) {
                return new IndirectOperand(reg1.orElseThrow(), 0, null, displacement, displacementType);
            } else
            // [disp]
            if (reg1.isEmpty() && c.isEmpty() && reg2.isEmpty() && displacement.isPresent()) {
                return new IndirectOperand(null, 0, null, displacement, displacementType);
            } else
            // [reg1*c + reg2]
            if (reg1.isPresent() && c.isPresent() && reg2.isPresent() && displacement.isEmpty()) {
                return new IndirectOperand(
                        reg1.orElseThrow(), c.orElseThrow(), reg2.orElseThrow(), displacement, displacementType);
            } else
            // [reg1*c + disp]
            if (reg1.isPresent() && c.isPresent() && reg2.isEmpty() && displacement.isPresent()) {
                return new IndirectOperand(reg1.orElseThrow(), c.orElseThrow(), null, displacement, displacementType);
            } else
            // [reg1*c + reg2 + disp]
            if (reg1.isPresent() && c.isPresent() && reg2.isPresent() && displacement.isPresent()) {
                return new IndirectOperand(
                        reg1.orElseThrow(), c.orElseThrow(), reg2.orElseThrow(), displacement, displacementType);
            }

            throw new IllegalStateException("Cannot build an IndirectOperand with "
                    + (reg1.isEmpty() ? "no reg1" : "reg1=" + reg1.orElseThrow()) + ", "
                    + (c.isEmpty() ? "no constant" : "constant=" + c.orElseThrow()) + ", "
                    + (reg2.isEmpty() ? "no reg2" : "reg2=" + reg2.orElseThrow()) + ", "
                    + (displacement.isEmpty() ? "no displacement" : "displacement=" + displacement.orElseThrow()));
        }
    }

    private IndirectOperand(
            final Register reg1,
            final int constant,
            final Register reg2,
            final Optional<Long> displacement,
            final Type displacementType) {
        this.reg1 = reg1;
        this.constant = constant;
        this.reg2 = reg2;
        this.displacement = Objects.requireNonNull(displacement);
        this.displacementType = Objects.requireNonNull(displacementType);

        if (constant != 0 && Integer.bitCount(constant) != 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid 'constant' value: expected 0 or a power of two but was %,d", constant));
        }
    }

    @Override
    public String toIntelSyntax() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (reg1 != null) {
            sb.append(reg1.toIntelSyntax());
        }
        if (reg2 != null) {
            sb.append('+');
            sb.append(reg2.toIntelSyntax());
        }
        if (constant != 0 && constant != 1) {
            sb.append('*');
            sb.append(constant);
        }
        if (displacement.isPresent()) {
            long d = displacement.orElseThrow();
            if (displacement.orElseThrow() < 0) {
                d = switch (displacementType) {
                    case BYTE -> (~BitUtils.asByte(d)) + 1;
                    case SHORT -> (~BitUtils.asShort(d)) + 1;
                    case INT -> (~BitUtils.asInt(d)) + 1;
                    case LONG -> (~d) + 1;};
            }
            if (sb.length() > 1) {
                sb.append((displacement.orElseThrow() < 0) ? '-' : '+');
            }
            sb.append(String.format("0x%x", d));
        }
        sb.append(']');
        return sb.toString();
    }
}
