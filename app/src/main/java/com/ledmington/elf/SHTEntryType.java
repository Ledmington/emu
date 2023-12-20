package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

import com.ledmington.utils.MiniLogger;

public final class SHTEntryType {

    private static final MiniLogger logger = MiniLogger.getLogger("sht-entry-type");
    private static final Map<Integer, SHTEntryType> codeToType = new HashMap<>();

    public static final SHTEntryType SHT_NULL = new SHTEntryType(0x00000000, "Entry unused");
    public static final SHTEntryType SHT_PROGBITS = new SHTEntryType(0x00000001, "Program data");
    public static final SHTEntryType SHT_SYMTAB = new SHTEntryType(0x00000002, "Symbol table");
    public static final SHTEntryType SHT_STRTAB = new SHTEntryType(0x00000003, "String table");
    public static final SHTEntryType SHT_RELA = new SHTEntryType(0x00000004, "Relocation entries with addends");
    public static final SHTEntryType SHT_HASH = new SHTEntryType(0x00000005, "Symbol Hash table");
    public static final SHTEntryType SHT_DYNAMIC = new SHTEntryType(0x00000006, "Dynamic linking info");
    public static final SHTEntryType SHT_NOTE = new SHTEntryType(0x00000007, "Notes");
    public static final SHTEntryType SHT_NOBITS = new SHTEntryType(0x00000008, "Program space with no data (bss)");
    public static final SHTEntryType SHT_REL = new SHTEntryType(0x00000009, "Relocation entries (no addends)");
    public static final SHTEntryType SHT_SHLIB = new SHTEntryType(0x0000000a, "Reserved");
    public static final SHTEntryType SHT_DYNSYM = new SHTEntryType(0x0000000b, "Dynamic linker symbol table");
    public static final SHTEntryType SHT_INIT_ARRAY = new SHTEntryType(0x0000000e, "Array of constructors");
    public static final SHTEntryType SHT_FINI_ARRAY = new SHTEntryType(0x0000000f, "Array of destructors");
    public static final SHTEntryType SHT_PREINIT_ARRAY = new SHTEntryType(0x00000010, "Array of pre-constructors");
    public static final SHTEntryType SHT_GROUP = new SHTEntryType(0x00000011, "Section group");
    public static final SHTEntryType SHT_SYMTAB_SHNDX = new SHTEntryType(0x00000012, "Extended section indices");
    public static final SHTEntryType SHT_NUM = new SHTEntryType(0x00000013, "Number of defined types");

    public static final SHTEntryType SHT_LOOS = new SHTEntryType(0x60000000, "Unknown (OS specific)", false);
    public static final SHTEntryType SHT_GNU_HASH = new SHTEntryType(0x6ffffff6, "GNU Hash table");
    public static final SHTEntryType SHT_GNU_verdef = new SHTEntryType(0x6ffffffd, "GNU version symbol definitions");
    public static final SHTEntryType SHT_GNU_verneed =
            new SHTEntryType(0x6ffffffe, "GNU version symbol needed elements");
    public static final SHTEntryType SHT_GNU_versym = new SHTEntryType(0x6fffffff, "GNU version symbol table");
    public static final SHTEntryType SHT_HIOS = new SHTEntryType(0x6fffffff, "Unknown (OS specific)", false);

    public static final SHTEntryType SHT_LOPROC = new SHTEntryType(0x70000000, "Unknown (Processor specific)", false);
    public static final SHTEntryType SHT_HIPROC = new SHTEntryType(0x7fffffff, "Unknown (Processor specific)", false);

    public static final SHTEntryType SHT_LOUSER = new SHTEntryType(0x80000000, "Unknown (Application specific)", false);
    public static final SHTEntryType SHT_HIUSER = new SHTEntryType(0xffffffff, "Unknown (Application specific)", false);

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || code >= SHT_LOOS.code();
    }

    public static SHTEntryType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= SHT_LOOS.code() && code <= SHT_HIOS.code()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SHTEntryType(code, String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= SHT_LOPROC.code() && code <= SHT_HIPROC.code()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SHTEntryType(code, String.format("0x%08x (Processor specific)", code), false);
            }
            if (code >= SHT_LOUSER.code() && code <= SHT_HIUSER.code()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SHTEntryType(code, String.format("0x%08x (Application specific)", code), false);
            }
            throw new IllegalArgumentException(String.format("Unknown SHT entry identifier: 0x%08x", code));
        }
        return codeToType.get(code);
    }

    private final int code;
    private final String description;

    private SHTEntryType(final int code, final String description, final boolean addToMap) {
        this.code = code;
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

    private SHTEntryType(final int code, final String description) {
        this(code, description, true);
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String toString() {
        return description;
    }
}
