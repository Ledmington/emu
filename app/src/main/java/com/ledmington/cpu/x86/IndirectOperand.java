package com.ledmington.cpu.x86;

import java.util.Objects;
import java.util.Optional;

/**
 * This class maps the following cases:
 * [reg1]
 * [reg1 + displacement]
 * [displacement]
 * [reg1 * constant + reg2]
 * [reg1 * constant + reg2 + displacement]
 */
public final class IndirectOperand implements Operand {

    private final Optional<Register> reg1;
    private final Optional<Integer> constant;
    private final Optional<Register> reg2;
    private final Optional<Long> displacement;

    public static IndirectOperandBuilder builder() {
        return new IndirectOperandBuilder();
    }

    public static final class IndirectOperandBuilder {

        private Optional<Register> reg1 = Optional.empty();
        private Optional<Integer> c = Optional.empty();
        private Optional<Register> reg2 = Optional.empty();
        private Optional<Long> displacement = Optional.empty();
        private boolean alreadyBuilt = false;

        public IndirectOperandBuilder() {}

        public IndirectOperandBuilder reg1(final Register r) {
            if (this.reg1.isPresent()) {
                throw new IllegalStateException("Cannot define reg1 twice");
            }
            this.reg1 = Optional.of(r);
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
            this.reg2 = Optional.of(r);
            return this;
        }

        public IndirectOperandBuilder displacement(final long disp) {
            if (this.displacement.isPresent()) {
                throw new IllegalStateException("Cannot define displacement twice");
            }
            this.displacement = Optional.of(disp);
            return this;
        }

        public IndirectOperand build() {
            if (alreadyBuilt) {
                throw new IllegalStateException("Cannot build the same IndirectOperandBuilder twice");
            }
            alreadyBuilt = true;

            if (reg1.isPresent() && c.isEmpty() && reg2.isEmpty() && displacement.isEmpty()) {
                return new IndirectOperand(reg1, c, reg2, displacement);
            } else if (reg1.isPresent() && c.isEmpty() && reg2.isPresent() && displacement.isEmpty()) {
                return new IndirectOperand(reg1, c, reg2, displacement);
            } else if (reg1.isEmpty() && c.isEmpty() && reg2.isPresent() && displacement.isEmpty()) {
                return new IndirectOperand(reg1, c, reg2, displacement);
            } else if (reg1.isPresent() && c.isPresent() && reg2.isPresent() && displacement.isEmpty()) {
                return new IndirectOperand(reg1, c, reg2, displacement);
            } else if (reg1.isPresent() && c.isPresent() && reg2.isPresent() && displacement.isPresent()) {
                return new IndirectOperand(reg1, c, reg2, displacement);
            }

            throw new IllegalStateException("Cannot build an IndirectOperand with "
                    + (reg1.isEmpty() ? "no reg1" : "reg1=" + reg1.orElseThrow().toString()) + ", "
                    + (c.isEmpty() ? "no constant" : "constant=" + c.orElseThrow()) + ", "
                    + (reg2.isEmpty() ? "no reg2" : "reg2=" + reg2.orElseThrow().toString()) + ", "
                    + (displacement.isEmpty() ? "no displacement" : "displacement=" + displacement.orElseThrow()));
        }
    }

    private IndirectOperand(
            final Optional<Register> reg1,
            final Optional<Integer> constant,
            final Optional<Register> reg2,
            final Optional<Long> displacement) {
        this.reg1 = Objects.requireNonNull(reg1);
        this.constant = Objects.requireNonNull(constant);
        this.reg2 = Objects.requireNonNull(reg2);
        this.displacement = Objects.requireNonNull(displacement);

        if (constant.isPresent()) {
            if (constant.orElseThrow() != 0 && Integer.bitCount(constant.orElseThrow()) != 1) {
                throw new IllegalArgumentException(String.format(
                        "Invalid 'constant' value: expected 0 or a power of two but was %,d", constant.orElseThrow()));
            }
        }
    }

    @Override
    public String toIntelSyntax() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (reg1.isPresent()) {
            sb.append(reg1.orElseThrow().toIntelSyntax());
        }
        if (constant.isPresent() && constant.orElseThrow() != 0) {
            sb.append('*');
            sb.append(constant.orElseThrow());
        }
        if (reg2.isPresent() && !reg2.equals(reg1)) {
            sb.append('+');
            sb.append(reg2.orElseThrow().toIntelSyntax());
        }
        if (displacement.isPresent()) {
            sb.append(String.format("%c0x%x", displacement.orElseThrow() < 0 ? '-' : '+', displacement.orElseThrow()));
        }
        sb.append(']');
        return sb.toString();
    }
}
