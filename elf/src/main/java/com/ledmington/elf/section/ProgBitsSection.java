package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;

public class ProgBitsSection extends Section {

    protected final byte[] content;

    public ProgBitsSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
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
