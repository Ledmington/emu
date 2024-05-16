package com.ledmington.elf;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.Section;

/**
 * This class is just a data holder.
 * No check (other than non-null) is performed in the constructor on the given
 * data.
 * <p>
 * References:
 * <a href="http://www.skyfree.org/linux/references/ELF_Format.pdf">32 bit</a>
 * and
 * <a href="https://uclibc.org/docs/elf-64-gen.pdf">64 bit</a>.
 */
public final class ELF {

    private final FileHeader fileHeader;
    private final PHTEntry[] programHeaderTable;
    private final Section[] sectionTable;

    /**
     * Creates an ELF object.
     *
     * @param fileHeader
     *      The file header containing general information about the file.
     * @param programHeaderTable
     *      The program header table containing information about memory segments.
     * @param sectionTable
     *      The section table containing information about file sections.
     */
    public ELF(final FileHeader fileHeader, final PHTEntry[] programHeaderTable, final Section[] sectionTable) {
        this.fileHeader = Objects.requireNonNull(fileHeader);
        this.programHeaderTable = Objects.requireNonNull(programHeaderTable);
        this.sectionTable = Objects.requireNonNull(sectionTable);
    }

    public FileHeader fileHeader() {
        return fileHeader;
    }

    public PHTEntry[] programHeader() {
        return programHeaderTable;
    }

    public Section[] sections() {
        return sectionTable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(1_000);
        sb.append(" --- File Header --- \n")
                .append(fileHeader)
                .append('\n')
                .append(" --- Program Header Table --- \n\n");
        for (int i = 0; i < programHeaderTable.length; i++) {
            sb.append(String.format("PHT entry n.%,d\n", i))
                    .append(programHeaderTable[i].toString())
                    .append('\n');
        }
        sb.append(" --- End of Program Header Table --- \n\n").append(" --- Section Table --- \n\n");
        for (int i = 0; i < sectionTable.length; i++) {
            sb.append(String.format("Section n.%,d\n", i))
                    .append(sectionTable[i].toString())
                    .append('\n');
        }
        sb.append(" --- End of Section Table --- \n");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + fileHeader.hashCode();
        h = 31 * h + Arrays.hashCode(programHeaderTable);
        h = 31 * h + Arrays.hashCode(sectionTable);
        return h;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final ELF elf = (ELF) other;
        return this.fileHeader.equals(elf.fileHeader)
                && Arrays.equals(this.programHeaderTable, elf.programHeaderTable)
                && Arrays.equals(this.sectionTable, elf.sectionTable);
    }
}
