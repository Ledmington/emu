package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

import com.ledmington.utils.MiniLogger;

public final class PHTEntryType {

    private static final MiniLogger logger = MiniLogger.getLogger("pht-entry-type");
    private static final Map<Integer, PHTEntryType> codeToType = new HashMap<>();

    public static final PHTEntryType PT_NULL = new PHTEntryType(0x00000000, "Unused");
    public static final PHTEntryType PT_LOAD = new PHTEntryType(0x00000001, "Loadable");
    public static final PHTEntryType PT_DYNAMIC = new PHTEntryType(0x00000002, "Dynamic linking info");
    public static final PHTEntryType PT_INTERP = new PHTEntryType(0x00000003, "Interpreter info");
    public static final PHTEntryType PT_NOTE = new PHTEntryType(0x00000004, "Auxiliary info");
    public static final PHTEntryType PT_SHLIB = new PHTEntryType(0x00000005, "Reserved");
    public static final PHTEntryType PT_PHDR = new PHTEntryType(0x00000006, "Program header table");
    public static final PHTEntryType PT_TLS = new PHTEntryType(0x00000007, "Thread-Local Storage template");

    public static final PHTEntryType PT_LOOS = new PHTEntryType(0x60000000, "Unknown (OS specific)", false);
    public static final PHTEntryType PT_GNU_EH_FRAME = new PHTEntryType(0x6474e550, "Exception handling");
    public static final PHTEntryType PT_GNU_STACK = new PHTEntryType(0x6474e551, "Stack executablity");
    public static final PHTEntryType PT_GNU_RELRO = new PHTEntryType(0x6474e552, "Read-only after relocation");
    public static final PHTEntryType PT_GNU_PROPERTY =
            new PHTEntryType(0x6474e553, ".note.gnu.property notes sections");
    public static final PHTEntryType PT_HIOS = new PHTEntryType(0x6fffffff, "Unknown (OS specific)", false);

    public static final PHTEntryType PT_LOPROC = new PHTEntryType(0x70000000, "Unknown (Processor specific)", false);
    public static final PHTEntryType PT_HIPROC = new PHTEntryType(0x7fffffff, "Unknown (Processor specific)", false);

    public static final PHTEntryType PT_LOUSER = new PHTEntryType(0x80000000, "Unknown (Application specific)", false);
    public static final PHTEntryType PT_HIUSER = new PHTEntryType(0xffffffff, "Unknown (Application specific)", false);

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || (code >= PT_LOOS.code());
    }

    public static PHTEntryType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= PT_LOOS.code() && code <= PT_HIOS.code()) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(code, String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= PT_LOPROC.code() && code <= PT_HIPROC.code()) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(code, String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= PT_LOUSER.code() && code <= PT_HIUSER.code()) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(code, String.format("0x%08x (OS specific)", code), false);
            }
            throw new IllegalArgumentException(String.format("Unknown ELF PHT entry type identifier: 0x%02x", code));
        }
        return codeToType.get(code);
    }

    private final int code;
    private final String description;

    private PHTEntryType(final int code, final String description, final boolean addToMap) {
        this.code = code;
        this.description = description;

        if (addToMap) {
            if (codeToType.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "ELF PHT entry type enum value with code %d (0x%02x) and name '%s' already exists",
                        code, code, description));
            }
            codeToType.put(code, this);
        }
    }

    private PHTEntryType(final int code, final String description) {
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
