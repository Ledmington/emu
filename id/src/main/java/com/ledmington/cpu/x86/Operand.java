package com.ledmington.cpu.x86;

/**
 * An operand of an x86 instruction.
 */
public interface Operand {

    /**
     * Reference obtainable through 'objdump -Mintel-mnemonic ...'
     */
    String toIntelSyntax();
}
