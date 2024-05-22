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
    static NoteSectionEntry[] loadNoteSectionEntries(
            final boolean is32Bit, final ReadOnlyByteBuffer b, final long length) {
        final long start = b.getPosition();
        final List<NoteSectionEntry> entries = new ArrayList<>();

        while (b.getPosition() - start < length) {
            final long namesz = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            System.out.printf("namesz : 0x%016x\n", namesz);
            final long descsz = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            System.out.printf("descsz : 0x%016x\n", descsz);
            final long type = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            System.out.printf("type : 0x%016x\n", type);

            final byte[] nameBytes = new byte[BitUtils.asInt(namesz)];
            for (int i = 0; i < namesz; i++) {
                nameBytes[i] = b.read1();
            }
            final String name = new String(nameBytes);

            // ensure alignment to 4 byte boundary for 32 bits (8 byte for 64 bits)
            if (is32Bit) {
                final long here = b.getPosition();
                final long processed = here - start;
                b.setPosition(here + ((processed + 4) & 0xfc));
            } else {
                final long here = b.getPosition();
                final long processed = here - start;
                b.setPosition(here + ((processed + 8) & 0xf8));
            }

            final byte[] descriptionBytes = new byte[BitUtils.asInt(descsz)];
            for (int i = 0; i < descsz; i++) {
                descriptionBytes[i] = b.read1();
            }
            final String description = new String(descriptionBytes);

            // ensure alignment to 4 byte boundary for 32 bits (8 byte for 64 bits)
            if (is32Bit) {
                final long here = b.getPosition();
                final long processed = here - start;
                b.setPosition(here + ((processed + 4) & 0xfc));
            } else {
                final long here = b.getPosition();
                final long processed = here - start;
                b.setPosition(here + ((processed + 8) & 0xf8));
            }

            entries.add(new NoteSectionEntry(type, name, description));
        }

        return entries.toArray(new NoteSectionEntry[0]);
    }
}
