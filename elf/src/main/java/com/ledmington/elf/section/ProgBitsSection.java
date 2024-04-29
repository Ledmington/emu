package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;

public class ProgBitsSection extends LoadableSection {

    protected final byte[] content;

    public ProgBitsSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int size = (int) sectionHeader.sectionSize();
        this.content = new byte[size];
        for (int i = 0; i < size; i++) {
            this.content[i] = b.read1();
        }
    }

    @Override
    public byte[] content() {
        return content;
    }
}
