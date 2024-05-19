package com.ledmington.elf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public final class TestOSABI {
    @Test
    void allABIAreValid() {
        for (final OSABI abi : OSABI.values()) {
            assertTrue(OSABI.isValid(abi.getCode()));
        }
    }
}
