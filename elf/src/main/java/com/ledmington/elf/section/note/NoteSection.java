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
package com.ledmington.elf.section.note;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.ledmington.elf.section.LoadableSection;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** An ELF section with type SHT_NOTE (.note*). */
public interface NoteSection extends LoadableSection {

    /**
     * Reads an array of NoteSectionEntry objects by parsing the given ReadOnlyByteBuffer.
     *
     * @param is32Bit Used to determine the alignment of the buffer: if true, 4-byte alignment, otherwise 8-byte.
     * @param b The byte buffer to read the entries from.
     * @param length Maximum number of bytes to read.
     * @return A non-null array of NoteSectionEntry.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    static NoteSectionEntry[] loadNoteSectionEntries(
            final boolean is32Bit, final ReadOnlyByteBuffer b, final long length) {
        final long start = b.getPosition();
        final List<NoteSectionEntry> entries = new ArrayList<>();

        // for alignment
        final long bytes = is32Bit ? 4L : 8L;
        final long byteShift = is32Bit ? 2L : 3L;

        b.setAlignment(1L);
        while (b.getPosition() - start < length) {
            final int namesz = b.read4();
            final int descsz = b.read4();
            final int type = b.read4();

            final byte[] nameBytes = new byte[namesz];
            for (int i = 0; i < namesz; i++) {
                nameBytes[i] = b.read1();
            }
            final String name = new String(
                    nameBytes, 0, nameBytes[namesz - 1] == '\0' ? namesz - 1 : namesz, StandardCharsets.UTF_8);

            final byte[] descriptionBytes = new byte[descsz];
            for (int i = 0; i < descsz; i++) {
                descriptionBytes[i] = b.read1();
            }

            // alignment
            final long newPosition = (b.getPosition() % bytes != 0L)
                    ? (((b.getPosition() >>> byteShift) + 1L) << byteShift)
                    : b.getPosition();
            b.setPosition(newPosition);

            entries.add(new NoteSectionEntry(name, descriptionBytes, NoteSectionEntryType.fromCode(type), is32Bit));
        }

        return entries.toArray(new NoteSectionEntry[0]);
    }

    int getNumEntries();

    NoteSectionEntry getEntry(final int idx);

    @Override
    default byte[] getLoadableContent() {
        final int numEntries = getNumEntries();
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(IntStream.range(0, numEntries)
                .map(i -> getEntry(i).getAlignedSize())
                .sum());
        int runningTotal = 0;
        for (int i = 0; i < numEntries; i++) {
            final NoteSectionEntry nse = getEntry(i);
            bb.write(nse.getName().length());
            bb.write(nse.getDescriptionLength());
            bb.write(nse.getType().getCode());
            bb.write(nse.getName().getBytes(StandardCharsets.UTF_8));
            for (int j = 0; j < nse.getDescriptionLength(); j++) {
                bb.write(nse.getDescriptionByte(j));
            }
            runningTotal += nse.getAlignedSize();
            bb.setPosition(runningTotal);
        }
        return bb.array();
    }
}
