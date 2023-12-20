package com.ledmington.elf;

public enum SectionHeaderEntryFlags {
    SHF_WRITE(0x00000000000001L, "Writable"),
    SHF_ALLOC(0x00000000000002L, "Occupies memory during execution"),
    SHT_EXECINSTR(0x00000000000004L, "Executable"),
    SHF_MERGE(0x00000000000010L, "Might be merged"),
    SHF_STRINGS(0x00000000000020L, "Contains null-terminated strings"),
    SHF_INFO_LINK(0x00000000000040L, "'sh_info' contains SHT index"),
    SHF_LINK_ORDER(0x00000000000080L, "Preserve order after combining"),
    SHF_OS_NONCONFORMING(0x00000000000100L, "Non-standard OS specific handling required"),
    SHF_GROUP(0x00000000000200L, "Section is member of a group"),
    SHF_TLS(0x00000000000400L, "Section hold thread-local data"),
    SHF_MASKOS(0x000000000ff00000L, "OS specific"),
    SHF_MASKPROC(0x00000000f0000000L, "Processor specific"),
    SHF_ORDERED(0x0000000004000000L, "Special ordering requirement (Solaris)"),
    SHF_EXCLUDE(0x0000000008000000L, "Section is excluded unless referenced or allocated (Solaris)");

    public static boolean isValid(long flags) {
        for (final SectionHeaderEntryFlags f : SectionHeaderEntryFlags.values()) {
            flags = flags & (~f.code());
        }
        return flags == 0L;
    }

    private final long code;
    private final String description;

    SectionHeaderEntryFlags(final long code, final String description) {
        this.code = code;
        this.description = description;
    }

    public long code() {
        return code;
    }

    public String description() {
        return description;
    }
}
