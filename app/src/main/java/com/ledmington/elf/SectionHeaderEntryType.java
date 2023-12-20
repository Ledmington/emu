package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public enum SectionHeaderEntryType {
    SHT_NULL(0x00000000, "Entry unused"),
    SHT_PROGBITS(0x00000001, "Program data"),
    SHT_SYMTAB(0x00000002, "Symbol table"),
    SHT_STRTAB(0x00000003, "String table"),
    SHT_RELA(0x00000004, "Relocation entries with addends"),
    SHT_HASH(0x00000005, "Symbol Hash table"),
    SHT_DYNAMIC(0x00000006, "Dynamic linking info"),
    SHT_NOTE(0x00000007, "Notes"),
    SHT_NOBITS(0x00000008, "Program space with no data (bss)"),
    SHT_REL(0x00000009, "Relocation entries (no addends)"),
    SHT_SHLIB(0x0000000a, "Reserved"),
    SHT_DYNSYM(0x0000000b, "Dynamic linker symbol table"),
    SHT_INIT_ARRAY(0x0000000e, "Array of constructors"),
    SHT_FINI_ARRAY(0x0000000f, "Array of destructors"),
    SHT_PREINIT_ARRAY(0x00000010, "Array of pre-constructors"),
    SHT_GROUP(0x00000011, "Section group"),
    SHT_SYMTAB_SHNDX(0x00000012, "Extended section indices"),
    SHT_NUM(0x00000013, "Number of defined types"),
    SHT_LOOS(0x60000000, "OS specific");

    private static final Map<Integer, SectionHeaderEntryType> codeToType = new HashMap<>();

    static {
        for (final SectionHeaderEntryType x : SectionHeaderEntryType.values()) {
            if (codeToType.containsKey(x.code())) {
                throw new IllegalStateException(String.format(
                        "SHT entry enum value with code %d (0x%02x) and description '%s' already exists",
                        x.code(), x.code(), x.description()));
            }
            codeToType.put(x.code(), x);
        }
    }

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || code >= 0x60000000;
    }

    public static SectionHeaderEntryType fromCode(final int code) {
        if (code >= 0x60000000) {
            return SHT_LOOS;
        }
        if (!codeToType.containsKey(code)) {
            throw new IllegalArgumentException(String.format("Unknown SHT entry identifier: 0x%08x", code));
        }
        return codeToType.get(code);
    }

    private final int code;
    private final String description;

    SectionHeaderEntryType(final int code, final String description) {
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
