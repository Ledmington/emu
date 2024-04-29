package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public sealed class NoteSection extends LoadableSection permits GnuPropertySection {

    protected final byte[] ownerBytes;
    protected final String owner;
    protected final byte[] descriptionBytes;
    protected final String description;
    protected final int type;

    public NoteSection(final String name, final SectionHeader entry, final ReadOnlyByteBuffer b) {
        super(name, entry);

        b.setPosition((int) entry.fileOffset());

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
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(4 + 4 + 4 + ownerBytes.length + descriptionBytes.length);
        bb.write(ownerBytes.length);
        bb.write(descriptionBytes.length);
        bb.write(type);
        bb.write(ownerBytes);
        bb.write(descriptionBytes);
        return bb.array();
    }
}
