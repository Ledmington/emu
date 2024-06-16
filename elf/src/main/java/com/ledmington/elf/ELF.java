/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.elf;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.Section;

/**
 * This class is just a data holder. No check (other than non-null) is performed in the constructor on the given data.
 *
 * <p>References: <a href="https://uclibc.org/docs/elf.pdf">32 bit</a> and <a
 * href="https://uclibc.org/docs/elf-64-gen.pdf">64 bit</a>.
 *
 * @param fileHeader The file header containing general information about the file.
 * @param programHeaderTable The program header table containing information about memory segments.
 * @param sectionTable The section table containing information about file sections.
 */
public record ELF(FileHeader fileHeader, PHTEntry[] programHeaderTable, Section... sectionTable) {

    /**
     * Creates an ELF object.
     *
     * @param fileHeader The file header containing general information about the file.
     * @param programHeaderTable The program header table containing information about memory segments.
     * @param sectionTable The section table containing information about file sections.
     */
    public ELF(final FileHeader fileHeader, final PHTEntry[] programHeaderTable, final Section... sectionTable) {
        this.fileHeader = Objects.requireNonNull(fileHeader);
        this.programHeaderTable = new PHTEntry[Objects.requireNonNull(programHeaderTable).length];
        for (int i = 0; i < programHeaderTable.length; i++) {
            this.programHeaderTable[i] = Objects.requireNonNull(programHeaderTable[i]);
        }
        this.sectionTable = new Section[Objects.requireNonNull(sectionTable).length];
        for (int i = 0; i < sectionTable.length; i++) {
            this.sectionTable[i] = Objects.requireNonNull(sectionTable[i]);
        }
    }

    /**
     * Returns the File Header of this ELF file object.
     *
     * @return The File Header.
     */
    @Override
    public FileHeader fileHeader() {
        return fileHeader;
    }

    /**
     * Returns the Program Header Table of this ELF file object.
     *
     * @return The Program Header Table.
     */
    @Override
    public PHTEntry[] programHeaderTable() {
        final PHTEntry[] v = new PHTEntry[programHeaderTable.length];
        System.arraycopy(programHeaderTable, 0, v, 0, programHeaderTable.length);
        return v;
    }

    /**
     * Returns the Section Table of this ELF file object.
     *
     * @return The Section Table.
     */
    @Override
    public Section[] sectionTable() {
        final Section[] v = new Section[sectionTable.length];
        System.arraycopy(sectionTable, 0, v, 0, sectionTable.length);
        return v;
    }

    /**
     * Looks for a section with the given name inside the Section Table and returns it.
     *
     * @param name The name of the section to look for.
     * @return The Section encountered such that {@code s.getName().equals(name)} returns true.
     */
    public Section getSectionByName(final String name) {
        for (final Section s : sectionTable) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        throw new IllegalArgumentException(String.format("No section found with name '%s'", name));
    }

    @Override
    public String toString() {
        return "ELF(fileHeader=" + fileHeader + ";programHeaderTable=" + Arrays.toString(programHeaderTable)
                + ";sectionTable=" + Arrays.toString(sectionTable) + ")";
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
