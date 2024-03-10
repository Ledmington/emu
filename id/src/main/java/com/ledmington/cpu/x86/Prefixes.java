package com.ledmington.cpu.x86;

import java.util.Optional;

public record Prefixes(
        Optional<Byte> p1,
        Optional<Byte> p2,
        boolean hasOperandSizeOverridePrefix,
        boolean hasAddressSizeOverridePrefix,
        RexPrefix rex) {}
