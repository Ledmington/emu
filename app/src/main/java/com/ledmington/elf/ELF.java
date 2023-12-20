package com.ledmington.elf;

import java.util.Objects;

/**
 * This class is just a data holder.
 * No check (other than non-null) is performed in the constructor on the given
 * data.
 */
public final class ELF {

    private final FileHeader fileHeader;
    private final PHTEntry[] programHeaderTable;
    private final SHTEntry[] sectionHeaderTable;

    public ELF(final FileHeader fileHeader, final PHTEntry[] programHeaderTable, final SHTEntry[] sectionHeaderTable) {
        this.fileHeader = Objects.requireNonNull(fileHeader);
        this.programHeaderTable = Objects.requireNonNull(programHeaderTable);
        this.sectionHeaderTable = Objects.requireNonNull(sectionHeaderTable);
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
        sb.append(" --- Section Header Table --- \n\n");
        for (int i = 0; i < sectionHeaderTable.length; i++) {
            sb.append(String.format("SHT entry n.%,d\n", i));
            sb.append(sectionHeaderTable[i].toString());
            sb.append('\n');
        }
        sb.append(" --- End of Section Header Table --- \n");
        return sb.toString();
    }
}
