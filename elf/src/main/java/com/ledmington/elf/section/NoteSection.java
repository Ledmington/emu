package com.ledmington.elf.section;

import com.ledmington.utils.ByteBuffer;

public sealed class NoteSection extends Section permits GnuPropertySection {

    protected final byte[] ownerBytes;
    protected final String owner;
    protected final byte[] descriptionBytes;
    protected final String description;
    protected final int type;

    public NoteSection(final String name, final SectionHeader entry, final ByteBuffer b) {
        super(name, entry);

        b.setPosition((int) entry.fileOffset());

        // for some reason, even though on the ELF64 reference says that the fields of a
        // SHT_NOTE section
        // are all 8-byte words and aligned on 8-byte boundaries, here the only code
        // that works is the one
        // which uses 4-byte words regardless of the actual ELF_CLASS
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
    }
}
