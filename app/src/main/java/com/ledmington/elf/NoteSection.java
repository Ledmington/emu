package com.ledmington.elf;

public final class NoteSection extends Section {

    // private final String owner;
    // private final String description;

    public NoteSection(final String name, final SectionHeader entry, final ByteBuffer b, final boolean is32Bit) {
        super(name, entry);

        b.setPosition((int) entry.fileOffset());
        final int size = (int) entry.size();

        for (int i = 0; i < size; i++) {
            // System.out.printf("%c", (char) b.read1());
            System.out.printf("%02x", b.read1());
        }
        System.out.println();

        /*
        long namesz;
        long descsz;
        long type;

        if (is32Bit) {
            namesz = b.read4AsLong();
            descsz = b.read4AsLong();
            type = b.read4AsLong();
        } else {
            namesz = b.read8();
            descsz = b.read8();
            type = b.read8();
        }

        System.out.printf("Name size : 0x%016x bytes (%,d)\n", namesz, (int) namesz);
        System.out.printf("Desc size : 0x%016x bytes (%,d)\n", descsz, (int) descsz);

        {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < namesz; i++) {
                sb.append((char) b.read1());
            }
            this.owner = sb.toString();
        }

        // skip bytes to ensure alignment
        final int alignment = is32Bit ? 4 : 8;
        for (int i = 0; i < alignment - (namesz % alignment); i++) {
            b.read1();
        }

        final byte[] descBytes = new byte[(int) descsz];
        for (int i = 0; i < descsz; i++) {
            descBytes[i] = b.read1();
        }
        this.description = new String(descBytes);*/
    }
}
