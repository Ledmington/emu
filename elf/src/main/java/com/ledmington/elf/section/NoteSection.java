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

import java.util.ArrayList;
import java.util.List;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

public interface NoteSection extends LoadableSection {

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    static NoteSectionEntry[] loadNoteSectionEntries(
            final boolean is32Bit, final ReadOnlyByteBuffer b, final long length) {
        final long start = b.getPosition();
        final List<NoteSectionEntry> entries = new ArrayList<>();

        /*
         * For some reason, even though on the ELF64 reference (available here
         * https://uclibc.org/docs/elf-64-gen.pdf) says that the fields of a
         * SHT_NOTE section must be 8-byte words and aligned on 8-byte boundaries, here
         * the only code that works is the one which uses 4-byte words regardless of the
         * actual ELF_CLASS.
         *
         * See
         * https://stackoverflow.com/questions/78531879
         */
        b.setAlignment(1L);
        while (b.getPosition() - start < length) {
            final long namesz = BitUtils.asLong(b.read4());
            final long descsz = BitUtils.asLong(b.read4());
            final long type = BitUtils.asLong(b.read4());

            final byte[] nameBytes = new byte[BitUtils.asInt(namesz)];
            for (int i = 0; i < namesz; i++) {
                nameBytes[i] = b.read1();
            }
            final String name = new String(nameBytes);

            final byte[] descriptionBytes = new byte[BitUtils.asInt(descsz)];
            for (int i = 0; i < descsz; i++) {
                descriptionBytes[i] = b.read1();
            }
            final String description = new String(descriptionBytes);

            entries.add(new NoteSectionEntry(name, description, type));
        }

        return entries.toArray(new NoteSectionEntry[0]);
    }
}
