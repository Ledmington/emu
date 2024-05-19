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

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class InterpreterPathSection extends ProgBitsSection {

    private final String interpreterFilePath;

    public InterpreterPathSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        super(name, sectionHeader, b);

        this.interpreterFilePath = null;
        /*
         * final int start = (int) sectionHeader.fileOffset();
         * b.setPosition(start);
         *
         * final StringBuilder sb = new StringBuilder();
         * char c = (char) b.read1();
         * while (c != '\0') {
         * sb.append(c);
         * c = (char) b.read1();
         * }
         * this.interpreterFilePath = sb.toString();
         */
    }
}
