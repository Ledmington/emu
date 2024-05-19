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
import com.ledmington.utils.WriteOnlyByteBuffer;

/**
 * Reference <a href= "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.2.
 */
public final class GnuVersionSection extends LoadableSection {

    private final short[] versions;

    public GnuVersionSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int nEntries = (int) (sectionHeader.sectionSize() / 2);
        this.versions = new short[nEntries];
        for (int i = 0; i < nEntries; i++) {
            versions[i] = b.read2();
        }
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(versions.length * 2);
        for (final short version : versions) {
            bb.write(version);
        }
        return bb.array();
    }
}
