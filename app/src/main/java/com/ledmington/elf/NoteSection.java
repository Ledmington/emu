package com.ledmington.elf;

public final class NoteSection extends Section {

    private final String owner;
    private final String description;

    public NoteSection(final String name, final SectionHeader entry, final ByteBuffer b) {
        super(name, entry);

        b.setPosition((int) entry.fileOffset());

        // for some reason, even though on the ELF64 reference says that the fields of a SHT_NOTE section
        // are all 8-byte words and aligned on 8-byte boundaries, here the only code that works is the one
        // which uses 4-byte words regardless of the actual ELF_CLASS
        int namesz = b.read4();
        int descsz = b.read4();
        int type = b.read4();

        {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < namesz; i++) {
                sb.append((char) b.read1());
            }
            this.owner = sb.toString();
        }

        final byte[] descBytes = new byte[(int) descsz];
        for (int i = 0; i < descsz; i++) {
            descBytes[i] = b.read1();
        }
        this.description = new String(descBytes);
    }
}
