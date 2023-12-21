package com.ledmington.elf;

import java.util.Objects;

/**
 * This class is just a data holder.
 * No check (other than non-null) is performed in the constructor on the given
 * data.
 * <p>
 * References:
 * <a href="http://www.skyfree.org/linux/references/ELF_Format.pdf">32 bit</a>
 * <a href="https://uclibc.org/docs/elf-64-gen.pdf">64 bit</a>
 */
public final class ELF {

    private final FileHeader fileHeader;
    private final PHTEntry[] programHeaderTable;
    private final Section[] sectionTable;

    public ELF(final FileHeader fileHeader, final PHTEntry[] programHeaderTable, final Section[] sectionTable) {
        this.fileHeader = Objects.requireNonNull(fileHeader);
        this.programHeaderTable = Objects.requireNonNull(programHeaderTable);
        this.sectionTable = Objects.requireNonNull(sectionTable);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(" --- File Header --- \n");
        sb.append(fileHeader);
        sb.append('\n');
        sb.append(" --- Program Header Table --- \n\n");
        for (int i = 0; i < programHeaderTable.length; i++) {
            sb.append(String.format("PHT entry n.%,d\n", i));
            sb.append(programHeaderTable[i].toString());
            sb.append('\n');
        }
        sb.append(" --- End of Program Header Table --- \n\n");
        sb.append(" --- Section Table --- \n\n");
        for (int i = 0; i < sectionTable.length; i++) {
            sb.append(String.format("Section n.%,d\n", i));
            sb.append(sectionTable[i].toString());
            sb.append('\n');
        }
        sb.append(" --- End of Section Table --- \n");
        return sb.toString();
    }
}
