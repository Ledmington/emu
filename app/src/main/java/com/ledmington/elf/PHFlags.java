package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public enum PHFlags {
    PF_X(0x00000001, "Executable"),
    PF_W(0x00000002, "Writable"),
    PF_R(0x00000004, "Readable");

    private static final Map<Integer, PHFlags> codeToFlags = new HashMap<>();

    static {
        for (final PHFlags x : PHFlags.values()) {
            if (codeToFlags.containsKey(x.code())) {
                throw new IllegalStateException(String.format(
                        "PHT flags enum value with code %d (0x%02x) and description '%s' already exists",
                        x.code(), x.code(), x.description()));
            }
            codeToFlags.put(x.code(), x);
        }
    }

    private final int code;
    private final String description;

    PHFlags(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }
}
