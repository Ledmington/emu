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
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class GnuPropertySection implements NoteSection {

    private final String name;
    private final SectionHeader header;
    private final byte[] ownerBytes;
    private final String owner;
    private final byte[] descriptionBytes;
    private final String description;
    private final int type;

    public GnuPropertySection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        this.name = Objects.requireNonNull(name);
        this.header = Objects.requireNonNull(sectionHeader);

        b.setPosition((int) sectionHeader.getFileOffset());

        /*
         * TODO: For some reason, even though on the ELF64 reference says that the
         * fields of a SHT_NOTE section are all 8-byte words and aligned on 8-byte
         * boundaries, here the only code that works is the one which uses 4-byte words
         * regardless of the actual ELF_CLASS.
         */
        final int namesz = b.read4();
        final int descsz = b.read4();
        this.type = b.read4();

        this.ownerBytes = new byte[namesz];
        for (int i = 0; i < namesz; i++) {
            this.ownerBytes[i] = b.read1();
        }
        this.owner = new String(ownerBytes);

        this.descriptionBytes = new byte[descsz];
        for (int i = 0; i < descsz; i++) {
            this.descriptionBytes[i] = b.read1();
        }
        this.description = new String(descriptionBytes);

        if (this.owner.length() != 4 || !"GNU\0".equals(this.owner)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid owner for .note.gnu.property section: expected 'GNU' but was '%s'", this.owner));
        }
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
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(4 + 4 + 4 + ownerBytes.length + descriptionBytes.length);
        bb.write(ownerBytes.length);
        bb.write(descriptionBytes.length);
        bb.write(type);
        bb.write(ownerBytes);
        bb.write(descriptionBytes);
        return bb.array();
    }

    @Override
    public String toString() {
        return "GnuPropertySection(name=" + name + ";header=" + header + ";type=" + type + ";owner=" + owner
                + ";description="
                + description + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + header.hashCode();
        h = 31 * h + type;
        h = 31 * h + owner.hashCode();
        h = 31 * h + description.hashCode();
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
        final GnuPropertySection gps = (GnuPropertySection) other;
        return this.name.equals(gps.name)
                && this.header.equals(gps.header)
                && this.type == gps.type
                && this.owner.equals(gps.owner)
                && this.description.equals(gps.description);
    }
}
