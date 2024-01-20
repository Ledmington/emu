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

    public static IndirectOperand of(final Register r) {
        return new IndirectOperand(Optional.of(r), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static IndirectOperand of(final Register r, final long displacement) {
        return new IndirectOperand(Optional.of(r), Optional.empty(), Optional.empty(), Optional.of(displacement));
    }

    public static IndirectOperand of(final long displacement) {
        return new IndirectOperand(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(displacement));
    }

    public static IndirectOperand of(final Register r1, final int constant, final Register r2) {
        return new IndirectOperand(Optional.of(r1), Optional.of(constant), Optional.of(r2), Optional.empty());
    }

    public static IndirectOperand of(
            final Register r1, final int constant, final Register r2, final long displacement) {
        return new IndirectOperand(Optional.of(r1), Optional.of(constant), Optional.of(r2), Optional.of(displacement));
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
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("QWORD PTR ");
        sb.append('[');
        if (reg1.isPresent()) {
            sb.append(reg1.orElseThrow());
        }
        if (constant.isPresent()) {
            sb.append('*');
            sb.append(constant.orElseThrow());
        }
        if (reg2.isPresent()) {
            sb.append('+');
            sb.append(reg2.orElseThrow());
        }
        if (displacement.isPresent()) {
            sb.append(String.format("%c0x%x", displacement.orElseThrow() < 0 ? '-' : '+', displacement.orElseThrow()));
        }
        sb.append(']');
        return sb.toString();
    }
}
