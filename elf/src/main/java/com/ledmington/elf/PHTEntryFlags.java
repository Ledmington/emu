package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public enum PHTEntryFlags {
    PF_X(0x00000001, "Executable", 'X'),
    PF_W(0x00000002, "Writable", 'W'),
    PF_R(0x00000004, "Readable", 'R');

    private static final Map<Integer, PHTEntryFlags> codeToFlags = new HashMap<>();

    static {
        for (final PHTEntryFlags x : PHTEntryFlags.values()) {
            if (codeToFlags.containsKey(x.getCode())) {
                throw new IllegalStateException(String.format(
                        "PHT flags enum value with code %d (0x%02x) and description '%s' already exists",
                        x.getCode(), x.getCode(), x.getDescription()));
            }
            codeToFlags.put(x.getCode(), x);
        }
    }

    private final int code;
    private final String description;
    private final char id;

    PHTEntryFlags(final int code, final String description, final char id) {
        this.code = code;
        this.description = description;
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public char getId() {
        return id;
    }
}
