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
            if (codeToFileType.containsKey(x.getCode())) {
                throw new IllegalStateException(String.format(
                        "ELF file type enum value with code %d (0x%02x) and name '%s' already exists",
                        x.getCode(), x.getCode(), x.getName()));
            }
            codeToFileType.put(x.getCode(), x);
        }
    }

    /**
     * Checks whether the given code corresponds to a known ELF file type.
     *
     * @param code
     *      The code to be checked.
     * @return
     *      True if an ELF file type corresponding to the given code exists, false otherwise.
     */
    public static boolean isValid(final short code) {
        return codeToFileType.containsKey(code)
                || (code >= (short) 0xfe00 && code <= (short) 0xfeff)
                || (code >= (short) 0xff00 && code <= (short) 0xffff);
    }

    /**
     * Returns the {@link FileType} corresponding to the given code.
     *
     * @param code
     *      The code representing the FileType.
     * @return
     *      The FileType object corresponding to the given code.
     */
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

    /**
     * Hexadecimal 16-bits code.
     *
     * @return
     *      The code of this FileType object.
     */
    public short getCode() {
        return code;
    }

    /**
     * Name of the FileType.
     *
     * @return
     *      A string representation of this FileType object.
     */
    public String getName() {
        return fileTypeName;
    }
}
