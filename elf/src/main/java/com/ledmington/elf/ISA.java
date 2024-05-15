package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

/**
 * The ISA which can be specified in an ELF header.
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
            if (codeToISA.containsKey(x.getCode())) {
                throw new IllegalStateException(String.format(
                        "ISA enum value with code %d (0x%02x) and name '%s' already exists",
                        x.getCode(), x.getCode(), x.getName()));
            }
            codeToISA.put(x.getCode(), x);
        }
    }

    /**
     * Checks whether the given code is a valid ELF ISA.
     *
     * @param code
     *      The code representing the ISA.
     * @return
     *      True if the given code exists, false otherwise.
     */
    public static boolean isValid(final short code) {
        return codeToISA.containsKey(code);
    }

    /**
     * Returns the {@link ISA} corresponding to the given code.
     *
     * @param code
     *      The code representing the ISA.
     * @return
     *      The ISA object corresponding to the given code.
     */
    public static ISA fromCode(final short code) {
        if (!codeToISA.containsKey(code)) {
            throw new IllegalArgumentException(String.format("Unknown ISA identifier: 0x%02x", code));
        }
        return codeToISA.get(code);
    }

    private final short code;
    private final String name;

    ISA(final short code, final String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Hexadecimal 16-bits code.
     *
     * @return
     *      The code of this ISA object.
     */
    public short getCode() {
        return code;
    }

    /**
     * Name of the ISA.
     *
     * @return
     *      A string representation of this ISA object.
     */
    public String getName() {
        return name;
    }
}
