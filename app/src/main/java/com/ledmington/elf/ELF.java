package com.ledmington.elf;

import java.util.Objects;

/**
 * This class is just a data holder.
 * No check (other than non-null) is performed in the constructor on the given
 * data.
 */
public final class ELF {

    private final FileHeader fileHeader;
    private final ProgramHeaderEntry[] programHeaderTable;
    private final SectionHeaderEntry[] sectionHeaderTable;

    public ELF(
            final FileHeader fileHeader,
            final ProgramHeaderEntry[] programHeaderTable,
            final SectionHeaderEntry[] sectionHeaderTable) {
        this.fileHeader = Objects.requireNonNull(fileHeader);
        this.programHeaderTable = Objects.requireNonNull(programHeaderTable);
        this.sectionHeaderTable = Objects.requireNonNull(sectionHeaderTable);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(" --- File Header --- \n");
        sb.append(fileHeader);
        sb.append('\n');
        sb.append(" --- Program Header Table --- \n");
        for (final ProgramHeaderEntry entry : programHeaderTable) {
            sb.append(entry.toString());
            sb.append('\n');
        }
        sb.append(" --- Section Header Table --- \n");
        for (final SectionHeaderEntry entry : sectionHeaderTable) {
            sb.append(entry.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}
