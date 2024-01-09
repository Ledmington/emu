package com.ledmington.elf;

import com.ledmington.utils.ByteBuffer;

public final class ProgBitsSection extends Section {

    private final byte[] content;

    public ProgBitsSection(final String name, final SectionHeader sectionHeader, final ByteBuffer b) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int size = (int) sectionHeader.size();
        this.content = new byte[size];
        for (int i = 0; i < size; i++) {
            this.content[i] = b.read1();
        }
    }

    public byte[] content() {
        return content;
    }
}
