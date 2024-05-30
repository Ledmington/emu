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
import java.util.Arrays;
import java.util.List;

import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

public interface NoteSection extends LoadableSection {

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    static NoteSectionEntry[] loadNoteSectionEntries(
            final boolean is32Bit, final ReadOnlyByteBuffer b, final long length) {
        final long start = b.getPosition();
        final List<NoteSectionEntry> entries = new ArrayList<>();

        b.setAlignment(1L);
        while (b.getPosition() - start < length) {
            final int namesz = b.read4();
            final int descsz = b.read4();
            final int type = b.read4();

            final byte[] nameBytes = new byte[namesz];
            for (int i = 0; i < namesz; i++) {
                nameBytes[i] = b.read1();
            }
            final String name = new String(nameBytes);

            final byte[] descriptionBytes = new byte[descsz];
            for (int i = 0; i < descsz; i++) {
                descriptionBytes[i] = b.read1();
            }
            final String description = new String(descriptionBytes);

            // alignmnent
            final long bytes = is32Bit ? 4L : 8L;
            final long byteShift = is32Bit ? 2L : 3L;
            final long newPosition = (b.getPosition() % bytes != 0L)
                    ? (((b.getPosition() >>> byteShift) + 1L) << byteShift)
                    : b.getPosition();
            b.setPosition(newPosition);

            entries.add(new NoteSectionEntry(name, description, type, is32Bit));
        }

        return entries.toArray(new NoteSectionEntry[0]);
    }

    NoteSectionEntry[] getEntries();

    @Override
    default byte[] getContent() {
        final NoteSectionEntry[] entries = getEntries();
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(
                Arrays.stream(entries).mapToInt(e -> e.getAlignedSize()).sum());
        int runningTotal = 0;
        for (final NoteSectionEntry nse : entries) {
            bb.write(nse.name().length());
            bb.write(nse.description().length());
            bb.write(nse.type());
            bb.write(nse.name().getBytes());
            bb.write(nse.description().getBytes());
            runningTotal += nse.getAlignedSize();
            bb.setPosition(runningTotal);
        }
        return bb.array();
    }
}
