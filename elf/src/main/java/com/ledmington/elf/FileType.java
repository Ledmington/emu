package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

/**
 * The type of an ELF file.
 */
public enum FileType {

    /**
     * Unknown type.
     */
    ET_NONE((short) 0x0000, "Unknown"),

    /**
     * Relocatable file.
     */
    ET_REL((short) 0x0001, "Relocatable file"),

    /**
     * Executable file.
     */
    ET_EXEC((short) 0x0002, "Executable file"),

    /**
     * Shared object or Position-Independent Executable file.
     */
    ET_DYN((short) 0x0003, "Shared object or Position-Independent Executable file"),

    /**
     * Core file.
     */
    ET_CORE((short) 0x0004, "Core file");

    private static final Map<Short, FileType> codeToFileType = new HashMap<>();

    static {
        for (final FileType x : FileType.values()) {
            if (codeToFileType.containsKey(x.code())) {
                throw new IllegalStateException(String.format(
                        "ELF file type enum value with code %d (0x%02x) and name '%s' already exists",
                        x.code(), x.code(), x.fileTypeName()));
            }
            codeToFileType.put(x.code(), x);
        }
    }

    public static boolean isValid(final short fileType) {
        return codeToFileType.containsKey(fileType)
                || (fileType >= (short) 0xfe00 && fileType <= (short) 0xfeff)
                || (fileType >= (short) 0xff00 && fileType <= (short) 0xffff);
    }

    public static FileType fromCode(final short code) {
        if (!codeToFileType.containsKey(code)) {
            throw new IllegalArgumentException(String.format("Unknown ELF file type identifier: 0x%02x", code));
        }
        return codeToFileType.get(code);
    }

    private final short code;
    private final String fileTypeName;

    FileType(final short code, final String fileTypeName) {
        this.code = code;
        this.fileTypeName = fileTypeName;
    }

    public short code() {
        return code;
    }

    public String fileTypeName() {
        return fileTypeName;
    }
}
