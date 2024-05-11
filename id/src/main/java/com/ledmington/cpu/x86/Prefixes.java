package com.ledmington.cpu.x86;

import java.util.Optional;

public record Prefixes(
        Optional<InstructionPrefix> p1,
        Optional<Byte> p2,
        boolean hasOperandSizeOverridePrefix,
        boolean hasAddressSizeOverridePrefix,
        boolean hasRexPrefix,
        RexPrefix rex) {

    @Override
    public String toString() {
        return "Prefixes[p1=" + (p1.isPresent() ? p1.orElseThrow().name() : p1) + ";p2="
                + (p2.isPresent() ? String.format("0x%02x", p2.orElseThrow()) : p2) + ";hasOperandSizeOverridePrefix="
                + hasOperandSizeOverridePrefix + ";hasAddressSizeoverridePrefix=" + hasAddressSizeOverridePrefix
                + ";rex=" + rex.toString() + "]";
    }
}
