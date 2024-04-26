package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class ConstructorsSection extends Section {
    public ConstructorsSection(final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
        super(name, sectionHeader);
    }
}
