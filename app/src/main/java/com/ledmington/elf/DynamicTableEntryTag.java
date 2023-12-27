package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ledmington.utils.MiniLogger;

public final class DynamicTableEntryTag {

    private static final MiniLogger logger = MiniLogger.getLogger("dyntab-entry-tag");
    private static final Map<Long, DynamicTableEntryTag> codeToTag = new HashMap<>();

    public static final DynamicTableEntryTag DT_NULL = new DynamicTableEntryTag(0L, "NULL");
    public static final DynamicTableEntryTag DT_NEEDED = new DynamicTableEntryTag(1L, "NEEDED");
    public static final DynamicTableEntryTag DT_PLTRELSZ = new DynamicTableEntryTag(2L, "PTRELSZ");
    public static final DynamicTableEntryTag DT_PLTGOT = new DynamicTableEntryTag(3L, "PLTGOT");
    public static final DynamicTableEntryTag DT_HASH = new DynamicTableEntryTag(4L, "HASH");
    public static final DynamicTableEntryTag DT_STRTAB = new DynamicTableEntryTag(5L, "STRTAB");
    public static final DynamicTableEntryTag DT_SYMTAB = new DynamicTableEntryTag(6L, "SYMTAB");
    public static final DynamicTableEntryTag DT_RELA = new DynamicTableEntryTag(7L, "RELA");
    public static final DynamicTableEntryTag DT_RELASZ = new DynamicTableEntryTag(8L, "RELASZ");
    public static final DynamicTableEntryTag DT_RELAENT = new DynamicTableEntryTag(9L, "RELAENT");
    public static final DynamicTableEntryTag DT_STRSZ = new DynamicTableEntryTag(10L, "STRSZ");
    public static final DynamicTableEntryTag DT_SYMENT = new DynamicTableEntryTag(11L, "SYMENT");
    public static final DynamicTableEntryTag DT_INIT = new DynamicTableEntryTag(12L, "INIT");
    public static final DynamicTableEntryTag DT_FINI = new DynamicTableEntryTag(13L, "FINI");
    public static final DynamicTableEntryTag DT_SONAME = new DynamicTableEntryTag(14L, "SONAME");
    public static final DynamicTableEntryTag DT_RPATH = new DynamicTableEntryTag(15L, "RPATH");
    public static final DynamicTableEntryTag DT_SYMBOLIC = new DynamicTableEntryTag(16L, "SYMBOLIC");
    public static final DynamicTableEntryTag DT_REL = new DynamicTableEntryTag(17L, "REL");
    public static final DynamicTableEntryTag DT_RELSZ = new DynamicTableEntryTag(18L, "RELSZ");
    public static final DynamicTableEntryTag DT_RELENT = new DynamicTableEntryTag(19L, "RELENT");
    public static final DynamicTableEntryTag DT_PLTREL = new DynamicTableEntryTag(20L, "PLTREL");
    public static final DynamicTableEntryTag DT_DEBUG = new DynamicTableEntryTag(21L, "DEBUG");
    public static final DynamicTableEntryTag DT_TEXTREL = new DynamicTableEntryTag(22L, "TEXTREL");
    public static final DynamicTableEntryTag DT_JMPREL = new DynamicTableEntryTag(23L, "JMPREL");
    public static final DynamicTableEntryTag DT_BIND_NOW = new DynamicTableEntryTag(24L, "BIND_NOW");
    public static final DynamicTableEntryTag DT_INIT_ARRAY = new DynamicTableEntryTag(25L, "INIT_ARRAY");
    public static final DynamicTableEntryTag DT_FINI_ARRAY = new DynamicTableEntryTag(26L, "FINI_ARRAY");
    public static final DynamicTableEntryTag DT_INIT_ARRAYSZ = new DynamicTableEntryTag(27L, "INIT_ARRAYSZ");
    public static final DynamicTableEntryTag DT_FINI_ARRAYSZ = new DynamicTableEntryTag(28L, "FINI_ARRAYSZ");
    public static final DynamicTableEntryTag DT_RUNPATH = new DynamicTableEntryTag(29L, "RUNPATH");
    public static final DynamicTableEntryTag DT_FLAGS = new DynamicTableEntryTag(30L, "FLAGS");
    public static final DynamicTableEntryTag DT_PREINIT_ARRAY = new DynamicTableEntryTag(32L, "PREINIT_ARRAY");
    public static final DynamicTableEntryTag DT_GNU_HASH = new DynamicTableEntryTag(0x000000006ffffef5L, "GNU_HASH");
    public static final DynamicTableEntryTag DT_VERSYM = new DynamicTableEntryTag(0x000000006ffffff0L, "VERSYM");
    public static final DynamicTableEntryTag DT_RELACOUNT = new DynamicTableEntryTag(0x000000006ffffff9L, "VERSYM");
    public static final DynamicTableEntryTag DT_FLAGS_1 = new DynamicTableEntryTag(0x000000006ffffffbL, "FLAGS_1");

    // Address of version definition
    public static final DynamicTableEntryTag DT_VERDEF = new DynamicTableEntryTag(0x000000006ffffffcL, "VERDEF");

    // Number of version definitions
    public static final DynamicTableEntryTag DT_VERDEFNUM = new DynamicTableEntryTag(0x000000006ffffffdL, "VERDEFNUM");
    public static final DynamicTableEntryTag DT_VERNEEDED = new DynamicTableEntryTag(0x000000006ffffffeL, "VERNEEDED");
    public static final DynamicTableEntryTag DT_VERNEEDNUM =
            new DynamicTableEntryTag(0x000000006fffffffL, "VERNEEDEDNUM");
    public static final DynamicTableEntryTag DT_LOOS =
            new DynamicTableEntryTag(0x0000000060000000, "OS-specific", false);
    public static final DynamicTableEntryTag DT_HIOS =
            new DynamicTableEntryTag(0x000000006fffffff, "OS-specific", false);
    public static final DynamicTableEntryTag DT_LOPROC =
            new DynamicTableEntryTag(0x0000000070000000, "Processor-specific", false);
    public static final DynamicTableEntryTag DT_HIPROC =
            new DynamicTableEntryTag(0x000000007fffffff, "Processor-specific", false);

    public static DynamicTableEntryTag fromCode(final long code) {
        if (!codeToTag.containsKey(code)) {
            if (code >= DT_LOOS.code() && code <= DT_HIOS.code()) {
                logger.warning("Unknown Dynamic table entry tag found: 0x%016x", code);
                return new DynamicTableEntryTag(code, "OS-specific", false);
            }
            if (code >= DT_LOPROC.code() && code <= DT_HIPROC.code()) {
                logger.warning("Unknown Dynamic table entry tag found: 0x%016x", code);
                return new DynamicTableEntryTag(code, "Processor-specific", false);
            }
            throw new IllegalArgumentException(
                    String.format("Unknown Dynamic table entry tag identifier: 0x%016x", code));
        }
        return codeToTag.get(code);
    }

    private final long code;
    private final String name;

    private DynamicTableEntryTag(final long code, final String name, final boolean addToMap) {
        this.code = code;
        this.name = Objects.requireNonNull(name);

        if (addToMap) {
            if (codeToTag.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "Dynamic table entry tag value with code %d (0x%016x) already exists", code, code));
            }
            codeToTag.put(code, this);
        }
    }

    private DynamicTableEntryTag(final long code, final String name) {
        this(code, name, true);
    }

    public long code() {
        return code;
    }

    public String name() {
        return name;
    }
}
