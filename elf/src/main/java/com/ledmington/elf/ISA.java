package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

/**
 * The ISA which can be specified in the ELF header.
 */
public enum ISA {

    /**
     * Unknown ISA.
     */
    UNKNOWN((short) 0x0000, "Not specified"),

    /**
     * AT{@literal &}T WE 32100.
     */
    AT_T_WE_32100((short) 0x0001, "AT&T WE 32100"),

    /**
     * AMD x86_64.
     */
    AMD_X86_64((short) 0x003e, "AMD x86-64");

    private static final Map<Short, ISA> codeToISA = new HashMap<>();

    static {
        for (final ISA x : ISA.values()) {
            if (codeToISA.containsKey(x.code())) {
                throw new IllegalStateException(String.format(
                        "ISA enum value with code %d (0x%02x) and name '%s' already exists",
                        x.code(), x.code(), x.ISAName()));
            }
            codeToISA.put(x.code(), x);
        }
    }

    /**
     * Checks whether the given code is a valid ELF ISA.
     */
    public static boolean isValid(final short code) {
        return codeToISA.containsKey(code);
    }

    /**
     * Returns the {@link ISA} corresponding to the given code.
     */
    public static ISA fromCode(final short code) {
        if (!codeToISA.containsKey(code)) {
            throw new IllegalArgumentException(String.format("Unknown ISA identifier: 0x%02x", code));
        }
        return codeToISA.get(code);
    }

    private final short code;
    private final String ISAName;

    ISA(final short code, final String ISAName) {
        this.code = code;
        this.ISAName = ISAName;
    }

    /**
     * Hexadecimal 16-bits code.
     */
    public short code() {
        return code;
    }

    /**
     * Name of the ISA.
     */
    public String ISAName() {
        return ISAName;
    }
}
