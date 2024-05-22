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
package com.ledmington.elf.section;

import java.util.Objects;

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class InterpreterPathSection implements ProgBitsSection {

    private final String name;
    private final SectionHeader header;
    private final String interpreterFilePath;

    public InterpreterPathSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);

        b.setPosition(sectionHeader.getFileOffset());

        final StringBuilder sb = new StringBuilder();
        char c = (char) b.read1();
        while (c != '\0') {
            sb.append(c);
            c = (char) b.read1();
        }
        this.interpreterFilePath = sb.toString();
    }

    public String getInterpreterFilePath() {
        return interpreterFilePath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SectionHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getContent() {
        return interpreterFilePath.getBytes();
    }

    @Override
    public String toString() {
        return "InterpreterPathSection(name=" + name + ";header=" + header + ";interpreterPath=" + interpreterFilePath
                + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + interpreterFilePath.hashCode();
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
        final InterpreterPathSection ips = (InterpreterPathSection) other;
        return this.name.equals(ips.name)
                && this.header.equals(ips.header)
                && this.interpreterFilePath.equals(ips.interpreterFilePath);
    }
}
