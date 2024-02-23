package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public enum ISA {
    UNKNOWN((short) 0x0000, "Not specified"),
    AT_T_WE_32100((short) 0x0001, "AT&T WE 32100"),
    // others...
    AMD_X86_64((short) 0x003e, "AMD x86-64")
// others...
;

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

    public static boolean isValid(final short code) {
        return codeToISA.containsKey(code);
    }

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

    public short code() {
        return code;
    }

    public String ISAName() {
        return ISAName;
    }
}
