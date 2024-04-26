package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class DestructorsSection extends Section {
    public DestructorsSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        super(name, sectionHeader);
    }
}
