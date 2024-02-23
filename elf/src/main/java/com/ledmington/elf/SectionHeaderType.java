package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

import com.ledmington.utils.MiniLogger;

public final class SectionHeaderType {

    private static final MiniLogger logger = MiniLogger.getLogger("sht-entry-type");
    private static final Map<Integer, SectionHeaderType> codeToType = new HashMap<>();

    public static final SectionHeaderType SHT_NULL = new SectionHeaderType(0x00000000, "SHT_NULL", "Entry unused");
    public static final SectionHeaderType SHT_PROGBITS =
            new SectionHeaderType(0x00000001, "SHT_PROGBITS", "Program data");
    public static final SectionHeaderType SHT_SYMTAB = new SectionHeaderType(0x00000002, "SHT_SYMTAB", "Symbol table");
    public static final SectionHeaderType SHT_STRTAB = new SectionHeaderType(0x00000003, "SHT_STRTAB", "String table");
    public static final SectionHeaderType SHT_RELA =
            new SectionHeaderType(0x00000004, "SHT_RELA", "Relocation entries with addends");
    public static final SectionHeaderType SHT_HASH = new SectionHeaderType(0x00000005, "SHT_HASH", "Symbol Hash table");
    public static final SectionHeaderType SHT_DYNAMIC =
            new SectionHeaderType(0x00000006, "SHT_DYNAMIC", "Dynamic linking info");
    public static final SectionHeaderType SHT_NOTE = new SectionHeaderType(0x00000007, "SHT_NOTE", "Notes");
    public static final SectionHeaderType SHT_NOBITS =
            new SectionHeaderType(0x00000008, "SHT_NOBITS", "Program space with no data (bss)");
    public static final SectionHeaderType SHT_REL =
            new SectionHeaderType(0x00000009, "SHT_REL", "Relocation entries (no addends)");
    public static final SectionHeaderType SHT_SHLIB = new SectionHeaderType(0x0000000a, "SHT_SHLIB", "Reserved");
    public static final SectionHeaderType SHT_DYNSYM =
            new SectionHeaderType(0x0000000b, "SHT_DYNSYM", "Dynamic linker symbol table");
    public static final SectionHeaderType SHT_INIT_ARRAY =
            new SectionHeaderType(0x0000000e, "SHT_INIT_ARRAY", "Array of constructors");
    public static final SectionHeaderType SHT_FINI_ARRAY =
            new SectionHeaderType(0x0000000f, "SHT_FINI_ARRAY", "Array of destructors");
    public static final SectionHeaderType SHT_PREINIT_ARRAY =
            new SectionHeaderType(0x00000010, "SHT_PREINIT_ARRAY", "Array of pre-constructors");
    public static final SectionHeaderType SHT_GROUP = new SectionHeaderType(0x00000011, "SHT_GROUP", "Section group");
    public static final SectionHeaderType SHT_SYMTAB_SHNDX =
            new SectionHeaderType(0x00000012, "SHT_SYMTAB_SHNDX", "Extended section indices");
    public static final SectionHeaderType SHT_NUM =
            new SectionHeaderType(0x00000013, "SHT_NUM", "Number of defined types");

    public static final SectionHeaderType SHT_LOOS =
            new SectionHeaderType(0x60000000, "SHT_LOOS", "Unknown (OS specific)", false);
    public static final SectionHeaderType SHT_GNU_HASH =
            new SectionHeaderType(0x6ffffff6, "SHT_GNU_HASH", "GNU Hash table");
    public static final SectionHeaderType SHT_GNU_verdef =
            new SectionHeaderType(0x6ffffffd, "SHT_GNU_verdef", "GNU version symbol definitions");
    public static final SectionHeaderType SHT_GNU_verneed =
            new SectionHeaderType(0x6ffffffe, "SHT_GNU_verneed", "GNU version symbol needed elements");
    public static final SectionHeaderType SHT_GNU_versym =
            new SectionHeaderType(0x6fffffff, "SHT_GNU_versym", "GNU version symbol table");
    public static final SectionHeaderType SHT_HIOS =
            new SectionHeaderType(0x6fffffff, "SHT_HIOS", "Unknown (OS specific)", false);

    public static final SectionHeaderType SHT_LOPROC =
            new SectionHeaderType(0x70000000, "SHT_LOPROC", "Unknown (Processor specific)", false);
    public static final SectionHeaderType SHT_HIPROC =
            new SectionHeaderType(0x7fffffff, "SHT_HIPROC", "Unknown (Processor specific)", false);

    public static final SectionHeaderType SHT_LOUSER =
            new SectionHeaderType(0x80000000, "SHT_LOUSER", "Unknown (Application specific)", false);
    public static final SectionHeaderType SHT_HIUSER =
            new SectionHeaderType(0xffffffff, "SHT_HIUSER", "Unknown (Application specific)", false);

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || code >= SHT_LOOS.code();
    }

    public static SectionHeaderType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= SHT_LOOS.code() && code <= SHT_HIOS.code()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(code, "Unknown", String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= SHT_LOPROC.code() && code <= SHT_HIPROC.code()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(
                        code, "Unknown", String.format("0x%08x (Processor specific)", code), false);
            }
            if (code >= SHT_LOUSER.code() && code <= SHT_HIUSER.code()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(
                        code, "Unknown", String.format("0x%08x (Application specific)", code), false);
            }
            throw new IllegalArgumentException(String.format("Unknown SHT entry identifier: 0x%08x", code));
        }
        return codeToType.get(code);
    }

    private final int code;
    private final String name;
    private final String description;

    private SectionHeaderType(final int code, final String name, final String description, final boolean addToMap) {
        this.code = code;
        this.name = name;
        this.description = description;

        if (addToMap) {
            if (codeToType.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "SHT entry enum value with code %d (0x%02x) and description '%s' already exists",
                        code, code, description));
            }
            codeToType.put(code, this);
        }
    }

    private SectionHeaderType(final int code, final String name, final String description) {
        this(code, name, description, true);
    }

    public int code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }
}
