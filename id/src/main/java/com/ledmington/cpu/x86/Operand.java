package com.ledmington.cpu.x86;

public interface Operand {

    /**
     * Reference obtainable through 'objdump -Mintel-mnemonic ...'
     */
    String toIntelSyntax();
}
