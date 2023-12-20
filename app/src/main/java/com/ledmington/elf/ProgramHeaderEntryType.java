package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public enum ProgramHeaderEntryType {
    PT_NULL(0x00000000, "Unused"),
    PT_LOAD(0x00000001, "Loadable"),
    PT_DYNAMIC(0x00000002, "Dynamic linking info"),
    PT_INTERP(0x00000003, "Interpreter info"),
    PT_NOTE(0x00000004, "Auxiliary info"),
    PT_SHLIB(0x00000005, "Reserved"),
    PT_PHDR(0x00000006, "Program header table"),
    PT_TLS(0x00000007, "Thread-Local Storage template"),
    PT_LOOS(0x60000000, "OS specific"),
    PT_HIOS(0x6fffffff, "OS specific"),
    PT_LOPROC(0x70000000, "Processor specific"),
    PT_HIPROC(0x7fffffff, "Processor specific");

    private static final Map<Integer, ProgramHeaderEntryType> codeToType = new HashMap<>();

    static {
        for (final ProgramHeaderEntryType x : ProgramHeaderEntryType.values()) {
            if (codeToType.containsKey(x.code())) {
                throw new IllegalStateException(String.format(
                        "ELF PHT entry type enum value with code %d (0x%02x) and name '%s' already exists",
                        x.code(), x.code(), x.description()));
            }
            codeToType.put(x.code(), x);
        }
    }

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || ((code & 0x60000000) != 0) || ((code & 0x70000000) != 0);
    }

    public static ProgramHeaderEntryType fromCode(final int code) {
        if (!isValid(code)) {
            throw new IllegalArgumentException(String.format("Unknown ELF PHT entry type identifier: 0x%02x", code));
        }
        if (((code & 0x60000000) != 0)) {
            return PT_LOOS;
        }
        if (((code & 0x70000000) != 0)) {
            return PT_LOPROC;
        }
        return codeToType.get(code);
    }

    private final int code;

    private final String description;

    ProgramHeaderEntryType(final int code, final String description) {
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
